package com.app.minder.data.repository

import androidx.room.withTransaction
import com.app.minder.data.local.dao.ProfileDao
import com.app.minder.data.local.database.MedDB
import com.app.minder.data.local.entity.ProfileEntity
import com.app.minder.data.local.entity.toDomain
import com.app.minder.domain.interfaces.ProfileRepository
import com.app.minder.domain.model.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileRepImpl(
    val profileDao: ProfileDao,
    val database: MedDB
) : ProfileRepository {
    override fun getCurrentProfile(): Flow<Profile?> {
        return profileDao.getCurrentProfile().map {it?.toDomain()}
    }

    override fun getProfilesByUser(): Flow<List<Profile>> {
        return profileDao.getProfilesByUser().map { profiles -> profiles.map{it.toDomain()} }
    }

    override suspend fun switchProfile(id: String) {
        database.withTransaction {
            val profile = profileDao.getProfileById(id) ?: throw Exception("Не удалось найти профиль")

            profileDao.clearCurrentProfile()
            profileDao.updateProfile(profile.copy(isCurrent = true))
        }
    }

    override suspend fun createProfile(name: String, userId: String) {
        profileDao.insertProfile(
            ProfileEntity(
                userId = userId,
                name = name
            )
        )
    }
}