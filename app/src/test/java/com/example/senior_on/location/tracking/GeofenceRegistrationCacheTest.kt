package com.example.senior_on.location.tracking

import org.junit.Assert.*
import org.junit.Test

class GeofenceRegistrationCacheTest {
    private val key = GeofenceRegistrationKey("parent:1", StoredHomeLocation(37.0, 127.0), 150f)

    @Test fun onlySuccessfulUnchangedRegistrationIsReused() {
        val cache = GeofenceRegistrationCache()
        assertFalse(cache.matches(key))
        cache.registered(key, cache.generation())
        assertTrue(cache.matches(key))
        assertFalse(cache.matches(key.copy(account = "parent:2")))
        assertFalse(cache.matches(key.copy(radius = 1000f)))
        assertFalse(cache.matches(key.copy(home = StoredHomeLocation(38.0, 127.0))))
    }

    @Test fun invalidationRequiresRegistrationAndRejectsLateSuccess() {
        val cache = GeofenceRegistrationCache()
        val old = cache.generation()
        cache.registered(key, old)
        cache.invalidate()
        cache.registered(key, old)
        assertFalse(cache.matches(key))
        cache.registered(key, cache.generation())
        assertTrue(cache.matches(key))
        assertFalse(GeofenceRegistrationCache().matches(key))
    }
}
