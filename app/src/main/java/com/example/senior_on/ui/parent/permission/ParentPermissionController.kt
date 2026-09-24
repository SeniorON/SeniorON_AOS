package com.example.senior_on.ui.parent.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.senior_on.location.tracking.hasBackgroundLocationPermission
import com.example.senior_on.location.tracking.hasForegroundLocationPermission
import com.example.senior_on.ui.parent.launcher.ParentHomeRoleManager

enum class ParentPermissionStatus { Required, Granted, Manual, NotApplicable }

/** Keeps platform requests separate from the shared guide UI. */
interface ParentPermissionController {
    fun status(step: ParentPermissionStep): ParentPermissionStatus
    fun request(step: ParentPermissionStep, permissions: (Array<String>) -> Unit, settings: (Intent) -> Unit)
}

class AndroidParentPermissionController(private val context: Context) : ParentPermissionController {
    private val preferences = context.getSharedPreferences("parent_permission_requests", Context.MODE_PRIVATE)
    private val activity get() = context.permissionActivity()

    override fun status(step: ParentPermissionStep): ParentPermissionStatus {
        if (step == ParentPermissionStep.SleepingApps) {
            return if (Build.MANUFACTURER.equals("samsung", ignoreCase = true))
                ParentPermissionStatus.Manual else ParentPermissionStatus.NotApplicable
        }
        val granted = when (step) {
            ParentPermissionStep.BatteryOptimization -> context.getSystemService(PowerManager::class.java)
                .isIgnoringBatteryOptimizations(context.packageName)
            ParentPermissionStep.Notification -> NotificationManagerCompat.from(context).areNotificationsEnabled() &&
                (Build.VERSION.SDK_INT < 33 || granted(Manifest.permission.POST_NOTIFICATIONS))
            ParentPermissionStep.ForegroundLocation -> context.hasForegroundLocationPermission()
            ParentPermissionStep.BackgroundLocation -> context.hasForegroundLocationPermission() && context.hasBackgroundLocationPermission()
            ParentPermissionStep.DefaultHome -> ParentHomeRoleManager.isDefaultHome(context)
            ParentPermissionStep.SleepingApps -> false
        }
        return if (granted) ParentPermissionStatus.Granted else ParentPermissionStatus.Required
    }

    override fun request(step: ParentPermissionStep, permissions: (Array<String>) -> Unit, settings: (Intent) -> Unit) {
        when (step) {
            // Open this app's details; the user selects Battery > Unrestricted.
            ParentPermissionStep.BatteryOptimization -> settings(appDetails())
            ParentPermissionStep.Notification -> {
                if (Build.VERSION.SDK_INT >= 33 && !granted(Manifest.permission.POST_NOTIFICATIONS) && canRequest(Manifest.permission.POST_NOTIFICATIONS)) {
                    requestRuntime(arrayOf(Manifest.permission.POST_NOTIFICATIONS), permissions)
                } else settings(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
            }
            ParentPermissionStep.ForegroundLocation -> {
                if (canRequest(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestRuntime(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), permissions)
                } else settings(appDetails())
            }
            ParentPermissionStep.BackgroundLocation -> {
                check(context.hasForegroundLocationPermission()) { "먼저 위치 권한을 허용해 주세요." }
                if (Build.VERSION.SDK_INT == 29 && canRequest(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    requestRuntime(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), permissions)
                } else settings(appDetails())
            }
            ParentPermissionStep.DefaultHome -> {
                val currentActivity = checkNotNull(activity) { "기본 홈 설정을 열 수 없습니다." }
                // Only an explicit button press reverses the earlier exit-to-home choice.
                ParentHomeRoleManager.setHomeExitRequested(context, false)
                settings(ParentHomeRoleManager.createHomeSelectionIntent(currentActivity) ?: Intent(Settings.ACTION_HOME_SETTINGS))
            }
            // Samsung's documented deep link. A return result does not prove exemption.
            ParentPermissionStep.SleepingApps -> settings(Intent("com.samsung.android.sm.ACTION_OPEN_CHECKABLE_LISTACTIVITY")
                .setPackage("com.samsung.android.lool").putExtra("activity_type", 2))
        }
    }

    private fun granted(permission: String) = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    private fun canRequest(permission: String) = !preferences.getBoolean(permission, false) ||
        activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, permission) } == true
    private fun requestRuntime(values: Array<String>, launch: (Array<String>) -> Unit) {
        launch(values)
        preferences.edit().apply { values.forEach { putBoolean(it, true) } }.apply()
    }
    private fun appDetails() = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
}

private tailrec fun Context.permissionActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.permissionActivity()
    else -> null
}

/** Manual checks must never be treated as system-granted permissions. */
fun ParentPermissionStep.nextRequired(status: (ParentPermissionStep) -> ParentPermissionStatus): ParentPermissionStep? =
    ParentPermissionStep.entries.drop(ordinal + 1).firstOrNull {
        status(it) == ParentPermissionStatus.Required || status(it) == ParentPermissionStatus.Manual
    }
