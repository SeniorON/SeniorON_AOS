package com.example.senior_on.device

/** Only user input records activity. Entry establishes a missing baseline, never refreshes it. */
internal class ParentActivityRecorder(
    private val isLoggedInParent: () -> Boolean,
    private val isDeviceConnectionReady: () -> Boolean,
    private val initializeIfMissing: () -> Unit,
    private val recordActivity: () -> Unit,
) {
    private var familyConnected = false

    fun onFamilyConnectionChanged(connected: Boolean) {
        familyConnected = connected
        if (eligible()) initializeIfMissing()
    }

    fun onUserInteraction() {
        if (eligible()) recordActivity()
    }

    private fun eligible(): Boolean =
        familyConnected && isLoggedInParent() && isDeviceConnectionReady()
}
