package com.example.senior_on.device

import java.util.concurrent.TimeUnit

internal object ParentInactivityPolicy {
    fun shouldCreateEvent(
        state: ParentInactivityState,
        nowMillis: Long,
        thresholdOverrideMillis: Long? = null,
    ): Boolean {
        val lastActiveAt = state.lastActiveAtMillis ?: return false
        if (!state.enabled) return false
        if (state.alertedForActivityAtMillis == lastActiveAt) return false

        val thresholdMillis = thresholdOverrideMillis ?: TimeUnit.HOURS.toMillis(state.thresholdHours.toLong())
        return nowMillis - lastActiveAt >= thresholdMillis
    }
}
