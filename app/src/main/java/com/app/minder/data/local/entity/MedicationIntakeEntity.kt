package com.app.minder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "medication_intake",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicationId"), Index("createdAt")]
)
data class MedicationIntakeEntity (
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val medicationId: String,
    val createdAt: Long = System.currentTimeMillis()
)