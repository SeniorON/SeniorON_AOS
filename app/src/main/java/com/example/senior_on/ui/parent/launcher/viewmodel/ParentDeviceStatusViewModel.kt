package com.example.senior_on.ui.parent.launcher.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.device.ParentInactivityMonitor
import com.example.senior_on.domain.repository.server.DeviceRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class ParentDeviceStatusViewModel(
    private val repository: DeviceRepository,
    private val inactivityMonitor: ParentInactivityMonitor,
) : ViewModel() {
    private var foregroundUpdateJob: Job? = null

    fun updateStatusOnce() {
        viewModelScope.launch {
            runCatching { repository.updateStatus() }
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
            while (true) {
                runCatching { repository.updateStatus() }
                    .onFailure { throwable ->
                        Log.w(LogTag, "Foreground device status sync failed.", throwable)
                    }
                checkInactivity()
                delay(ForegroundUpdateIntervalMillis)
            }
        }
    }

    fun stopForegroundUpdates() {
        foregroundUpdateJob?.cancel()
        foregroundUpdateJob = null
    }

    private suspend fun checkInactivity() {
        runCatching { inactivityMonitor.refreshSettingAndCheck() }
            .onFailure { throwable ->
                Log.w(LogTag, "Foreground inactivity check failed.", throwable)
            }
    }

    companion object {
        private const val LogTag = "ParentDeviceStatus"
        private const val ForegroundUpdateIntervalMillis = 5L * 60L * 1_000L

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
