package com.example.senior_on.ui.parent.emergency.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.repository.location.LocationRepository
import com.example.senior_on.domain.repository.server.EventRepository
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
    private val repository: EventRepository,
    private val locationRepository: LocationRepository,
    private val deviceBattery: Int?,
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

            runCatching {
                val currentLocation = locationRepository.getCurrentLocation()
                repository.createSos(
                    latitude = currentLocation.latitude,
                    longitude = currentLocation.longitude,
                    battery = deviceBattery,
                )
            }
                .onSuccess {
                    _uiState.update {
                        it.copy(status = ParentEmergencyAlertStatus.Sent)
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            status = ParentEmergencyAlertStatus.Failed,
                            errorMessage = throwable.message
                                ?: "긴급알림을 보내지 못했어요. 다시 시도해 주세요."
                        )
                    }
                }
        }
    }

    fun onLocationPermissionDenied() {
        countdownJob?.cancel()
        countdownJob = null
        _uiState.update {
            it.copy(
                status = ParentEmergencyAlertStatus.Failed,
                errorMessage = "긴급알림에 현재 위치를 보내려면 위치 권한이 필요합니다.",
            )
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
        fun factory(
            repository: EventRepository,
            locationRepository: LocationRepository,
            deviceBattery: Int?,
        ) = viewModelFactory {
            initializer {
                ParentEmergencyAlertViewModel(
                    repository = repository,
                    locationRepository = locationRepository,
                    deviceBattery = deviceBattery,
                )
            }
        }
    }
}
