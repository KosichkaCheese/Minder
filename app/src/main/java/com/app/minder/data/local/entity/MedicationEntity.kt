package com.app.minder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.app.minder.domain.model.MeasurementUnit
import com.app.minder.domain.model.Medication
import com.app.minder.domain.model.Timing
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
    val unit: MeasurementUnit,
    val timing: Timing,
    val stock: Double,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

fun MedicationEntity.toDomain(): Medication{
    return Medication(
        id = id,
        profileId = profileId,
        name = name,
        dosage = dosage,
        unit = unit,
        timing = timing,
        stock = stock,
        note = note
    )
}

fun Medication.toEntity(): MedicationEntity{
    return MedicationEntity(
        id = id,
        profileId = profileId,
        name = name,
        dosage = dosage,
        unit = unit,
        timing = timing,
        stock = stock,
        note = note
    )
}