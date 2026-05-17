package com.app.minder.domain.usecase

import com.app.minder.data.local.entity.MeasurementEntity
import com.app.minder.domain.interfaces.MeasurementRepository

class SaveMeasurementUseCase(
    private val measurementRep: MeasurementRepository
) {
    suspend operator fun invoke(measurement: MeasurementEntity): Result<Unit> {
        return try {
            measurementRep.saveMeasurement(measurement)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}