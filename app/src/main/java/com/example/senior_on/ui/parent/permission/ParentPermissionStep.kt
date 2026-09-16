package com.example.senior_on.ui.parent.permission

/** Stable identifiers, independent of display order and of actual Android permission state. */
enum class ParentPermissionStep {
    BatteryOptimization, Notification, ForegroundLocation, BackgroundLocation, DefaultHome, SleepingApps;

    fun previous(): ParentPermissionStep? = entries.getOrNull(ordinal - 1)
    fun next(): ParentPermissionStep? = entries.getOrNull(ordinal + 1)
}
