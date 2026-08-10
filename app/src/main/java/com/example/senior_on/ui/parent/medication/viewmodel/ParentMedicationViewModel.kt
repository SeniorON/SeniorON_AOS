package com.example.senior_on.ui.parent.medication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.model.parent.ParentMedication
import com.example.senior_on.domain.model.server.MedicationSchedule
import com.example.senior_on.domain.repository.server.MedicationRepository
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
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
    private val repository: MedicationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ParentMedicationUiState())
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var submitJob: Job? = null

    fun loadMedication() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    content = ParentMedicationContent.Loading,
                    errorMessage = null
                )
            }

            runCatching {
                repository.getMySchedules(LocalDate.now().toString())
            }.onSuccess { schedules ->
                val pendingMedication = schedules
                    .filterNot(MedicationSchedule::taken)
                    .minByOrNull { schedule ->
                        Duration.between(
                            LocalTime.now(),
                            schedule.plannedTime.toLocalTimeOrNull() ?: LocalTime.MAX,
                        ).abs()
                    }
                    ?.toParentMedication()
                _uiState.update {
                    it.copy(
                        content = when {
                            pendingMedication != null -> ParentMedicationContent.Due
                            else -> ParentMedicationContent.Empty
                        },
                        medication = pendingMedication,
                        isSubmitting = false
                    )
                }
            }.onFailure { throwable ->
                if (throwable is CancellationException) {
                    return@onFailure
                }
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

        submitJob = viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            runCatching {
                val logId = medication.id.toLongOrNull()
                if (logId != null && logId > 0L) {
                    repository.markTaken(logId)
                } else {
                    repository.markNearestTaken()
                }
            }
                .onSuccess { checked ->
                    val takenAt = checked.takenAt
                        ?.let { value -> runCatching { Instant.parse(value) }.getOrNull() }
                        ?: Instant.now()
                    _uiState.update {
                        it.copy(
                            content = ParentMedicationContent.Completed,
                            medication = it.medication?.copy(takenAt = takenAt),
                            isSubmitting = false,
                        )
                    }
                }
                .onFailure { throwable ->
                    if (throwable is CancellationException) {
                        return@onFailure
                    }
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = "복약 확인을 전송하지 못했어요.",
                        )
                    }
                }
        }
    }

    fun reset() {
        loadJob?.cancel()
        loadJob = null
        submitJob?.cancel()
        submitJob = null
        _uiState.value = ParentMedicationUiState()
    }

    override fun onCleared() {
        loadJob?.cancel()
        submitJob?.cancel()
        super.onCleared()
    }

    companion object {
        fun factory(repository: MedicationRepository) = viewModelFactory {
            initializer {
                ParentMedicationViewModel(repository)
            }
        }
    }
}

private fun MedicationSchedule.toParentMedication(
    taken: Boolean = this.taken,
): ParentMedication = ParentMedication(
    id = logId.toString(),
    name = name,
    scheduledTime = plannedTime.toLocalTimeOrNull() ?: LocalTime.MIDNIGHT,
    takenAt = if (taken) Instant.EPOCH else null,
)

private fun String.toLocalTimeOrNull(): LocalTime? {
    val value = trim()
    if (value.isEmpty()) return null
    return runCatching { LocalTime.parse(value) }.getOrNull()
        ?: runCatching {
            LocalTime.parse(value, java.time.format.DateTimeFormatter.ofPattern("H:mm"))
        }.getOrNull()
}
