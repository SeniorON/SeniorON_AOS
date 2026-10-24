package com.example.senior_on.location.tracking

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.senior_on.data.local.KeystoreTokenCipher
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class OutingMigrationTest {
    @Test fun migratesCiphertextAndPreservesPendingSnapshot() = runBlocking { exercise(false) }
    @Test fun failedMigrationKeepsOriginalPreferences() = runBlocking { exercise(true) }

    private suspend fun exercise(failMigration: Boolean) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "outing_migration_test_${UUID.randomUUID()}"
        val legacy = context.getSharedPreferences(name, Context.MODE_PRIVATE)
        val file = context.cacheDir.resolve("$name.preferences_pb")
        val key = stringPreferencesKey("confirmed_state_v2")
        val cipher = KeystoreTokenCipher()
        val original = OutingSnapshot(account = "parent", home = StoredHomeLocation(37.0, 127.0),
            state = OutingTrackingState.Outing, deliveredState = OutingTrackingState.Home,
            pending = PendingOutingEvent(state = OutingTrackingState.Outing,
                detectionType = OutingDetectionType.RECONNECTION, confirmedAt = 123,
                latitude = 38.0, longitude = 128.0))
        val encrypted = cipher.encrypt(Gson().toJson(original))
        assertTrue(legacy.edit().putString(key.name, encrypted).commit())
        val migrations = mutableListOf<DataMigration<Preferences>>(
            SharedPreferencesMigration(context, name, keysToMigrate = setOf(key.name)))
        if (failMigration) migrations += object : DataMigration<Preferences> {
            override suspend fun shouldMigrate(currentData: Preferences) = true
            override suspend fun migrate(currentData: Preferences): Preferences = throw IOException("test failure")
            override suspend fun cleanUp() = Unit
        }
        val job = SupervisorJob()
        try {
            val data = PreferenceDataStoreFactory.create(migrations = migrations,
                scope = CoroutineScope(job + Dispatchers.IO), produceFile = { file })
            if (failMigration) {
                try { data.data.first(); fail("Expected migration failure") } catch (_: IOException) { }
                assertEquals(encrypted, legacy.getString(key.name, null))
            } else {
                assertEquals(encrypted, data.data.first()[key])
                assertFalse(legacy.contains(key.name))
                val store = OutingTrackingStateStore(data, { "parent" }, cipher::encrypt, cipher::decrypt)
                assertEquals(original, store.snapshot())
                store.markNeedsRecheck()
                assertEquals(original.pending, store.snapshot()?.pending)
            }
        } finally {
            job.cancelAndJoin()
            context.deleteSharedPreferences(name)
            file.delete()
        }
    }
}
