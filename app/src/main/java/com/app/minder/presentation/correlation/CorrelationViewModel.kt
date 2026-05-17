package com.app.minder.presentation.correlation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.minder.data.local.entity.MeasurementTypeEntity
import com.app.minder.domain.interfaces.MeasurementRepository
import com.app.minder.domain.interfaces.MedicationRepository
import com.app.minder.domain.interfaces.ProfileRepository
import com.app.minder.domain.model.Medication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.sqrt

sealed class CorrelationItem {
    abstract val label: String

    data class MeasurementItem(
        val typeId: String,
        override val label: String
    ) : CorrelationItem()

    data class MedicationItem(
        val medicationId: String,
        override val label: String
    ) : CorrelationItem()
}

data class DataPoint(
    val timestamp: Long,
    val value: Double
)

data class CorrelationUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val profileId: String? = null,
    val measurementTypes: List<MeasurementTypeEntity> = emptyList(),
    val medications: List<Medication> = emptyList(),
    val selectedItems: Set<CorrelationItem> = emptySet(),
    val series: Map<CorrelationItem, List<DataPoint>> = emptyMap(),
    val correlation: Double? = null
)

class CorrelationViewModel (
    private val measurementRep: MeasurementRepository,
    private val medicationRep: MedicationRepository,
    private val profileRep: ProfileRepository,
): ViewModel() {
    private val _uiState = MutableStateFlow(CorrelationUiState())
    val uiState: StateFlow<CorrelationUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun clearState(){
        _uiState.value = _uiState.value.copy(
            selectedItems = emptySet(),
            series = emptyMap(),
            correlation = null,
            error = null
        )
    }

    private fun loadAll() {
        viewModelScope.launch {
            profileRep.getCurrentProfile()
                .filterNotNull()
                .catch { e -> _uiState.value = _uiState.value.copy(error = e.message) }
                .collect { profile ->
                    _uiState.value = _uiState.value.copy(profileId = profile.id)
                    loadMeasurementTypes()
                    loadMedications(profile.id)
                }
        }
    }

    private fun loadMeasurementTypes() {
        viewModelScope.launch {
            measurementRep.getAllMeasurementTypes()
                .catch { e -> _uiState.value = _uiState.value.copy(error = e.message) }
                .collect { types ->
                    _uiState.value = _uiState.value.copy(measurementTypes = types)
                }
        }
    }

    private fun loadMedications(profileId: String) {
        viewModelScope.launch {
            medicationRep.getMedicationList(profileId)
                .catch { e -> _uiState.value = _uiState.value.copy(error = e.message) }
                .collect { medications ->
                    _uiState.value = _uiState.value.copy(medications = medications)
                }
        }
    }

    fun toggleItem(item: CorrelationItem) {
        val current = _uiState.value.selectedItems
        val updated = if (current.contains(item)) {
            current - item
        } else if (current.size < 2) {
            current + item
        } else {
            current
        }
        _uiState.value = _uiState.value.copy(selectedItems = updated)

        if (updated.size == 2) {
            loadCorrelation(updated)
        } else {
            _uiState.value = _uiState.value.copy(series = emptyMap(), correlation = null)
        }
    }

    private fun loadCorrelation(items: Set<CorrelationItem>){
        val profileId = _uiState.value.profileId ?: return
        val start = System.currentTimeMillis() - 14L * 24 * 60 * 60 * 1000

        viewModelScope.launch {
            val flows = items.map { item ->
                when (item) {
                    is CorrelationItem.MeasurementItem ->
                        measurementRep.getMeasurementsByRange(item.typeId, profileId, start)
                            .map { measurements ->
                                item to measurements.map { DataPoint(it.createdAt, it.result) }
                            }
                    is CorrelationItem.MedicationItem ->
                        medicationRep.getIntakesByRange(item.medicationId, start)
                            .map { intakes ->
                                val pointsByDay = intakes
                                    .groupBy {
                                        val cal = Calendar.getInstance()
                                        cal.timeInMillis = it.createdAt
                                        cal.set(Calendar.HOUR_OF_DAY, 0)
                                        cal.set(Calendar.MINUTE, 0)
                                        cal.set(Calendar.SECOND, 0)
                                        cal.set(Calendar.MILLISECOND, 0)
                                        cal.timeInMillis
                                    }
                                    .map { (dayTimestamp, dayIntakes) ->
                                        DataPoint(dayTimestamp, dayIntakes.size.toDouble())
                                    }
                                    .sortedBy { it.timestamp }
                                item to pointsByDay
                            }
                }
            }
            combine(flows[0], flows[1]) { first, second ->
                mapOf(first, second)
            }
                .catch{e -> _uiState.value = _uiState.value.copy(error = e.message)}
                .collect {
                        series ->
                    _uiState.value = _uiState.value.copy(
                        series = series,
                        correlation = calculateCorrelation(
                            series.values.first(),
                            series.values.last()
                        )
                    )
                }

        }
    }

    private fun calculateCorrelation(
        series1: List<DataPoint>,
        series2: List<DataPoint>
    ): Double? {
        if (series1.size < 3 || series2.size < 3) return null

        val pairs = series1.mapNotNull { p1 ->
            val closest = series2.minByOrNull { abs(it.timestamp - p1.timestamp) }
            if (closest != null) p1.value to closest.value else null
        }
        if (pairs.size < 3) return null

        val x = pairs.map { it.first }
        val y = pairs.map { it.second }
        val xMean = x.average()
        val yMean = y.average()

        val numerator = pairs.sumOf { (xi, yi) -> (xi - xMean) * (yi - yMean) }
        val denomX = x.sumOf { (it - xMean) * (it - xMean) }
        val denomY = y.sumOf { (it - yMean) * (it - yMean) }
        val denominator = sqrt(denomX * denomY)

        if (denominator==0.0){
            return null
        } else {
            return numerator / denominator
        }
    }
}