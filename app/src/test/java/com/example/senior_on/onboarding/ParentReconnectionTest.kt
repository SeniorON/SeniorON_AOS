package com.example.senior_on.onboarding

import com.example.senior_on.data.local.ParentConnectionGate
import com.example.senior_on.data.remote.api.DeviceApi
import com.example.senior_on.data.remote.api.DeviceReconnectionStatus
import com.example.senior_on.data.repository.impl.ParentReconnectionRepository
import com.example.senior_on.data.repository.impl.ParentConnectionDestination
import com.example.senior_on.domain.model.device.DeviceRegistration
import com.example.senior_on.domain.repository.auth.AuthRepository
import com.example.senior_on.domain.repository.auth.SessionRepository
import com.example.senior_on.domain.repository.device.DeviceRegistrationRepository
import com.example.senior_on.ui.onboarding.viewmodel.ParentReconnectionViewModel
import com.example.senior_on.ui.child.settings.viewmodel.SettingsViewModel
import java.lang.reflect.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import retrofit2.Response

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ParentReconnectionTest {
    @Before fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()); ParentConnectionGate.beginSession(true) }
    @After fun tearDown() { Dispatchers.resetMain(); ParentConnectionGate.initialize(false) }

    @Test fun allServerBranchesAndMissingFields() = runTest {
        for ((status, expected) in listOf(
            DeviceReconnectionStatus(false, false) to ParentConnectionDestination.FirstConnection,
            DeviceReconnectionStatus(false, true) to ParentConnectionDestination.FirstConnection,
            DeviceReconnectionStatus(true, true) to ParentConnectionDestination.Reconnect,
            DeviceReconnectionStatus(true, false) to ParentConnectionDestination.Home,
        )) {
            val repository = ParentReconnectionRepository(stub<DeviceApi> { status })
            assertEquals(expected, repository.destination())
        }
        assertTrue(runCatching { ParentReconnectionRepository(stub<DeviceApi> { DeviceReconnectionStatus(null, null) }).destination() }.isFailure)
    }

    @Test fun reconnectionKeepsWritesBlockedUntilPatchAndVerificationSucceed() = runTest {
        val calls = mutableListOf<String>()
        var disconnected = true
        val repository = ParentReconnectionRepository(stub<DeviceApi> { name ->
            calls += name
            assertFalse(ParentConnectionGate.isReady())
            when (name) {
                "getReconnection" -> DeviceReconnectionStatus(true, disconnected)
                "reconnect" -> { disconnected = false; Response.success<Unit>(204, null) }
                else -> error(name)
            }
        })
        val vm = model(repository)
        vm.check({ assertEquals(ParentConnectionDestination.Reconnect, it) }, { fail(it) })
        assertFalse(ParentConnectionGate.isReady())
        var entered = false
        vm.confirm { entered = true }
        assertTrue(entered)
        assertTrue(ParentConnectionGate.isReady())
        assertEquals(listOf("getReconnection", "reconnect", "getReconnection"), calls)
    }

    @Test fun queryOrPatchFailureNeverEntersHome() = runTest {
        val vm = model(ParentReconnectionRepository(stub<DeviceApi> { error("network") }))
        vm.check({ fail("Must not enter") }, {})
        assertNotNull(vm.state.value.error)
        vm.confirm { fail("Must not enter") }
        assertNotNull(vm.state.value.error)
        assertFalse(ParentConnectionGate.isReady())
    }

    @Test fun firstConnectionDoesNotOpenGateAndConnectedAccountDoes() = runTest {
        var family = false
        val vm = model(ParentReconnectionRepository(stub<DeviceApi> { DeviceReconnectionStatus(family, false) }))
        vm.check({ assertEquals(ParentConnectionDestination.FirstConnection, it) }, { fail(it) })
        assertFalse(ParentConnectionGate.isReady())
        family = true
        vm.check({ assertEquals(ParentConnectionDestination.Home, it) }, { fail(it) })
        assertTrue(ParentConnectionGate.isReady())
    }

    @Test fun disconnectedResponseAfterPatchKeepsGateClosed() = runTest {
        val vm = model(ParentReconnectionRepository(stub<DeviceApi> {
            if (it == "reconnect") Response.success<Unit>(204, null) else DeviceReconnectionStatus(true, true)
        }))
        vm.confirm { fail("Server still reports disconnected") }
        assertFalse(ParentConnectionGate.isReady())
        assertNotNull(vm.state.value.error)
    }

    @Test fun endpointsAreAuthenticatedSelfOperationsWithoutSeniorId() {
        val read = DeviceApi::class.java.methods.single { it.name == "getReconnection" }
        val patch = DeviceApi::class.java.methods.single { it.name == "reconnect" }
        assertEquals("api/devices/reconnection", read.getAnnotation(retrofit2.http.GET::class.java)?.value)
        assertEquals("api/devices/reconnection", patch.getAnnotation(retrofit2.http.PATCH::class.java)?.value)
        // Only Kotlin's suspend continuation, no path/query/body arguments.
        assertEquals(1, read.parameterCount)
        assertEquals(1, patch.parameterCount)
    }

    @Test fun cancellationClearsSessionEvenWhenServerLogoutFails() = runTest {
        var cleared = false
        var returned = false
        val vm = model(ParentReconnectionRepository(stub<DeviceApi> { error("No reconnect on cancel") }),
            sessions = stub { cleared = true; Unit }, auth = stub { error("logout unavailable") })
        vm.cancel { returned = true }
        assertTrue(cleared)
        assertTrue(returned)
        assertFalse(ParentConnectionGate.isReady())
    }

    @Test fun disconnectedSettingsForceLocalLogoutOnFailure() = runTest {
        var cleared = false
        val vm = SettingsViewModel(stub { error("logout unavailable") }, stub { cleared = true; Unit }, devices())
        vm.logout(clearLocalOnFailure = true)
        assertTrue(cleared)
        assertTrue(vm.uiState.value.logoutCompleted)
    }

    @Test fun normalLogoutFailureKeepsExistingSession() = runTest {
        var cleared = false
        val vm = SettingsViewModel(stub { error("logout unavailable") }, stub { cleared = true; Unit }, devices())
        vm.logout()
        assertFalse(cleared)
        assertFalse(vm.uiState.value.logoutCompleted)
    }

    @Test fun staleBackgroundApprovalCannotOverrideNewLoginOrLogout() {
        ParentConnectionGate.initialize(true)
        val oldVersion = requireNotNull(ParentConnectionGate.backgroundCheckVersion())
        ParentConnectionGate.beginSession(true)
        assertFalse(ParentConnectionGate.approve(oldVersion))
        assertNull(ParentConnectionGate.backgroundCheckVersion())
        assertTrue(ParentConnectionGate.blocks("PUT", "/api/devices/status"))
        assertTrue(ParentConnectionGate.blocks("PATCH", "/api/devices/location"))
        assertTrue(ParentConnectionGate.blocks("POST", "/api/event/sos"))
        assertFalse(ParentConnectionGate.blocks("PATCH", "/api/devices/reconnection"))
        assertFalse(ParentConnectionGate.blocks("POST", "/api/users/logout"))
        val version = ParentConnectionGate.hold()
        ParentConnectionGate.hold()
        assertFalse(ParentConnectionGate.approve(version))
        ParentConnectionGate.beginSession(false)
        assertFalse(ParentConnectionGate.blocks("PATCH", "/api/devices/fcm-token"))
    }

    private fun model(repository: ParentReconnectionRepository,
        sessions: SessionRepository = stub { Unit }, auth: AuthRepository = stub { Unit }) =
        ParentReconnectionViewModel(repository, auth, sessions, devices())

    private fun devices(): DeviceRegistrationRepository = stub { DeviceRegistration(fcmToken = "fcm", deviceIdentifier = "device") }

    private inline fun <reified T> stub(crossinline call: (String) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, _ -> call(method.name) } as T
}
