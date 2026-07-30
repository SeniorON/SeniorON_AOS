package com.example.senior_on.ui.parent.schedule.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.model.parent.ParentSchedule
import com.example.senior_on.domain.repository.server.HomeServerRepository
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ParentScheduleUiState(
    val date: LocalDate = LocalDate.now(),
    val schedules: List<ParentSchedule> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val hasSchedules: Boolean
        get() = schedules.isNotEmpty()
}

class ParentScheduleViewModel(
    private val repository: HomeServerRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ParentScheduleUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadTodaySchedules()
    }

    fun loadTodaySchedules() {
        viewModelScope.launch {
            val today = LocalDate.now()
            _uiState.update {
                it.copy(date = today, isLoading = true, errorMessage = null)
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
                            isLoading = false
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            schedules = emptyList(),
                            isLoading = false,
                            errorMessage = "일정을 불러오지 못했어요."
                        )
                    }
                }
        }
    }

    companion object {
        fun factory(repository: HomeServerRepository) = viewModelFactory {
            initializer {
                ParentScheduleViewModel(repository)
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
