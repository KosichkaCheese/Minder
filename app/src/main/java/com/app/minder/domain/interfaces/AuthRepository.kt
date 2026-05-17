package com.app.minder.domain.interfaces

import com.app.minder.domain.model.AuthResponse
import com.app.minder.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(email: String, name: String, password: String): Result<AuthResponse>
    suspend fun login(email: String, password: String): Result<AuthResponse>
    suspend fun logout()
    fun getCurrentUser(): Flow<User?>
    suspend fun getAuthToken(): String?
    suspend fun isAuthenticated(): Boolean
}