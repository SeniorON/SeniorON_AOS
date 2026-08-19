package com.example.senior_on.device

import android.content.Context
import android.util.Log
import com.example.senior_on.core.time.KoreaZoneId
import com.example.senior_on.domain.model.location.GeoLocation
import com.example.senior_on.domain.repository.location.LocationRepository
import com.example.senior_on.domain.repository.server.DeviceRepository
import com.example.senior_on.domain.repository.server.EventRepository
import com.example.senior_on.domain.repository.server.NotificationRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ParentInactivityMonitor(
    context: Context,
    private val notificationRepository: NotificationRepository,
    private val eventRepository: EventRepository,
    private val locationRepository: LocationRepository,
    private val deviceRepository: DeviceRepository,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) {
    private val stateStore = ParentInactivityStateStore(context)

    suspend fun refreshSettingAndCheck() = CheckMutex.withLock {
        refreshSetting()

        var state = stateStore.snapshot()
        if (state.lastActiveAtMillis == null) {
            stateStore.recordActivity(nowMillis())
            Log.d(LogTag, "Initialized last activity timestamp")
            return@withLock
        }

        val currentTime = nowMillis()
        if (!ParentInactivityPolicy.shouldCreateEvent(state, currentTime)) {
            Log.d(
                LogTag,
                "Inactivity check skipped; enabled=${state.enabled}, " +
                    "thresholdHours=${state.thresholdHours}, " +
                    "alreadyAlerted=${state.alertedForActivityAtMillis == state.lastActiveAtMillis}",
            )
            return@withLock
        }

        // The mutex prevents the foreground loop and WorkManager from creating the same event.
        state = stateStore.snapshot()
        if (!ParentInactivityPolicy.shouldCreateEvent(state, currentTime)) return@withLock

        val lastActiveAt = checkNotNull(state.lastActiveAtMillis)
        val location = resolveLocation()
        eventRepository.createInactivity(
            latitude = location.latitude,
            longitude = location.longitude,
            battery = deviceRepository.getBatteryLevel(),
            lastSeenAt = lastActiveAt.toLocalDateTimeText(),
        )
        stateStore.markAlertSentFor(lastActiveAt)
        Log.i(
            LogTag,
            "Inactivity event created; thresholdHours=${state.thresholdHours}, " +
                "lastActiveAt=${lastActiveAt.toLocalDateTimeText()}",
        )
    }

    private suspend fun refreshSetting() {
        runCatching { notificationRepository.getMyInactivitySetting() }
            .onSuccess { setting ->
                stateStore.updateSetting(
                    thresholdHours = setting.thresholdHours,
                    enabled = setting.enabled,
                )
                Log.d(
                    LogTag,
                    "Inactivity setting refreshed; enabled=${setting.enabled}, " +
                        "thresholdHours=${setting.thresholdHours}",
                )
            }
            .onFailure { throwable ->
                Log.w(LogTag, "Using cached inactivity setting because refresh failed", throwable)
            }
    }

    private suspend fun resolveLocation(): GeoLocation =
        runCatching { locationRepository.getCurrentLocation() }
            .getOrElse { locationFailure ->
                Log.w(LogTag, "Using registered home as inactivity location", locationFailure)
                deviceRepository.getHomeLocation().let { home ->
                    GeoLocation(home.latitude, home.longitude)
                }
            }

    private fun Long.toLocalDateTimeText(): String = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(this),
        KoreaZoneId,
    ).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    private companion object {
        const val LogTag = "SeniorOnInactivity"
        val CheckMutex = Mutex()
    }
}
