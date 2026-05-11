package com.app.minder.data.repository

import com.app.minder.domain.model.User
import com.app.minder.domain.model.AuthResponse
import com.app.minder.domain.model.AuthToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

class AuthRepMock : AuthRepository {
    private var currentUser: User? = null
    private var token: String? = null

    override suspend fun register(email: String, name: String, password: String): Result<AuthResponse> {
        delay(500) // имитация сетевого запроса

        val user = User(
            id = UUID.randomUUID().toString(),
            name = name,
            email = email,
            createdAt = System.currentTimeMillis()
        )

        val authToken = AuthToken(
            accessToken = "mock_token_${UUID.randomUUID()}",
            refreshToken = null,
        )

        currentUser = user
        token = authToken.accessToken

        return Result.success(AuthResponse(user, authToken))
    }

    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        delay(500)

        val user = User(
            id = UUID.randomUUID().toString(),
            name = "User Mock",
            email = email,
            createdAt = System.currentTimeMillis()
        )

        val authToken = AuthToken(
            accessToken = "mock_token_${UUID.randomUUID()}",
            refreshToken = null,
        )

        currentUser = user
        token = authToken.accessToken

        return Result.success(AuthResponse(user, authToken))
    }

    override suspend fun logout() {
        currentUser = null
        token = null
    }

    override fun getCurrentUser(): Flow<User?> = flowOf(currentUser)

    override suspend fun getAuthToken(): String? = token

    override suspend fun isAuthenticated(): Boolean = token != null
}