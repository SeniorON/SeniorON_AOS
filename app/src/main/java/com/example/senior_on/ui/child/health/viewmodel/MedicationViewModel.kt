package com.example.senior_on.ui.child.health.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.senior_on.core.time.koreaNow
import com.example.senior_on.core.time.koreaToday
import com.example.senior_on.domain.model.server.MedicationInfo
import com.example.senior_on.domain.model.server.MedicationSchedule
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import com.example.senior_on.domain.repository.server.MedicationRepository
import com.example.senior_on.ui.child.health.MedicationDoseStatus
import com.example.senior_on.ui.child.health.MedicationDraft
import com.example.senior_on.ui.child.health.MedicationEditorMode
import com.example.senior_on.ui.child.health.MedicationRepeatDuration
import com.example.senior_on.ui.child.health.MedicationRepeatFrequency
import com.example.senior_on.ui.child.health.MedicationRepeatSelection
import com.example.senior_on.ui.child.health.MedicationWeekdayLabels
import com.example.senior_on.ui.child.health.RegisteredMedicationUiState
import com.example.senior_on.ui.child.health.TodayMedicationUiState
import com.example.senior_on.ui.child.health.buildTodayMedicationsFromRegistered
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MedicationUiState(
    val selectedDate: LocalDate = koreaToday(),
    val registeredMedications: List<RegisteredMedicationUiState> = emptyList(),
    val todayMedications: List<TodayMedicationUiState> = emptyList(),
    val medicationMarkedDates: Set<LocalDate> = emptySet(),
    val editorMode: MedicationEditorMode? = null,
    val editingMedication: RegisteredMedicationUiState? = null,
    val addMedicationStartDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

class MedicationViewModel(
    private val medicationRepository: MedicationRepository,
    private val familyRepository: FamilyServerRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MedicationUiState())
    val uiState: StateFlow<MedicationUiState> = _uiState.asStateFlow()

    private var parentUserId: Long? = null
    private var fullLoadJob: Job? = null
    private var scheduleLoadJob: Job? = null
    private var hasEnteredScreen = false

    init {
        loadMedicationData()
    }

    fun selectDate(date: LocalDate) {
        val currentDate = _uiState.value.selectedDate
        if (date == currentDate) return
        fullLoadJob?.cancel()
        val monthChanged = YearMonth.from(date) != YearMonth.from(currentDate)
        _uiState.update { state ->
            state.copy(
                selectedDate = date,
                todayMedications = buildTodayMedicationsFromRegistered(
                    date = date,
                    registered = state.registeredMedications,
                ),
            )
        }
        loadSchedules(date, refreshMonthly = monthChanged)
    }

    fun openAddMedication() {
        openAddMedication(startDate = null)
    }

    fun openAddTodayMedication() {
        openAddMedication(startDate = koreaToday())
    }

    private fun openAddMedication(startDate: LocalDate?) {
        _uiState.update {
            it.copy(
                editorMode = MedicationEditorMode.Add,
                editingMedication = null,
                addMedicationStartDate = startDate,
            )
        }
    }

    fun openMedication(medication: RegisteredMedicationUiState) {
        _uiState.update {
            it.copy(
                editorMode = MedicationEditorMode.View,
                editingMedication = medication,
            )
        }
    }

    fun openEditMedication() {
        if (_uiState.value.editingMedication == null) return
        _uiState.update { it.copy(editorMode = MedicationEditorMode.Edit) }
    }

    fun closeMedicationEditor() {
        _uiState.update {
            it.copy(
                editorMode = null,
                editingMedication = null,
                addMedicationStartDate = null,
            )
        }
    }

    fun backFromMedicationEditor() {
        _uiState.update { state ->
            if (state.editorMode == MedicationEditorMode.Edit) {
                state.copy(editorMode = MedicationEditorMode.View)
            } else {
                state.copy(
                    editorMode = null,
                    editingMedication = null,
                    addMedicationStartDate = null,
                )
            }
        }
    }

    fun saveMedication(draft: MedicationDraft) {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                val parentId = resolveParentUserId()
                val current = _uiState.value.editingMedication
                val domain = draft.toDomain(current)
                if (_uiState.value.editorMode == MedicationEditorMode.Edit && current != null) {
                    medicationRepository.update(parentId, domain)
                } else {
                    medicationRepository.create(parentId, domain)
                }
                loadRemoteData(parentId, _uiState.value.selectedDate)
            }
                .onSuccess { result ->
                    applyRemoteData(result, closeEditor = true)
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = "복약 정보를 저장하지 못했습니다.",
                        )
                    }
                }
        }
    }

    fun deleteMedication() {
        val medication = _uiState.value.editingMedication ?: return
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                val parentId = resolveParentUserId()
                medicationRepository.delete(parentId, medication.id)
                loadRemoteData(parentId, _uiState.value.selectedDate)
            }
                .onSuccess { result ->
                    applyRemoteData(result, closeEditor = true)
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = "복약 정보를 삭제하지 못했습니다.",
                        )
                    }
                }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun retry() {
        loadMedicationData()
    }

    fun loadLatestMedicationData() {
        if (!hasEnteredScreen) {
            hasEnteredScreen = true
            return
        }
        if (fullLoadJob?.isActive == true || scheduleLoadJob?.isActive == true) return
        loadMedicationData(isPullRefresh = false)
    }

    fun refreshMedicationData() {
        if (fullLoadJob?.isActive == true || scheduleLoadJob?.isActive == true) return
        loadMedicationData(isPullRefresh = true)
    }

    fun onMedicationChecked(
        checkedParentUserId: Long,
        medicationLogId: Long,
    ) {
        if (medicationLogId <= 0L) return

        viewModelScope.launch {
            val resolvedParentUserId = runCatching { resolveParentUserId() }
                .getOrNull()
                ?: return@launch
            if (resolvedParentUserId != checkedParentUserId) return@launch

            val selectedDate = _uiState.value.selectedDate
            if (selectedDate != koreaToday()) return@launch

            _uiState.update { state ->
                state.copy(
                    todayMedications = state.todayMedications.map { medication ->
                        if (medication.medicationLogId == medicationLogId) {
                            medication.copy(status = MedicationDoseStatus.Taken)
                        } else {
                            medication
                        }
                    },
                )
            }
            loadSchedules(selectedDate, refreshMonthly = false)
        }
    }

    private fun loadMedicationData(isPullRefresh: Boolean = false) {
        scheduleLoadJob?.cancel()
        fullLoadJob?.cancel()
        val requestedDate = _uiState.value.selectedDate
        fullLoadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !isPullRefresh,
                    isRefreshing = isPullRefresh,
                    errorMessage = null,
                )
            }
            runCatching {
                val parentId = resolveParentUserId()
                loadRemoteData(parentId, requestedDate)
            }.onSuccess(::applyRemoteData)
                .onFailure { throwable ->
                    if (throwable is CancellationException) return@onFailure
                    _uiState.update {
                        if (it.selectedDate == requestedDate) {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = throwable.message,
                            )
                        } else {
                            it
                        }
                    }
                }
        }
    }

    private fun loadSchedules(date: LocalDate, refreshMonthly: Boolean) {
        fullLoadJob?.cancel()
        scheduleLoadJob?.cancel()
        scheduleLoadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val parentId = resolveParentUserId()
                val schedules = medicationRepository.getParentSchedules(parentId, date.toString())
                    .map { schedule ->
                        schedule.toUiState(
                            date = date,
                            registered = _uiState.value.registeredMedications,
                        )
                    }
                val markedDates = if (refreshMonthly) {
                    val month = YearMonth.from(date)
                    medicationRepository.getParentMonthlySchedules(
                        parentId = parentId,
                        year = month.year,
                        month = month.monthValue,
                    ).scheduledDates
                } else {
                    null
                }
                schedules to markedDates
            }.onSuccess { (schedules, markedDates) ->
                _uiState.update { state ->
                    if (state.selectedDate != date) {
                        state
                    } else {
                        state.copy(
                            isLoading = false,
                            todayMedications = buildTodayMedicationsFromRegistered(
                                date = date,
                                registered = state.registeredMedications,
                                remoteSchedules = schedules,
                            ),
                            medicationMarkedDates = markedDates
                                ?: state.medicationMarkedDates,
                        )
                    }
                }
            }.onFailure { throwable ->
                if (throwable is CancellationException) return@onFailure
                _uiState.update {
                    if (it.selectedDate == date) {
                        it.copy(isLoading = false, errorMessage = throwable.message)
                    } else {
                        it
                    }
                }
            }
        }
    }

    private suspend fun loadRemoteData(
        parentId: Long,
        date: LocalDate,
    ): RemoteMedicationData {
        val medications = medicationRepository.getMedications(parentId)
            .map(MedicationInfo::toUiState)
        val schedules = medicationRepository.getParentSchedules(parentId, date.toString())
            .map { it.toUiState(date, medications) }
        val month = YearMonth.from(date)
        val markedDates = medicationRepository.getParentMonthlySchedules(
            parentId = parentId,
            year = month.year,
            month = month.monthValue,
        ).scheduledDates
        return RemoteMedicationData(
            requestedDate = date,
            medications = medications,
            schedules = schedules,
            markedDates = markedDates,
        )
    }

    private fun applyRemoteData(
        result: RemoteMedicationData,
        closeEditor: Boolean = false,
    ) {
        _uiState.update { state ->
            if (state.selectedDate != result.requestedDate) {
                return@update state.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isSaving = false,
                )
            }
            val selectedDate = state.selectedDate
            state.copy(
                registeredMedications = result.medications,
                todayMedications = buildTodayMedicationsFromRegistered(
                    date = selectedDate,
                    registered = result.medications,
                    remoteSchedules = result.schedules,
                ),
                medicationMarkedDates = result.markedDates,
                editorMode = if (closeEditor) null else state.editorMode,
                editingMedication = if (closeEditor) null else state.editingMedication,
                isLoading = false,
                isRefreshing = false,
                isSaving = false,
                errorMessage = null,
            )
        }
    }

    private suspend fun resolveParentUserId(): Long {
        parentUserId?.let { return it }
        val id = familyRepository.getMembers()
            .firstOrNull { member -> member.role.equals(ParentRole, ignoreCase = true) }
            ?.id
            ?.takeIf { it > 0L }
            ?: error("연결된 시니어 사용자를 찾을 수 없습니다.")
        parentUserId = id
        return id
    }

    companion object {
        fun factory(
            medicationRepository: MedicationRepository,
            familyRepository: FamilyServerRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MedicationViewModel(medicationRepository, familyRepository) as T
        }
    }
}

private data class RemoteMedicationData(
    val requestedDate: LocalDate,
    val medications: List<RegisteredMedicationUiState>,
    val schedules: List<TodayMedicationUiState>,
    val markedDates: Set<LocalDate>,
)

private fun MedicationInfo.toUiState(): RegisteredMedicationUiState {
    val frequency = when (repeatType.trim().uppercase()) {
        "WEEKLY" -> MedicationRepeatFrequency.Weekly
        else -> MedicationRepeatFrequency.Daily
    }
    val weekdays = when (frequency) {
        MedicationRepeatFrequency.Daily -> MedicationWeekdayLabels.indices.toSet()
        MedicationRepeatFrequency.Weekly -> days.toWeekdayIndexSet()
    }
    val parsedStartDate = startDate.toLocalDateOrNull()
    val parsedEndDate = endDate.toLocalDateOrNull()
    val repeat = MedicationRepeatSelection(
        frequency = frequency,
        cycleValue = repeatInterval.coerceAtLeast(1),
        weekdays = weekdays,
        duration = when (repeatEndType.trim().uppercase()) {
            "DURATION" -> MedicationRepeatDuration.Period
            "END_DATE" -> MedicationRepeatDuration.Date
            else -> MedicationRepeatDuration.Continuous
        },
        periodValue = durationWeeks?.coerceAtLeast(1) ?: 3,
        endDate = parsedEndDate,
    )
    return RegisteredMedicationUiState(
        id = groupId.ifBlank { id?.toString().orEmpty() },
        category = name,
        name = ingredient.orEmpty(),
        times = times.mapNotNull(String::toLocalTimeOrNull).distinct().sorted(),
        weekdays = weekdays,
        startDate = parsedStartDate,
        repeat = repeat,
    )
}

private fun List<String>.toWeekdayIndexSet(): Set<Int> {
    if (isEmpty()) return emptySet()
    if (any { value ->
            value.trim().equals("매일", ignoreCase = true) ||
                value.trim().equals("EVERYDAY", ignoreCase = true) ||
                value.trim().equals("EVERY_DAY", ignoreCase = true) ||
                value.trim().equals("DAILY", ignoreCase = true)
        }
    ) {
        return (0..6).toSet()
    }
    return mapNotNull(String::toWeekdayIndexOrNull).toSet()
}

private fun MedicationSchedule.toUiState(
    date: LocalDate,
    registered: List<RegisteredMedicationUiState>,
): TodayMedicationUiState {
    val time = plannedTime.toLocalTimeOrNull() ?: LocalTime.MIDNIGHT
    val registeredMedication = registered.firstOrNull { it.category == name }
    val uiStatus = when (status?.uppercase()) {
        "TAKEN" -> MedicationDoseStatus.Taken
        "MISSED" -> MedicationDoseStatus.Missed
        "SCHEDULED" -> MedicationDoseStatus.Scheduled
        else -> when {
        taken -> MedicationDoseStatus.Taken
        LocalDateTime.of(date, time).isBefore(koreaNow()) -> MedicationDoseStatus.Missed
        else -> MedicationDoseStatus.Scheduled
        }
    }
    return TodayMedicationUiState(
        date = plannedDate?.let { value ->
            runCatching { LocalDate.parse(value) }.getOrNull()
        } ?: date,
        category = name,
        name = ingredient ?: registeredMedication?.name.orEmpty(),
        time = time,
        status = uiStatus,
        medicationLogId = logId,
        takenTime = takenTime?.toLocalTimeOrNull(),
    )
}

private fun MedicationDraft.toDomain(
    current: RegisteredMedicationUiState?,
): MedicationInfo {
    val resolvedStartDate = startDate ?: koreaToday()
    val repeatType = when (repeat.frequency) {
        MedicationRepeatFrequency.Daily -> "DAILY"
        MedicationRepeatFrequency.Weekly -> "WEEKLY"
    }
    val repeatEndType = when (repeat.duration) {
        MedicationRepeatDuration.Continuous -> "ONGOING"
        MedicationRepeatDuration.Period -> "DURATION"
        MedicationRepeatDuration.Date -> "END_DATE"
    }
    val durationWeeks = when (repeat.duration) {
        MedicationRepeatDuration.Period -> repeat.periodValue.coerceAtLeast(1)
        else -> null
    }
    val resolvedEndDate = when (repeat.duration) {
        MedicationRepeatDuration.Date -> repeat.endDate
        MedicationRepeatDuration.Period ->
            resolvedStartDate
                .plusWeeks(repeat.periodValue.coerceAtLeast(1).toLong())
        MedicationRepeatDuration.Continuous -> null
    }
    return MedicationInfo(
        id = null,
        groupId = current?.id.orEmpty(),
        name = category.trim(),
        ingredient = name.trim().takeIf(String::isNotEmpty),
        times = times.sorted().map { it.format(TimeFormatter) },
        days = weekdays.sorted().map(::weekdayApiValue),
        startDate = resolvedStartDate.toString(),
        repeatType = repeatType,
        repeatInterval = repeat.cycleValue.coerceAtLeast(1),
        repeatEndType = repeatEndType,
        durationWeeks = durationWeeks,
        endDate = resolvedEndDate?.toString(),
    )
}

private fun String?.toLocalDateOrNull(): LocalDate? {
    val value = this?.trim().orEmpty()
    if (value.isEmpty()) return null
    return runCatching { LocalDate.parse(value) }.getOrNull()
}

private fun String.toLocalTimeOrNull(): LocalTime? {
    val value = trim()
    return value.toKoreanLocalTimeOrNull()
        ?: TimeFormatters.firstNotNullOfOrNull { formatter ->
        try {
            LocalTime.parse(value, formatter)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}

private fun String.toKoreanLocalTimeOrNull(): LocalTime? {
    val match = KoreanTimePattern.matchEntire(this) ?: return null
    val period = match.groupValues[1]
    val displayHour = match.groupValues[2].toIntOrNull() ?: return null
    val minute = match.groupValues[3].takeIf(String::isNotEmpty)?.toIntOrNull() ?: 0
    if (displayHour !in 1..12 || minute !in 0..59) return null

    val hour = when (period) {
        "오전" -> if (displayHour == 12) 0 else displayHour
        "오후" -> if (displayHour == 12) 12 else displayHour + 12
        else -> return null
    }
    return LocalTime.of(hour, minute)
}

private fun String.toWeekdayIndexOrNull(): Int? = when (trim().uppercase()) {
    "SUNDAY", "SUN", "일", "일요일", "0" -> 0
    "MONDAY", "MON", "월", "월요일", "1" -> 1
    "TUESDAY", "TUE", "화", "화요일", "2" -> 2
    "WEDNESDAY", "WED", "수", "수요일", "3" -> 3
    "THURSDAY", "THU", "목", "목요일", "4" -> 4
    "FRIDAY", "FRI", "금", "금요일", "5" -> 5
    "SATURDAY", "SAT", "토", "토요일", "6" -> 6
    else -> null
}

private fun weekdayApiValue(index: Int): String = when (index) {
    0 -> "일"
    1 -> "월"
    2 -> "화"
    3 -> "수"
    4 -> "목"
    5 -> "금"
    6 -> "토"
    else -> error("지원하지 않는 요일입니다: $index")
}

private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val TimeFormatters = listOf(
    DateTimeFormatter.ISO_LOCAL_TIME,
    TimeFormatter,
    DateTimeFormatter.ofPattern("H:mm"),
)
private val KoreanTimePattern = Regex("(오전|오후)\\s+(\\d{1,2})(?:시|:(\\d{2}))")
private const val ParentRole = "PARENT"
