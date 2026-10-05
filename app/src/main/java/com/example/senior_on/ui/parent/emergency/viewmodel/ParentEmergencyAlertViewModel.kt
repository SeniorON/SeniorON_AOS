package com.example.senior_on.ui.parent.emergency.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.repository.location.LocationRepository
import com.example.senior_on.domain.repository.server.EventRepository
import com.example.senior_on.domain.repository.server.DeviceRepository
import kotlinx.coroutines.CancellationException
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
    private val deviceRepository: DeviceRepository,
    private val sharingGuard: com.example.senior_on.data.repository.impl.ParentSharingGuard?,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ParentEmergencyAlertUiState())
    val uiState = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var sendJob: Job? = null

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

        _uiState.update { it.copy(status = ParentEmergencyAlertStatus.Sending, errorMessage = null) }
        sendJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    status = ParentEmergencyAlertStatus.Sending,
                    errorMessage = null
                )
            }

            runCatching {
                val currentLocation = if (sharingGuard?.refresh()?.locationEnabled == true) {
                    locationRepository.getCurrentLocation()
                } else null
                repository.createSos(
                    latitude = currentLocation?.latitude,
                    longitude = currentLocation?.longitude,
                    // ViewModel이 재사용되어도 각 SOS 전송 시점의 배터리를 기록합니다.
                    battery = deviceRepository.getBatteryLevel(),
                )
            }
                .onSuccess {
                    _uiState.update {
                        it.copy(status = ParentEmergencyAlertStatus.Sent)
                    }
                }
                .onFailure { throwable ->
                    if (throwable is CancellationException) {
                        return@onFailure
                    }
                    _uiState.update {
                        it.copy(
                            status = ParentEmergencyAlertStatus.Failed,
                            errorMessage = "긴급알림 전송 결과를 확인하지 못했어요.\n인터넷 연결을 확인해 주세요.\n다시 시도하면 알림이 다시 전송될 수 있어요."
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

    fun onSharingCheckFailed() {
        countdownJob?.cancel()
        _uiState.update { it.copy(status = ParentEmergencyAlertStatus.Failed,
            errorMessage = "공유 상태를 확인하지 못했어요. 다시 시도해 주세요.") }
    }

    fun cancel() {
        countdownJob?.cancel()
        countdownJob = null
        sendJob?.cancel()
        sendJob = null
        _uiState.value = ParentEmergencyAlertUiState()
    }

    fun reset() {
        cancel()
    }

    override fun onCleared() {
        countdownJob?.cancel()
        sendJob?.cancel()
        super.onCleared()
    }

    companion object {
        fun factory(
            repository: EventRepository,
            locationRepository: LocationRepository,
            deviceRepository: DeviceRepository,
            sharingGuard: com.example.senior_on.data.repository.impl.ParentSharingGuard?,
        ) = viewModelFactory {
            initializer {
                ParentEmergencyAlertViewModel(
                    repository = repository,
                    locationRepository = locationRepository,
                    deviceRepository = deviceRepository,
                    sharingGuard = sharingGuard,
                )
            }
        }
    }
}
