package com.example.senior_on.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Only an invalidation signal. Account-specific information must be fetched from the server. */
object SeniorWithdrawalEventStore {
    private val revision = MutableStateFlow(0L)
    val changes = revision.asStateFlow()
    fun publish(data: Map<String, String>) {
        if (data["type"] == "SENIOR_WITHDRAWAL") revision.update { it + 1 }
    }
}
