package com.app.minder.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.app.minder.data.local.dao.MedicationDao
import com.app.minder.data.local.dao.MedicationIntakeDao
import com.app.minder.data.local.dao.MedicationScheduleDao
import com.app.minder.data.local.database.MedDB
import com.app.minder.data.local.entity.MedicationIntakeEntity
import com.app.minder.data.local.entity.toDomain
import com.app.minder.data.local.entity.toEntity
import com.app.minder.domain.model.Medication
import com.app.minder.domain.model.MedicationSchedule
import com.app.minder.domain.model.TodayIntake
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar

class MedicationRepImpl(
    private val medicationDao: MedicationDao,
    private val scheduleDao: MedicationScheduleDao,
    private val intakeDao: MedicationIntakeDao,
    private val database: MedDB
) : MedicationRepository {
    override fun getTodayIntakes(profileId: String): Flow<List<TodayIntake>> {
        val calendar = Calendar.getInstance()
        val dayOfWeek = if (calendar.get(Calendar.DAY_OF_WEEK)== Calendar.SUNDAY) 7 else (calendar.get(Calendar.DAY_OF_WEEK)-1)

        val dayStart = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val dayEnd = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        return combine(
            medicationDao.getMedicationsByProfile(profileId),
            scheduleDao.getTodaySchedules(dayOfWeek),
            intakeDao.getAllTodayIntakes(dayStart, dayEnd)
        ){
            medications, schedules, allIntakes ->
            val result = mutableListOf<TodayIntake>()

            for (schedule in schedules){
                val medication = medications.find { it.id == schedule.medicationId }
                    ?: continue

                val intakes = allIntakes.filter{it.medicationId==medication.id}
                Log.d("GetTodayIntakes", "Schedule time: ${schedule.timeMinutes}")
                intakes.forEach { intake ->
                    val intakeMinutes = ((intake.createdAt - dayStart) / 60000).toInt()
                    val diff = kotlin.math.abs(intakeMinutes - schedule.timeMinutes)
                    Log.d("GetTodayIntakes", "Intake at $intakeMinutes, diff: $diff")
                }

                val intake = intakes.find{ intake ->
                    val intakeMinutes = ((intake.createdAt-dayStart)/60000).toInt()
                    kotlin.math.abs(intakeMinutes-schedule.timeMinutes)<=30
                }

                result.add(
                    TodayIntake(
                        scheduleId = schedule.id,
                        medication = medication.toDomain(),
                        timeMinutes = schedule.timeMinutes,
                        isTaken = intake!=null,
                        takenAt = intake?.createdAt
                    )
                )

            }
            result.sortedBy { it.timeMinutes }
        }
    }

    override fun getMedicationList(profileId: String): Flow<List<Medication>> {
        val medicationEntities = medicationDao.getMedicationsByProfile(profileId)
        val medications = medicationEntities.map{ entities -> entities.map{it.toDomain()}}
        return medications
    }

    override fun getMedicationById(id: String): Flow<Medication?> {
        val medication = medicationDao.getMedicationById(id).map {it?.toDomain()}
        return medication
    }

    override fun getScheduleByMedication(medicationId: String): Flow<List<MedicationSchedule>> {
        val schedules = scheduleDao.getScheduleByMedication(medicationId)
            .map { schedule -> schedule.map { it.toDomain() } }
        return schedules
    }

    override suspend fun deleteMedication(id: String) {
        medicationDao.deleteMedication(id)
    }

    override suspend fun saveMedication(
        medication: Medication,
        schedules: List<MedicationSchedule>
    ) {
        database.withTransaction {
            val existing = medicationDao.getMedicationByIdSync(medication.id)
            if (existing != null) {
                medicationDao.updateMedication(medication.toEntity())
            } else {
                medicationDao.insertMedication(medication.toEntity())
            }

            scheduleDao.deleteByMedication(medication.id)

            schedules.forEach { schedule ->
                scheduleDao.insertMedicationSchedule(schedule.toEntity())
            }
        }
    }

    override suspend fun takeMedication(medicationId: String) {
        database.withTransaction {
            val medication = medicationDao.getMedicationByIdSync(medicationId)
                ?: throw Exception("Лекарство не найдено")

            intakeDao.insertMedicationIntake(
                MedicationIntakeEntity(
                    medicationId = medicationId
                )
            )

            val newStock = (medication.stock - medication.dosage).coerceAtLeast(0.0)
            medicationDao.updateMedication(
                medication.copy( stock = newStock)
            )
        }
    }

    override fun getIntakesByRange(medicationId: String, start: Long): Flow<List<MedicationIntakeEntity>> {
        return intakeDao.getIntakesByRange(medicationId, start)
    }

}