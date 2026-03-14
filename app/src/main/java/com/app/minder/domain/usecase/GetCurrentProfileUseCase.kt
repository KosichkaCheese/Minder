package com.app.minder.domain.usecase

import android.util.Log
import com.app.minder.data.repository.ProfileRepository
import com.app.minder.domain.model.Profile
import kotlinx.coroutines.flow.Flow

class GetCurrentProfileUseCase(
    private val profileRep: ProfileRepository
    ) {
        operator fun invoke(): Flow<Profile?> {
            return profileRep.getCurrentProfile()
        }
    }