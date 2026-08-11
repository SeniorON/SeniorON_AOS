package com.example.senior_on.location.tracking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.senior_on.data.local.AccessTokenStore
import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.data.source.auth.PersistedSessionStore
import com.example.senior_on.notification.FcmTokenSyncScheduler
import com.example.senior_on.device.ParentDeviceStatusScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ParentBootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (
            intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val hasToken = AccessTokenStore.getBearerToken() != null
                val isParentSession = PersistedSessionStore(context).getSession()?.role == UserRole.PARENT

                if (hasToken) {
                    FcmTokenSyncScheduler.enqueueIfLoggedIn(context)
                }
                if (hasToken && isParentSession) {
                    ParentDeviceStatusScheduler.schedulePeriodic(context)
                    ParentDeviceStatusScheduler.enqueueImmediate(context)
                }

                if (hasToken && isParentSession) {
                    val stateStore = OutingTrackingStateStore(context)
                    val home = stateStore.getHomeLocation()
                    if (home != null && context.hasRequiredGeofencePermissions()) {
                        ParentGeofenceManager(context).register(
                            home = home,
                            origin = "boot_completed_receiver",
                        )
                    }
                    if (stateStore.getState() == OutingTrackingState.Outing) {
                        OutingLocationService.start(context)
                    }
                }
            } catch (throwable: Throwable) {
                Log.e(LogTag, "Failed to restore outing tracking after boot", throwable)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val LogTag = "SeniorOnBootTracking"
    }
}
