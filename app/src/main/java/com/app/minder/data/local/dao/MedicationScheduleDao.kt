package com.app.minder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.minder.data.local.entity.MedicationScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationSchedule(medicationScheduleEntity: MedicationScheduleEntity)

    @Query("SELECT * FROM medication_schedules WHERE medicationId = :medicationId")
    fun getScheduleByMedication(medicationId: String): Flow<List<MedicationScheduleEntity>>

    @Query("""
        SELECT * FROM medication_schedules 
        WHERE (dayOfWeek IS NULL OR dayOfWeek = :dayOfWeek)
        ORDER BY timeMinutes
        """)
    fun getTodaySchedules(dayOfWeek: Int): Flow<List<MedicationScheduleEntity>>
}