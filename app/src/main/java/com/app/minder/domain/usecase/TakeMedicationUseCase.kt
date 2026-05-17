package com.app.minder.domain.usecase

import com.app.minder.domain.interfaces.MedicationRepository
import com.app.minder.util.notifications.NotificationScheduler
import kotlinx.coroutines.flow.first

class TakeMedicationUseCase(
    private val medicationRep: MedicationRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(medicationId: String): Result<Unit> {
        return try {
            val medication = medicationRep.getMedicationById(medicationId).first()
                ?: return Result.failure(Exception("Лекарство не найдено"))

            medicationRep.takeMedication(medicationId)

            val updatedMedication = medicationRep.getMedicationById(medicationId).first()
                ?: return Result.failure(Exception("Лекарство не найдено"))

            notificationScheduler.checkLowStock(
                medicationId=medicationId,
                medicationName = updatedMedication.name,
                dosage = updatedMedication.dosage,
                stock = updatedMedication.stock
            )

            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }
}