package com.app.minder.domain.model

class Measurement (
    val id: String,
    val result: Double,
    val note: String?,
    val createdAt: Long = System.currentTimeMillis()
)