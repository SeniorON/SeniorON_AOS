package com.example.senior_on.device

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.ExistingWorkPolicy
import androidx.work.workDataOf
import com.example.senior_on.SeniorOnApplication
import com.example.senior_on.data.local.AccessTokenStore
import com.example.senior_on.data.local.ParentConnectionGate
import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.data.source.auth.PersistedSessionStore
import com.example.senior_on.data.repository.impl.ParentConnectionDestination
import com.example.senior_on.logging.AppLog
import kotlinx.coroutines.CancellationException
import java.io.IOException

class DeviceStatusPatchWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val account = inputData.getString("account") ?: return Result.success()
        val fields = inputData.getInt("fields", 0)
        fun sessionMatches(): Boolean {
            val session = PersistedSessionStore(applicationContext).getSession()
            return session?.userId == account && session.role == UserRole.PARENT &&
                AccessTokenStore.getBearerToken() != null
        }
        if (!sessionMatches() || fields == 0) return Result.success()
        if (!com.example.senior_on.data.source.device.AndroidDeviceStatusDataSource(applicationContext)
                .getStatusSnapshot().networkConnected) return Result.success()
        val container = (applicationContext as SeniorOnApplication).appContainer
        return try {
            if (!ParentConnectionGate.isReady()) {
                val version = ParentConnectionGate.backgroundCheckVersion() ?: return Result.success()
                if (container.parentReconnectionRepository.destination() != ParentConnectionDestination.Home ||
                    !sessionMatches() || !ParentConnectionGate.approve(version)) return Result.success()
            }
            val updated = container.deviceRepository.patchStatus(
                battery = fields and BATTERY_CHANGED != 0,
                charging = fields and CHARGING_CHANGED != 0,
                network = fields and NETWORK_RECOVERED != 0,
                canSend = { sessionMatches() && ParentConnectionGate.isReady() },
            )
            AppLog.d("SeniorOnDeviceStatus", "Partial device status sync completed; updated=$updated")
            Result.success()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            AppLog.w("SeniorOnDeviceStatus", "Partial device status sync failed", error)
            val transient = error is IOException ||
                (error is com.example.senior_on.domain.model.auth.RemoteRequestException &&
                    (error.status >= 500 || error.status == 429))
            if (transient && sessionMatches() && runAttemptCount < 3) Result.retry() else Result.success()
        }
    }
}

object DeviceStatusPatchScheduler {
    private const val NAME = "parent_device_status_patch"

    fun enqueue(context: Context, account: String, fields: Int) {
        val request = OneTimeWorkRequestBuilder<DeviceStatusPatchWorker>()
            .setInputData(workDataOf("account" to account, "fields" to fields))
            .build()
        // KEEP loses changes received while running; REPLACE cancels an in-flight request.
        WorkManager.getInstance(context).enqueueUniqueWork(NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    fun cancel(context: Context) = WorkManager.getInstance(context).cancelUniqueWork(NAME)
}
