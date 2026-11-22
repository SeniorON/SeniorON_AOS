package com.example.senior_on.ui.child

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.senior_on.R
import com.example.senior_on.ui.child.health.SeniorOnConfirmDialog
import com.example.senior_on.ui.theme.SeniorOnColors

private enum class PermissionDialog { Explain, SettingsGuide }

internal fun Context.childNotificationsEnabled(): Boolean =
    (Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
        NotificationManagerCompat.from(this).areNotificationsEnabled()

@Composable
internal fun ChildNotificationPermissionPrompt(inNotificationTab: Boolean) {
    if (LocalInspectionMode.current) return
    val activity = LocalActivity.current ?: return
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val prefs = remember(activity) { activity.getSharedPreferences("child_notification_prompt", Context.MODE_PRIVATE) }
    val permission = Manifest.permission.POST_NOTIFICATIONS
    val runtime = Build.VERSION.SDK_INT >= 33
    fun granted() = !runtime || ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    fun enabled() = activity.childNotificationsEnabled()
    fun rationale() = runtime && ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    var allowed by remember { mutableStateOf(enabled()) }
    var requesting by rememberSaveable { mutableStateOf(false) }
    var retry by rememberSaveable { mutableStateOf(false) }
    var dialog by rememberSaveable { mutableStateOf<PermissionDialog?>(null) }
    fun offerSettingsOnce() {
        if (prefs.getBoolean("settingsGuideShown", false)) {
            dialog = null
        } else {
            prefs.edit().putBoolean("settingsGuideShown", true).apply()
            dialog = PermissionDialog.SettingsGuide
        }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        requesting = false
        allowed = enabled()
        if (retry && !allowed && !rationale()) {
            // A denied callback alone does not tell us how many times the user tapped Deny.
            offerSettingsOnce()
        }
        retry = false
    }
    DisposableEffect(lifecycle, activity) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                allowed = enabled()
                if (allowed) dialog = null
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(Unit) {
        if (runtime && !granted() && !prefs.getBoolean("entryRequested", false)) {
            prefs.edit().putBoolean("entryRequested", true).apply()
            if (!rationale()) {
                requesting = true
                launcher.launch(permission)
            }
        }
    }
    LaunchedEffect(inNotificationTab, requesting, allowed) {
        if (!inNotificationTab || allowed) dialog = null
        else if (!requesting && !prefs.getBoolean("tabOffered", false)) {
            prefs.edit().putBoolean("tabOffered", true).apply()
            dialog = PermissionDialog.Explain
        }
    }
    if (!inNotificationTab || allowed || requesting) return
    fun ask() {
        if (granted() || prefs.getBoolean("tabRequested", false) && !rationale()) {
            offerSettingsOnce()
        } else {
            prefs.edit().putBoolean("tabRequested", true).apply()
            dialog = null
            retry = true
            requesting = true
            launcher.launch(permission)
        }
    }
    val current = dialog ?: return
    val settings = current == PermissionDialog.SettingsGuide
    SeniorOnConfirmDialog(
        iconResId = R.drawable.ic_device_status_notification,
        title = if (settings) "설정에서 알림을 켜 주세요" else "알림을 허용해 주세요",
        description = if (settings) "이 휴대폰의 설정에서 시니어On 알림을 직접 켜 주세요."
            else "시니어의 알림을 이 휴대폰에서 받으려면 알림을 허용해 주세요.",
        cancelLabel = if (settings) "닫기" else "나중에",
        confirmLabel = if (settings) "설정 열기" else "알림 허용",
        confirmColor = SeniorOnColors.Primary600,
        onCancel = { if (settings) dialog = null else offerSettingsOnce() },
        onConfirm = {
            if (!settings) ask() else {
                dialog = null
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
                if (Build.VERSION.SDK_INT >= 26 && intent.resolveActivity(activity.packageManager) != null) {
                    activity.startActivity(intent)
                } else {
                    activity.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + activity.packageName)))
                }
            }
        },
    )
}
