package com.app.minder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.minder.domain.model.MeasurementType

@Entity(tableName = "measurement_types")
data class MeasurementTypeEntity (
    @PrimaryKey
    val id: String,
    val name: String,
    val unit: String,
    val lowerBound: Double?,
    val upperBound: Double?
)

fun MeasurementTypeEntity.toDomain(): MeasurementType {
    return MeasurementType(
        id = id,
        name = name,
        unit = unit,
        lowerBound = lowerBound,
        upperBound = upperBound
    )
}

fun MeasurementType.toEntity(): MeasurementTypeEntity{
    return MeasurementTypeEntity(
        id = id,
        name = name,
        unit = unit,
        lowerBound = lowerBound,
        upperBound = upperBound
    )
}