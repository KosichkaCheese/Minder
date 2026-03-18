package com.app.minder.util.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.app.minder.MainActivity
import com.app.minder.R
import com.app.minder.data.local.database.MedDB
import kotlinx.coroutines.flow.first
import java.util.Calendar

class MedicationReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    companion object {
        const val CHANNEL_ID = "medication_reminders"
    }

    private fun createChannel(notificationManager: NotificationManager){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Напоминания о приеме лекарств",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showIntakeNotification(
        medicationId: String,
        medicationName: String,
        timeMinutes: Int
    ){
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannel(notificationManager)

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("medicationId", medicationId)
            putExtra("action", "take_medication")
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            medicationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val hours = timeMinutes / 60
        val minutes = timeMinutes % 60
        val timeStr = String.format("%02d:%02d", hours, minutes)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle("Пора принять лекарство")
            .setContentText("$medicationName в $timeStr")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(medicationId.hashCode(), notification)
    }

    private suspend fun isMedicationTaken(medicationId: String, timeMinutes: Int): Boolean {
        val database = MedDB.getDB(applicationContext)

        val calendar = Calendar.getInstance()
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

        val intakes = database.medicationIntakeDao()
            .getTodayIntakesByMedication(medicationId, dayStart, dayEnd)
            .first()

        return intakes.any { intake ->
            val intakeMinutes = ((intake.createdAt - dayStart) / 60000).toInt()
            kotlin.math.abs(intakeMinutes - timeMinutes) < 30
        }
    }

    override suspend fun doWork(): Result {
        val medicationId = inputData.getString("medicationId") ?: return Result.failure()
        val medicationName = inputData.getString("medicationName") ?: return Result.failure()
        val timeMinutes = inputData.getInt("timeMinutes", 0)

        if (isMedicationTaken(medicationId, timeMinutes)) {
            return Result.success()
        }

        showIntakeNotification(
            medicationId,
            medicationName,
            timeMinutes
        )

        return Result.success()
    }

}