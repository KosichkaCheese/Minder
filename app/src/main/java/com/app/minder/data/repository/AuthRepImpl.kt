package com.app.minder.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.room.withTransaction
import com.app.minder.data.local.dao.MedicationDao
import com.app.minder.data.local.dao.ProfileDao
import com.app.minder.data.local.dao.UserDao
import com.app.minder.data.local.database.MedDB
import com.app.minder.data.local.entity.ProfileEntity
import com.app.minder.data.local.entity.UserEntity
import com.app.minder.data.local.entity.toDomain
import com.app.minder.data.remote.Api
import com.app.minder.data.remote.dto.Login
import com.app.minder.data.remote.dto.Register
import com.app.minder.domain.model.User
import com.app.minder.domain.model.AuthResponse
import com.app.minder.domain.model.AuthToken
import com.app.minder.util.PreferencesKeys
import com.app.minder.util.notifications.NotificationScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class AuthRepImpl (
    private val api: Api,
    private val userDao: UserDao,
    private val profileDao: ProfileDao,
    private val medicationDao: MedicationDao,
    private val database: MedDB,
    private val dataStore: DataStore<Preferences>,
    private val notificationScheduler: NotificationScheduler
): AuthRepository {

    override suspend fun register(email: String, name: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.register(Register(email, password, name))
            dataStore.edit { prefs ->
                prefs[PreferencesKeys.ACCESS_TOKEN] = response.accessToken
                prefs[PreferencesKeys.REFRESH_TOKEN] = response.refreshToken
                prefs[PreferencesKeys.CURRENT_USER_ID] = response.user.id
            }

            val user = UserEntity(
                id = response.user.id,
                name = response.user.name,
                email = response.user.email,
                password = "",
                createdAt = System.currentTimeMillis(),
                isCurrent = true
            )
            val profile = ProfileEntity(
                id = UUID.randomUUID().toString(),
                userId = response.user.id,
                name = name,
                isDefault = true,
                isCurrent = true,
                createdAt = System.currentTimeMillis()
            )

            database.withTransaction {
                userDao.clearCurrentUser()
                userDao.insertUser(user)
                profileDao.clearCurrentProfile()
                profileDao.insertProfile(profile)
            }

            Result.success(AuthResponse(user.toDomain(), AuthToken(response.accessToken, response.refreshToken)))
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val message = try {
                org.json.JSONObject(errorBody ?: "").getString("detail")
            } catch (_: Exception) {
                "Ошибка регистрации"
            }
            Result.failure(Exception(message))
        } catch (e: java.net.ConnectException) {
            Result.failure(Exception("Нет подключения к серверу"))
        } catch (e: Exception) {
            Result.failure(Exception("Неизвестная ошибка: ${e.message}"))
        }
    }

    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(Login(email, password))

            dataStore.edit { preferences ->
                preferences[PreferencesKeys.ACCESS_TOKEN] = response.accessToken
                preferences[PreferencesKeys.REFRESH_TOKEN] = response.refreshToken
                preferences[PreferencesKeys.CURRENT_USER_ID] = response.user.id
            }

            val user = UserEntity(
                id = response.user.id,
                name = response.user.name,
                email = response.user.email,
                password = "",
                isCurrent = true,
                createdAt = System.currentTimeMillis()
            )

            database.withTransaction {
                userDao.clearCurrentUser()
                profileDao.clearCurrentProfile()
                userDao.insertUser(user)
                userDao.setCurrentUser(user.id, user.name, user.email)

                val existingProfile = profileDao.getDefaultProfile(user.id)
                if (existingProfile == null) {
                    val profile = ProfileEntity(
                        userId = user.id,
                        name = response.user.name,
                        isDefault = true,
                        isCurrent = true
                    )
                    profileDao.insertProfile(profile)
                } else {
                    profileDao.updateProfile(existingProfile.copy(isCurrent = true))
                }
            }

            val currentProfile = profileDao.getCurrentProfile().first()
            currentProfile?.let {
                notificationScheduler.rescheduleReminders(it.id)
            }

            Result.success(AuthResponse(user.toDomain(), AuthToken(response.accessToken, response.refreshToken)))
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val message = try {
                org.json.JSONObject(errorBody ?: "").getString("detail")
            } catch (_: Exception) {
                "Ошибка авторизации"
            }
            Result.failure(Exception(message))
        } catch (e: java.net.ConnectException) {
            Result.failure(Exception("Нет подключения к серверу"))
        } catch (e: Exception) {
            Result.failure(Exception("Неизвестная ошибка: ${e.message}"))
        }
    }

    override suspend fun logout() {
        val currentProfile = profileDao.getCurrentProfile().first()
        currentProfile?.let { profile ->
            val medications = medicationDao.getMedicationsByProfileSync(profile.id)
            medications.forEach { medication ->
                notificationScheduler.cancelMedicationReminders(medication.id)
            }
        }

        dataStore.edit { preferences ->
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
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.ACCESS_TOKEN]
        }.first()
    }

    override suspend fun isAuthenticated(): Boolean {
        val token = getAuthToken()
        return token != null
    }

}