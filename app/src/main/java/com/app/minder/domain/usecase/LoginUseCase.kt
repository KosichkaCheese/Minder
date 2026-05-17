package com.app.minder.domain.usecase

import com.app.minder.domain.interfaces.AuthRepository
import com.app.minder.domain.model.AuthResponse

class LoginUseCase(
    private val authRepository: AuthRepository
){
    suspend operator fun invoke (email: String, password: String): Result<AuthResponse>{
        if (email.isBlank() || password.isBlank()){
            return Result.failure(Exception("Нужно заполнить все поля"))
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            return Result.failure(Exception("Введите корректный email"))
        }

        return authRepository.login(email, password)
    }
}