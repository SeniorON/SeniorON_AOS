package com.example.senior_on

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.senior_on.ui.app.SeniorOnApp
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.notification.MedicationReminderEventStore
import com.example.senior_on.notification.NotificationNavigationEventStore
import com.example.senior_on.ui.parent.launcher.ParentLauncherActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        publishMedicationReminder(intent)
        publishNotificationNavigation(intent)
        enableEdgeToEdge()
        setContent {
            SENIOR_ONTheme {
                SeniorOnApp(
                    appContainer = (application as SeniorOnApplication).appContainer,
                    onOpenParentLauncher = ::openParentLauncher,
                )
            }
        }
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
        publishNotificationNavigation(intent)
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

    private companion object {
        const val FirebaseMessageIdKey = "google.message_id"
        const val FirebaseNotificationEnabledKey = "gcm.n.e"
    }
}
