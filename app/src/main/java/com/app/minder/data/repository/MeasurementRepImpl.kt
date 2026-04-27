package com.app.minder.data.repository

import com.app.minder.data.local.dao.MeasurementDao
import com.app.minder.data.local.dao.MeasurementGoalDao
import com.app.minder.data.local.dao.MeasurementTypeDao
import com.app.minder.data.local.database.MedDB
import com.app.minder.data.local.entity.MeasurementEntity
import com.app.minder.data.local.entity.MeasurementGoalEntity
import com.app.minder.data.local.entity.MeasurementTypeEntity
import com.app.minder.data.local.entity.toDomain
import com.app.minder.data.local.entity.toEntity
import com.app.minder.domain.model.Measurement
import com.app.minder.domain.model.MeasurementGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MeasurementRepImpl(
    private val measurementGoalDao: MeasurementGoalDao,
    private val measurementDao: MeasurementDao,
    private val measurementTypeDao: MeasurementTypeDao,
    private val database: MedDB
): MeasurementRepository {
    override fun getMeasurementsTypeById(measurementTypeId: String): Flow<MeasurementTypeEntity?> {
        return measurementTypeDao.getMeasurementTypeById(measurementTypeId)
    }

    override fun getGoal(measurementTypeId: String, profileId: String): Flow<MeasurementGoal?> {
        val goal = measurementGoalDao.getMeasurementGoalByTypeId(measurementTypeId, profileId)
        return goal.map { it?.toDomain() }
    }

    override suspend fun saveGoal(measurementGoal: MeasurementGoalEntity) {
        measurementGoalDao.insertMeasurementGoal(measurementGoal)
    }

    override suspend fun updateGoal(measurementGoal: MeasurementGoal) {
        measurementGoalDao.updateMeasurementGoal(measurementGoal.toEntity())
    }

    override fun getMeasurementsByRange(typeId: String, profileId: String, start: Long): Flow<List<Measurement>> {
        val measurements = measurementDao.getMeasurementsByTypeIdAndRange(typeId, profileId, start)
        return measurements.map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun saveMeasurement(measurement: MeasurementEntity) {
        measurementDao.insertMeasurement(measurement)
    }

    override fun getAllMeasurementTypes(): Flow<List<MeasurementTypeEntity>> {
        return measurementTypeDao.getAllMeasurementTypes()
    }
}