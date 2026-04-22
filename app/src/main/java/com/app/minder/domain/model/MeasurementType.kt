package com.app.minder.domain.model

data class MeasurementType (
    val id: String,
    val name: String,
    val unit: String,
    val lowerBound: Double?,
    val upperBound: Double?
)

const val BLOOD_PRESSURE_SYSTOLIC = "blood_pressure_systolic"
const val BLOOD_PRESSURE_DIASTOLIC = "blood_pressure_diastolic"
