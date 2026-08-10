package com.example.senior_on.notification

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.senior_on.SeniorOnApplication
import com.example.senior_on.data.local.AccessTokenStore
import com.example.senior_on.data.source.device.FcmTokenStore
import java.util.concurrent.TimeUnit

class FcmTokenSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        if (AccessTokenStore.getBearerToken() == null) {
            Log.d(LogTag, "Skipping FCM token sync because the user is logged out")
            return Result.success()
        }

        val token = FcmTokenStore(applicationContext).get()
        if (token.isNullOrBlank()) {
            Log.d(LogTag, "Skipping FCM token sync because no local token is available")
            return Result.success()
        }

        val repository = (applicationContext as SeniorOnApplication)
            .appContainer
            .deviceRepository

        return runCatching {
            repository.updateFcmToken(token)
        }.fold(
            onSuccess = {
                Log.d(LogTag, "FCM token sync completed")
                Result.success()
            },
            onFailure = { throwable ->
                Log.w(LogTag, "FCM token sync failed", throwable)
                when {
                    AccessTokenStore.getBearerToken() == null -> Result.success()
                    runAttemptCount >= MaxRetryCount -> Result.failure()
                    else -> Result.retry()
                }
            },
        )
    }

    private companion object {
        const val LogTag = "SeniorOnFcmSync"
        const val MaxRetryCount = 5
    }
}

object FcmTokenSyncScheduler {
    private const val UniqueWorkName = "sync_fcm_token"

    fun enqueueIfLoggedIn(context: Context) {
        if (AccessTokenStore.getBearerToken() != null) enqueue(context)
    }

    fun enqueue(context: Context) {
        val request = OneTimeWorkRequestBuilder<FcmTokenSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS,
            )
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            UniqueWorkName,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
}
