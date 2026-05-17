package com.app.minder.domain.usecase

import com.app.minder.domain.interfaces.MedicationRepository
import com.app.minder.domain.interfaces.ProfileRepository
import com.app.minder.domain.model.TodayIntake
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class GetTodayIntakesUseCase(
    private val medicationRep: MedicationRepository,
    private val profileRep: ProfileRepository
    ) {
        operator fun invoke(): Flow<List<TodayIntake>> {
            return profileRep.getCurrentProfile()
                .flatMapLatest { profile ->
                    profile?.let {
                        medicationRep.getTodayIntakes(it.id)
                    } ?: flowOf(emptyList())
                }
        }

    }