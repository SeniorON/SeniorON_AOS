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

private val Context.inactivityOutbox by preferencesDataStore("inactivity_outbox")

/** One slot, including a terminal marker: an expired/sent episode must not be recreated. */
internal data class PendingInactivity(
    val account: String,
    val lastActive: Long,
    val queuedAt: Long,
    val status: String = "pending",
    val clientEventId: String = UUID.randomUUID().toString(),
) {
    fun isExpired(now: Long) = now - queuedAt >= TimeUnit.HOURS.toMillis(48)
    fun isCurrent(account: String, state: ParentInactivityState) =
        this.account == account && lastActive == state.lastActiveAtMillis
}

internal class InactivityOutbox(private val store: DataStore<Preferences>) {
    constructor(context: Context) : this(context.applicationContext.inactivityOutbox)
    suspend fun read(): PendingInactivity? {
        var event: PendingInactivity? = null
        // Older queued episodes have no ID. Persist one atomically before any send,
        // so repeated reads and process restarts reuse the same identity.
        store.edit { p ->
            val account = p[Account] ?: return@edit
            val activity = p[Activity] ?: return@edit
            val queued = p[Queued] ?: return@edit
            val id = p[ClientEventId]?.takeIf { it.isNotBlank() }
                ?: UUID.randomUUID().toString().also { p[ClientEventId] = it }
            event = PendingInactivity(account, activity, queued, p[Status] ?: "pending", id)
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
        }
    }
    suspend fun clear() { store.edit { it.clear() } }
    private companion object {
        val Account = stringPreferencesKey("account")
        val Activity = longPreferencesKey("activity")
        val Queued = longPreferencesKey("queued")
        val Status = stringPreferencesKey("status")
        val ClientEventId = stringPreferencesKey("client_event_id")
    }
}
