package com.example.senior_on

import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.senior_on.ui.app.SeniorOnApp
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.notification.MedicationReminderEventStore
import com.example.senior_on.notification.MedicationCheckedEventStore
import com.example.senior_on.notification.NotificationNavigationEventStore
import com.example.senior_on.ui.parent.launcher.ParentLauncherActivity
import com.example.senior_on.ui.parent.launcher.ParentHomeRoleManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        publishMedicationReminder(intent)
        publishMedicationChecked(intent)
        publishNotificationNavigation(intent)
        enableEdgeToEdge()
        setContent {
            val currentDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = currentDensity.density,
                    fontScale = 1f,
                )
            ) {
                SENIOR_ONTheme {
                    SeniorOnApp(
                        appContainer = (application as SeniorOnApplication).appContainer,
                        onOpenParentLauncher = ::openParentLauncher,
                        startAtParentLogin = intent.getBooleanExtra("start_at_parent_login", false),
                    )
                }
            }
        }
        if (savedInstanceState == null) offerHomeSelectionAfterSessionEnd()
    }

    private fun offerHomeSelectionAfterSessionEnd() {
        if (!intent.getBooleanExtra(SelectHomeAfterSessionEnd, false)) return
        // Consume before opening Settings so returning/recreating does not open it again.
        intent.removeExtra(SelectHomeAfterSessionEnd)
        if (!ParentHomeRoleManager.isDefaultHome(this)) return

        val opened = listOf(Settings.ACTION_HOME_SETTINGS, Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            .any { action -> runCatching { startActivity(Intent(action)) }.isSuccess }
        Toast.makeText(
            this,
            if (opened) R.string.parent_exit_home_select else R.string.parent_exit_home_failed,
            Toast.LENGTH_LONG,
        ).show()
    }

    private fun openParentLauncher() {
        startActivity(
            Intent(this, ParentLauncherActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        )
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        publishMedicationReminder(intent)
        publishMedicationChecked(intent)
        publishNotificationNavigation(intent)
        offerHomeSelectionAfterSessionEnd()
    }

    private fun publishMedicationReminder(intent: Intent?) {
        if (
            intent?.getStringExtra(MedicationReminderEventStore.NotificationTypeKey) !=
            MedicationReminderEventStore.MedicationReminderType
        ) return

        MedicationReminderEventStore.publish(
            mapOf(
                MedicationReminderEventStore.NotificationTypeKey to
                    MedicationReminderEventStore.MedicationReminderType,
                MedicationReminderEventStore.MedicationLogIdKey to
                    intent.getStringExtra(MedicationReminderEventStore.MedicationLogIdKey).orEmpty(),
                MedicationReminderEventStore.MedicineNameKey to
                    intent.getStringExtra(MedicationReminderEventStore.MedicineNameKey).orEmpty(),
                MedicationReminderEventStore.PlannedTimeKey to
                    intent.getStringExtra(MedicationReminderEventStore.PlannedTimeKey).orEmpty(),
            )
        )
    }

    private fun publishMedicationChecked(intent: Intent?) {
        if (
            intent?.getStringExtra(MedicationCheckedEventStore.NotificationTypeKey) !=
            MedicationCheckedEventStore.MedicationCheckedType
        ) return

        MedicationCheckedEventStore.publish(
            mapOf(
                MedicationCheckedEventStore.NotificationTypeKey to
                    MedicationCheckedEventStore.MedicationCheckedType,
                MedicationCheckedEventStore.ParentUserIdKey to
                    intent.getStringExtra(MedicationCheckedEventStore.ParentUserIdKey).orEmpty(),
                MedicationCheckedEventStore.MedicationLogIdKey to
                    intent.getStringExtra(MedicationCheckedEventStore.MedicationLogIdKey).orEmpty(),
                MedicationCheckedEventStore.MedicineNameKey to
                    intent.getStringExtra(MedicationCheckedEventStore.MedicineNameKey).orEmpty(),
            )
        )
    }

    private fun publishNotificationNavigation(intent: Intent?) {
        intent ?: return
        val data = intent.extras
            ?.keySet()
            ?.mapNotNull { key ->
                intent.getStringExtra(key)?.let { value -> key to value }
            }
            ?.toMap()
            .orEmpty()

        NotificationNavigationEventStore.publish(
            data = data,
            openNotificationTab =
                intent.action == NotificationNavigationEventStore.OpenNotificationAction ||
                    intent.extras?.containsKey(FirebaseMessageIdKey) == true ||
                    intent.extras?.containsKey(FirebaseNotificationEnabledKey) == true,
        )
    }

    companion object {
        const val SelectHomeAfterSessionEnd = "select_home_after_session_end"
        const val FirebaseMessageIdKey = "google.message_id"
        const val FirebaseNotificationEnabledKey = "gcm.n.e"
    }
}
