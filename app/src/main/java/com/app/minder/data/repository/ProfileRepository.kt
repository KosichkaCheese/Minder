package com.app.minder.data.repository

import com.app.minder.domain.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getCurrentProfile(): Flow<Profile?>
}