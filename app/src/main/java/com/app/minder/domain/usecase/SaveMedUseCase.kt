package com.app.minder.domain.usecase

import com.app.minder.domain.interfaces.MedicationRepository
import com.app.minder.domain.model.Medication
import com.app.minder.domain.model.MedicationSchedule
import com.app.minder.util.notifications.NotificationScheduler

class SaveMedUseCase(
    private val medicationRep: MedicationRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(
        medication: Medication,
        schedules: List<MedicationSchedule>
    ): Result<Unit> {
        return try {
            medicationRep.saveMedication(medication, schedules)

            notificationScheduler.scheduleMedicationReminders(
                medication.id,
                medication.name,
                schedules
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}