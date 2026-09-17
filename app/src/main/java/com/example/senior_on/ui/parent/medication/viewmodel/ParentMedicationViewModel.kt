package com.example.senior_on.ui.parent.medication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.core.time.koreaNow
import com.example.senior_on.core.time.koreaToday
import com.example.senior_on.domain.model.parent.ParentMedication
import com.example.senior_on.domain.model.server.MedicationSchedule
import com.example.senior_on.domain.repository.server.MedicationRepository
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.senior_on.domain.repository.parent.ParentHomeUpdatesRepository
import com.example.senior_on.domain.repository.parent.ParentHomeUpdateEvent

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
    val isRefreshing: Boolean = false,
)

class ParentMedicationViewModel(
    private val repository: MedicationRepository,
    private val updatesRepository: ParentHomeUpdatesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ParentMedicationUiState())
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var submitJob: Job? = null
    private var pendingLoad = false
    private var pendingVisible = false
    private var submissionVersion = 0

    suspend fun observeUpdates() {
        refreshSilently()
        updatesRepository.observeUpdates().collect { event ->
            if (event == ParentHomeUpdateEvent.Subscribed || event == ParentHomeUpdateEvent.MedicationUpdated) {
                refreshSilently()
            }
        }
    }

    private fun refreshSilently() = loadMedication(
        highlightedMedicationLogId = _uiState.value.highlightedMedicationId?.toLongOrNull(),
        isRefresh = true,
        silent = true,
    )

    fun loadMedication(
        highlightedMedicationLogId: Long? = null,
        isRefresh: Boolean = false,
        silent: Boolean = false,
    ) {
        if (!isRefresh) {
            _uiState.update { it.copy(highlightedMedicationId = highlightedMedicationLogId?.toString()) }
        }
        pendingLoad = true
        pendingVisible = pendingVisible || (isRefresh && !silent)
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            while (pendingLoad) {
                delay(150)
                // A socket acknowledgement of our own write must not reset its pending UI.
                submitJob?.join()
                pendingLoad = false
                val visible = pendingVisible
                pendingVisible = false
                val version = submissionVersion
                _uiState.update {
                    it.copy(
                        isRefreshing = visible,
                        message = null,
                        messageType = ParentMedicationMessageType.Default,
                    )
                }

                val result = runCatching {
                    repository.getMySchedules(koreaToday().toString())
                }
                result.exceptionOrNull()?.let { if (it is CancellationException) throw it }
                // Discard a read started before the user submitted a dose; fetch again after the write.
                if (version != submissionVersion) {
                    pendingLoad = true
                    continue
                }
                result.onSuccess { schedules ->
                    val medications = schedules
                        .sortedBy { it.plannedTime.toLocalTimeOrNull() ?: LocalTime.MAX }
                        .map(MedicationSchedule::toParentMedication)
                    _uiState.update {
                        it.copy(
                            content = if (it.content == ParentMedicationContent.Completed && !visible) {
                                ParentMedicationContent.Completed
                            } else if (medications.isEmpty()) {
                                ParentMedicationContent.Empty
                            } else {
                                ParentMedicationContent.List
                            },
                            medications = medications,
                            isRefreshing = false,
                        )
                    }
                }.onFailure { throwable ->
                    if (throwable is CancellationException) throw throwable
                    _uiState.update {
                        it.copy(
                            content = if (it.content == ParentMedicationContent.Loading) ParentMedicationContent.Empty else it.content,
                            message = "복약 정보를 불러오지 못했어요.",
                            messageType = ParentMedicationMessageType.Default,
                            isRefreshing = false,
                        )
                    }
                }
            }
        }
    }

    fun refresh() = loadMedication(
        highlightedMedicationLogId = _uiState.value.highlightedMedicationId?.toLongOrNull(),
        isRefresh = true,
    )

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
        submissionVersion++
        _uiState.update { it.copy(submittingMedicationId = medicationId) }
        submitJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    submittingMedicationId = medicationId,
                    message = null,
                    messageType = ParentMedicationMessageType.Default,
                )
            }

            runCatching {
                val medicationLogId = medication.id.toLongOrNull()
                if (medicationLogId != null && medicationLogId > 0L) {
                    repository.markTaken(medicationLogId)
                } else {
                    repository.markNearestTaken()
                }
            }.onSuccess { checked ->
                val takenAt = checked.takenAt
                    ?.let { value -> runCatching { Instant.parse(value) }.getOrNull() }
                    ?: Instant.now()
                _uiState.update { state ->
                    state.copy(
                        content = ParentMedicationContent.Completed,
                        medications = state.medications.map { item ->
                            if (item.id == medicationId) {
                                item.copy(takenAt = takenAt)
                            } else {
                                item
                            }
                        },
                        submittingMedicationId = null,
                    )
                }
            }.onFailure { throwable ->
                if (throwable is CancellationException) throw throwable
                _uiState.update {
                    it.copy(
                        submittingMedicationId = null,
                        message = "복약 확인을 전송하지 못했어요.",
                        messageType = ParentMedicationMessageType.Default,
                    )
                }
            }
            refreshSilently()
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
        pendingLoad = false
        pendingVisible = false
        submissionVersion++
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
        fun factory(repository: MedicationRepository, updatesRepository: ParentHomeUpdatesRepository) = viewModelFactory {
            initializer {
                ParentMedicationViewModel(repository, updatesRepository)
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

private fun ParentMedication.isWithinTakingWindow(
    now: LocalTime = koreaNow().toLocalTime(),
): Boolean {
    val directDifference = Duration.between(scheduledTime, now).abs()
    val wrappedDifference = Duration.ofDays(1).minus(directDifference)
    return minOf(directDifference, wrappedDifference) <= Duration.ofHours(3)
}

private fun String.toLocalTimeOrNull(): LocalTime? {
    val value = trim()
    if (value.isEmpty()) return null
    return runCatching { LocalTime.parse(value) }.getOrNull()
        ?: runCatching {
            LocalTime.parse(value, DateTimeFormatter.ofPattern("H:mm"))
        }.getOrNull()
}

private fun String?.toInstantOrNull(): Instant? = try {
    this?.let(Instant::parse)
} catch (_: DateTimeParseException) {
    null
}
