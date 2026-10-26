package com.example.senior_on.device

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.map

private val Context.inactivityOutbox by preferencesDataStore(DetectionQa.storeName("inactivity_outbox"))

/** One slot, including a terminal marker: an expired/sent episode must not be recreated. */
internal data class PendingInactivity(
    val account: String,
    val lastActive: Long,
    val queuedAt: Long,
    val status: String = "pending",
    val clientEventId: String = UUID.randomUUID().toString(),
    val attempts: Int = 0,
    val lastAttemptAt: Long? = null,
    val completedAt: Long? = null,
) {
    fun isExpired(now: Long) = now - queuedAt >= TimeUnit.HOURS.toMillis(48)
    fun isCurrent(account: String, state: ParentInactivityState) =
        this.account == account && lastActive == state.lastActiveAtMillis
}

internal class InactivityOutbox(private val store: DataStore<Preferences>) {
    constructor(context: Context) : this(context.applicationContext.inactivityOutbox)
    // Observation must not generate an ID, retry a send, or write to DataStore.
    fun observe() = store.data.map { p -> decode(p) }

    private fun decode(p: Preferences): PendingInactivity? {
        return PendingInactivity(
            account = p[Account] ?: return null,
            lastActive = p[Activity] ?: return null,
            queuedAt = p[Queued] ?: return null,
            status = p[Status] ?: "pending",
            clientEventId = p[ClientEventId].orEmpty(),
            attempts = p[Attempts]?.toInt() ?: 0,
            lastAttemptAt = p[LastAttempt],
            completedAt = p[Completed],
        )
    }
    suspend fun read(): PendingInactivity? {
        var event: PendingInactivity? = null
        // Older queued episodes have no ID. Persist one atomically before any send,
        // so repeated reads and process restarts reuse the same identity.
        store.edit { p ->
            if (p[Account] == null || p[Activity] == null || p[Queued] == null) return@edit
            if (p[ClientEventId].isNullOrBlank()) p[ClientEventId] = UUID.randomUUID().toString()
            event = decode(p)
        }
        return event
    }
    suspend fun save(event: PendingInactivity) {
        store.edit {
            it.clear()
            it[Account] = event.account
            it[Activity] = event.lastActive
            it[Queued] = event.queuedAt
            it[Status] = event.status
            it[ClientEventId] = event.clientEventId
            it[Attempts] = event.attempts.toLong()
            event.lastAttemptAt?.let { time -> it[LastAttempt] = time }
            event.completedAt?.let { time -> it[Completed] = time }
        }
    }
    suspend fun clear() { store.edit { it.clear() } }
    private companion object {
        val Account = stringPreferencesKey("account")
        val Activity = longPreferencesKey("activity")
        val Queued = longPreferencesKey("queued")
        val Status = stringPreferencesKey("status")
        val ClientEventId = stringPreferencesKey("client_event_id")
        val Attempts = longPreferencesKey("attempts")
        val LastAttempt = longPreferencesKey("last_attempt")
        val Completed = longPreferencesKey("completed")
    }
}
