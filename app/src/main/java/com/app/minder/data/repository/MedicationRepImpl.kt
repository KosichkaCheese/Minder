package com.app.minder.data.repository

import com.app.minder.data.local.dao.MedicationDao
import com.app.minder.data.local.dao.MedicationIntakeDao
import com.app.minder.data.local.dao.MedicationScheduleDao
import com.app.minder.data.local.entity.toDomain
import com.app.minder.domain.model.TodayIntake
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.util.Calendar

class MedicationRepImpl(
    private val medicationDao: MedicationDao,
    private val scheduleDao: MedicationScheduleDao,
    private val intakeDao: MedicationIntakeDao
) : MedicationRepository {
    override fun getTodayIntakes(profileId: String): Flow<List<TodayIntake>> {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        val dayStart = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val dayEnd = dayStart + 86400000

        return combine(
            medicationDao.getMedicationsByProfile(profileId),
            scheduleDao.getTodaySchedules(dayOfWeek)
        ){
            medications, schedules ->
            val result = mutableListOf<TodayIntake>()

            for (schedule in schedules){
                val medication = medications.find { it.id == schedule.medicationId }
                    ?: continue

                val intakes = intakeDao.getTodayIntakesByMedication(medication.id, dayStart, dayEnd).first()
                val intake = intakes.find{ intake ->
                    val intakeMinutes = ((intake.createdAt-dayStart)/60000).toInt()
                    kotlin.math.abs(intakeMinutes-schedule.timeMinutes)<30
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
}