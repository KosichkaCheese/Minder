package com.app.minder.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.minder.data.remote.Api
import com.app.minder.data.remote.dto.CheckEmailRequest
import com.app.minder.domain.usecase.LoginUseCase
import com.app.minder.domain.usecase.RegisterUseCase
import com.app.minder.util.EmailVerifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState (
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val verificationSent: Boolean = false
)

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val api: Api
): ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun sendVerification(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                api.checkEmail(CheckEmailRequest(email))
                EmailVerifier.createAndSendVerification(email, password)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    verificationSent = true
                )
            } catch (e: retrofit2.HttpException) {
                val message = try {
                    org.json.JSONObject(
                        e.response()?.errorBody()?.string() ?: ""
                    ).getString("detail")
                } catch (_: Exception) {
                    "Email уже зарегистрирован"
                }
                _uiState.value = _uiState.value.copy(isLoading = false, error = message)
            } catch (e: Exception) {
                val message = when {
                    e.message?.contains("already in use") == true -> "Email уже зарегистрирован"
                    e.message?.contains("badly formatted") == true -> "Некорректный email"
                    e.message?.contains("weak password") == true -> "Пароль слишком короткий (минимум 6 символов)"
                    else -> "Ошибка: ${e.message}"
                }
                _uiState.value = _uiState.value.copy(isLoading = false, error = message)
            }
        }
    }

    fun checkVerificationAndRegister(email: String, name: String, password: String, passwordConfirm: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val verified = EmailVerifier.isEmailVerified()

                if (!verified) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Email еще не подтвержден. Проверьте почту."
                    )
                    return@launch
                }

                registerUseCase(email, name, password, passwordConfirm)
                    .onSuccess {
                        EmailVerifier.cleanup()
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isAuthenticated = true
                        )
                    }
                    .onFailure { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Ошибка регистрации: ${e.message}"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Ошибка регистрации: ${e.message}"
                )
            }
        }
    }

    fun login(email:String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            loginUseCase(email, password)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    fun register(email:String, name:String, password: String, passwordConfirm: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            registerUseCase(email, name, password, passwordConfirm)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetAuthState() {
        _uiState.value = _uiState.value.copy(isAuthenticated = false, error = null, verificationSent = false)
    }

    fun cancelVerification() {
        EmailVerifier.cleanup()
        EmailVerifier.signOut()
        _uiState.value = _uiState.value.copy(
            verificationSent = false,
            error = null
        )
    }
}