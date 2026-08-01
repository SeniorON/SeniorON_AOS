package com.example.senior_on.notification

import com.example.senior_on.data.source.device.FcmTokenStore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class SeniorOnFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        MedicationReminderEventStore.publish(message.data)
        MedicationCheckedEventStore.publish(message.data)
        SeniorOnNotificationManager.showRemoteMessage(
            context = applicationContext,
            message = message,
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FcmTokenStore(applicationContext).save(token)
    }
}
