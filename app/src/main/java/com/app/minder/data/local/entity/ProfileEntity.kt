package com.app.minder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.app.minder.domain.model.Profile
import java.util.UUID

@Entity(
    tableName = "profiles",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProfileEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String,
    val isDefault: Boolean = false,
    val isCurrent: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

fun ProfileEntity.toDomain(): Profile {
    return Profile(
        id = id,
        userId = userId,
        name=name,
        isDefault = isDefault,
        isCurrent = isCurrent
    )
}