package com.example.senior_on.device

import java.util.concurrent.TimeUnit

internal object ParentInactivityPolicy {
    fun shouldCreateEvent(
        state: ParentInactivityState,
        nowMillis: Long,
    ): Boolean {
        val lastActiveAt = state.lastActiveAtMillis ?: return false
        if (!state.enabled) return false
        if (state.alertedForActivityAtMillis == lastActiveAt) return false

        val thresholdMillis = TimeUnit.HOURS.toMillis(state.thresholdHours.toLong())
        return nowMillis - lastActiveAt >= thresholdMillis
    }
}
