package com.app.minder.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.minder.domain.model.TodayIntake
import com.app.minder.domain.usecase.GetTodayIntakesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val intakes: List<TodayIntake> = emptyList(),
    val progressPercent: Int = 0
)

class HomeViewModel(
    private val getTodayIntakesUseCase: GetTodayIntakesUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTodayIntakes()
    }

    private fun loadTodayIntakes(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            getTodayIntakesUseCase()
                .catch {
                    e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        intakes = emptyList(),
                        error = "Не удалось загрузить данные: ${e.message}"
                    )
                }
                .collect {
                    intakes ->
                    val takenCount = intakes.count { it.isTaken }
                    val totalCount = intakes.size
                    val progress = if (totalCount>0) (takenCount*100/totalCount) else 0

                    _uiState.value = HomeUiState(
                        isLoading = false,
                        intakes = intakes,
                        error = null,
                        progressPercent = progress
                    )
                }
        }
    }
}