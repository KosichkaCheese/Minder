package com.app.minder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.app.minder.domain.model.Measurement
import java.util.UUID

@Entity(
    tableName = "measurements",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MeasurementTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["typeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("createdAt")]
)
data class MeasurementEntity (
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val profileId: String,
    val typeId: String,
    val result: Double,
    val note: String?,
    val createdAt: Long = System.currentTimeMillis()
)

fun MeasurementEntity.toDomain(): Measurement{
    return Measurement(
        id = id,
        result = result,
        note = note,
        createdAt = createdAt
    )
}

