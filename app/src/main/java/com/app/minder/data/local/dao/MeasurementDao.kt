package com.app.minder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.minder.data.local.entity.MeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: MeasurementEntity)

    @Query("SELECT * FROM measurements WHERE typeId = :measurementTypeId AND profileId = :profileId")
    fun getMeasurementsByTypeId(measurementTypeId: String, profileId: String): Flow<List<MeasurementEntity>>

    @Query("SELECT * from measurements WHERE typeId=:typeId AND profileId = :profileId AND createdAt>=:start")
    fun getMeasurementsByTypeIdAndRange(typeId: String, profileId: String, start: Long): Flow<List<MeasurementEntity>>
}