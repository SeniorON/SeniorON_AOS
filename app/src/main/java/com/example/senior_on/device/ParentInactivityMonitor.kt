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
    private val sharingGuard: com.example.senior_on.data.repository.impl.ParentSharingGuard? = null,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) {
    private val appContext = context.applicationContext
    private val stateStore = ParentInactivityStateStore(context)
    private val outbox = InactivityOutbox(context)
    private val sessions = com.example.senior_on.data.source.auth.PersistedSessionStore(context)

    private fun account(): String? = sessions.getSession()?.takeIf {
        it.role == com.example.senior_on.data.remote.dto.UserRole.PARENT &&
            com.example.senior_on.data.local.AccessTokenStore.getBearerToken() != null
    }?.let { "${it.role}:${it.usersId}:${it.userId}" }

    suspend fun refreshSettingAndCheck() = CheckMutex.withLock {
        val owner = account() ?: return@withLock
        if (!com.example.senior_on.data.local.ParentConnectionGate.isReady()) return@withLock
        var pending = outbox.read()
        if (pending != null && !pending.isCurrent(owner, stateStore.snapshot())) {
            outbox.clear()
            pending = null
        }
        // Persist before any network request, including the permission lookup.
        val cached = stateStore.snapshot()
        if (pending == null && ParentInactivityPolicy.shouldCreateEvent(cached, nowMillis())) {
            pending = PendingInactivity(owner, checkNotNull(cached.lastActiveAtMillis), nowMillis())
            outbox.save(pending)
            InactivityRetryScheduler.enqueue(appContext)
        }
        try {
            // Settings synchronization is independent of duplicate notification suppression.
            // Keep terminal markers so a threshold/permission change cannot revive this episode.
            refreshSetting()
            if (account() != owner || !com.example.senior_on.data.local.ParentConnectionGate.isReady()) {
                return@withLock
            }
            if (pending != null && !pending.isCurrent(owner, stateStore.snapshot())) {
                outbox.clear()
                pending = null
            }
            if (pending?.status != null && pending.status != "pending") return@withLock
            if (pending?.isExpired(nowMillis()) == true) {
                outbox.save(pending.copy(status = "expired"))
                return@withLock
            }
            val permissions = sharingGuard?.refresh()
            if (permissions?.inactivityDetectionEnabled == false) {
                outbox.clear()
                return@withLock
            }

            var state = stateStore.snapshot()
            if (state.lastActiveAtMillis == null) {
                Log.d(LogTag, "Inactivity check skipped: no user activity baseline")
                return@withLock
            }

            val currentTime = nowMillis()
            if (!ParentInactivityPolicy.shouldCreateEvent(state, currentTime)) {
                outbox.clear()
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
            val event = pending ?: PendingInactivity(owner, lastActiveAt, currentTime).also {
                outbox.save(it)
                InactivityRetryScheduler.enqueue(appContext)
            }
            val location = if (permissions?.locationEnabled != false) resolveLocation() else null
            // Location and permission requests may suspend while the user resumes activity.
            if (account() != owner || !com.example.senior_on.data.local.ParentConnectionGate.isReady() ||
                !event.isCurrent(owner, stateStore.snapshot()) ||
                !ParentInactivityPolicy.shouldCreateEvent(stateStore.snapshot(), nowMillis())
            ) return@withLock
            eventRepository.createInactivity(
                latitude = location?.latitude,
                longitude = location?.longitude,
                battery = deviceRepository.getBatteryLevel(),
                lastSeenAt = lastActiveAt.toLocalDateTimeText(),
            )
            outbox.save(event.copy(status = "sent"))
            if (account() == owner && stateStore.snapshot().lastActiveAtMillis == lastActiveAt) {
                stateStore.markAlertSentFor(lastActiveAt)
            }
            Log.i(
                LogTag,
                "Inactivity event created; thresholdHours=${state.thresholdHours}, " +
                    "lastActiveAt=${lastActiveAt.toLocalDateTimeText()}",
            )
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            InactivityRetryScheduler.enqueue(appContext)
            throw error
        }
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
                if (throwable is kotlinx.coroutines.CancellationException) throw throwable
                Log.w(LogTag, "Deferring inactivity transmission: settings unavailable", throwable)
                // Cached values may queue an episode, but must not authorize transmission.
                throw throwable
            }
    }

    private suspend fun resolveLocation(): GeoLocation? = try {
        kotlinx.coroutines.withTimeoutOrNull(10_000) { locationRepository.getFreshLocation() }
    } catch (cancelled: kotlinx.coroutines.CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        Log.w(LogTag, "Location unavailable; sending inactivity without coordinates", error)
        null
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
