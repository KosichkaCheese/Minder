package com.app.minder.domain.interfaces

import com.app.minder.data.local.entity.MeasurementEntity
import com.app.minder.data.local.entity.MeasurementGoalEntity
import com.app.minder.data.local.entity.MeasurementTypeEntity
import com.app.minder.domain.model.Measurement
import com.app.minder.domain.model.MeasurementGoal
import kotlinx.coroutines.flow.Flow

interface MeasurementRepository {
    fun getMeasurementsTypeById(measurementTypeId: String): Flow<MeasurementTypeEntity?>
    fun getGoal(measurementTypeId: String, profileId: String): Flow<MeasurementGoal?>
    suspend fun saveGoal(measurementGoal: MeasurementGoalEntity)
    suspend fun updateGoal(measurementGoal: MeasurementGoal)
    fun getMeasurementsByRange(typeId: String, profileId: String, start: Long): Flow<List<Measurement>>
    suspend fun saveMeasurement(measurement: MeasurementEntity)
    fun getAllMeasurementTypes(): Flow<List<MeasurementTypeEntity>>
}