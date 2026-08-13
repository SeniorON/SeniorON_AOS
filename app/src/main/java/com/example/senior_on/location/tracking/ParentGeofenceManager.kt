package com.example.senior_on.location.tracking

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

internal class ParentGeofenceManager(context: Context) {
    private val applicationContext = context.applicationContext
    private val client = LocationServices.getGeofencingClient(applicationContext)

    @SuppressLint("MissingPermission")
    suspend fun register(
        home: StoredHomeLocation,
        origin: String,
    ) {
        val stateStore = OutingTrackingStateStore(applicationContext)
        Log.d(
            LogTag,
            "Registering home geofence; origin=$origin, " +
                "requestId=$HomeGeofenceRequestId, " +
                "center=${home.latitude},${home.longitude}, " +
                "radius=${OutingBoundaryMeters.toInt()}m, " +
                "trackingState=${stateStore.getState()}, " +
                "foregroundPermission=${applicationContext.hasForegroundLocationPermission()}, " +
                "backgroundPermission=${applicationContext.hasBackgroundLocationPermission()}, " +
                applicationContext.locationSettingsSummary(),
        )
        if (!applicationContext.hasRequiredGeofencePermissions()) {
            Log.w(LogTag, "Skipping geofence registration because location permission is missing")
            return
        }

        val geofence = Geofence.Builder()
            .setRequestId(HomeGeofenceRequestId)
            .setCircularRegion(
                home.latitude,
                home.longitude,
                OutingBoundaryMeters,
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or
                    Geofence.GEOFENCE_TRANSITION_EXIT
            )
            .build()
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(
                GeofencingRequest.INITIAL_TRIGGER_ENTER or
                    GeofencingRequest.INITIAL_TRIGGER_EXIT
            )
            .addGeofence(geofence)
            .build()

        logFusedLocationSnapshot(stage = "before-registration", home = home)
        try {
            client.addGeofences(request, geofencePendingIntent()).awaitResult()
        } catch (securityException: SecurityException) {
            Log.w(
                LogTag,
                "Geofence registration stopped because location permission was revoked",
                securityException,
            )
            return
        } catch (throwable: Throwable) {
            Log.e(
                LogTag,
                "GeofencingClient rejected registration; origin=$origin, " +
                    "requestId=$HomeGeofenceRequestId",
                throwable,
            )
            throw throwable
        }
        Log.d(
            LogTag,
            "GeofencingClient accepted registration; origin=$origin, " +
                "requestId=$HomeGeofenceRequestId",
        )
        logFusedLocationSnapshot(stage = "after-registration", home = home)
    }

    fun unregister() {
        Log.d(LogTag, "Removing home geofence; requestId=$HomeGeofenceRequestId")
        client.removeGeofences(geofencePendingIntent())
            .addOnSuccessListener {
                Log.d(LogTag, "Home geofence removal accepted")
            }
            .addOnFailureListener { throwable ->
                Log.e(LogTag, "Home geofence removal failed", throwable)
            }
    }

    @SuppressLint("MissingPermission")
    private suspend fun logFusedLocationSnapshot(
        stage: String,
        home: StoredHomeLocation,
    ) {
        if (!applicationContext.hasForegroundLocationPermission()) {
            Log.d(LogTag, "$stage: location snapshot skipped because permission is missing")
            return
        }

        val availability = runCatching {
            LocationServices.getFusedLocationProviderClient(applicationContext)
                .locationAvailability
                .awaitResult()
        }.getOrNull()
        val location = runCatching {
            LocationServices.getFusedLocationProviderClient(applicationContext)
                .lastLocation
                .awaitResult()
        }.onFailure { throwable ->
            Log.w(LogTag, "$stage: failed to read Fused lastLocation", throwable)
        }.getOrNull()

        if (location == null) {
            Log.w(
                LogTag,
                "$stage: Fused lastLocation=null, " +
                    "locationAvailable=${availability?.isLocationAvailable}",
            )
            return
        }

        val ageMillis = (
            (SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos) /
                1_000_000L
            ).coerceAtLeast(0L)
        val distance = distanceMeters(
            home.latitude,
            home.longitude,
            location.latitude,
            location.longitude,
        )
        Log.d(
            LogTag,
            "$stage: Fused lastLocation=${location.latitude},${location.longitude}, " +
                "distanceFromHome=${distance.toInt()}m, accuracy=${location.accuracy}m, " +
                "age=${ageMillis}ms, provider=${location.provider}, " +
                "mock=${location.isMockLocation()}, " +
                "locationAvailable=${availability?.isLocationAvailable}",
        )
    }

    private fun geofencePendingIntent(): PendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        GeofencePendingIntentRequestCode,
        Intent(applicationContext, ParentGeofenceReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
    )

    private companion object {
        const val LogTag = "SeniorOnGeofence"
        const val HomeGeofenceRequestId = "senior_home_geofence"
        const val GeofencePendingIntentRequestCode = 7101
    }
}

private fun Context.locationSettingsSummary(): String {
    val manager = getSystemService(LocationManager::class.java)
    val locationEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        manager.isLocationEnabled
    } else {
        manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    return "locationEnabled=$locationEnabled, " +
        "gpsEnabled=${manager.isProviderEnabled(LocationManager.GPS_PROVIDER)}, " +
        "networkEnabled=${manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)}"
}

@Suppress("DEPRECATION")
private fun android.location.Location.isMockLocation(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) isMock else isFromMockProvider

internal fun Context.hasForegroundLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

internal fun Context.hasBackgroundLocationPermission(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

internal fun Context.hasRequiredGeofencePermissions(): Boolean =
    hasForegroundLocationPermission() && hasBackgroundLocationPermission()
