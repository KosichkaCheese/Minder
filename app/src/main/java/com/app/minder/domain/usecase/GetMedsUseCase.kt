package com.app.minder.domain.usecase

import com.app.minder.domain.interfaces.MedicationRepository
import com.app.minder.domain.interfaces.ProfileRepository
import com.app.minder.domain.model.Medication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class GetMedsUseCase(
    private val medicationRep: MedicationRepository,
    private val profileRep: ProfileRepository
) {
    operator fun invoke(): Flow<List<Medication>> {
        return profileRep.getCurrentProfile()
            .flatMapLatest { profile ->
                profile?.let{
                    medicationRep.getMedicationList(it.id)
                } ?: flowOf(emptyList())
            }
    }
}