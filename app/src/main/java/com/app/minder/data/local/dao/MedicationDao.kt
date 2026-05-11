package com.app.minder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.minder.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMedication(med: MedicationEntity)

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationByIdSync(id: String): MedicationEntity?

    @Update
    suspend fun updateMedication(med: MedicationEntity)

    @Query("SELECT * FROM medications WHERE profileId = :profileId")
    fun getMedicationsByProfile(profileId: String): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE profileId = :profileId")
    suspend fun getMedicationsByProfileSync(profileId: String): List<MedicationEntity>

    @Query("SELECT * FROM medications WHERE id = :id")
    fun getMedicationById(id: String): Flow<MedicationEntity?>

    @Query("DELETE FROM medications WHERE id=:id")
    suspend fun deleteMedication(id: String)
}