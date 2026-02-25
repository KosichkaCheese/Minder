package com.app.minder.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Database
import androidx.room.withTransaction
import com.app.minder.data.local.dao.ProfileDao
import com.app.minder.data.local.dao.UserDao
import com.app.minder.data.local.database.MedDB
import com.app.minder.data.local.entity.ProfileEntity
import com.app.minder.data.local.entity.UserEntity
import com.app.minder.data.local.entity.toDomain
import com.app.minder.domain.model.User
import com.app.minder.domain.model.AuthResponse
import com.app.minder.domain.model.AuthToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.mindrot.jbcrypt.BCrypt
import java.security.MessageDigest
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class AuthRepImpl (
    private val userDao: UserDao,
    private val profileDao: ProfileDao,
    private val database: MedDB,
    private val context: Context
): AuthRepository {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
//        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
//        private val TOKEN_EXPIRES_AT_KEY = longPreferencesKey("token_expires_at")
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

            // Временная реализация для клиента

            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                return Result.failure(Exception("Пользователь с таким email уже существует"))
            }

            val userId = UUID.randomUUID().toString()
            val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
            val user = UserEntity(
                id= userId,
                name= name,
                email= email,
                password = passwordHash,
                createdAt = System.currentTimeMillis(),
                isCurrent = true
            )
            val profile = ProfileEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = name,
                isDefault = true,
                isCurrent = true,
                createdAt = System.currentTimeMillis()
            )

            database.withTransaction {
                userDao.insertUser(user)
                profileDao.insertProfile(profile)
            }

            val token = AuthToken(
                accessToken = "local_token_$userId",
                refreshToken = null,
                expiresAt = Long.MAX_VALUE
            )
            context.dataStore.edit { preferences ->
                preferences[ACCESS_TOKEN_KEY] = token.accessToken
                preferences[CURRENT_USER_ID_KEY] = userId
            }

            Result.success(AuthResponse(user.toDomain(), token))
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

            // Временно для клиента
            val user = userDao.getUserByEmail(email)
                ?: return Result.failure(Exception("Неверный email или пароль"))

            if (!BCrypt.checkpw(password, user.password)){
                return Result.failure(Exception("Неверный email или пароль"))
            }

            database.withTransaction {
                userDao.clearCurrentUser()
                profileDao.clearCurrentProfile()
                userDao.updateUser(user.copy(isCurrent = false))

                val defaultProfile = profileDao.getDefaultProfile(user.id)
                defaultProfile?.let{
                    profileDao.updateProfile(defaultProfile.copy(isCurrent = true))
                }
            }

            val token = AuthToken(
                accessToken = "local_token_${user.id}",
                refreshToken = null,
                expiresAt = Long.MAX_VALUE
            )
            context.dataStore.edit { preferences ->
                preferences[ACCESS_TOKEN_KEY] = token.accessToken
                preferences[CURRENT_USER_ID_KEY] = user.id
            }

            Result.success(AuthResponse(user.toDomain(), token))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
        database.withTransaction {
            userDao.clearCurrentUser()
            profileDao.clearCurrentProfile()
        }
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
        return token != null
    }

}