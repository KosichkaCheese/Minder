package com.app.minder.presentation.medDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.minder.data.repository.MedicationRepository
import com.app.minder.domain.model.MeasurementUnit
import com.app.minder.domain.model.Medication
import com.app.minder.domain.model.MedicationSchedule
import com.app.minder.domain.model.Profile
import com.app.minder.domain.model.Timing
import com.app.minder.domain.usecase.DeleteMedUseCase
import com.app.minder.domain.usecase.GetCurrentProfileUseCase
import com.app.minder.domain.usecase.SaveMedUseCase
import com.app.minder.domain.usecase.TakeMedicationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID

data class MedDetailUiState(
    val medication: Medication? = null,
    val schedules: List<MedicationSchedule> = emptyList(),
    val currentProfile: Profile? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class MedDetailViewModel(
    private val medicationId: String?,
    private val saveMedicationUseCase: SaveMedUseCase,
    private val deleteMedicationUseCase: DeleteMedUseCase,
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val takeMedicationUseCase: TakeMedicationUseCase,
    private val medicationRep: MedicationRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(MedDetailUiState())
    val uiState: StateFlow<MedDetailUiState> = _uiState.asStateFlow()

    init {
        loadCurrentProfile()
        if (medicationId != null) {
            loadMedication(medicationId)
        } else {
            _uiState.value = MedDetailUiState(isLoading = false)
        }
    }

    private fun loadCurrentProfile(){
        viewModelScope.launch {
            getCurrentProfileUseCase()
                .collect { profile ->
                    _uiState.value = _uiState.value.copy(currentProfile = profile)
                }
        }
    }

    private fun loadMedication(id: String) {
        viewModelScope.launch {
            combine(
                medicationRep.getMedicationById(id),
                medicationRep.getScheduleByMedication(id)
            ) { medication, schedules ->
                _uiState.value.copy(
                    medication = medication,
                    schedules = schedules,
                    isLoading = false
                )
            }.catch { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Ошибка загрузки: ${e.message}"
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun saveMedication(
        name: String,
        dosage: Double,
        unit: MeasurementUnit,
        stock: Double,
        note: String,
        timing: Timing,
        selectedDays: Set<Int>,
        selectedTimes: List<String>
    ) {
        val profile = _uiState.value.currentProfile ?: return
        viewModelScope.launch {
            val medication = Medication(
                id = medicationId ?: UUID.randomUUID().toString(),
                profileId = profile.id,
                name = name,
                dosage = dosage,
                unit = unit,
                timing = timing,
                note = note,
                stock = stock
            )

            val schedules = buildSchedules(
                medicationId = medication.id,
                selectedDays = selectedDays,
                selectedTimes = selectedTimes
            )

            saveMedicationUseCase(medication, schedules)
                .onSuccess {
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = "Ошибка сохранения: ${e.message}"
                    )
                }
        }
    }

    fun deleteMedication() {
        medicationId?.let { id ->
            viewModelScope.launch {
                deleteMedicationUseCase(id)
                    .onSuccess {
                    }
                    .onFailure { e ->
                        _uiState.value = _uiState.value.copy(
                            error = "Ошибка удаления: ${e.message}"
                        )
                    }
            }
        }
    }

    private fun buildSchedules(
        medicationId: String,
        selectedDays: Set<Int>,
        selectedTimes: List<String>
    ): List<MedicationSchedule> {
        val schedules = mutableListOf<MedicationSchedule>()

        selectedTimes.forEach { time ->
            val (hours, minutes) = time.split(":").map { it.toInt() }
            val timeMinutes = hours * 60 + minutes

            if (selectedDays.isEmpty()) {
                schedules.add(
                    MedicationSchedule(
                        id = UUID.randomUUID().toString(),
                        medicationId = medicationId,
                        dayOfWeek = null,
                        timeMinutes = timeMinutes
                    )
                )
            } else {
                selectedDays.forEach { day ->
                    schedules.add(
                        MedicationSchedule(
                            id = UUID.randomUUID().toString(),
                            medicationId = medicationId,
                            dayOfWeek = day,
                            timeMinutes = timeMinutes
                        )
                    )
                }
            }
        }

        return schedules
    }

    fun markAsTaken(onSuccess:()->Unit){
        val id = medicationId ?: return
        viewModelScope.launch {
            takeMedicationUseCase(id)
                .onSuccess { onSuccess() }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Не удалось отметить прием: ${e.message}"
                    )
                }
        }
    }
}