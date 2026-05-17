package com.app.minder.presentation.measurementDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.minder.data.local.entity.MeasurementEntity
import com.app.minder.data.local.entity.MeasurementGoalEntity
import com.app.minder.data.local.entity.toDomain
import com.app.minder.domain.interfaces.MeasurementRepository
import com.app.minder.domain.interfaces.ProfileRepository
import com.app.minder.domain.model.BLOOD_PRESSURE_DIASTOLIC
import com.app.minder.domain.model.BLOOD_PRESSURE_SYSTOLIC
import com.app.minder.domain.model.Measurement
import com.app.minder.domain.model.MeasurementAnalysis
import com.app.minder.domain.model.MeasurementGoal
import com.app.minder.domain.model.MeasurementType
import com.app.minder.domain.usecase.GetMeasurementAnalysisUseCase
import com.app.minder.domain.usecase.SaveMeasurementUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

data class MeasurementDetailUiState (
    val loading: Boolean = false,
    val error: String? = null,
    val type: MeasurementType? = null,
    val secondaryType: MeasurementType? = null,
    val goal: MeasurementGoal? = null,
    val goalSecondary: MeasurementGoal? = null,
    val target: Double,
    val targetSecondary: Double,
    val profileId: String? = null,
    val analysis: MeasurementAnalysis? = null,
    val measurements: List<Measurement> = emptyList(),
    val measurementsSecondary: List<Measurement> = emptyList()
)

class MeasurementDetailViewModel(
    private val measurementTypeId: String?,
    private val measurementRep: MeasurementRepository,
    private val profileRep: ProfileRepository,
    private val getMeasurementAnalysisUseCase: GetMeasurementAnalysisUseCase,
    private val saveMeasurementUseCase: SaveMeasurementUseCase
): ViewModel() {
    companion object{
        val DEFAULT_GOALS = mapOf(
        BLOOD_PRESSURE_SYSTOLIC to 120.0,
        BLOOD_PRESSURE_DIASTOLIC to 80.0,
        "heart_rate" to 80.0,
        "blood_glucose" to 4.2,
        "mood" to 5.0,
        "blood_oxygen" to 100.0,
        "temperature" to 36.6
        )
    }

    private val _uiState = MutableStateFlow(MeasurementDetailUiState(target = 0.0, targetSecondary = 0.0))
    val uiState: StateFlow<MeasurementDetailUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        if (measurementTypeId != null){
            loadAll()

            _uiState.value = _uiState.value.copy(loading = false)
        }
    }

    private fun loadAll(){
        viewModelScope.launch {
            profileRep.getCurrentProfile()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
                .filterNotNull()
                .flatMapLatest { profile ->
                    _uiState.value = _uiState.value.copy(profileId = profile.id)

                    flow {
                        loadMeasurementType(measurementTypeId!!)
                        if (measurementTypeId == BLOOD_PRESSURE_SYSTOLIC) {
                            loadSecondaryMeasurementType(BLOOD_PRESSURE_DIASTOLIC)
                            loadGoalSecondary(BLOOD_PRESSURE_DIASTOLIC, profile.id)
                        }
                        loadGoal(measurementTypeId, profile.id)
                        loadAnalysis(measurementTypeId, profile.id)
                        loadMeasurements(measurementTypeId, profile.id)
                        emit(Unit)
                    }
                }
                .collect {}
        }
    }

    private fun loadMeasurements(typeId: String, profileId: String) {
        val now = System.currentTimeMillis()
        val twoWeeksAgo = now - 14L * 24 * 60 * 60 * 1000

        viewModelScope.launch {
            measurementRep.getMeasurementsByRange(typeId, profileId, twoWeeksAgo )
                .collect { list ->
                    _uiState.value = _uiState.value.copy(measurements = list)
                }
        }
        if (typeId == BLOOD_PRESSURE_SYSTOLIC) {
            viewModelScope.launch {
                measurementRep.getMeasurementsByRange(BLOOD_PRESSURE_DIASTOLIC, profileId, twoWeeksAgo)
                    .collect { list ->
                        _uiState.value = _uiState.value.copy(measurementsSecondary = list)
                    }
            }
        }
    }

    private fun loadAnalysis(typeId: String, profileId: String){
        val goalFlow = measurementRep.getGoal(typeId, profileId)
            .map { it?.value ?: DEFAULT_GOALS[typeId] }

        val goalSecondaryFlow = if (typeId == BLOOD_PRESSURE_SYSTOLIC) {
            measurementRep.getGoal(BLOOD_PRESSURE_DIASTOLIC, profileId)
                .map { it?.value ?: DEFAULT_GOALS[BLOOD_PRESSURE_DIASTOLIC] }
        } else flowOf(null)

        viewModelScope.launch {
                getMeasurementAnalysisUseCase(
                    typeId = typeId,
                    profileId = profileId,
                    goalFlow = goalFlow,
                    goalSecondaryFlow = goalSecondaryFlow
                )
                    .catch { e ->
                        _uiState.value = _uiState.value.copy(error = e.message)
                    }
                    .collect { analysis ->
                        _uiState.value = _uiState.value.copy(analysis = analysis)
                    }
        }
    }

    private fun loadMeasurementType(typeId: String){
        viewModelScope.launch {
            measurementRep.getMeasurementsTypeById(typeId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
                .collect { type ->
                    _uiState.value = _uiState.value.copy(type = type?.toDomain())
                }

        }
    }

    private fun loadSecondaryMeasurementType(typeId: String){
        viewModelScope.launch {
            measurementRep.getMeasurementsTypeById(typeId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
                .collect { type ->
                    _uiState.value = _uiState.value.copy(secondaryType = type?.toDomain())
                }
        }
    }

    private fun loadGoal(typeId: String, profileId: String){
        viewModelScope.launch {
            measurementRep.getGoal(typeId, profileId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
                .collect { goal ->
                    _uiState.value = _uiState.value.copy(goal = goal, target = goal?.value ?: DEFAULT_GOALS[typeId] ?: 0.0)
                }
        }
    }

    private fun loadGoalSecondary(typeId: String, profileId: String){
        viewModelScope.launch {
            measurementRep.getGoal(typeId, profileId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
                .collect { goal ->
                    _uiState.value = _uiState.value.copy(goalSecondary = goal, targetSecondary = goal?.value ?: DEFAULT_GOALS[typeId] ?: 0.0)
                }
        }
    }

    fun saveGoal(typeId: String, value: Double){
        viewModelScope.launch {
            val newGoal = MeasurementGoalEntity(
                id = if (typeId==BLOOD_PRESSURE_DIASTOLIC) _uiState.value.goalSecondary?.id ?: UUID.randomUUID().toString()
                    else _uiState.value.goal?.id ?: UUID.randomUUID().toString(),
                typeId = typeId,
                value = value,
                profileId = _uiState.value.profileId!!
            )
            measurementRep.saveGoal(newGoal)
        }
    }

    fun saveMeasurement(result: Double, note: String?, result2: Double? = null){
        viewModelScope.launch {
            val newMeasurement = MeasurementEntity(
                profileId = _uiState.value.profileId!!,
                typeId = _uiState.value.type?.id!!,
                result = result,
                note = note
            )
            saveMeasurementUseCase(newMeasurement)
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(error = null)
                }

            if (result2!=null){
                val newMeasurement = MeasurementEntity(
                    profileId = _uiState.value.profileId!!,
                    typeId = BLOOD_PRESSURE_DIASTOLIC,
                    result = result2,
                    note = note
                )
                saveMeasurementUseCase(newMeasurement)
                    .onFailure { e ->
                        _uiState.value = _uiState.value.copy(error = e.message)
                    }
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(error = null)
                    }
            }
        }
    }

}