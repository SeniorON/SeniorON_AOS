package com.example.senior_on.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.example.senior_on.domain.model.server.ServerButton
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ParentHomeLayoutCacheTest {
    @get:Rule val folder = TemporaryFolder(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir)

    @Test fun restoredLayoutAndLateWritesAreBoundToTheCapturedLogin() = runBlocking {
        val file = File(folder.root, "layout.preferences_pb")
        val layout = CachedHomeLayout("LARGE", listOf(
            ServerButton(Long.MAX_VALUE, 42L, 1, "전화", null, "CALL", "123", null),
            ServerButton(2L, null, 2, "App", "icon", null, null, "example.app"),
        ))
        var scope: String? = "login-1"
        val firstJob = SupervisorJob()
        try {
            val first = PreferenceDataStoreFactory.create(scope = CoroutineScope(firstJob + Dispatchers.IO)) { file }
            ScopedHomeLayoutCache(first) { scope }.write(layout)
        } finally {
            firstJob.cancelAndJoin()
        }

        val job = SupervisorJob()
        try {
            val store = PreferenceDataStoreFactory.create(scope = CoroutineScope(job + Dispatchers.IO)) { file }
            val old = ScopedHomeLayoutCache(store) { scope }
            assertEquals(layout, old.read())
            scope = null
            val loggedOut = ScopedHomeLayoutCache(store) { scope }
            assertNull(old.read())
            scope = "login-2"
            val current = ScopedHomeLayoutCache(store) { scope }
            assertNull(current.read())
            val empty = CachedHomeLayout("SMALL", emptyList())
            current.write(empty)
            old.write(layout)
            loggedOut.write(layout)
            assertEquals(empty, current.read())
            assertNull(old.read())
        } finally {
            job.cancelAndJoin()
        }
    }
}
