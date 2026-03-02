package com.app.minder.domain.model

data class MedicationSchedule(
    val id: String,
    val medicationId: String,
    val dayOfWeek: Int?,
    val timeMinutes: Int
)
