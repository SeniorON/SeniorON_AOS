package com.example.senior_on.ui.parent.launcher.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.repository.server.DeviceRepository
import com.example.senior_on.location.tracking.OutingLocationService
import com.example.senior_on.location.tracking.OutingTrackingState
import com.example.senior_on.location.tracking.OutingTrackingStateStore
import com.example.senior_on.location.tracking.ParentGeofenceManager
import com.example.senior_on.location.tracking.StoredHomeLocation
import com.example.senior_on.domain.repository.location.LocationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ParentLocationTrackingViewModel(
    context: Context,
    private val repository: DeviceRepository,
    private val locationRepository: LocationRepository,
    private val sharingGuard: com.example.senior_on.data.repository.impl.ParentSharingGuard? = null,
) : ViewModel() {
    private val applicationContext = context.applicationContext
    private val stateStore = OutingTrackingStateStore(applicationContext)
    private val geofenceManager = ParentGeofenceManager(applicationContext)
    private var initializationJob: Job? = null

    fun initialize() {
        if (initializationJob?.isActive == true) {
            if (com.example.senior_on.BuildConfig.DEBUG) Log.d(LogTag, "Initialization is already running")
            return
        }
        if (com.example.senior_on.BuildConfig.DEBUG) Log.d(LogTag, "Starting parent location tracking initialization")
        initializationJob = viewModelScope.launch {
            runCatching {
                if (sharingGuard?.refresh()?.locationEnabled == false) {
                    com.example.senior_on.location.tracking.ParentOutingTrackingController.reset(applicationContext)
                    return@runCatching
                }
                if (com.example.senior_on.BuildConfig.DEBUG) Log.d(LogTag, "Requesting senior home location from server")
                val home = repository.getHomeLocation().let {
                    StoredHomeLocation(it.latitude, it.longitude)
                }
                val saved = stateStore.saveHomeLocation(home.latitude, home.longitude)
                check(saved) { "외출·귀가 상태를 저장하지 못했어요." }
                if (com.example.senior_on.BuildConfig.DEBUG) Log.d(LogTag, "Saved encrypted home location=$saved")
                geofenceManager.register(
                    home = home,
                    origin = "parent_launcher_initialization",
                )
                if (com.example.senior_on.BuildConfig.DEBUG) Log.d(LogTag, "Parent home geofence registration completed")
                com.example.senior_on.location.tracking.OutingRecoveryScheduler.enqueue(applicationContext)
                if (stateStore.getState() == OutingTrackingState.Outing) {
                    if (com.example.senior_on.BuildConfig.DEBUG) Log.d(LogTag, "Restoring outing foreground location service")
                    OutingLocationService.start(applicationContext)
                }
            }.onFailure { throwable ->
                if (com.example.senior_on.BuildConfig.DEBUG) Log.e(LogTag, "Failed to initialize parent location tracking", throwable)
                else Log.e(LogTag, "OUTING_INITIALIZATION_FAILED")
            }
        }
    }

    companion object {
        private const val LogTag = "SeniorOnTrackingInit"

        fun factory(
            context: Context,
            repository: DeviceRepository,
            locationRepository: LocationRepository,
            sharingGuard: com.example.senior_on.data.repository.impl.ParentSharingGuard? = null,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ParentLocationTrackingViewModel(
                    context = context,
                    repository = repository,
                    locationRepository = locationRepository,
                    sharingGuard = sharingGuard,
                )
            }
        }
    }
}
