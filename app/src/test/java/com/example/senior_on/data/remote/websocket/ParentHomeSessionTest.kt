package com.example.senior_on.data.remote.websocket

import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.data.repository.impl.ParentHomeUpdatesRepositoryImpl
import com.example.senior_on.data.source.auth.SavedSession
import com.example.senior_on.data.source.auth.SessionSnapshotStore
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParentHomeSessionTest {
    private class Store(session: SavedSession?) : SessionSnapshotStore {
        val state = MutableStateFlow(session)
        override fun getSession() = state.value
        override fun observeSession() = state
        override fun saveSession(session: SavedSession) { state.value = session }
    }

    @Test fun textLoginUsesExplicitServerId() = runTest {
        val store = Store(SavedSession(UserRole.PARENT, "seniorLogin", 42))
        val factory = ParentHomeStompSourceTest.FakeFactory()
        val source = ParentHomeStompSource(factory, "https://example.test/ws") { "Bearer token" }
        val repository = ParentHomeUpdatesRepositoryImpl(store, source, { error("No migration needed") })
        backgroundScope.launch { repository.observeUpdates().collect() }
        runCurrent()
        factory.sockets.single().connect()
        runCurrent()
        assertTrue(factory.sockets.single().sent.last().contains("/topic/senior/42/home"))
    }

    @Test fun legacyNumericLoginIsNotMistakenForServerId() = runTest {
        val store = Store(SavedSession(UserRole.PARENT, "123456"))
        val factory = ParentHomeStompSourceTest.FakeFactory()
        val source = ParentHomeStompSource(factory, "https://example.test/ws") { "Bearer token" }
        val repository = ParentHomeUpdatesRepositoryImpl(store, source, { 42L })
        backgroundScope.launch { repository.observeUpdates().collect() }
        runCurrent()
        assertEquals(42L, store.getSession()?.usersId)
        factory.sockets.single().connect()
        runCurrent()
        assertTrue(factory.sockets.single().sent.last().contains("/topic/senior/42/home"))
    }

    @Test fun failedLegacyLookupRetriesWithoutDiscardingSession() = runTest {
        val original = SavedSession(UserRole.PARENT, "seniorLogin")
        val store = Store(original)
        val factory = ParentHomeStompSourceTest.FakeFactory()
        val source = ParentHomeStompSource(factory, "https://example.test/ws") { "Bearer token" }
        var attempts = 0
        val repository = ParentHomeUpdatesRepositoryImpl(store, source, {
            if (++attempts == 1) throw IOException()
            42L
        })
        backgroundScope.launch { repository.observeUpdates().collect() }
        runCurrent()
        assertEquals(original, store.getSession())
        assertTrue(factory.sockets.isEmpty())
        advanceTimeBy(30_000)
        runCurrent()
        assertEquals(1, factory.sockets.size)
    }

    @Test fun logoutDuringLookupDoesNotRestoreSessionOrConnect() = runTest {
        val store = Store(SavedSession(UserRole.PARENT, "seniorLogin"))
        val response = CompletableDeferred<Long?>()
        val factory = ParentHomeStompSourceTest.FakeFactory()
        val source = ParentHomeStompSource(factory, "https://example.test/ws") { "Bearer token" }
        val repository = ParentHomeUpdatesRepositoryImpl(store, source, { response.await() })
        backgroundScope.launch { repository.observeUpdates().collect() }
        runCurrent()
        store.state.value = null
        response.complete(42)
        runCurrent()
        assertNull(store.getSession())
        assertTrue(factory.sockets.isEmpty())
    }
}
