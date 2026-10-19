package com.example.senior_on.device

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

private val Context.inactivityOutbox by preferencesDataStore("inactivity_outbox")

/** One slot, including a terminal marker: an expired/sent episode must not be recreated. */
internal data class PendingInactivity(
    val account: String,
    val lastActive: Long,
    val queuedAt: Long,
    val status: String = "pending",
) {
    fun isExpired(now: Long) = now - queuedAt >= TimeUnit.HOURS.toMillis(48)
    fun isCurrent(account: String, state: ParentInactivityState) =
        this.account == account && lastActive == state.lastActiveAtMillis
}

internal class InactivityOutbox(context: Context) {
    private val store = context.applicationContext.inactivityOutbox
    suspend fun read(): PendingInactivity? {
        val p = store.data.first()
        return PendingInactivity(
            p[Account] ?: return null, p[Activity] ?: return null,
            p[Queued] ?: return null, p[Status] ?: "pending",
        )
    }
    suspend fun save(event: PendingInactivity) {
        store.edit {
            it.clear()
            it[Account] = event.account
            it[Activity] = event.lastActive
            it[Queued] = event.queuedAt
            it[Status] = event.status
        }
    }
    suspend fun clear() { store.edit { it.clear() } }
    private companion object {
        val Account = stringPreferencesKey("account")
        val Activity = longPreferencesKey("activity")
        val Queued = longPreferencesKey("queued")
        val Status = stringPreferencesKey("status")
    }
}
