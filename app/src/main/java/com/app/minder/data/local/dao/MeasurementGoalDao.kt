package com.app.minder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.minder.data.local.entity.MeasurementGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurementGoal(measurementGoal: MeasurementGoalEntity)

    @Query("SELECT * FROM measurement_goals WHERE typeId = :typeId AND profileId = :profileId")
    fun getMeasurementGoalByTypeId(typeId: String, profileId: String): Flow<MeasurementGoalEntity?>

    @Update
    suspend fun updateMeasurementGoal(measurementGoal: MeasurementGoalEntity)
}