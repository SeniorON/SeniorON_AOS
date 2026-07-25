package com.example.senior_on.ui.parent.emergency.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.repository.parent.ParentEmergencyAlertRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ParentEmergencyAlertStatus {
    Idle,
    CountingDown,
    Sending,
    Sent,
    Failed
}

data class ParentEmergencyAlertUiState(
    val remainingSeconds: Int = COUNTDOWN_SECONDS,
    val status: ParentEmergencyAlertStatus = ParentEmergencyAlertStatus.Idle,
    val errorMessage: String? = null
) {
    companion object {
        const val COUNTDOWN_SECONDS = 5
    }
}

class ParentEmergencyAlertViewModel(
    private val repository: ParentEmergencyAlertRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ParentEmergencyAlertUiState())
    val uiState = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    fun startCountdown() {
        countdownJob?.cancel()
        _uiState.value = ParentEmergencyAlertUiState(
            status = ParentEmergencyAlertStatus.CountingDown
        )

        countdownJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0) {
                delay(1_000)
                _uiState.update {
                    it.copy(remainingSeconds = it.remainingSeconds - 1)
                }
            }
            sendEmergencyAlert()
        }
    }

    fun sendEmergencyAlert() {
        val status = _uiState.value.status
        if (
            status == ParentEmergencyAlertStatus.Sending ||
            status == ParentEmergencyAlertStatus.Sent
        ) {
            return
        }

        countdownJob?.cancel()
        countdownJob = null

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    status = ParentEmergencyAlertStatus.Sending,
                    errorMessage = null
                )
            }

            runCatching { repository.sendEmergencyAlert() }
                .onSuccess {
                    _uiState.update {
                        it.copy(status = ParentEmergencyAlertStatus.Sent)
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            status = ParentEmergencyAlertStatus.Failed,
                            errorMessage = "긴급알림을 보내지 못했어요. 다시 시도해 주세요."
                        )
                    }
                }
        }
    }

    fun cancel() {
        countdownJob?.cancel()
        countdownJob = null
        _uiState.value = ParentEmergencyAlertUiState()
    }

    fun reset() {
        cancel()
    }

    override fun onCleared() {
        countdownJob?.cancel()
        super.onCleared()
    }

    companion object {
        fun factory(repository: ParentEmergencyAlertRepository) = viewModelFactory {
            initializer {
                ParentEmergencyAlertViewModel(repository)
            }
        }
    }
}
