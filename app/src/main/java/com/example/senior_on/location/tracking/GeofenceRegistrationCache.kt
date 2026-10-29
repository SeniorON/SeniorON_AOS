package com.example.senior_on.location.tracking

internal data class GeofenceRegistrationKey(val account: String, val home: StoredHomeLocation, val radius: Float)

/** Process-local success only: process restart conservatively registers once again. */
internal class GeofenceRegistrationCache {
    private var key: GeofenceRegistrationKey? = null
    private var version = 0L
    @Synchronized fun matches(candidate: GeofenceRegistrationKey) = key == candidate
    @Synchronized fun generation() = version
    @Synchronized fun invalidate() { key = null; version++ }
    @Synchronized fun registered(candidate: GeofenceRegistrationKey, expectedVersion: Long) {
        if (version == expectedVersion) key = candidate
    }
}
