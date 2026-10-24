package com.example.senior_on.device

import android.content.Context
import androidx.work.*
import com.example.senior_on.SeniorOnApplication
import com.example.senior_on.data.local.AccessTokenStore
import com.example.senior_on.data.local.ParentConnectionGate
import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.data.source.auth.PersistedSessionStore
import com.example.senior_on.data.repository.impl.ParentConnectionDestination
import kotlinx.coroutines.CancellationException
import java.util.concurrent.TimeUnit

class InactivityRetryWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        if (AccessTokenStore.getBearerToken() == null ||
            PersistedSessionStore(applicationContext).getSession()?.role != UserRole.PARENT
        ) return Result.success()
        val c = (applicationContext as SeniorOnApplication).appContainer
        return try {
            if (!ParentConnectionGate.isReady()) {
                val version = ParentConnectionGate.backgroundCheckVersion() ?: return Result.retry()
                if (c.parentReconnectionRepository.destination() != ParentConnectionDestination.Home) {
                    return Result.success()
                }
                if (!ParentConnectionGate.approve(version)) return Result.retry()
            }
            ParentInactivityMonitor(applicationContext, c.notificationRepository, c.eventRepository,
                c.locationRepository, c.deviceRepository, c.parentSharingGuard).refreshSettingAndCheck()
            Result.success()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            Result.retry()
        }
    }
}

internal object InactivityRetryScheduler {
    fun enqueue(context: Context) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            "parent_inactivity_retry", ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<InactivityRetryWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build(),
        )
    }
}
