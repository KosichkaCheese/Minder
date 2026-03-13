package com.app.minder.domain.model

data class Medication(
    val id: String,
    val profileId: String,
    val name: String,
    val dosage: Double,
    val unit: MeasurementUnit,
    val timing: Timing,
    val stock: Double,
)
