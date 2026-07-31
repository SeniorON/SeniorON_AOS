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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ParentDeviceStatusViewModel(
    private val repository: DeviceRepository,
) : ViewModel() {
    private var statusUpdateJob: Job? = null

    fun startStatusUpdates() {
        if (statusUpdateJob?.isActive == true) return

        statusUpdateJob = viewModelScope.launch {
            while (isActive) {
                runCatching { repository.updateStatus() }
                    .onFailure { throwable ->
                        Log.w(
                            LogTag,
                            "Failed to update parent device status.",
                            throwable,
                        )
                    }
                delay(StatusUpdateIntervalMillis)
            }
        }
    }

    fun stopStatusUpdates() {
        statusUpdateJob?.cancel()
        statusUpdateJob = null
    }

    companion object {
        private const val LogTag = "ParentDeviceStatus"
        private const val StatusUpdateIntervalMillis = 5 * 60 * 1_000L

        fun factory(repository: DeviceRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    ParentDeviceStatusViewModel(repository)
                }
            }
    }
}
