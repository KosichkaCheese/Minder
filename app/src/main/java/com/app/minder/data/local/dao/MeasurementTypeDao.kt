package com.app.minder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.minder.data.local.entity.MeasurementTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementTypeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(types: List<MeasurementTypeEntity>)

    @Query("SELECT COUNT(*) FROM measurement_types")
    suspend fun getCount(): Int

    @Query("SELECT * FROM measurement_types")
    fun getAllMeasurementTypes(): Flow<List<MeasurementTypeEntity>>

    @Query("SELECT * FROM measurement_types WHERE id = :id")
    fun getMeasurementTypeById(id: String): Flow<MeasurementTypeEntity>
}