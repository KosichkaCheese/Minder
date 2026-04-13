package com.app.minder.domain.usecase

import com.app.minder.data.repository.ProfileRepository

class CreateProfileUseCase(
    private val profileRep: ProfileRepository
) {
    suspend operator fun invoke(name: String, userId: String): Result<Unit>{
        if (name.isBlank()){
            return Result.failure(Exception("Профиль должен иметь имя"))
        }
        try{
            profileRep.createProfile(name, userId)
            return Result.success(Unit)
        } catch (e: Exception){
            return Result.failure(e)
        }
    }
}