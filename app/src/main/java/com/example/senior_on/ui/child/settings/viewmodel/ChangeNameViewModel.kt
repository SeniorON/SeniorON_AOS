package com.example.senior_on.ui.child.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.senior_on.domain.repository.server.UserSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChangeNameUiState(
    val currentName: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val loadErrorMessage: String? = null,
    val saveErrorMessage: String? = null,
    val savedName: String? = null,
)

class ChangeNameViewModel(
    private val userSettingsRepository: UserSettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChangeNameUiState())
    val uiState: StateFlow<ChangeNameUiState> = _uiState.asStateFlow()

    fun loadCurrentName() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadErrorMessage = null,
                )
            }
            runCatching {
                userSettingsRepository.getName()
            }.onSuccess { name ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentName = name,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadErrorMessage = throwable.message
                            ?: "현재 이름을 불러오지 못했습니다.",
                    )
                }
            }
        }
    }

    fun saveName(newName: String) {
        if (_uiState.value.isSaving) return
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        if (trimmed == _uiState.value.currentName.trim()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    saveErrorMessage = null,
                    savedName = null,
                )
            }
            runCatching {
                userSettingsRepository.updateName(trimmed)
            }.onSuccess { updatedName ->
                val resolvedName = updatedName.ifBlank { trimmed }
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        currentName = resolvedName,
                        savedName = resolvedName,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveErrorMessage = throwable.message
                            ?: "이름 변경에 실패했습니다.",
                    )
                }
            }
        }
    }

    fun consumeLoadError() {
        _uiState.update { it.copy(loadErrorMessage = null) }
    }

    fun consumeSaveError() {
        _uiState.update { it.copy(saveErrorMessage = null) }
    }

    fun consumeSavedName() {
        _uiState.update { it.copy(savedName = null) }
    }

    companion object {
        fun factory(
            userSettingsRepository: UserSettingsRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ChangeNameViewModel(userSettingsRepository) as T
        }
    }
}
