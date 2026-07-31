package com.example.senior_on

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.senior_on.ui.app.SeniorOnApp
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.notification.MedicationReminderEventStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        publishMedicationReminder(intent)
        enableEdgeToEdge()
        setContent {
            SENIOR_ONTheme {
                SeniorOnApp(
                    appContainer = (application as SeniorOnApplication).appContainer,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        publishMedicationReminder(intent)
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
}
