package com.app.minder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.minder.data.local.entity.MedicationIntakeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationIntakeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationIntake(medicationIntake: MedicationIntakeEntity)

    @Query("""
        SELECT * FROM medication_intake
        WHERE medicationId = :medicationId
        AND createdAt >= :dayStart
        AND createdAt <= :dayEnd
        """)
    fun getTodayIntakesByMedication(medicationId: String, dayStart: Long, dayEnd: Long,): Flow<List<MedicationIntakeEntity>>

}