package com.app.minder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "measurement_types")
data class MeasurementTypeEntity (
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val unit: String,
    val lowerBound: Double?,
    val upperBound: Double?
)