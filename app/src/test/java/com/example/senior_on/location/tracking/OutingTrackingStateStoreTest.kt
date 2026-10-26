package com.example.senior_on.location.tracking

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class OutingTrackingStateStoreTest {
    private val key = stringPreferencesKey("confirmed_state_v2")

    @Test fun offlineMarkerPersistsAndGenericRecheckDoesNotInventOrEraseIt() = runBlocking {
        fixture { data, store ->
            store.saveHomeLocation(37.0, 127.0)
            store.markNeedsRecheck()
            assertFalse(store.snapshot()!!.offlineObserved)
            store.markNeedsRecheck(offlineObserved = true)
            store.markNeedsRecheck()
            val reopened = OutingTrackingStateStore(data, { "parent" }, { "cipher:$it" }, { it.removePrefix("cipher:") })
            assertTrue(reopened.snapshot()!!.offlineObserved)
        }
    }

    private suspend fun fixture(test: suspend (DataStore<Preferences>, OutingTrackingStateStore) -> Unit) {
        // File migration and Android Keystore are covered by the instrumented test.
        val data = object : DataStore<Preferences> {
            override val data = MutableStateFlow<Preferences>(emptyPreferences())
            private val mutex = Mutex()
            override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences = mutex.withLock {
                transform(data.value).also { data.value = it }
            }
        }
        val store = OutingTrackingStateStore(data, { "parent" }, { "cipher:$it" }, { it.removePrefix("cipher:") })
        test(data, store)
    }

    @Test fun preservesPendingAndAllSnapshotFields() = runBlocking {
        fixture { data, store ->
            assertTrue(store.saveHomeLocation(37.0, 127.0))
            val old = store.snapshot()!!
            val pending = PendingOutingEvent(state = OutingTrackingState.Outing,
                detectionType = OutingDetectionType.RECONNECTION, confirmedAt = 123, latitude = 38.0, longitude = 128.0)
            assertTrue(store.update(old, old.copy(state = OutingTrackingState.Outing,
                deliveredState = OutingTrackingState.Home, pending = pending, sentAt = 100,
                confirmedAt = 123, latitude = 38.0, longitude = 128.0, returnCandidateAt = 150)))
            val saved = store.snapshot()!!
            val reopened = OutingTrackingStateStore(data, { "parent" }, { "cipher:$it" }, { it.removePrefix("cipher:") })
            assertEquals(saved, reopened.snapshot())
            assertEquals(pending, saved.pending)
            // Reopening the store and rechecking after a gap must not regenerate metadata.
            reopened.markNeedsRecheck()
            val restored = reopened.snapshot()!!
            assertTrue(reopened.update(restored, applyOutingObservation(restored,
                OutingTrackingState.Outing, 39.0, 129.0, 999)))
            val retry = reopened.snapshot()!!.pending!!
            assertEquals(pending.id, retry.id)
            assertEquals(pending.confirmedAt, retry.confirmedAt)
            assertEquals(pending.detectedAtText(), retry.detectedAtText())
            assertTrue(data.data.first()[key]!!.startsWith("cipher:"))
        }
    }

    @Test fun rejectsStaleUpdateAndDoesNotResurrectClearedState() = runBlocking {
        fixture { _, store ->
            store.saveHomeLocation(37.0, 127.0)
            val old = store.snapshot()!!
            assertTrue(store.update(old, old.copy(needsRecheck = false)))
            assertFalse(store.update(old, old.copy(sentAt = 10)))
            store.clearAndAwait()
            assertFalse(store.update(old, old))
            assertNull(store.snapshot())
        }
    }

    @Test fun sameHomeDoesNotErasePendingState() = runBlocking {
        fixture { _, store ->
            store.saveHomeLocation(37.0, 127.0)
            val old = store.snapshot()!!
            store.update(old, old.copy(state = OutingTrackingState.Outing, sentAt = 50))
            val saved = store.snapshot()
            store.saveHomeLocation(37.0, 127.0)
            assertEquals(saved, store.snapshot())
        }
    }

    @Test fun accountMismatchCannotReadOrOverwriteOldRevision() = runBlocking {
        fixture { data, store ->
            store.saveHomeLocation(37.0, 127.0)
            val old = store.snapshot()!!
            val other = OutingTrackingStateStore(data, { "other" }, { "cipher:$it" }, { it.removePrefix("cipher:") })
            assertNull(other.snapshot())
            assertFalse(other.update(old, old))
            assertEquals(old, store.snapshot())
        }
    }

    @Test fun failedDecryptionDoesNotOverwriteOriginal() = runBlocking {
        fixture { data, _ ->
            data.edit { it[key] = "existing-ciphertext" }
            val broken = OutingTrackingStateStore(data, { "parent" }, { it }, { throw IOException("locked key") })
            try {
                broken.saveHomeLocation(37.0, 127.0)
                fail("Expected decryption failure")
            } catch (_: IOException) { }
            assertEquals("existing-ciphertext", data.data.first()[key])
        }
    }
}
