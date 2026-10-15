package com.example.senior_on.ui.child.health.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.senior_on.core.time.koreaToday
import com.example.senior_on.core.time.koreaYearMonth
import com.example.senior_on.domain.model.server.HospitalAppointment
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import com.example.senior_on.domain.repository.server.HospitalRepository
import com.example.senior_on.ui.child.health.HospitalAppointmentDraft
import com.example.senior_on.ui.child.health.HospitalAppointmentUiState
import com.example.senior_on.ui.child.health.HospitalEditorMode
import com.example.senior_on.ui.child.health.HospitalReminder
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HospitalUiState(
    val queryError: String? = null,
    val hasLoadedSelectedDate: Boolean = false,
    val displayedMonth: YearMonth = koreaYearMonth(),
    val selectedDate: LocalDate = koreaToday(),
    val monthlyAppointments: List<HospitalAppointmentUiState> = emptyList(),
    val selectedDateAppointments: List<HospitalAppointmentUiState> = emptyList(),
    val upcomingAppointments: List<HospitalAppointmentUiState> = emptyList(),
    val editorMode: HospitalEditorMode? = null,
    val editingAppointment: HospitalAppointmentUiState? = null,
    val editorDate: LocalDate = koreaToday(),
    val isLoading: Boolean = false,
    val hasLoadedContent: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

class HospitalViewModel(
    private val hospitalRepository: HospitalRepository,
    private val familyRepository: FamilyServerRepository,
    private val seniorId: Long? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HospitalUiState())
    val uiState: StateFlow<HospitalUiState> = _uiState.asStateFlow()

    private var fullLoadJob: Job? = null
    private var monthLoadJob: Job? = null
    private var dailyLoadJob: Job? = null
    private var hasEnteredScreen = false
    private enum class QueryScope { All, Month, Date }
    private var retryScope = QueryScope.All

    init {
        loadHospitalData()
    }

    fun selectMonth(month: YearMonth) {
        if (month == _uiState.value.displayedMonth) return
        val selectedDay = _uiState.value.selectedDate.dayOfMonth
            .coerceAtMost(month.lengthOfMonth())
        _uiState.update {
            it.copy(
                displayedMonth = month,
                selectedDate = month.atDay(selectedDay),
                hasLoadedSelectedDate = false,
                selectedDateAppointments = emptyList(),
            )
        }
        loadMonthly(month)
    }

    fun selectDate(date: LocalDate) {
        if (date == _uiState.value.selectedDate) return
        val monthChanged = YearMonth.from(date) != _uiState.value.displayedMonth
        _uiState.update {
            it.copy(
                selectedDate = date,
                hasLoadedSelectedDate = false,
                selectedDateAppointments = emptyList(),
                displayedMonth = YearMonth.from(date),
            )
        }
        if (monthChanged) {
            loadMonthly(YearMonth.from(date))
        } else {
            loadDaily(date)
        }
    }

    fun openAdd(date: LocalDate) {
        _uiState.update {
            it.copy(
                editorMode = HospitalEditorMode.Add,
                editingAppointment = null,
                editorDate = date,
            )
        }
    }

    fun openView(appointment: HospitalAppointmentUiState) {
        _uiState.update {
            it.copy(
                editorMode = HospitalEditorMode.View,
                editingAppointment = appointment,
                editorDate = appointment.date,
            )
        }
    }

    fun openEdit(appointment: HospitalAppointmentUiState) {
        _uiState.update {
            it.copy(
                editorMode = HospitalEditorMode.Edit,
                editingAppointment = appointment,
                editorDate = appointment.date,
            )
        }
    }

    fun closeEditor() {
        _uiState.update {
            it.copy(editorMode = null, editingAppointment = null)
        }
    }

    fun saveAppointment(draft: HospitalAppointmentDraft) {
        val state = _uiState.value
        if (state.isSaving || state.editorMode !in setOf(HospitalEditorMode.Add, HospitalEditorMode.Edit)) return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching {
                val targetSeniorId = requireSeniorId()
                val current = state.editingAppointment
                val domain = draft.toDomain(current?.id ?: 0L)
                if (state.editorMode == HospitalEditorMode.Edit) {
                    check(current != null && current.id > 0L) { "수정할 병원 일정이 없어요." }
                    hospitalRepository.update(targetSeniorId, domain.copy(id = current.id))
                } else {
                    hospitalRepository.create(targetSeniorId, domain)
                }
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, editorMode = null, editingAppointment = null) }
                loadHospitalData(failureMessage = "저장은 완료됐지만 목록을 불러오지 못했어요. 다시 시도해 주세요.")
            }.onFailure { throwable ->
                if (throwable is CancellationException) throw throwable
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "병원 일정을 저장하지 못했습니다.",
                    )
                }
            }
        }
    }

    fun deleteAppointment(appointment: HospitalAppointmentUiState? = null) {
        val target = appointment ?: _uiState.value.editingAppointment ?: return
        if (target.id <= 0L) return
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                val targetSeniorId = requireSeniorId()
                hospitalRepository.delete(targetSeniorId, target.id)
                refreshAll(targetSeniorId, _uiState.value.displayedMonth)
            }.onSuccess { result ->
                applyRemoteData(result, closeEditor = true)
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "병원 일정을 삭제하지 못했습니다.",
                    )
                }
            }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun loadLatestHospitalData() {
        if (!hasEnteredScreen) {
            hasEnteredScreen = true
            return
        }
        if (fullLoadJob?.isActive == true || monthLoadJob?.isActive == true || dailyLoadJob?.isActive == true) return
        loadHospitalData(isPullRefresh = false)
    }

    fun refreshHospitalData() {
        if (fullLoadJob?.isActive == true || monthLoadJob?.isActive == true || dailyLoadJob?.isActive == true) return
        loadHospitalData(isPullRefresh = true)
    }

    fun retry() {
        if (fullLoadJob?.isActive == true || monthLoadJob?.isActive == true || dailyLoadJob?.isActive == true) return
        when (if (_uiState.value.hasLoadedContent) retryScope else QueryScope.All) {
            QueryScope.All -> loadHospitalData()
            QueryScope.Month -> loadMonthly(_uiState.value.displayedMonth)
            QueryScope.Date -> loadDaily(_uiState.value.selectedDate)
        }
    }

    private fun loadHospitalData(isPullRefresh: Boolean = false, failureMessage: String? = null) {
        retryScope = QueryScope.All
        fullLoadJob?.cancel()
        monthLoadJob?.cancel()
        dailyLoadJob?.cancel()
        val requestedDate = _uiState.value.selectedDate
        val requestedMonth = _uiState.value.displayedMonth
        fullLoadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !isPullRefresh && !it.hasLoadedContent,
                    isRefreshing = isPullRefresh,
                    queryError = null,
                    errorMessage = null,
                )
            }
            runCatching {
                val targetSeniorId = requireSeniorId()
                refreshAll(targetSeniorId, requestedMonth, requestedDate)
            }.onSuccess { result ->
                if (_uiState.value.selectedDate == requestedDate && _uiState.value.displayedMonth == requestedMonth) applyRemoteData(result)
            }
                .onFailure { throwable ->
                    if (throwable is CancellationException) throw throwable
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            queryError = failureMessage ?: "병원 일정을 불러오지 못했어요.",
                        )
                    }
                }
        }
    }

    private fun loadMonthly(month: YearMonth) {
        retryScope = QueryScope.Month
        fullLoadJob?.cancel()
        dailyLoadJob?.cancel()
        monthLoadJob?.cancel()
        monthLoadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, queryError = null) }
            val requestedDate = _uiState.value.selectedDate
            runCatching {
                val targetSeniorId = requireSeniorId()
                val monthly = hospitalRepository.getMonthly(targetSeniorId, month.year, month.monthValue)
                    .map { it.toUiState(highlighted = false) }
                val daily = hospitalRepository.getDaily(targetSeniorId, requestedDate.toString())
                    .map { it.toUiState(highlighted = false) }
                monthly to daily
            }.onSuccess { (monthly, daily) ->
                _uiState.update {
                    if (it.displayedMonth == month && it.selectedDate == requestedDate) {
                        it.copy(
                            isLoading = false,
                            monthlyAppointments = monthly,
                            selectedDateAppointments = daily,
                            hasLoadedSelectedDate = true,
                        )
                    } else {
                        it
                    }
                }
            }.onFailure { throwable ->
                if (throwable is CancellationException) throw throwable
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        queryError = "병원 일정을 불러오지 못했어요.",
                    )
                }
            }
        }
    }

    private fun loadDaily(date: LocalDate) {
        retryScope = QueryScope.Date
        fullLoadJob?.cancel()
        monthLoadJob?.cancel()
        dailyLoadJob?.cancel()
        dailyLoadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, queryError = null) }
            runCatching {
                val targetSeniorId = requireSeniorId()
                hospitalRepository.getDaily(targetSeniorId, date.toString())
                    .map { it.toUiState(highlighted = false) }
            }.onSuccess { daily ->
                if (_uiState.value.selectedDate == date) {
                    _uiState.update { it.copy(selectedDateAppointments = daily, hasLoadedSelectedDate = true, isLoading = false) }
                }
            }.onFailure { throwable ->
                if (throwable is CancellationException) throw throwable
                _uiState.update {
                    it.copy(isLoading = false, queryError = "선택한 날짜의 병원 일정을 불러오지 못했어요.")
                }
            }
        }
    }

    private suspend fun refreshAll(
        targetSeniorId: Long,
        month: YearMonth,
        date: LocalDate = _uiState.value.selectedDate,
    ): RemoteHospitalData {
        val monthly = hospitalRepository.getMonthly(targetSeniorId, month.year, month.monthValue)
            .map { it.toUiState(highlighted = false) }
        val daily = hospitalRepository.getDaily(targetSeniorId, date.toString())
            .map { it.toUiState(highlighted = false) }
        val upcoming = hospitalRepository.getUpcoming(targetSeniorId)
            .flatMapIndexed { index, group ->
                group.appointments.map { appointment ->
                    appointment.toUiState(highlighted = index == 0)
                }
            }
        return RemoteHospitalData(monthly = monthly, daily = daily, upcoming = upcoming)
    }

    private fun applyRemoteData(
        result: RemoteHospitalData,
        closeEditor: Boolean = false,
    ) {
        _uiState.update { state ->
            state.copy(
                hasLoadedContent = true,
                hasLoadedSelectedDate = true,
                queryError = null,
                monthlyAppointments = result.monthly,
                selectedDateAppointments = result.daily,
                upcomingAppointments = result.upcoming,
                editorMode = if (closeEditor) null else state.editorMode,
                editingAppointment = if (closeEditor) null else state.editingAppointment,
                isLoading = false,
                isRefreshing = false,
                isSaving = if (closeEditor) false else state.isSaving,
                errorMessage = null,
            )
        }
    }

    private suspend fun requireSeniorId(): Long =
        requireNotNull(seniorId?.takeIf { it > 0 }) { "관리할 시니어를 먼저 선택해 주세요." }

    companion object {
        fun factory(
            hospitalRepository: HospitalRepository,
            familyRepository: FamilyServerRepository,
            seniorId: Long? = null,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HospitalViewModel(hospitalRepository, familyRepository, seniorId) as T
        }
    }
}

private data class RemoteHospitalData(
    val monthly: List<HospitalAppointmentUiState>,
    val daily: List<HospitalAppointmentUiState>,
    val upcoming: List<HospitalAppointmentUiState>,
)

private fun HospitalAppointment.toUiState(highlighted: Boolean): HospitalAppointmentUiState {
    val parsedDate = date.toLocalDateOrNull() ?: koreaToday()
    val daysLeft = ChronoUnit.DAYS.between(koreaToday(), parsedDate).toInt().coerceAtLeast(0)
    return HospitalAppointmentUiState(
        id = id,
        date = parsedDate,
        hospitalName = hospitalName,
        specialty = department,
        time = time.toLocalTimeOrNull() ?: LocalTime.MIDNIGHT,
        daysLeft = daysLeft,
        highlighted = highlighted,
        reminder = reminderType.toHospitalReminder(),
    )
}

private fun HospitalAppointmentDraft.toDomain(id: Long): HospitalAppointment =
    HospitalAppointment(
        id = id,
        hospitalName = hospitalName.trim(),
        department = specialty.trim(),
        date = date.toString(),
        time = time.format(TimeFormatter),
        reminderType = reminder.toApiValue(),
    )

private fun HospitalReminder.toApiValue(): String = when (this) {
    HospitalReminder.DayBefore -> "DAY_BEFORE"
    HospitalReminder.SameDay -> "SAME_DAY"
    HospitalReminder.None -> "NONE"
}

private fun String?.toHospitalReminder(): HospitalReminder = when (this?.trim()?.uppercase()) {
    "SAME_DAY" -> HospitalReminder.SameDay
    "NONE" -> HospitalReminder.None
    else -> HospitalReminder.DayBefore
}

private fun String?.toLocalDateOrNull(): LocalDate? {
    val value = this?.trim().orEmpty()
    if (value.isEmpty()) return null
    return runCatching { LocalDate.parse(value) }.getOrNull()
}

private fun String?.toLocalTimeOrNull(): LocalTime? {
    val value = this?.trim().orEmpty()
    if (value.isEmpty()) return null
    return TimeFormatters.firstNotNullOfOrNull { formatter ->
        try {
            LocalTime.parse(value, formatter)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}

private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val TimeFormatters = listOf(
    TimeFormatter,
    DateTimeFormatter.ofPattern("H:mm"),
    DateTimeFormatter.ISO_LOCAL_TIME,
)
private const val ParentRole = "PARENT"
