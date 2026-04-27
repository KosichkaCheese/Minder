package com.app.minder.data.repository

import com.app.minder.data.local.entity.MedicationEntity
import com.app.minder.data.local.entity.MedicationIntakeEntity
import com.app.minder.data.local.entity.MedicationScheduleEntity
import com.app.minder.domain.model.Medication
import com.app.minder.domain.model.MedicationSchedule
import com.app.minder.domain.model.TodayIntake
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun getTodayIntakes(profileId: String): Flow<List<TodayIntake>>

    fun getMedicationList(profileId: String): Flow<List<Medication>>
    fun getMedicationById(id: String): Flow<Medication?>
    suspend fun deleteMedication(id: String)
    fun getScheduleByMedication(medicationId: String): Flow<List<MedicationSchedule>>
    suspend fun saveMedication(medication: Medication, schedules: List<MedicationSchedule>)
    suspend fun takeMedication(medicationId: String)
    fun getIntakesByRange(medicationId: String, start: Long): Flow<List<MedicationIntakeEntity>>
}