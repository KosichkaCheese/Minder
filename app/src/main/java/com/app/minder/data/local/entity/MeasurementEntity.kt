package com.app.minder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
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