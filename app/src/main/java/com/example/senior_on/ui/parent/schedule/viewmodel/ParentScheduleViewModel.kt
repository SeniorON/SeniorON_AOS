package com.example.senior_on.ui.parent.schedule.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.core.time.koreaToday
import com.example.senior_on.domain.model.parent.ParentSchedule
import com.example.senior_on.domain.repository.server.HomeServerRepository
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import com.example.senior_on.domain.repository.parent.ParentHomeUpdatesRepository
import com.example.senior_on.domain.repository.parent.ParentHomeUpdateEvent

data class ParentScheduleUiState(
    val date: LocalDate = koreaToday(),
    val schedules: List<ParentSchedule> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
) {
    val hasSchedules: Boolean
        get() = schedules.isNotEmpty()
}

class ParentScheduleViewModel(
    private val repository: HomeServerRepository,
    private val updatesRepository: ParentHomeUpdatesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ParentScheduleUiState())
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var pending = false
    private var pendingVisible = false
    private var hasLoaded = false

    suspend fun observeUpdates() {
        loadTodaySchedules(silent = true)
        updatesRepository.observeUpdates().collect { event ->
            if (event == ParentHomeUpdateEvent.Subscribed || event == ParentHomeUpdateEvent.ScheduleUpdated) {
                loadTodaySchedules(silent = true)
            }
        }
    }

    fun loadTodaySchedules(isRefresh: Boolean = false, silent: Boolean = false) {
        pending = true
        pendingVisible = pendingVisible || (isRefresh && !silent)
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            while (pending) {
                delay(150)
                pending = false
                val visible = pendingVisible
                pendingVisible = false
                val today = koreaToday()
                _uiState.update {
                    it.copy(
                        date = today,
                        isLoading = !hasLoaded,
                        isRefreshing = visible,
                        errorMessage = null,
                    )
                }

                runCatching {
                    repository.getTodayHospitalSchedules().map { schedule ->
                        ParentSchedule(
                            id = schedule.id.toString(),
                            date = schedule.date,
                            time = schedule.time,
                            title = schedule.hospitalName,
                            description = schedule.department.takeIf(String::isNotBlank),
                        )
                    }
                }
                    .onSuccess { schedules ->
                        _uiState.update {
                            it.copy(
                                schedules = schedules.sortedBy(ParentSchedule::time),
                                isLoading = false,
                                isRefreshing = false,
                            )
                        }
                    }
                    .onFailure { error ->
                        if (error is CancellationException) throw error
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = "일정을 불러오지 못했어요."
                            )
                        }
                    }
                hasLoaded = true
            }
        }
    }

    fun refresh() = loadTodaySchedules(isRefresh = true)

    companion object {
        fun factory(repository: HomeServerRepository, updatesRepository: ParentHomeUpdatesRepository) = viewModelFactory {
            initializer {
                ParentScheduleViewModel(repository, updatesRepository)
            }
        }
    }
}

internal fun LocalTime.toParentDisplayTime(): String {
    val period = if (hour < 12) "오전" else "오후"
    val displayHour = when (val value = hour % 12) {
        0 -> 12
        else -> value
    }
    return "$period $displayHour:${minute.toString().padStart(2, '0')}"
}
