package com.example.senior_on.data.source.device

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings

interface LocalDeviceStatusDataSource {
    fun getDeviceName(): String
    fun getBatteryLevel(): Int
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

        val batteryIntent = applicationContext.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
        )
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

        return if (level >= 0 && scale > 0) {
            ((level * 100f) / scale).toInt().coerceIn(0, 100)
        } else {
            0
        }
    }
}
