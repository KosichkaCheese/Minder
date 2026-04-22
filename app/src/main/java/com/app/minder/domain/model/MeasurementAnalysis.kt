package com.app.minder.domain.model


data class MeasurementAnalysis (
    val mean: Double? = null,
    val meanSecondary: Double? = null,
    val trend: Trend? = null,
    val trendSecondary: Trend? = null,
    val deviation: Double? = null,
    val deviationSecondary: Double? = null,
    val stability: Double? = null,
    val stabilitySecondary: Double? = null,
    val timeInRange: Double? = null,
    val timeInRangeSecondary: Double? = null
)