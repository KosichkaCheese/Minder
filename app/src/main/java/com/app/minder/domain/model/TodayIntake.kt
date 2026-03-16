package com.app.minder.domain.model

import android.icu.util.Calendar
import android.util.Log

data class TodayIntake(
    val scheduleId: String,
    val medication: Medication,
    val timeMinutes: Int,
    val isTaken: Boolean,
    val takenAt: Long? = null
){
    val timeString: String
        get(){
            val hours = timeMinutes/60
            val minutes = timeMinutes%60
            return String.format("%02d:%02d", hours, minutes)
        }

    val status: IntakeStatus
        get(){
            val now = System.currentTimeMillis()
            val offset = java.util.TimeZone.getDefault().getOffset(now)
            val todayMinutes = ((now + offset) % 86400000) / 60000
            Log.i("TimeNow", todayMinutes.toString())
            Log.i("NeedTime", timeMinutes.toString())

            return when {
                isTaken -> IntakeStatus.TAKEN
                todayMinutes > timeMinutes -> IntakeStatus.MISSED
                else -> IntakeStatus.UPCOMING
            }
        }

    val canBeTaken: Boolean
        get(){
            if (isTaken) return false

            val calendar = Calendar.getInstance()
            val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY)*60 + calendar.get(Calendar.MINUTE)
            val diff = kotlin.math.abs(currentMinutes - timeMinutes)

            return diff<=30
        }
}

enum class IntakeStatus {
    UPCOMING,
    TAKEN,
    MISSED
}