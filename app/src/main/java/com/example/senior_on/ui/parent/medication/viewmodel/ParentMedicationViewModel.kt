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
import java.time.format.DateTimeParseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ParentMedicationContent {
    Loading,
    List,
    Completed,
    Empty,
}

enum class ParentMedicationMessageType {
    Default,
    OutsideTakingWindow,
}

data class ParentMedicationUiState(
    val content: ParentMedicationContent = ParentMedicationContent.Loading,
    val medications: List<ParentMedication> = emptyList(),
    val highlightedMedicationId: String? = null,
    val submittingMedicationId: String? = null,
    val message: String? = null,
    val messageType: ParentMedicationMessageType = ParentMedicationMessageType.Default,
)

class ParentMedicationViewModel(
    private val repository: MedicationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ParentMedicationUiState())
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var submitJob: Job? = null

    fun loadMedication(highlightedMedicationLogId: Long? = null) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    content = ParentMedicationContent.Loading,
                    highlightedMedicationId = highlightedMedicationLogId?.toString(),
                    message = null,
                    messageType = ParentMedicationMessageType.Default,
                )
            }

            runCatching {
                repository.getMySchedules(LocalDate.now().toString())
            }.onSuccess { schedules ->
                val medications = schedules
                    .sortedBy { it.plannedTime.toLocalTimeOrNull() ?: LocalTime.MAX }
                    .map(MedicationSchedule::toParentMedication)
                _uiState.update {
                    it.copy(
                        content = if (medications.isEmpty()) {
                            ParentMedicationContent.Empty
                        } else {
                            ParentMedicationContent.List
                        },
                        medications = medications,
                        submittingMedicationId = null,
                    )
                }
            }.onFailure { throwable ->
                if (throwable is CancellationException) return@onFailure
                _uiState.update {
                    it.copy(
                        content = ParentMedicationContent.Empty,
                        medications = emptyList(),
                        message = "복약 정보를 불러오지 못했어요.",
                        messageType = ParentMedicationMessageType.Default,
                    )
                }
            }
        }
    }

    fun markAsTaken(medicationId: String) {
        val medication = _uiState.value.medications
            .firstOrNull { it.id == medicationId }
            ?: return
        if (_uiState.value.submittingMedicationId != null || medication.takenAt != null) return

        if (!medication.isWithinTakingWindow()) {
            _uiState.update {
                it.copy(
                    message = "복용 시간이 아니에요.",
                    messageType = ParentMedicationMessageType.OutsideTakingWindow,
                )
            }
            return
        }

        submitJob?.cancel()
        submitJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    submittingMedicationId = medicationId,
                    message = null,
                    messageType = ParentMedicationMessageType.Default,
                )
            }

            runCatching { repository.markNearestTaken() }
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            content = ParentMedicationContent.Completed,
                            medications = state.medications.map { item ->
                                if (item.id == medicationId) {
                                    item.copy(takenAt = Instant.now())
                                } else {
                                    item
                                }
                            },
                            submittingMedicationId = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    if (throwable is CancellationException) return@onFailure
                    _uiState.update {
                        it.copy(
                            submittingMedicationId = null,
                            message = "복약 확인을 전송하지 못했어요.",
                            messageType = ParentMedicationMessageType.Default,
                        )
                    }
                }
        }
    }

    fun consumeMessage() {
        _uiState.update {
            it.copy(
                message = null,
                messageType = ParentMedicationMessageType.Default,
            )
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

private fun MedicationSchedule.toParentMedication(): ParentMedication = ParentMedication(
    id = logId.toString(),
    name = name,
    scheduledTime = plannedTime.toLocalTimeOrNull() ?: LocalTime.MIDNIGHT,
    takenAt = if (taken) takenAt.toInstantOrNull() ?: Instant.EPOCH else null,
)

private fun ParentMedication.isWithinTakingWindow(now: LocalTime = LocalTime.now()): Boolean {
    val directDifference = Duration.between(scheduledTime, now).abs()
    val wrappedDifference = Duration.ofDays(1).minus(directDifference)
    return minOf(directDifference, wrappedDifference) <= Duration.ofHours(3)
}

private fun String.toLocalTimeOrNull(): LocalTime? = try {
    LocalTime.parse(trim())
} catch (_: DateTimeParseException) {
    null
}

private fun String?.toInstantOrNull(): Instant? = try {
    this?.let(Instant::parse)
} catch (_: DateTimeParseException) {
    null
}
