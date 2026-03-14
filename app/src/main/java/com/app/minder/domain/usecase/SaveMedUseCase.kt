package com.app.minder.domain.usecase

import com.app.minder.data.repository.MedicationRepository
import com.app.minder.domain.model.Medication
import com.app.minder.domain.model.MedicationSchedule

class SaveMedUseCase(
    private val medicationRep: MedicationRepository
) {
    suspend operator fun invoke(
        medication: Medication,
        schedules: List<MedicationSchedule>
    ): Result<Unit> {
        return try {
            medicationRep.saveMedication(medication, schedules)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}