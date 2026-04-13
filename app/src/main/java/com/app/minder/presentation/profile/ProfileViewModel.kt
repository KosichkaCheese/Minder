package com.app.minder.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.minder.data.repository.AuthRepository
import com.app.minder.data.repository.ProfileRepository
import com.app.minder.domain.model.Profile
import com.app.minder.domain.model.User
import com.app.minder.domain.usecase.CreateProfileUseCase
import com.app.minder.domain.usecase.GetCurrentProfileUseCase
import com.app.minder.domain.usecase.SwitchProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: User? = null,
    val profiles: List<Profile> = emptyList(),
    val currentProfile: Profile? = null
)

class ProfileViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val switchProfileUseCase: SwitchProfileUseCase,
    private val createProfileUseCase: CreateProfileUseCase,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init{
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )
        loadProfiles()
        loadCurrentProfile()
    }

    private fun loadCurrentProfile(){
        viewModelScope.launch {
            getCurrentProfileUseCase()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ошибка загрузки профиля: ${e.message}"
                    )
                }
                .collect { profile ->
                    _uiState.value = _uiState.value.copy(
                        currentProfile = profile,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    private fun loadProfiles(){
        viewModelScope.launch {
            combine(
                authRepository.getCurrentUser(),
                profileRepository.getProfilesByUser()
            ) { user, profiles ->
                _uiState.value.copy(
                    isLoading = false,
                    error = null,
                    user = user,
                    profiles = profiles
                )
            }.catch { e ->
                _uiState.value.copy(
                    isLoading = false,
                    error = "Ошибка загрузки профилей: ${e.message}"
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun switchProfile(id: String){
        viewModelScope.launch {
            switchProfileUseCase(id)
                .onFailure { e ->
                    _uiState.value  = _uiState.value.copy(
                        error = "Ошибка переключения профиля: ${e.message}"
                    )
                }
        }
    }

    fun createProfile(name: String){
        viewModelScope.launch {
            createProfileUseCase(name, uiState.value.user!!.id)
                .onFailure { e ->
                    _uiState.value  = _uiState.value.copy(
                        error = "Ошибка создания профиля: ${e.message}"
                    )
                }
        }
    }

}