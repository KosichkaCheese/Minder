package com.app.minder.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.minder.data.remote.Api
import com.app.minder.data.remote.dto.InvitationAcceptRequest
import com.app.minder.data.remote.dto.UserResponse
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
    val currentProfile: Profile? = null,
    val observers: List<UserResponse> = emptyList(),
    val patients: List<UserResponse> = emptyList(),
    val inviteCode: String? = null,
    val isLinksLoading: Boolean = true,
)

class ProfileViewModel(
    private val api: Api,
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
        loadLinkedUsers()
    }

    fun loadLinkedUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLinksLoading = true, error = null)
            try {
                val observers = api.getObservers()
                val patients = api.getPatients()
                _uiState.value = _uiState.value.copy(
                    observers = observers,
                    patients = patients,
                    isLinksLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLinksLoading = false,
                    error = "Не удалось загрузить данные"
                )
            }
        }
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

    fun logout(){
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun createInvitation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = api.inviteObserver()
                _uiState.value = _uiState.value.copy(
                    inviteCode = response.code,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Не удалось создать приглашение"
                )
            }
        }
    }

    fun acceptInvitation(code: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                api.acceptInvite(InvitationAcceptRequest(code))
                loadLinkedUsers()
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: retrofit2.HttpException) {
                val message = try {
                    org.json.JSONObject(
                        e.response()?.errorBody()?.string() ?: ""
                    ).getString("detail")
                } catch (_: Exception) {
                    "Не удалось принять приглашение"
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = message
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Нет подключения к серверу"
                )
            }
        }
    }

    fun removeLink(patientId: String, observerId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                api.removeObserver(patientId, observerId)
                loadLinkedUsers()
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Не удалось отвязать пользователя"
                )
            }
        }
    }

}