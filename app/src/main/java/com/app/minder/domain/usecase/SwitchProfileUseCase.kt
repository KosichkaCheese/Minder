package com.app.minder.domain.usecase;

import com.app.minder.data.repository.ProfileRepository;

class SwitchProfileUseCase(
        private val profileRep: ProfileRepository
) {
    suspend operator fun invoke(profileId: String): Result<Unit> {
        return try {
            profileRep.switchProfile(profileId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
