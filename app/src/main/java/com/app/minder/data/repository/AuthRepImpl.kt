package com.app.minder.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.minder.data.local.dao.UserDao
import com.app.minder.data.local.entity.toDomain
import com.app.minder.data.local.entity.toEntity
import com.app.minder.domain.model.User
import com.app.minder.domain.model.AuthResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class AuthRepImpl (
    private val userDao: UserDao,
    private val context: Context
): AuthRepository {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val TOKEN_EXPIRES_AT_KEY = longPreferencesKey("token_expires_at")
        private val CURRENT_USER_ID_KEY = stringPreferencesKey("current_user_id")
    }

    override suspend fun register(email: String, name: String, password: String): Result<AuthResponse> {
        return try {
            // TODO: Когда будет сервер
            // val response = authApiService.register(
            //     RegisterRequest(name, email, password)
            // )
            // saveAuthData(response)
            // Result.success(response.toDomain())

            // ВРЕМЕННАЯ ЗАГЛУШКА
            Result.failure(Exception("Сервер еще не подключен"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            // TODO: Когда будет сервер
            // val response = authApiService.login(
            //     LoginRequest(email, password)
            // )
            // saveAuthData(response)
            // Result.success(response.toDomain())

            // ВРЕМЕННАЯ ЗАГЛУШКА
            Result.failure(Exception("Сервер еще не подключен"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }

        userDao.clearCurrentUser()
    }

    override fun getCurrentUser(): Flow<User?> {
        return userDao.getCurrentUser().map { it?.toDomain() }
    }

    override suspend fun getAuthToken(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY]
        }.first()
    }

    override suspend fun isAuthenticated(): Boolean {
        val token = getAuthToken()
        val expiresAt = context.dataStore.data.map { it[TOKEN_EXPIRES_AT_KEY] }.first()

        return token != null && expiresAt != null && expiresAt > System.currentTimeMillis()
    }

    private suspend fun saveAuthData(response: AuthResponse) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = response.token.accessToken
            response.token.refreshToken?.let {
                preferences[REFRESH_TOKEN_KEY] = it
            }
            preferences[TOKEN_EXPIRES_AT_KEY] = response.token.expiresAt
            preferences[CURRENT_USER_ID_KEY] = response.user.id
        }

        userDao.clearCurrentUser()
        userDao.insertUser(response.user.toEntity(isCurrent = true))
    }
}