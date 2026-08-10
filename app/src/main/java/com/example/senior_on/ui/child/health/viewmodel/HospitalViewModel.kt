package com.example.senior_on.ui.child.health.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HospitalUiState(
    val displayedMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val monthlyAppointments: List<HospitalAppointmentUiState> = emptyList(),
    val upcomingAppointments: List<HospitalAppointmentUiState> = emptyList(),
    val editorMode: HospitalEditorMode? = null,
    val editingAppointment: HospitalAppointmentUiState? = null,
    val editorDate: LocalDate = LocalDate.now(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

class HospitalViewModel(
    private val hospitalRepository: HospitalRepository,
    private val familyRepository: FamilyServerRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HospitalUiState())
    val uiState: StateFlow<HospitalUiState> = _uiState.asStateFlow()

    private var parentUserId: Long? = null
    private var monthLoadJob: Job? = null

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
            )
        }
        loadMonthly(month)
    }

    fun selectDate(date: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                displayedMonth = YearMonth.from(date),
            )
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
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                val parentId = resolveParentUserId()
                val current = _uiState.value.editingAppointment
                val domain = draft.toDomain(current?.id ?: 0L)
                if (_uiState.value.editorMode == HospitalEditorMode.Edit &&
                    current != null &&
                    current.id > 0L
                ) {
                    hospitalRepository.update(parentId, domain.copy(id = current.id))
                } else {
                    hospitalRepository.create(parentId, domain)
                }
                refreshAll(parentId, _uiState.value.displayedMonth)
            }.onSuccess { result ->
                applyRemoteData(result, closeEditor = true)
            }.onFailure { throwable ->
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
                val parentId = resolveParentUserId()
                hospitalRepository.delete(parentId, target.id)
                refreshAll(parentId, _uiState.value.displayedMonth)
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

    private fun loadHospitalData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val parentId = resolveParentUserId()
                refreshAll(parentId, _uiState.value.displayedMonth)
            }.onSuccess(::applyRemoteData)
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "병원 일정을 불러오지 못했습니다.",
                        )
                    }
                }
        }
    }

    private fun loadMonthly(month: YearMonth) {
        monthLoadJob?.cancel()
        monthLoadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val parentId = resolveParentUserId()
                hospitalRepository.getMonthly(parentId, month.year, month.monthValue)
                    .map { it.toUiState(highlighted = false) }
            }.onSuccess { monthly ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        monthlyAppointments = monthly,
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "병원 일정을 불러오지 못했습니다.",
                    )
                }
            }
        }
    }

    private suspend fun refreshAll(
        parentId: Long,
        month: YearMonth,
    ): RemoteHospitalData {
        val monthly = hospitalRepository.getMonthly(parentId, month.year, month.monthValue)
            .map { it.toUiState(highlighted = false) }
        val upcoming = hospitalRepository.getUpcoming(parentId)
            .flatMapIndexed { index, group ->
                group.appointments.map { appointment ->
                    appointment.toUiState(highlighted = index == 0)
                }
            }
        return RemoteHospitalData(monthly = monthly, upcoming = upcoming)
    }

    private fun applyRemoteData(
        result: RemoteHospitalData,
        closeEditor: Boolean = false,
    ) {
        _uiState.update { state ->
            state.copy(
                monthlyAppointments = result.monthly,
                upcomingAppointments = result.upcoming,
                editorMode = if (closeEditor) null else state.editorMode,
                editingAppointment = if (closeEditor) null else state.editingAppointment,
                isLoading = false,
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
            hospitalRepository: HospitalRepository,
            familyRepository: FamilyServerRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HospitalViewModel(hospitalRepository, familyRepository) as T
        }
    }
}

private data class RemoteHospitalData(
    val monthly: List<HospitalAppointmentUiState>,
    val upcoming: List<HospitalAppointmentUiState>,
)

private fun HospitalAppointment.toUiState(highlighted: Boolean): HospitalAppointmentUiState {
    val parsedDate = date.toLocalDateOrNull() ?: LocalDate.now()
    val daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), parsedDate).toInt().coerceAtLeast(0)
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
