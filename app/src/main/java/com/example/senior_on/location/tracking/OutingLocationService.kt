package com.example.senior_on.location.tracking

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.senior_on.R
import com.example.senior_on.SeniorOnApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class OutingLocationService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var trackingJob: Job? = null
    private var returnCandidateCount = 0
    private var trackingCycle = 0

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        ServiceCompat.startForeground(
            this,
            NotificationId,
            NotificationCompat.Builder(this, ChannelId)
                .setSmallIcon(R.drawable.ic_system_notification)
                .setContentTitle("외출 위치를 확인하고 있어요")
                .setContentText("가족에게 최근 위치를 안전하게 전달합니다.")
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            } else {
                0
            },
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (trackingJob?.isActive != true) {
            trackingJob = serviceScope.launch { trackWhileOuting() }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        trackingJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    private suspend fun trackWhileOuting() {
        val stateStore = OutingTrackingStateStore(this)
        val container = (application as SeniorOnApplication).appContainer

        while (serviceScope.isActive) {
            trackingCycle += 1
            val state = stateStore.getState()
            Log.d(LogTag, "Tracking cycle=$trackingCycle started; state=$state")
            if (state != OutingTrackingState.Outing) {
                Log.d(LogTag, "Stopping location service because state is not Outing")
                stopTracking()
                return
            }

            val home = stateStore.getHomeLocation()
            if (home == null) {
                Log.e(LogTag, "Stored home location is missing")
                stopTracking()
                return
            }

            runCatching {
                val current = container.locationRepository.getCurrentLocation()
                runCatching {
                    container.deviceRepository.updateLocation(
                        current.latitude,
                        current.longitude,
                    )
                }.onFailure { throwable ->
                    Log.w(LogTag, "Failed to upload current location", throwable)
                }

                val distance = distanceMeters(
                    home.latitude,
                    home.longitude,
                    current.latitude,
                    current.longitude,
                )
                Log.d(
                    LogTag,
                    "Tracking cycle=$trackingCycle; current=${current.latitude},${current.longitude}, " +
                        "distanceFromHome=${distance.toInt()}m, " +
                        "returnCandidateCount=$returnCandidateCount",
                )
                if (distance <= ReturnBoundaryMeters) {
                    returnCandidateCount += 1
                } else {
                    returnCandidateCount = 0
                }
                Log.d(
                    LogTag,
                    "Tracking cycle=$trackingCycle evaluated; " +
                        "returnCandidateCount=$returnCandidateCount/$RequiredReturnConfirmations",
                )

                if (returnCandidateCount >= RequiredReturnConfirmations) {
                    container.eventRepository.createOutingReturn(
                        phase = ReturnPhase,
                        latitude = current.latitude,
                        longitude = current.longitude,
                        battery = container.deviceRepository.getBatteryLevel(),
                    )
                    stateStore.setState(OutingTrackingState.Home)
                    Log.d(LogTag, "RETURN event created; state changed Outing -> Home")
                    stopTracking()
                    return
                }
            }.onFailure { throwable ->
                Log.w(LogTag, "Location tracking cycle failed", throwable)
            }

            delay(LocationUploadIntervalMillis)
        }
    }

    private fun stopTracking() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(
                ChannelId,
                "외출 위치 확인",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "외출 중 최근 위치를 가족에게 전달합니다."
            }
        )
    }

    companion object {
        private const val ChannelId = "senior_on_outing_location"
        private const val NotificationId = 7102
        private const val LogTag = "SeniorOnOutingLocation"
        private const val ReturnPhase = "RETURN"
        private const val RequiredReturnConfirmations = 2
        // TODO: 외출/귀가 테스트 완료 후 5L * 60L * 1_000L로 원복
        private const val LocationUploadIntervalMillis = 10_000L

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, OutingLocationService::class.java),
            )
        }
    }
}
