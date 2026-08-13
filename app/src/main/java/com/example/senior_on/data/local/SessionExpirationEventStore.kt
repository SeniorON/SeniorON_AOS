package com.example.senior_on.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Delivers an authentication-expiration signal from OkHttp's synchronous
 * authenticator to the currently active application root.
 *
 * A StateFlow is used so an event raised while the app is moving between
 * lifecycle states remains pending until a root screen consumes it.
 */
object SessionExpirationEventStore {
    private val mutablePendingEvent = MutableStateFlow<Long?>(null)
    val pendingEvent = mutablePendingEvent.asStateFlow()

    @Synchronized
    fun publish() {
        mutablePendingEvent.value = System.nanoTime()
    }

    @Synchronized
    fun consume() {
        mutablePendingEvent.value = null
    }
}
