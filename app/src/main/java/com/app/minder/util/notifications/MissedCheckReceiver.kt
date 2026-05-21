package com.app.minder.util.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.app.minder.data.local.database.MedDB
import com.app.minder.data.remote.Client
import com.app.minder.data.remote.dto.MissedIntakeNotification
import com.app.minder.util.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class MissedCheckReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getStringExtra("medicationId") ?: return
        val timeMinutes = intent.getIntExtra("timeMinutes", 0)

        CoroutineScope(Dispatchers.IO).launch {
            if (!isMedicationTaken(context, medicationId, timeMinutes)) {
                notifyObservers(context, timeMinutes)
            }
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

    private suspend fun notifyObservers(context: Context, timeMinutes: Int) {
        try {
            val hours = timeMinutes / 60
            val minutes = timeMinutes % 60
            val timeFormatted = "%02d:%02d".format(hours, minutes)

            val api = Client.createApi(context.dataStore)
            api.notifyMissedIntake(MissedIntakeNotification(
                time = timeFormatted
            ))
            Log.d("MissedCheck", "Observers notified")
        } catch (e: Exception) {
            Log.e("MissedCheck", "Failed: ${e.message}")
        }
    }
}