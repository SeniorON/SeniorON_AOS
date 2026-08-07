package com.example.senior_on.ui.parent.launcher.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.repository.server.DeviceRepository
import kotlinx.coroutines.launch

class ParentDeviceStatusViewModel(
    private val repository: DeviceRepository,
) : ViewModel() {
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
        }
    }

    companion object {
        private const val LogTag = "ParentDeviceStatus"

        fun factory(repository: DeviceRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    ParentDeviceStatusViewModel(repository)
                }
            }
    }
}
