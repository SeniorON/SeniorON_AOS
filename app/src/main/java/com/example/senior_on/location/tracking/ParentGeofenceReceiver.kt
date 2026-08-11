package com.example.senior_on.location.tracking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.senior_on.SeniorOnApplication
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ParentGeofenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(
            LogTag,
            "Receiver invoked; action=${intent.action}, data=${intent.data}, " +
                "trackingState=${OutingTrackingStateStore(context).getState()}",
        )
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) {
            Log.e(LogTag, "GeofencingEvent.fromIntent returned null")
            return
        }
        if (geofencingEvent.hasError()) {
            Log.e(
                LogTag,
                "Geofence error code=${geofencingEvent.errorCode}, " +
                    "message=${GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)}",
            )
            return
        }

        val triggeringLocation = geofencingEvent.triggeringLocation
        Log.d(
            LogTag,
            "Received transition=${geofencingEvent.geofenceTransition}, " +
                "requestIds=${geofencingEvent.triggeringGeofences?.joinToString { it.requestId }}, " +
                "triggeringLocation=${triggeringLocation?.latitude},${triggeringLocation?.longitude}, " +
                "accuracy=${triggeringLocation?.accuracy}m, provider=${triggeringLocation?.provider}",
        )

        when (geofencingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_EXIT -> handleExit(context)
            Geofence.GEOFENCE_TRANSITION_ENTER -> handleEnter(context)
            else -> Log.w(
                LogTag,
                "Ignoring unsupported transition=${geofencingEvent.geofenceTransition}",
            )
        }
    }

    private fun handleExit(context: Context) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val application = context.applicationContext as SeniorOnApplication
                val container = application.appContainer
                val stateStore = OutingTrackingStateStore(context)
                val home = stateStore.getHomeLocation()
                if (home == null) {
                    Log.e(LogTag, "EXIT ignored because stored home location is missing")
                    return@launch
                }
                val current = container.locationRepository.getCurrentLocation()
                val distance = distanceMeters(
                    home.latitude,
                    home.longitude,
                    current.latitude,
                    current.longitude,
                )
                Log.d(
                    LogTag,
                    "Exit candidate verified: distance=${distance.toInt()}m, " +
                        "current=${current.latitude},${current.longitude}",
                )
                if (distance < OutingBoundaryMeters) {
                    Log.w(
                        LogTag,
                        "EXIT rejected by distance verification; " +
                            "distance=${distance.toInt()}m < ${OutingBoundaryMeters.toInt()}m",
                    )
                    return@launch
                }
                if (!stateStore.transition(
                        OutingTrackingState.Home,
                        OutingTrackingState.Outing,
                    )
                ) {
                    Log.w(
                        LogTag,
                        "EXIT ignored because state transition Home -> Outing failed; " +
                            "currentState=${stateStore.getState()}",
                    )
                    return@launch
                }
                Log.d(LogTag, "Tracking state changed Home -> Outing")

                runCatching {
                    container.deviceRepository.updateLocation(
                        current.latitude,
                        current.longitude,
                    )
                }
                runCatching {
                    container.eventRepository.createOutingReturn(
                        phase = OutingPhase,
                        latitude = current.latitude,
                        longitude = current.longitude,
                        battery = container.deviceRepository.getBatteryLevel(),
                    )
                }.onSuccess {
                    Log.d(LogTag, "OUTING event created; starting location service")
                    OutingLocationService.start(context)
                }.onFailure { throwable ->
                    stateStore.setState(OutingTrackingState.Home)
                    Log.e(LogTag, "Failed to create outing event", throwable)
                }
            } catch (throwable: Throwable) {
                Log.e(LogTag, "Failed to handle geofence exit", throwable)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleEnter(context: Context) {
        val state = OutingTrackingStateStore(context).getState()
        Log.d(
            LogTag,
            "Home ENTER received; trackingState=$state",
        )
        if (state == OutingTrackingState.Outing) {
            Log.d(LogTag, "Starting outing service to verify RETURN with hysteresis")
            OutingLocationService.start(context)
        } else {
            Log.d(LogTag, "ENTER requires no action because tracking state is Home")
        }
    }

    private companion object {
        const val LogTag = "SeniorOnGeofence"
        const val OutingPhase = "OUTING"
    }
}
