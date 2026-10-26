package com.example.senior_on.device

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class InactivityOutboxTest {
    private class MemoryStore : DataStore<Preferences> {
        override val data = MutableStateFlow<Preferences>(emptyPreferences())
        private val mutex = Mutex()
        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
            mutex.withLock { transform(data.value).also { data.value = it } }
    }

    @Test fun reopeningAndTerminalUpdatesKeepOriginalIdentity() = runTest {
        val data = MemoryStore()
        val first = PendingInactivity("parent-a", 100, 200)
        InactivityOutbox(data).save(first)
        val reopened = InactivityOutbox(data)
        val retry = reopened.read()!!
        assertEquals(first, retry)
        reopened.save(retry.copy(status = "sent"))
        assertEquals(first.clientEventId, reopened.read()!!.clientEventId)
        reopened.clear()
        assertNull(reopened.read())
        val next = PendingInactivity("parent-a", 300, 400)
        reopened.save(next)
        assertNotEquals(first.clientEventId, reopened.read()!!.clientEventId)
    }

    @Test fun legacyEpisodeGetsOnePersistedIdWithoutChangingOtherFields() = runTest {
        val data = MemoryStore()
        data.edit {
            it[stringPreferencesKey("account")] = "parent-a"
            it[longPreferencesKey("activity")] = 100
            it[longPreferencesKey("queued")] = 200
            it[stringPreferencesKey("status")] = "pending"
        }
        val first = InactivityOutbox(data).read()!!
        UUID.fromString(first.clientEventId)
        assertEquals(first, InactivityOutbox(data).read())
        assertEquals(100L, first.lastActive)
        assertEquals(200L, first.queuedAt)
        assertEquals("pending", first.status)
        assertEquals(first.clientEventId, data.data.value[stringPreferencesKey("client_event_id")])
    }
}
