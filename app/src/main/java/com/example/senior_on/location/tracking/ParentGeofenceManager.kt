package com.example.senior_on.location.tracking

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
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
        if (com.example.senior_on.BuildConfig.DEBUG) Log.d(
            LogTag,
            "Registering home geofence; origin=$origin, " +
                "requestId=$HomeGeofenceRequestId, " +
                "radius=${OutingBoundaryMeters.toInt()}m, " +
                "trackingState=${stateStore.getState()}, " +
                "foregroundPermission=${applicationContext.hasForegroundLocationPermission()}, " +
                "backgroundPermission=${applicationContext.hasBackgroundLocationPermission()}, " +
                applicationContext.locationSettingsSummary(),
        )
        if (!applicationContext.hasRequiredGeofencePermissions()) {
            if (com.example.senior_on.BuildConfig.DEBUG) Log.w(LogTag, "Skipping geofence registration because location permission is missing")
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

        try {
            client.addGeofences(request, geofencePendingIntent()).awaitResult()
        } catch (securityException: SecurityException) {
            if (com.example.senior_on.BuildConfig.DEBUG) Log.w(
                LogTag,
                "Geofence registration stopped because location permission was revoked",
                securityException,
            )
            return
        } catch (throwable: Throwable) {
            if (com.example.senior_on.BuildConfig.DEBUG) Log.e(
                LogTag,
                "GeofencingClient rejected registration; origin=$origin, " +
                    "requestId=$HomeGeofenceRequestId",
                throwable,
            )
            else Log.e(LogTag, "GEOFENCE_REGISTRATION_FAILED")
            throw throwable
        }
        if (com.example.senior_on.BuildConfig.DEBUG) Log.d(
            LogTag,
            "GeofencingClient accepted registration; origin=$origin, " +
                "requestId=$HomeGeofenceRequestId",
        )
    }

    fun unregister() {
        if (com.example.senior_on.BuildConfig.DEBUG) Log.d(LogTag, "Removing home geofence; requestId=$HomeGeofenceRequestId")
        client.removeGeofences(geofencePendingIntent())
            .addOnSuccessListener {
                if (com.example.senior_on.BuildConfig.DEBUG) Log.d(LogTag, "Home geofence removal accepted")
            }
            .addOnFailureListener { throwable ->
                if (com.example.senior_on.BuildConfig.DEBUG) Log.e(LogTag, "Home geofence removal failed", throwable)
                else Log.e(LogTag, "GEOFENCE_REMOVAL_FAILED")
            }
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
