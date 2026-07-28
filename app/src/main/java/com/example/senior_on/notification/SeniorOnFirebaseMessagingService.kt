package com.example.senior_on.notification

import com.example.senior_on.data.source.device.FcmTokenStore
import com.google.firebase.messaging.FirebaseMessagingService

class SeniorOnFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FcmTokenStore(applicationContext).save(token)
    }
}
