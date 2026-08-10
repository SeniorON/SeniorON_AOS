package com.example.senior_on.location.tracking

import android.content.Context
import android.content.Intent

object ParentOutingTrackingController {
    fun reset(context: Context) {
        val applicationContext = context.applicationContext
        ParentGeofenceManager(applicationContext).unregister()
        applicationContext.stopService(
            Intent(applicationContext, OutingLocationService::class.java),
        )
        OutingTrackingStateStore(applicationContext).clear()
    }
}
