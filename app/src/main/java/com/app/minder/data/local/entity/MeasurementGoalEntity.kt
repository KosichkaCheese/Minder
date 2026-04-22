package com.app.minder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.app.minder.domain.model.MeasurementGoal
import java.util.UUID

@Entity(
    tableName = "measurement_goals",
    foreignKeys = [
        ForeignKey(
            entity = MeasurementTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["typeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MeasurementGoalEntity (
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val typeId: String,
    val profileId: String,
    val value: Double
)

fun MeasurementGoalEntity.toDomain(): MeasurementGoal{
    return MeasurementGoal(
        id = id,
        typeId = typeId,
        profileId = profileId,
        value = value
    )
}

fun MeasurementGoal.toEntity(): MeasurementGoalEntity{
    return MeasurementGoalEntity(
        id = id,
        typeId = typeId,
        profileId = profileId,
        value = value
    )
}