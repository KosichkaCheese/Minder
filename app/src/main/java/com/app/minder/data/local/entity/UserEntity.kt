package com.app.minder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

import com.app.minder.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val password: String, //временно для клиента
    val createdAt: Long,
    val isCurrent: Boolean = false
)

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        name=name,
        email=email,
        createdAt=createdAt
    )
}

//fun User.toEntity(isCurrent: Boolean = false): UserEntity {
//    return UserEntity(
//        id = id,
//        name = name,
//        email = email,
//        createdAt = createdAt,
//        isCurrent = isCurrent
//    )
//}
