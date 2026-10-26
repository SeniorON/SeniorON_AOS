package com.example.senior_on.location.tracking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.senior_on.logging.AppLog as Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.*

class ParentGeofenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (!event.hasError() && event.geofenceTransition !in listOf(Geofence.GEOFENCE_TRANSITION_EXIT, Geofence.GEOFENCE_TRANSITION_ENTER)) return
        val result = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                withTimeout(8_000) {
                    if (event.hasError()) {
                        ParentGeofenceManager.invalidateRegistration()
                        com.example.senior_on.device.DetectionQaHistory.record(context, "재확인 설정 사유: 지오펜스 오류 code=${event.errorCode}")
                        if (com.example.senior_on.BuildConfig.DEBUG) Log.w("SeniorOnGeofence", "Geofence unavailable: ${event.errorCode}")
                        else Log.w("SeniorOnGeofence", "GEOFENCE_UNAVAILABLE")
                        OutingTrackingStateStore(context).markNeedsRecheck()
                        OutingRecoveryScheduler.enqueue(context)
                        return@withTimeout
                    }
                    if (!OutingReconciliation.check(context)) OutingRecoveryScheduler.enqueue(context)
                }
            } catch (_: TimeoutCancellationException) {
                com.example.senior_on.device.DetectionQaHistory.record(context, "재확인 설정 사유: 지오펜스 처리 8초 시간 초과 (온라인이어도 발생 가능)")
                try { OutingTrackingStateStore(context).markNeedsRecheck() }
                catch (error: Exception) {
                    if (error is CancellationException) throw error
                    if (com.example.senior_on.BuildConfig.DEBUG) Log.w("SeniorOnGeofence", "Failed to persist recheck flag", error)
                    else Log.e("SeniorOnGeofence", "GEOFENCE_RECHECK_SAVE_FAILED")
                }
                finally { OutingRecoveryScheduler.enqueue(context) }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                if (com.example.senior_on.BuildConfig.DEBUG) Log.w("SeniorOnGeofence", "State persistence/check failed", error)
                else Log.w("SeniorOnGeofence", "GEOFENCE_CHECK_DEFERRED")
                OutingRecoveryScheduler.enqueue(context)
            } finally { result.finish() }
        }
    }
}
