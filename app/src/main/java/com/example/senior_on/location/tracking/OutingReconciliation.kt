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
            store.markNeedsRecheck()
            return@withLock false
        }
        if (!ParentConnectionGate.isReady()) return@withLock false
        val container = app.appContainer
        var observationStored = false
        try {
            if (container.parentSharingGuard?.refresh()?.locationEnabled == false) {
                if (store.accountKey() != account) return@withLock true
                ParentOutingTrackingController.reset(app)
                return@withLock true
            }
            if (!app.hasRequiredGeofencePermissions()) {
                store.markNeedsRecheck()
                return@withLock false
            }
            // Load home after permission/session validation; also handles changed home addresses.
            val home = container.deviceRepository.getHomeLocation()
            if (store.accountKey() != account || !ParentConnectionGate.isReady()) return@withLock false
            val previousHome = store.getHomeLocation()
            check(store.saveHomeLocation(home.latitude, home.longitude))
            if (previousHome != StoredHomeLocation(home.latitude, home.longitude)) {
                ParentGeofenceManager(app).register(StoredHomeLocation(home.latitude, home.longitude), "outing_recovery")
            }
            val old = store.snapshot() ?: return@withLock false
            if (recoveryOnly && !old.needsRecheck && old.pending == null && old.state == OutingTrackingState.Home) return@withLock true
            val current = container.locationRepository.getFreshLocation()
            if (store.accountKey() != account) return@withLock true
            if (!app.isOutingNetworkAvailable() || !ParentConnectionGate.isReady()) {
                store.markNeedsRecheck()
                return@withLock false
            }
            val next = observedOutingState(distanceMeters(
                home.latitude, home.longitude, current.latitude, current.longitude,
            )) ?: run {
                store.markNeedsRecheck()
                return@withLock false
            }
            val now = System.currentTimeMillis()
            // Normal return retains two confirmations spaced by five minutes.
            if (!old.needsRecheck && old.state == OutingTrackingState.Outing && next == OutingTrackingState.Home) {
                val first = old.returnCandidateAt
                if (first == null || now < first || now - first < TimeUnit.MINUTES.toMillis(5)) {
                    if (first == null || now < first) store.update(old, old.copy(returnCandidateAt = now))
                    return@withLock true
                }
            }
            check(store.update(old, applyOutingObservation(old, next, current.latitude, current.longitude, now)))
            observationStored = true
            val confirmed = store.snapshot() ?: return@withLock false
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
            )
            // Never roll the observed state back on a failed HTTP request.
            store.update(confirmed, confirmed.copy(pending = null, deliveredState = pending.state, sentAt = System.currentTimeMillis()))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
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

/** Process-start is treated conservatively as a gap; never claims an unobserved transition. */
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
            if (!online || !wasOnline) OutingTrackingStateStore(app).markNeedsRecheck()
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
