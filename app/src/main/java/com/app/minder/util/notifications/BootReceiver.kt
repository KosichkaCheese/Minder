package com.app.minder.util.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.minder.data.local.database.MedDB
import com.app.minder.data.repository.MedicationRepImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val database = MedDB.getDB(context)
            val medicationRepository = MedicationRepImpl(
                database = database,
                medicationDao = database.medicationDao(),
                scheduleDao = database.medicationScheduleDao(),
                intakeDao = database.medicationIntakeDao()
            )

            val notificationScheduler = NotificationScheduler(
                context = context,
                medicationRep = medicationRepository
            )

            CoroutineScope(Dispatchers.IO).launch {
                val currentProfile = database.profileDao().getCurrentProfile().first()

                currentProfile?.let { profile ->
                    notificationScheduler.rescheduleReminders(profile.id)
                }
            }
        }
    }
}