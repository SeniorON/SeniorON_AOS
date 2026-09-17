package com.example.senior_on.data.source.device

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

data class LocalDeviceStatusSnapshot(
    val deviceName: String,
    val batteryLevel: Int,
    val charging: Boolean,
    val deviceStatusSharingEnabled: Boolean,
    val networkConnected: Boolean,
    val defaultHomeEnabled: Boolean,
    val locationPermissionGranted: Boolean,
    val gpsEnabled: Boolean,
    val notificationPermissionGranted: Boolean,
    val appExecutionMaintained: Boolean,
)

interface LocalDeviceStatusDataSource {
    fun getDeviceName(): String
    fun getBatteryLevel(): Int
    fun getStatusSnapshot(): LocalDeviceStatusSnapshot
}

class AndroidDeviceStatusDataSource(
    context: Context,
) : LocalDeviceStatusDataSource {
    private val applicationContext = context.applicationContext

    override fun getDeviceName(): String {
        val configuredName = Settings.Global.getString(
            applicationContext.contentResolver,
            Settings.Global.DEVICE_NAME,
        )?.trim()

        return configuredName
            ?.takeIf(String::isNotEmpty)
            ?: listOf(Build.MANUFACTURER, Build.MODEL)
                .map(String::trim)
                .filter(String::isNotEmpty)
                .distinct()
                .joinToString(" ")
                .ifBlank { "Android Device" }
    }

    override fun getBatteryLevel(): Int {
        val batteryManager = applicationContext.getSystemService(
            BatteryManager::class.java
        )
        val propertyLevel = batteryManager
            ?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        if (propertyLevel != null && propertyLevel in 0..100) {
            return propertyLevel
        }

        val batteryIntent = currentBatteryIntent()
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

        return if (level >= 0 && scale > 0) {
            ((level * 100f) / scale).toInt().coerceIn(0, 100)
        } else {
            0
        }
    }

    override fun getStatusSnapshot(): LocalDeviceStatusSnapshot = LocalDeviceStatusSnapshot(
        deviceName = getDeviceName(),
        batteryLevel = getBatteryLevel(),
        charging = isCharging(),
        // There is no user-facing opt-out setting yet. Sending a heartbeat means sharing is active.
        deviceStatusSharingEnabled = true,
        networkConnected = isNetworkConnected(),
        defaultHomeEnabled = isDefaultHome(),
        locationPermissionGranted = hasLocationPermission(),
        gpsEnabled = isGpsEnabled(),
        notificationPermissionGranted = hasNotificationPermission(),
        appExecutionMaintained = isIgnoringBatteryOptimizations(),
    )

    private fun currentBatteryIntent(): Intent? = applicationContext.registerReceiver(
        null,
        IntentFilter(Intent.ACTION_BATTERY_CHANGED),
    )

    private fun isCharging(): Boolean {
        val status = currentBatteryIntent()
            ?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun isNetworkConnected(): Boolean {
        val manager = applicationContext.getSystemService(ConnectivityManager::class.java)
            ?: return false
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun isDefaultHome(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = applicationContext.getSystemService(RoleManager::class.java)
            if (roleManager?.isRoleAvailable(RoleManager.ROLE_HOME) == true) {
                return roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            }
        }

        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolvedHome = applicationContext.packageManager.resolveActivity(
            homeIntent,
            PackageManager.MATCH_DEFAULT_ONLY,
        )
        return resolvedHome?.activityInfo?.packageName == applicationContext.packageName
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

    private fun isGpsEnabled(): Boolean {
        val manager = applicationContext.getSystemService(LocationManager::class.java)
            ?: return false
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                manager.isLocationEnabled
            } else {
                manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            }
        }.getOrDefault(false)
    }

    private fun hasNotificationPermission(): Boolean {
        val runtimePermissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        return runtimePermissionGranted &&
            NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = applicationContext.getSystemService(PowerManager::class.java)
            ?: return false
        return powerManager.isIgnoringBatteryOptimizations(applicationContext.packageName)
    }
}
