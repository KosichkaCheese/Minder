package com.app.minder.domain.usecase

import com.app.minder.data.repository.MedicationRepository
import com.app.minder.util.notifications.NotificationScheduler

class DeleteMedUseCase(
    private val medicationRep: MedicationRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(medicationId: String): Result<Unit>{
        return try {
            medicationRep.deleteMedication(medicationId)

            notificationScheduler.cancelMedicationReminders(medicationId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}