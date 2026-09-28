package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.api.SeniorPermissionSettings
import com.example.senior_on.data.remote.api.SeniorPermissionUpdate
import com.example.senior_on.data.remote.dto.*
import com.example.senior_on.data.source.event.EventDataSource
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ParentSharingGuardTest {
    @Test fun locationUploadAndGuardianReadAreBlockedBeforeCallingRemote() = runTest {
        var calls = 0
        val source = java.lang.reflect.Proxy.newProxyInstance(
            com.example.senior_on.data.source.device.DeviceDataSource::class.java.classLoader,
            arrayOf(com.example.senior_on.data.source.device.DeviceDataSource::class.java),
        ) { _, _, _ -> calls++; error("Must not call location API") } as com.example.senior_on.data.source.device.DeviceDataSource
        val repo = DeviceRepositoryImpl(
            source,
            object : com.example.senior_on.data.source.device.DeviceIdentifierDataSource {
                override fun getOrCreateIdentifier() = "test-device"
            },
            object : com.example.senior_on.data.source.device.LocalDeviceStatusDataSource {
                override fun getDeviceName() = "test"
                override fun getBatteryLevel() = 70
                override fun getStatusSnapshot(): com.example.senior_on.data.source.device.LocalDeviceStatusSnapshot = error("unused")
            },
            sharingGuard = guard(false, true),
            permissionsLoader = { SeniorPermissionSettings(it, false, true) },
        )
        assertTrue(runCatching { repo.updateLocation(37.0, 127.0) }.isFailure)
        assertTrue(runCatching { repo.getLatestLocation(1) }.isFailure)
        assertEquals(0, calls)
    }

    @Test fun locationOffStillSendsInactivityWithoutCoordinates() = runTest {
        val source = Events()
        val repo = EventRepositoryImpl(source, guard(false, true))
        repo.createInactivity(37.0, 127.0, 70, "2026-09-28T10:00:00")
        val body = source.requests.single()
        assertNull(body.latitude)
        assertNull(body.longitude)
        val json = Gson().toJsonTree(body).asJsonObject
        assertFalse(json.has("latitude"))
        assertFalse(json.has("longitude"))
        assertEquals(70, body.deviceBattery)
    }

    @Test fun independentPermissionsBlockOnlyCorrespondingEvent() = runTest {
        val source = Events()
        val locationOff = EventRepositoryImpl(source, guard(false, true))
        assertTrue(runCatching { locationOff.createOutingReturn("OUTING", 37.0, 127.0, 70) }.isFailure)
        val inactivityOff = EventRepositoryImpl(source, guard(true, false))
        assertTrue(runCatching { inactivityOff.createInactivity(37.0, 127.0, 70, "today") }.isFailure)
        inactivityOff.createOutingReturn("OUTING", 37.0, 127.0, 70)
        assertEquals(1, source.outings)
        assertTrue(source.requests.isEmpty())
    }

    @Test fun lookupFailureDoesNotUsePreviouslyAllowedState() = runTest {
        var fail = false
        val guard = ParentSharingGuard(
            load = { if (fail) error("offline") else SeniorPermissionSettings(1, true, true) },
            save = { error("unused") },
        )
        val source = Events()
        val repo = EventRepositoryImpl(source, guard)
        repo.createInactivity(37.0, 127.0, 70, "today")
        assertEquals(37.0, source.requests.single().latitude)
        fail = true
        assertTrue(runCatching { repo.createInactivity(37.0, 127.0, 70, "today") }.isFailure)
        assertEquals(1, source.requests.size)
        assertNull(guard.state.value)
    }

    @Test fun sessionChangeDuringLookupPreventsSending() = runTest {
        var session = "a"
        val guard = ParentSharingGuard(
            load = { session = "b"; SeniorPermissionSettings(1, true, true) },
            save = { error("unused") }, sessionKey = { session },
        )
        var sent = false
        assertTrue(runCatching { guard.withFreshPermissions { sent = true } }.isFailure)
        assertFalse(sent)
    }

    @Test fun revocationIsSerializedWithSendingAndNextSendUsesNewState() = runTest {
        var permissions = SeniorPermissionSettings(1, true, true)
        val guard = ParentSharingGuard(load = { permissions }, save = {
            permissions = permissions.copy(locationEnabled = it.locationEnabled ?: permissions.locationEnabled)
            permissions
        })
        val started = CompletableDeferred<Unit>()
        val finish = CompletableDeferred<Unit>()
        val send = launch { guard.withFreshPermissions { started.complete(Unit); finish.await() } }
        started.await()
        val revoke = async { guard.update(SeniorPermissionUpdate(locationEnabled = false)) }
        yield()
        assertFalse(revoke.isCompleted)
        finish.complete(Unit)
        send.join()
        revoke.await()
        assertFalse(guard.refresh().locationEnabled)
    }

    private fun guard(location: Boolean, inactivity: Boolean) = ParentSharingGuard(
        load = { SeniorPermissionSettings(1, location, inactivity) }, save = { error("unused") },
    )

    private class Events : EventDataSource {
        val requests = mutableListOf<InactivityRequest>()
        var outings = 0
        override suspend fun createInactivity(request: InactivityRequest): InactivityResponse {
            requests += request
            return InactivityResponse(null, request.latitude, request.longitude, request.lastSeenAt)
        }
        override suspend fun createOutingReturn(request: OutingReturnRequest): OutingReturnResponse {
            outings++
            return OutingReturnResponse(1, request.phase, null, request.latitude, request.longitude, null, request.deviceBattery)
        }
        override suspend fun createSos(request: SosEventRequest): SosEventResponse = error("unused")
        override suspend fun createRiskLink(request: RiskLinkRequest): RiskLinkResponse = error("unused")
        override suspend fun getDetail(eventId: Long): EventDetailResponse = error("unused")
    }
}
