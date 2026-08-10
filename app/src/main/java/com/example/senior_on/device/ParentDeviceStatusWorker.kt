package com.example.senior_on.device

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.senior_on.SeniorOnApplication
import com.example.senior_on.data.local.AccessTokenStore
import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.data.source.auth.PersistedSessionStore
import java.util.concurrent.TimeUnit

class ParentDeviceStatusWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val savedRole = PersistedSessionStore(applicationContext).getSession()?.role
        if (AccessTokenStore.getBearerToken() == null || savedRole != UserRole.PARENT) {
            Log.d(
                LogTag,
                "Skipping parent sync because parent session is unavailable; role=$savedRole",
            )
            return Result.success()
        }

        val appContainer = (applicationContext as SeniorOnApplication).appContainer
        val repository = appContainer.deviceRepository
        val inactivityMonitor = ParentInactivityMonitor(
            context = applicationContext,
            notificationRepository = appContainer.notificationRepository,
            eventRepository = appContainer.eventRepository,
            locationRepository = appContainer.locationRepository,
            deviceRepository = appContainer.deviceRepository,
        )

        val statusResult = runCatching { repository.updateStatus() }
            .onSuccess { Log.d(LogTag, "Parent device status sync completed") }
            .onFailure { throwable ->
                Log.w(LogTag, "Parent device status sync failed", throwable)
            }
        val inactivityResult = runCatching {
            inactivityMonitor.refreshSettingAndCheck()
        }.onFailure { throwable ->
            Log.w(LogTag, "Parent inactivity check failed", throwable)
        }

        return if (statusResult.isSuccess && inactivityResult.isSuccess) {
            Result.success()
        } else if (runAttemptCount >= MaxRetryCount) {
            Result.failure()
        } else {
            Result.retry()
        }
    }

    private companion object {
        const val LogTag = "SeniorOnDeviceStatus"
        const val MaxRetryCount = 3
    }
}

object ParentDeviceStatusScheduler {
    private const val PeriodicWorkName = "parent_device_status_periodic"
    private const val ImmediateWorkName = "parent_device_status_immediate"

    private val networkConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<ParentDeviceStatusWorker>(
            15,
            TimeUnit.MINUTES,
        )
            .setConstraints(networkConstraint)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            PeriodicWorkName,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun enqueueImmediate(context: Context) {
        val request = OneTimeWorkRequestBuilder<ParentDeviceStatusWorker>()
            .setConstraints(networkConstraint)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            ImmediateWorkName,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
}
