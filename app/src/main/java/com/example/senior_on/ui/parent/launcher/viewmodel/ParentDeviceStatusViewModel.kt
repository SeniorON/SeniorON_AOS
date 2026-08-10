package com.example.senior_on.ui.parent.launcher.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.device.ParentInactivityMonitor
import com.example.senior_on.domain.repository.server.DeviceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ParentDeviceStatusViewModel(
    private val repository: DeviceRepository,
    private val inactivityMonitor: ParentInactivityMonitor? = null,
    private val statusUpdateIntervalMillis: Long = StatusUpdateIntervalMillis,
) : ViewModel() {
    private var foregroundUpdateJob: Job? = null
    private val _isDeviceDisconnected = MutableStateFlow(false)
    val isDeviceDisconnected = _isDeviceDisconnected.asStateFlow()

    fun updateStatusOnce() {
        viewModelScope.launch {
            runCatching { repository.updateStatus() }
                .onSuccess { isConnected ->
                    _isDeviceDisconnected.value = !isConnected
                }
                .onFailure { throwable ->
                    Log.w(
                        LogTag,
                        "Failed to update parent device status.",
                        throwable,
                    )
                }
            checkInactivity()
        }
    }

    fun startForegroundUpdates() {
        if (foregroundUpdateJob?.isActive == true) return
        foregroundUpdateJob = viewModelScope.launch {
            while (isActive) {
                runCatching { repository.updateStatus() }
                    .onSuccess { isConnected ->
                        if (!isConnected) {
                            _isDeviceDisconnected.value = true
                            return@launch
                        }
                    }
                    .onFailure { throwable ->
                        Log.w(LogTag, "Foreground device status sync failed.", throwable)
                    }
                checkInactivity()
                delay(statusUpdateIntervalMillis)
            }
        }
    }

    fun stopForegroundUpdates() {
        foregroundUpdateJob?.cancel()
        foregroundUpdateJob = null
    }

    private suspend fun checkInactivity() {
        val monitor = inactivityMonitor ?: return
        runCatching { monitor.refreshSettingAndCheck() }
            .onFailure { throwable ->
                Log.w(LogTag, "Foreground inactivity check failed.", throwable)
            }
    }

    companion object {
        private const val LogTag = "ParentDeviceStatus"
        private const val StatusUpdateIntervalMillis = 5L * 60L * 1_000L

        fun factory(
            repository: DeviceRepository,
            inactivityMonitor: ParentInactivityMonitor,
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    ParentDeviceStatusViewModel(repository, inactivityMonitor)
                }
            }
    }
}
