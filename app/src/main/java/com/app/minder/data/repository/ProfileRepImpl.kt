package com.app.minder.data.repository

import com.app.minder.data.local.dao.ProfileDao
import com.app.minder.data.local.entity.toDomain
import com.app.minder.domain.model.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileRepImpl(
    val profileDao: ProfileDao
) : ProfileRepository {
    override fun getCurrentProfile(): Flow<Profile?> {
        return profileDao.getCurrentProfile().map { it?.toDomain()}
    }
}