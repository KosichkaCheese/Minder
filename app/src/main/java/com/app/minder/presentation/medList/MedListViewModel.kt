package com.app.minder.presentation.medList

import androidx.compose.runtime.MutableState
import com.app.minder.domain.model.Medication
import com.app.minder.domain.usecase.GetMedsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class MedListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val medications: List<Medication> = emptyList()
)

class MedListViewModel(
    private val getMedsUseCase: GetMedsUseCase
): ViewModel() {
    private val _uiState = MutableStateFlow(MedListUiState())
    val uiState: StateFlow<MedListUiState> = _uiState.asStateFlow()

    private fun loadMedications(){
        viewModelScope.launch{
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            getMedsUseCase()
                .catch {
                    e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Не удалось загрузить данные: ${e.message}",
                        medications = emptyList()
                    )
                }
                .collect {
                    medications ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                        medications = medications
                    )
                }
        }
    }
}