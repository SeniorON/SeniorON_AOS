package com.example.senior_on.ui.parent.launcher.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.repository.server.DeviceRepository
import com.example.senior_on.BuildConfig
import com.example.senior_on.location.tracking.OutingLocationService
import com.example.senior_on.location.tracking.OutingTrackingState
import com.example.senior_on.location.tracking.OutingTrackingStateStore
import com.example.senior_on.location.tracking.ParentGeofenceManager
import com.example.senior_on.location.tracking.StoredHomeLocation
import com.example.senior_on.location.tracking.distanceMeters
import com.example.senior_on.domain.repository.location.LocationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ParentLocationTrackingViewModel(
    context: Context,
    private val repository: DeviceRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {
    private val applicationContext = context.applicationContext
    private val stateStore = OutingTrackingStateStore(applicationContext)
    private val geofenceManager = ParentGeofenceManager(applicationContext)
    private var initializationJob: Job? = null
    private var debugLocationProbeJob: Job? = null

    fun initialize() {
        if (initializationJob?.isActive == true) {
            Log.d(LogTag, "Initialization is already running")
            return
        }
        Log.d(LogTag, "Starting parent location tracking initialization")
        initializationJob = viewModelScope.launch {
            runCatching {
                Log.d(LogTag, "Requesting senior home location from server")
                val home = repository.getHomeLocation().let {
                    StoredHomeLocation(it.latitude, it.longitude)
                }
                if (BuildConfig.DEBUG) {
                    Log.d(
                        LogTag,
                        "Received senior home location: " +
                            "latitude=${home.latitude}, longitude=${home.longitude}",
                    )
                }
                val saved = stateStore.saveHomeLocation(home.latitude, home.longitude)
                Log.d(LogTag, "Saved encrypted home location=$saved")
                geofenceManager.register(
                    home = home,
                    origin = "parent_launcher_initialization",
                )
                Log.d(LogTag, "Parent home geofence registration completed")
                // 에뮬레이터에서 Extended Controls의 좌표가 Fused Location에 반영되는지
                // 10초 간격으로 확인해야 할 때만 아래 호출의 주석을 해제한다.
                // startDebugLocationProbe(home)
                if (stateStore.getState() == OutingTrackingState.Outing) {
                    Log.d(LogTag, "Restoring outing foreground location service")
                    OutingLocationService.start(applicationContext)
                }
            }.onFailure { throwable ->
                Log.e(LogTag, "Failed to initialize parent location tracking", throwable)
            }
        }
    }

    private fun startDebugLocationProbe(home: StoredHomeLocation) {
        if (!BuildConfig.DEBUG || debugLocationProbeJob?.isActive == true) return

        Log.d(
            DebugProbeLogTag,
            "Starting emulator location probe; interval=${DebugProbeIntervalMillis}ms, " +
                "home=${home.latitude},${home.longitude}",
        )
        debugLocationProbeJob = viewModelScope.launch {
            var cycle = 0L
            while (isActive) {
                cycle += 1
                runCatching {
                    val current = locationRepository.getCurrentLocation()
                    val distance = distanceMeters(
                        home.latitude,
                        home.longitude,
                        current.latitude,
                        current.longitude,
                    )
                    Log.d(
                        DebugProbeLogTag,
                        "Probe cycle=$cycle; current=${current.latitude},${current.longitude}, " +
                            "distanceFromHome=${distance.toInt()}m, " +
                            "trackingState=${stateStore.getState()}",
                    )
                }.onFailure { throwable ->
                    Log.w(
                        DebugProbeLogTag,
                        "Probe cycle=$cycle failed to obtain current location",
                        throwable,
                    )
                }
                delay(DebugProbeIntervalMillis)
            }
        }
    }

    companion object {
        private const val LogTag = "SeniorOnTrackingInit"
        private const val DebugProbeLogTag = "SeniorOnLocationProbe"
        private const val DebugProbeIntervalMillis = 10_000L

        fun factory(
            context: Context,
            repository: DeviceRepository,
            locationRepository: LocationRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ParentLocationTrackingViewModel(
                    context = context,
                    repository = repository,
                    locationRepository = locationRepository,
                )
            }
        }
    }
}
