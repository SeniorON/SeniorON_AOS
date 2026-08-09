package com.example.senior_on.ui.parent.launcher.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.repository.server.DeviceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ParentDeviceStatusViewModel(
    private val repository: DeviceRepository,
    private val statusUpdateIntervalMillis: Long = StatusUpdateIntervalMillis,
) : ViewModel() {
    private var statusUpdateJob: Job? = null
    private val _isDeviceDisconnected = MutableStateFlow(false)
    val isDeviceDisconnected = _isDeviceDisconnected.asStateFlow()

    fun startStatusUpdates() {
        if (statusUpdateJob?.isActive == true) return

        statusUpdateJob = viewModelScope.launch {
            while (isActive) {
                runCatching { repository.updateStatus() }
                    .onSuccess { isConnected ->
                        if (!isConnected) {
                            _isDeviceDisconnected.value = true
                            return@launch
                        }
                    }
                    .onFailure { throwable ->
                        Log.w(
                            LogTag,
                            "Failed to update parent device status.",
                            throwable,
                        )
                    }
                delay(statusUpdateIntervalMillis)
            }
        }
    }

    fun stopStatusUpdates() {
        statusUpdateJob?.cancel()
        statusUpdateJob = null
    }

    companion object {
        private const val LogTag = "ParentDeviceStatus"
        private const val StatusUpdateIntervalMillis = 30_000L

        fun factory(repository: DeviceRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    ParentDeviceStatusViewModel(repository)
                }
            }
    }
}
