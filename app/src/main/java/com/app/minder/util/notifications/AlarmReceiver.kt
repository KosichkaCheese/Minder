package com.app.minder.util.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.app.minder.MainActivity
import com.app.minder.R
import com.app.minder.data.local.database.MedDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val medicationId = intent.getStringExtra("medicationId") ?: return@launch
                val medicationName = intent.getStringExtra("medicationName") ?: return@launch
                val timeMinutes = intent.getIntExtra("timeMinutes", 0)
                val scheduleId = intent.getStringExtra("scheduleId") ?: return@launch
                val dayOfWeek = intent.getIntExtra("dayOfWeek", -1)

                if (!isMedicationTaken(context, medicationId, timeMinutes)) {
                    showNotification(context, medicationId, medicationName, timeMinutes)
                }
                scheduleNext(
                    context,
                    medicationId,
                    medicationName,
                    timeMinutes,
                    scheduleId,
                    dayOfWeek
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun scheduleNext(
        context: Context,
        medicationId: String,
        medicationName: String,
        timeMinutes: Int,
        scheduleId: String,
        dayOfWeek: Int
    ){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextTrigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timeMinutes / 60)
            set(Calendar.MINUTE, timeMinutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (dayOfWeek == -1) {
                add(Calendar.DAY_OF_YEAR, 1)
            } else {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }.timeInMillis

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("medicationId", medicationId)
            putExtra("medicationName", medicationName)
            putExtra("timeMinutes", timeMinutes)
            putExtra("scheduleId", scheduleId)
            putExtra("dayOfWeek", dayOfWeek)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (medicationId + scheduleId).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTrigger,
                    pendingIntent
                )
                Log.w("NotificationScheduler", "No exact alarm permission, using inexact")
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTrigger,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.e("NotificationScheduler", "SecurityException: ${e.message}")
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTrigger,
                pendingIntent
            )
        }
    }

    private suspend fun isMedicationTaken(
        context: Context,
        medicationId: String,
        timeMinutes: Int
    ): Boolean {
        val database = MedDB.getDB(context)
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

    private fun showNotification(
        context: Context,
        medicationId: String,
        medicationName: String,
        timeMinutes: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Напоминания о приеме лекарств",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о времени приема лекарств"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("medicationId", medicationId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            medicationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val hours = timeMinutes / 60
        val minutes = timeMinutes % 60
        val timeStr = String.format("%02d:%02d", hours, minutes)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle("Пора принять лекарство")
            .setContentText("$medicationName в $timeStr")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(medicationId.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_ID = "medication_reminders"
    }
}