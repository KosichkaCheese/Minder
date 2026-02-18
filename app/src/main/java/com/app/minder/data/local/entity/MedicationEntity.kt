package com.app.minder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.app.minder.domain.model.Timing
import com.app.minder.domain.model.Unit
import java.util.UUID

@Entity(
    tableName="medications",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MedicationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val profileId: String,
    val name: String,
    val dosage: Double,
    val unit: Unit,
    val timing: Timing,
    val stock: Double,
    val createdAt: Long = System.currentTimeMillis()
    )