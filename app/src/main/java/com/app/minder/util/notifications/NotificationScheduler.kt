package com.app.minder.util.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.app.minder.domain.model.MedicationSchedule
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationScheduler(
    private val context: Context
) {
    private fun calculateInitialDelay(schedule: MedicationSchedule): Long{
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, schedule.timeMinutes / 60)
            set(Calendar.MINUTE, schedule.timeMinutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            schedule.dayOfWeek?.let { dayOfWeek ->
                val calendarDayOfWeek = if (dayOfWeek == 7) Calendar.SUNDAY else dayOfWeek + 1
                set(Calendar.DAY_OF_WEEK, calendarDayOfWeek)

                if (timeInMillis <= now.timeInMillis) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            } ?: run {
                if (timeInMillis <= now.timeInMillis) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }

        return target.timeInMillis - now.timeInMillis
    }

    private fun scheduleReminder(
        medicationId: String,
        medicationName: String,
        schedule: MedicationSchedule
    ) {
        val inputData = workDataOf(
            "medicationId" to medicationId,
            "medicationName" to medicationName,
            "timeMinutes" to schedule.timeMinutes,
        )
        val initialDelay = calculateInitialDelay(schedule)

        val workRequest = if (schedule.dayOfWeek==null) {
            PeriodicWorkRequestBuilder<MedicationReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("medication_$medicationId")
                .addTag("schedule_${schedule.id}")
                .build()
        } else {
            PeriodicWorkRequestBuilder<MedicationReminderWorker>(7, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("medication_$medicationId")
                .addTag("schedule_${schedule.id}")
                .build()
        }

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "medication_${medicationId}_${schedule.id}",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
    }

    suspend fun cancelMedicationReminders(medicationId: String) {
        WorkManager.getInstance(context)
            .cancelAllWorkByTag("medication_$medicationId")
    }

    suspend fun scheduleMedicationReminders(
        medicationId: String,
        medicationName: String,
        schedules: List<MedicationSchedule>
    ) {
        cancelMedicationReminders(medicationId)

        schedules.forEach { schedule ->
            scheduleReminder(medicationId, medicationName, schedule)
        }
    }

}