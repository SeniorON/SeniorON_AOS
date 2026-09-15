package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.data.remote.websocket.ParentHomeStompSource
import com.example.senior_on.data.source.auth.SessionSnapshotStore
import com.example.senior_on.domain.repository.parent.ParentHomeUpdateEvent
import com.example.senior_on.domain.repository.parent.ParentHomeUpdatesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellationException

class ParentHomeUpdatesRepositoryImpl(
    private val sessionStore: SessionSnapshotStore,
    private val source: ParentHomeStompSource,
    private val currentUsersId: suspend () -> Long?,
    private val log: (String) -> Unit = {},
) : ParentHomeUpdatesRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeUpdates(): Flow<ParentHomeUpdateEvent> = sessionStore.observeSession()
        .map { session -> session?.takeIf { it.role == UserRole.PARENT } }
        .distinctUntilChanged()
        .flatMapLatest { session ->
            if (session == null) flowOf(ParentHomeUpdateEvent.Disconnected)
            else flow {
                var usersId = session.usersId?.takeIf { it > 0 }
                // Legacy userId is a login name (even if all digits), never a server ID.
                while (usersId == null) {
                    emit(ParentHomeUpdateEvent.Disconnected)
                    usersId = try {
                        currentUsersId()?.takeIf { it > 0 }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        null
                    }
                    // Do not restore a logged-out or replaced account after a late response.
                    if (sessionStore.getSession() != session) return@flow
                    if (usersId == null) {
                        log("Server user ID unavailable; retry in 30s")
                        delay(30_000)
                    } else {
                        log("Legacy session server user ID restored")
                        sessionStore.saveSession(session.copy(usersId = usersId))
                        // The updated session emission starts the connection.
                        return@flow
                    }
                }
                emitAll(source.observe(usersId))
            }
        }
}
