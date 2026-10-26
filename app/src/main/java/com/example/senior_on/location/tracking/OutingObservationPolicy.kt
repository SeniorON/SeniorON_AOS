package com.example.senior_on.location.tracking

internal fun observedOutingState(distance: Float): OutingTrackingState? = when {
    !distance.isFinite() -> null
    distance >= OutingBoundaryMeters -> OutingTrackingState.Outing
    distance <= ReturnBoundaryMeters -> OutingTrackingState.Home
    else -> null // Hysteresis band: don't invent a transition.
}

internal fun applyOutingObservation(
    old: OutingSnapshot,
    next: OutingTrackingState,
    latitude: Double,
    longitude: Double,
    now: Long,
): OutingSnapshot {
    require(next != OutingTrackingState.Unknown)
    val type = if (old.needsRecheck) OutingDetectionType.RECONNECTION else OutingDetectionType.TRANSITION
    val pending = when {
        old.state == OutingTrackingState.Unknown -> null // First baseline is silent.
        old.pending != null && next == old.deliveredState -> null // Unsent situation has resolved.
        old.state != next -> PendingOutingEvent(
            state = next, detectionType = type, confirmedAt = now,
            latitude = latitude, longitude = longitude,
        )
        // The same event may already be saved remotely. Retry its original identity and
        // observation; only the snapshot below receives the freshly confirmed location/time.
        old.pending?.state == next -> old.pending
        else -> null
    }
    return old.copy(
        state = next, confirmedAt = now, latitude = latitude, longitude = longitude,
        deliveredState = if (old.state == OutingTrackingState.Unknown) next else old.deliveredState,
        needsRecheck = false, returnCandidateAt = null, pending = pending,
    )
}
