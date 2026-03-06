package com.app.minder.data.repository

import com.app.minder.domain.model.Medication
import com.app.minder.domain.model.TodayIntake
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun getTodayIntakes(profileId: String): Flow<List<TodayIntake>>

    fun getMedicationList(profileId: String): Flow<List<Medication>>
}