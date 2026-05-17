package com.app.minder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.app.minder.domain.model.MedicationSchedule
import java.util.Calendar
import java.util.UUID

@Entity(
    tableName = "medication_schedules",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MedicationScheduleEntity (
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val medicationId: String,
    val dayOfWeek: Int?,
    val timeMinutes: Int
)

fun MedicationScheduleEntity.toDomain(): MedicationSchedule {
    return MedicationSchedule(
        id = id,
        medicationId = medicationId,
        dayOfWeek = dayOfWeek,
        timeMinutes = timeMinutes
    )
}

fun MedicationSchedule.toEntity(): MedicationScheduleEntity {
    return MedicationScheduleEntity(
        id = id,
        medicationId = medicationId,
        dayOfWeek = dayOfWeek,
        timeMinutes = timeMinutes
    )
}

fun String.toMinutes(): Int {
    val (hours, minutes) = split(":").map { it.toInt() }
    return hours * 60 + minutes
}

fun Int.toTimeString(): String {
    val hours = this / 60
    val minutes = this % 60
    return String.format("%02d:%02d", hours, minutes)
}

fun Int.toCalendar(date: Calendar): Calendar {
    return date.apply {
        set(Calendar.HOUR_OF_DAY, this@toCalendar / 60)
        set(Calendar.MINUTE, this@toCalendar % 60)
        set(Calendar.SECOND, 0)
    }
}