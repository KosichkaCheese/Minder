package com.app.minder.domain.usecase

import com.app.minder.data.repository.MedicationRepository

class DeleteMedUseCase(
    private val medicationRep: MedicationRepository
) {
    suspend operator fun invoke(medicationId: String): Result<Unit>{
        return try {
            medicationRep.deleteMedication(medicationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}