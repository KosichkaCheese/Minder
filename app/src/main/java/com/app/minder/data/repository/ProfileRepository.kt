package com.app.minder.data.repository

import com.app.minder.domain.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getCurrentProfile(): Flow<Profile?>
    fun getProfilesByUser(): Flow<List<Profile>>
    suspend fun switchProfile(id: String)
    suspend fun createProfile(name: String, userId: String)
}