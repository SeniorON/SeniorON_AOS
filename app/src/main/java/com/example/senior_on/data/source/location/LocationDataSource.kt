package com.example.senior_on.data.source.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.senior_on.domain.model.location.GeoLocation
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface LocationDataSource {
    suspend fun getCurrentLocation(): GeoLocation
}

class AndroidLocationDataSource(
    private val context: Context,
    private val locationClient: FusedLocationProviderClient,
) : LocationDataSource {
    override suspend fun getCurrentLocation(): GeoLocation {
        if (isEmulator()) {
            Log.w(
                LOG_TAG,
                "Using hardcoded Seongdong-gu Office location on emulator",
            )
            return GeoLocation(
                latitude = EMULATOR_LATITUDE,
                longitude = EMULATOR_LONGITUDE,
            )
        }

        check(context.hasLocationPermission()) {
            "현재 위치를 전송하려면 위치 권한이 필요합니다."
        }

        val currentLocation = runCatching { requestCurrentLocation() }
            .onFailure { throwable ->
                Log.w(LOG_TAG, "Fresh location request failed", throwable)
            }
            .getOrNull()
        if (currentLocation != null) {
            Log.d(LOG_TAG, "Using fresh location, accuracy=${currentLocation.accuracy}m")
            return currentLocation.toDomain()
        }

        val lastLocation = runCatching { requestLastLocation() }
            .onFailure { throwable ->
                Log.w(LOG_TAG, "Last location request failed", throwable)
            }
            .getOrNull()
            ?.takeIf { it.ageMillis() <= MAX_FALLBACK_LOCATION_AGE_MILLIS }
        if (lastLocation != null) {
            Log.w(
                LOG_TAG,
                "Using recent cached location, age=${lastLocation.ageMillis()}ms, " +
                    "accuracy=${lastLocation.accuracy}m",
            )
            return lastLocation.toDomain()
        }

        Log.e(LOG_TAG, "No recent location is available")
        error("현재 위치를 확인할 수 없습니다.")
    }

    private suspend fun requestCurrentLocation(): Location? =
        suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()
            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdateAgeMillis(MAX_LOCATION_AGE_MILLIS)
                .setDurationMillis(LOCATION_TIMEOUT_MILLIS)
                .build()

            locationClient
                .getCurrentLocation(request, cancellationTokenSource.token)
                .addOnSuccessListener { location ->
                    if (!continuation.isActive) return@addOnSuccessListener
                    continuation.resume(location)
                }
                .addOnFailureListener { throwable ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(throwable)
                    }
                }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }

    private suspend fun requestLastLocation(): Location? =
        suspendCancellableCoroutine { continuation ->
            locationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (continuation.isActive) continuation.resume(location)
                }
                .addOnFailureListener { throwable ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(throwable)
                    }
                }
        }
}

private fun Location.toDomain(): GeoLocation = GeoLocation(
    latitude = latitude,
    longitude = longitude,
)

private fun Location.ageMillis(): Long =
    ((SystemClock.elapsedRealtimeNanos() - elapsedRealtimeNanos) / 1_000_000L)
        .coerceAtLeast(0L)

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

private fun isEmulator(): Boolean =
    Build.FINGERPRINT.startsWith("generic") ||
        Build.FINGERPRINT.contains("emulator", ignoreCase = true) ||
        Build.FINGERPRINT.contains("unknown", ignoreCase = true) ||
        Build.MODEL.contains("google_sdk", ignoreCase = true) ||
        Build.MODEL.contains("Emulator", ignoreCase = true) ||
        Build.MODEL.contains("Android SDK built for", ignoreCase = true) ||
        Build.MANUFACTURER.contains("Genymotion", ignoreCase = true) ||
        (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
        Build.PRODUCT.contains("sdk", ignoreCase = true)

private const val MAX_LOCATION_AGE_MILLIS = 30_000L
private const val MAX_FALLBACK_LOCATION_AGE_MILLIS = 5L * 60L * 1_000L
private const val LOCATION_TIMEOUT_MILLIS = 10_000L
private const val EMULATOR_LATITUDE = 37.5634270
private const val EMULATOR_LONGITUDE = 127.0369339
private const val LOG_TAG = "SeniorOnLocation"
