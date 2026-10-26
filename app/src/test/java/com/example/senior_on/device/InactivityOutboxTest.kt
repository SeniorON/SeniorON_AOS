package com.example.senior_on.device

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class InactivityOutboxTest {
    @Test fun observationDoesNotMigrateOrChangeData() = runTest {
        val data = MemoryStore()
        data.edit {
            it[stringPreferencesKey("account")] = "parent-a"
            it[longPreferencesKey("activity")] = 100
            it[longPreferencesKey("queued")] = 200
        }
        val before = data.data.value
        val observed = InactivityOutbox(data).observe().first()!!
        assertEquals("", observed.clientEventId)
        assertEquals(before, data.data.value)
    }

    @Test fun retryDiagnosticsPreserveDetectionTimeAcrossReopen() = runTest {
        val data = MemoryStore()
        val original = PendingInactivity("parent-a", 100, 120_100)
        val box = InactivityOutbox(data)
        box.save(original.copy(attempts = 1, lastAttemptAt = 125_000))
        val retry = InactivityOutbox(data).read()!!
        box.save(retry.copy(attempts = 2, lastAttemptAt = 150_000, status = "sent", completedAt = 151_000))
        val completed = InactivityOutbox(data).observe().first()!!
        assertEquals(original.queuedAt, completed.queuedAt)
        assertEquals(original.clientEventId, completed.clientEventId)
        assertEquals(2, completed.attempts)
        assertEquals(150_000L, completed.lastAttemptAt)
        assertEquals(151_000L, completed.completedAt)
        box.clear()
        assertNull(box.observe().first())
    }

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
