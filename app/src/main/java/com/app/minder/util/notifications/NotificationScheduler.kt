package com.app.minder.util.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.app.minder.MainActivity
import com.app.minder.R
import com.app.minder.domain.model.MedicationSchedule
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationScheduler(
    private val context: Context
) {
    companion object {
        const val STOCK_CHANNEL_ID = "stock_reminders"
    }

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

    fun cancelMedicationReminders(medicationId: String) {
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

    private fun createStockChannel(notificationManager: NotificationManager){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                STOCK_CHANNEL_ID,
                "Уведомления о запасе лекарств",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о том, когда заканчивается лекарство"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showStockNotification(
        medicationId: String,
        medicationName: String,
        remainingIntakes: Int
    ){
        val context = this.context
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createStockChannel(notificationManager)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("medicationId", medicationId)
            putExtra("action", "refill_medication")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            "stock_$medicationId".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = when (remainingIntakes) {
            1 -> "Осталось на 1 прием!"
            else -> "Осталось на $remainingIntakes приема!"
        }

        val notification = NotificationCompat.Builder(context, STOCK_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle("Заканчивается запас $medicationName")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationId = "stock_${medicationId}_$remainingIntakes".hashCode()
        notificationManager.notify(notificationId, notification)
    }

    fun checkLowStock(
        medicationId: String,
        medicationName: String,
        dosage: Double,
        stock: Double
    ){
        if (dosage<=0) return

        val remainingIntakes = (stock/dosage).toInt()

        if (remainingIntakes in 1..3){
            showStockNotification(
                medicationId,
                medicationName,
                remainingIntakes
            )
        }

    }

}