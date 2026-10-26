package com.example.senior_on.location.tracking

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.work.*
import com.example.senior_on.SeniorOnApplication
import com.example.senior_on.data.local.AccessTokenStore
import com.example.senior_on.data.local.ParentConnectionGate
import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.data.source.auth.PersistedSessionStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit
import com.example.senior_on.device.DetectionQaHistory

internal fun Context.isOutingNetworkAvailable(): Boolean {
    val manager = getSystemService(ConnectivityManager::class.java)
    val capabilities = manager.getNetworkCapabilities(manager.activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

/** Geofence, foreground service and recovery worker share this single writer. */
internal object OutingReconciliation {
    private val mutex = Mutex()

    suspend fun check(context: Context, recoveryOnly: Boolean = false): Boolean = mutex.withLock {
        val app = context.applicationContext as SeniorOnApplication
        val store = OutingTrackingStateStore(app)
        val account = store.accountKey()
        if (PersistedSessionStore(app).getSession()?.role != UserRole.PARENT ||
            AccessTokenStore.getBearerToken() == null) return@withLock true
        if (!app.isOutingNetworkAvailable()) {
            DetectionQaHistory.record(app, "오프라인 · 위치 판정 및 전송 보류")
            com.example.senior_on.device.DetectionQaStatus.update { it.copy(outingResult = "오프라인 — 판정 보류") }
            com.example.senior_on.device.DetectionQa.trace("OUTING_DEFERRED reason=offline")
            store.markNeedsRecheck(offlineObserved = true)
            return@withLock false
        }
        if (!ParentConnectionGate.isReady()) return@withLock false
        val container = app.appContainer
        var observationStored = false
        try {
            if (container.parentSharingGuard?.refresh()?.locationEnabled == false) {
                com.example.senior_on.device.DetectionQaStatus.update { it.copy(outingResult = "위치 공유 꺼짐", distanceMeters = null, locationCheckedAt = null) }
                if (store.accountKey() != account) return@withLock true
                ParentOutingTrackingController.reset(app)
                return@withLock true
            }
            if (!app.hasRequiredGeofencePermissions()) {
                ParentGeofenceManager.invalidateRegistration()
                DetectionQaHistory.record(app, "재확인 설정 사유: 위치 권한/GPS 확인 실패")
                com.example.senior_on.device.DetectionQaStatus.update { it.copy(outingResult = "위치 권한 또는 GPS 확인 필요") }
                store.markNeedsRecheck()
                return@withLock false
            }
            // Load home after permission/session validation; also handles changed home addresses.
            val home = container.deviceRepository.getHomeLocation()
            if (store.accountKey() != account || !ParentConnectionGate.isReady()) return@withLock false
            check(store.saveHomeLocation(home.latitude, home.longitude))
            ParentGeofenceManager(app).register(StoredHomeLocation(home.latitude, home.longitude), "outing_recovery")
            val old = store.snapshot() ?: return@withLock false
            if (recoveryOnly && !old.needsRecheck && old.pending == null && old.state == OutingTrackingState.Home) return@withLock true
            val current = container.locationRepository.getFreshLocation()
            if (store.accountKey() != account) return@withLock true
            if (!app.isOutingNetworkAvailable() || !ParentConnectionGate.isReady()) {
                DetectionQaHistory.record(app, "재확인 설정 사유: 위치 조회 도중 네트워크 또는 가족 연결 상태 변경")
                store.markNeedsRecheck(offlineObserved = !app.isOutingNetworkAvailable())
                return@withLock false
            }
            val distance = distanceMeters(
                home.latitude, home.longitude, current.latitude, current.longitude,
            )
            DetectionQaHistory.record(app, "온라인 · 판정용 위치 확인 ${distance.toInt()}m · 이전=${old.state} · 재확인=${old.needsRecheck} · 오프라인관측=${old.offlineObserved} · 전송대기=${old.pending != null}")
            com.example.senior_on.device.DetectionQaStatus.update {
                it.copy(distanceMeters = distance.takeIf { value -> value.isFinite() },
                    locationCheckedAt = System.currentTimeMillis(), outingState = old.state.name)
            }
            val next = observedOutingState(distance, activeOutingBoundaries) ?: run {
                DetectionQaHistory.record(app, "재확인 설정 사유: 경계 구간/위치 불확실 · 판정 보류")
                com.example.senior_on.device.DetectionQaStatus.update { it.copy(outingResult = "100~150m 경계 또는 위치 불확실 — 판정 보류") }
                store.markNeedsRecheck()
                return@withLock false
            }
            val now = System.currentTimeMillis()
            com.example.senior_on.device.DetectionQa.trace("OUTING_OBSERVED previous=${old.state} current=$next reconnect=${old.offlineObserved} needsRecheck=${old.needsRecheck} pending=${old.pending != null}")
            // Normal return retains two confirmations spaced by five minutes.
            if (requiresReturnConfirmation(old, next)) {
                val first = old.returnCandidateAt
                if (first == null || now < first || now - first < TimeUnit.MINUTES.toMillis(5)) {
                    DetectionQaHistory.record(app, "귀가 후보 · 5분 간격 재확인 대기 · ${distance.toInt()}m")
                    com.example.senior_on.device.DetectionQaStatus.update { it.copy(outingResult = "집 안 후보 — 5분 후 재확인 대기") }
                    if (first == null || now < first) store.update(old, old.copy(returnCandidateAt = now))
                    return@withLock true
                }
            }
            check(store.update(old, applyOutingObservation(old, next, current.latitude, current.longitude, now)))
            observationStored = true
            val confirmed = store.snapshot() ?: return@withLock false
            DetectionQaHistory.record(app, "판정 ${old.state} → ${confirmed.state} · ${distance.toInt()}m · " +
                (confirmed.pending?.let { "전송 대기 ${it.detectionType} · ID=${it.id} · 감지시각=${it.detectedAtText()}" } ?: "전송할 이벤트 없음"))
            com.example.senior_on.device.DetectionQaStatus.update {
                it.copy(outingState = confirmed.state.name,
                    outingResult = confirmed.pending?.let { event -> "전송 대기 · ${event.detectionType}" } ?: "판정 완료 · 전송할 이벤트 없음")
            }
            // A stale observation cannot resurrect state cleared by logout/revocation.
            if (confirmed.account != old.account || confirmed.home != old.home) return@withLock false
            if (next == OutingTrackingState.Outing) {
                // Android may deny a background FGS launch; recovery does not depend on it succeeding.
                runCatching { OutingLocationService.start(app) }
            }
            try {
                container.deviceRepository.updateLocation(current.latitude, current.longitude)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Coordinate upload failure must not discard the confirmed event.
            }
            val pending = confirmed.pending ?: return@withLock true
            if (confirmed.needsRecheck || !app.isOutingNetworkAvailable() ||
                store.snapshot()?.revision != confirmed.revision || !ParentConnectionGate.isReady()) return@withLock false
            container.eventRepository.createOutingReturn(
                phase = if (pending.state == OutingTrackingState.Outing) "OUTING" else "RETURN",
                latitude = pending.latitude, longitude = pending.longitude,
                battery = container.deviceRepository.getBatteryLevel(),
                detectionType = pending.detectionType.name,
                clientEventId = pending.id,
                detectedAt = pending.detectedAtText(),
            )
            // Never roll the observed state back on a failed HTTP request.
            val saved = store.update(confirmed, confirmed.copy(pending = null, deliveredState = pending.state, sentAt = System.currentTimeMillis()))
            DetectionQaHistory.record(app, "전송 후 상태 저장=${if (saved) "완료" else "경합으로 미반영"} · ID=${pending.id}")
            com.example.senior_on.device.DetectionQaStatus.update {
                it.copy(outingResult = if (saved) "모의 전송 완료 · ${pending.detectionType}" else "상태 변경됨 — 재확인 대기")
            }
            saved
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            DetectionQaHistory.record(app, "${if (observationStored) "전송/저장" else "위치/설정 확인"} 실패 · ${error.javaClass.simpleName} · 재시도 대기")
            com.example.senior_on.device.DetectionQaStatus.update { it.copy(outingResult = if (observationStored) "전송/저장 실패 — 재시도 대기" else "위치·설정 확인 실패 — 마지막 거리만 표시") }
            try {
                if (!observationStored && store.accountKey() == account) store.markNeedsRecheck()
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (storageError: Exception) { error.addSuppressed(storageError) }
            if (com.example.senior_on.BuildConfig.DEBUG) com.example.senior_on.logging.AppLog.w("SeniorOnOutingSync", "Outing confirmation/send deferred", error)
            else com.example.senior_on.logging.AppLog.w("SeniorOnOutingSync", "OUTING_SYNC_DEFERRED")
            false
        }
    }
}

class OutingRecoveryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as SeniorOnApplication
        if (PersistedSessionStore(app).getSession()?.role != UserRole.PARENT || AccessTokenStore.getBearerToken() == null) return Result.success()
        if (!ParentConnectionGate.isReady()) {
            val revision = ParentConnectionGate.backgroundCheckVersion() ?: return Result.retry()
            try {
                val destination = app.appContainer.parentReconnectionRepository.destination()
                if (destination != com.example.senior_on.data.repository.impl.ParentConnectionDestination.Home) return Result.success()
                if (!ParentConnectionGate.approve(revision)) return Result.retry()
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { return Result.retry() }
        }
        return try {
            if (OutingReconciliation.check(app, recoveryOnly = true)) Result.success() else Result.retry()
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { Result.retry() }
    }
}

object OutingRecoveryScheduler {
    private const val Name = "parent_outing_recovery"
    fun enqueue(context: Context) {
        if (PersistedSessionStore(context).getSession()?.role != UserRole.PARENT) return
        WorkManager.getInstance(context).enqueueUniqueWork(Name, ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<OutingRecoveryWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS).build())
    }
    fun cancel(context: Context) { WorkManager.getInstance(context).cancelUniqueWork(Name) }
}

/** Only an observed offline state marks the next successful observation as reconnection. */
object OutingNetworkObserver {
    fun register(context: Context) {
        val app = context.applicationContext
        val manager = app.getSystemService(ConnectivityManager::class.java)
        var wasOnline = false
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO)
        val networkMutex = Mutex()
        suspend fun checkNetwork() {
            if (PersistedSessionStore(app).getSession()?.role != UserRole.PARENT) return
            val online = app.isOutingNetworkAvailable()
            if (online != wasOnline) DetectionQaHistory.record(app,
                if (online) "네트워크 온라인 확인 · 복구 검사 요청 (프로세스 시작 후 최초 확인 포함)" else "네트워크 오프라인 확인 · 다음 온라인 때 재확인")
            if (!online || !wasOnline) OutingTrackingStateStore(app).markNeedsRecheck(offlineObserved = !online)
            if (online && !wasOnline) OutingRecoveryScheduler.enqueue(app)
            wasOnline = online
        }
        manager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            private fun refresh() {
                scope.launch {
                    networkMutex.withLock {
                        try { checkNetwork() }
                        catch (cancelled: CancellationException) { throw cancelled }
                        catch (error: Exception) {
                            if (com.example.senior_on.BuildConfig.DEBUG) com.example.senior_on.logging.AppLog.w("SeniorOnOutingSync", "Network state persistence failed", error)
                            else com.example.senior_on.logging.AppLog.e("SeniorOnOutingSync", "OUTING_NETWORK_STATE_SAVE_FAILED")
                            OutingRecoveryScheduler.enqueue(app)
                        }
                    }
                }
            }
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) = refresh()
            override fun onLost(network: Network) = refresh()
        })
    }
}
