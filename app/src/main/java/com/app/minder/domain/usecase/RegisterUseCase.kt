package com.app.minder.domain.usecase

import com.app.minder.data.repository.AuthRepository
import com.app.minder.domain.model.AuthResponse

class RegisterUseCase (
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke (email: String, name: String, password: String, passwordConfirm: String): Result<AuthResponse>{
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            return Result.failure(Exception("Введите корректный email"))
        }
        if (name.isBlank()){
            return Result.failure(Exception("Введите имя"))
        }
        if (password.length < 6 || password.contentEquals("password") || password.contentEquals("пароль")){
            return Result.failure(Exception("Пароль должен содержать не менее 6 символов и не быть простым словом вроде 'password' или 'пароль'"))
        }
        if (password != passwordConfirm){
            return Result.failure(Exception("Пароли не совпадают"))
        }

        return authRepository.register(email, name, password)
    }
}