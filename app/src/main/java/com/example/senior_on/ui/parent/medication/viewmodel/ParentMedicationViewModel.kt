package com.example.senior_on.ui.parent.medication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.repository.parent.ParentMedicationRepository
import com.example.senior_on.domain.model.parent.ParentMedication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ParentMedicationContent {
    Loading,
    Due,
    Completed,
    Empty
}

data class ParentMedicationUiState(
    val content: ParentMedicationContent = ParentMedicationContent.Loading,
    val medication: ParentMedication? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

class ParentMedicationViewModel(
    private val repository: ParentMedicationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ParentMedicationUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadMedication()
    }

    fun loadMedication() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    content = ParentMedicationContent.Loading,
                    errorMessage = null
                )
            }

            runCatching {
                repository.getTodayMedication()
            }.onSuccess { medication ->
                _uiState.update {
                    it.copy(
                        content = when {
                            medication == null -> ParentMedicationContent.Empty
                            medication.takenAt != null ->
                                ParentMedicationContent.Completed
                            else -> ParentMedicationContent.Due
                        },
                        medication = medication,
                        isSubmitting = false
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        content = ParentMedicationContent.Empty,
                        medication = null,
                        errorMessage = "복약 정보를 불러오지 못했어요."
                    )
                }
            }
        }
    }

    fun markAsTaken() {
        val medication = _uiState.value.medication ?: return
        if (_uiState.value.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            runCatching { repository.markAsTaken(medication.id) }
                .onSuccess { updatedMedication ->
                    _uiState.update {
                        it.copy(
                            content = ParentMedicationContent.Completed,
                            medication = updatedMedication,
                            isSubmitting = false
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = "복약 확인을 전송하지 못했어요."
                        )
                    }
                }
        }
    }

    companion object {
        fun factory(repository: ParentMedicationRepository) = viewModelFactory {
            initializer {
                ParentMedicationViewModel(repository)
            }
        }
    }
}
