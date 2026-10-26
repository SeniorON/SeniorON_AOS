package com.example.senior_on.location.tracking

import android.content.Context
import com.example.senior_on.data.local.KeystoreTokenCipher
import com.example.senior_on.data.source.auth.PersistedSessionStore
import com.google.gson.Gson
import java.util.UUID
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock

internal enum class OutingTrackingState { Unknown, Home, Outing }
internal data class StoredHomeLocation(val latitude: Double, val longitude: Double)
internal enum class OutingDetectionType { TRANSITION, RECONNECTION }
internal data class PendingOutingEvent(
    val id: String = UUID.randomUUID().toString(),
    val state: OutingTrackingState,
    val detectionType: OutingDetectionType,
    val confirmedAt: Long,
    val latitude: Double,
    val longitude: Double,
)

internal data class OutingSnapshot(
    val revision: String = UUID.randomUUID().toString(),
    val account: String,
    val home: StoredHomeLocation,
    val state: OutingTrackingState = OutingTrackingState.Unknown,
    val deliveredState: OutingTrackingState = OutingTrackingState.Unknown,
    val confirmedAt: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val needsRecheck: Boolean = true,
    val returnCandidateAt: Long? = null,
    // Latest unresolved state only; not an offline movement history.
    val pending: PendingOutingEvent? = null,
    val sentAt: Long? = null,
)

private val Context.outingDataStore by androidx.datastore.preferences.preferencesDataStore(
    name = "outing_tracking",
    produceMigrations = { context ->
        listOf(androidx.datastore.preferences.SharedPreferencesMigration(
            context, "outing_tracking", keysToMigrate = setOf("confirmed_state_v2"),
        ))
    },
)

/** Same encrypted snapshot and Keystore key; migration copies ciphertext without decrypting it. */
internal class OutingTrackingStateStore internal constructor(
    private val dataStore: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>,
    private val currentAccount: () -> String?,
    private val encrypt: (String) -> String,
    private val decrypt: (String) -> String,
) {
    constructor(context: Context) : this(
        context.applicationContext.outingDataStore,
        { PersistedSessionStore(context.applicationContext).getSession()?.let { "${it.role}:${it.usersId}:${it.userId}" } },
        { KeystoreTokenCipher().encrypt(it) },
        { KeystoreTokenCipher().decrypt(it) },
    )

    private val gson = Gson()
    fun accountKey(): String? = currentAccount()

    // Do not interpret I/O/decryption failure as an empty state and overwrite pending events.
    private suspend fun read(): OutingSnapshot? {
        val encrypted = dataStore.data.first()[Key] ?: return null
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            gson.fromJson(decrypt(encrypted), OutingSnapshot::class.java)
        }
            ?.takeIf { it.account == accountKey() }
    }

    suspend fun snapshot(): OutingSnapshot? = lock.withLock { read() }

    suspend fun saveHomeLocation(latitude: Double, longitude: Double): Boolean = lock.withLock {
        val account = accountKey() ?: return@withLock false
        val home = StoredHomeLocation(latitude, longitude)
        val old = read()
        if (accountKey() != account) return@withLock false
        if (old?.home == home) return@withLock true
        write(OutingSnapshot(account = account, home = home))
        true
    }

    suspend fun getHomeLocation(): StoredHomeLocation? = snapshot()?.home
    suspend fun getState(): OutingTrackingState = snapshot()?.state ?: OutingTrackingState.Unknown

    suspend fun update(expected: OutingSnapshot, next: OutingSnapshot): Boolean = lock.withLock {
        if (read()?.revision != expected.revision || accountKey() != expected.account ||
            next.account != expected.account) return@withLock false
        write(next.copy(revision = UUID.randomUUID().toString()))
        true
    }

    suspend fun markNeedsRecheck() = lock.withLock {
        read()?.let { old ->
            if ((!old.needsRecheck || old.returnCandidateAt != null) && accountKey() == old.account) {
                write(old.copy(needsRecheck = true, returnCandidateAt = null, revision = UUID.randomUUID().toString()))
            }
        }
    }

    suspend fun clearAndAwait() = lock.withLock { dataStore.edit { it.remove(Key) } }

    // Synchronous UI callbacks enqueue cleanup before later reads/writes can acquire the mutex.
    // Application-owned scope prevents Activity destruction from cancelling persistence.
    fun clear() {
        cleanupScope.launch(start = kotlinx.coroutines.CoroutineStart.UNDISPATCHED) {
            try { clearAndAwait() }
            catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (error: Exception) {
                if (com.example.senior_on.BuildConfig.DEBUG) com.example.senior_on.logging.AppLog.e("SeniorOnOutingStore", "Failed to clear outing state", error)
                else com.example.senior_on.logging.AppLog.e("SeniorOnOutingStore", "OUTING_STATE_CLEAR_FAILED")
            }
        }
    }

    private suspend fun write(value: OutingSnapshot) {
        val encrypted = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            encrypt(gson.toJson(value))
        }
        dataStore.edit { it[Key] = encrypted }
    }

    private companion object {
        val lock = kotlinx.coroutines.sync.Mutex()
        val cleanupScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO)
        val Key = androidx.datastore.preferences.core.stringPreferencesKey("confirmed_state_v2")
    }
}
