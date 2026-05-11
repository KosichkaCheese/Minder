package com.app.minder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.minder.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE users SET isCurrent = 1, name = :name, email = :email WHERE id = :id")
    suspend fun setCurrentUser(id: String, name: String, email: String)

    @Query("select * from users where id=:id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE isCurrent = 1 LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    @Query("UPDATE users SET isCurrent = 0")
    suspend fun clearCurrentUser()

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun deleteAll()

    @Query("SELECT * FROM users where email=:email")
    suspend fun getUserByEmail(email: String): UserEntity?
}