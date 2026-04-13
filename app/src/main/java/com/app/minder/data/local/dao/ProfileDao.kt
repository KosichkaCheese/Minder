package com.app.minder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.minder.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Query("UPDATE profiles SET isCurrent = 0")
    suspend fun clearCurrentProfile()

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Query("SELECT * FROM profiles WHERE userId=:userId AND isDefault=1 LIMIT 1")
    suspend fun getDefaultProfile(userId: String): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE userId in (SELECT userId FROM users WHERE isCurrent=1) AND isCurrent=1")
    fun getCurrentProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE userId in (SELECT userId FROM users WHERE isCurrent=1)")
    fun getProfilesByUser(): Flow<List<ProfileEntity>>

    @Query("SELECT * from profiles WHERE id=:id")
    suspend fun  getProfileById(id: String): ProfileEntity?
}