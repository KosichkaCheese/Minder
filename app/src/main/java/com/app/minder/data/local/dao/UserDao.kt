package com.app.minder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.minder.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("select * from users where id=:id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE isCurrent = 1 LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    @Query("UPDATE users SET isCurrent = 0")
    suspend fun clearCurrentUser()

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}