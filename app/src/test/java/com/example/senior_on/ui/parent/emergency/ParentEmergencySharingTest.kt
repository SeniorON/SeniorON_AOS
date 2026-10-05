package com.example.senior_on.ui.parent.emergency

import com.example.senior_on.data.remote.api.SeniorPermissionSettings
import com.example.senior_on.data.repository.impl.ParentSharingGuard
import com.example.senior_on.domain.model.location.GeoLocation
import com.example.senior_on.domain.model.server.SafetyEvent
import com.example.senior_on.domain.repository.location.LocationRepository
import com.example.senior_on.domain.repository.server.DeviceRepository
import com.example.senior_on.domain.repository.server.EventRepository
import com.example.senior_on.ui.parent.emergency.viewmodel.ParentEmergencyAlertViewModel
import com.example.senior_on.ui.parent.emergency.viewmodel.ParentEmergencyAlertStatus
import java.lang.reflect.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParentEmergencySharingTest {
    @Test fun offSkipsLocationAndOnIncludesCoordinates() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            for (enabled in listOf(false, true)) {
                var locations = 0
                var sent: List<Any?>? = null
                val events = stub<EventRepository> { name, args ->
                    check(name == "createSos")
                    sent = args.take(3)
                    SafetyEvent(1L, "SOS", null, null, null, null, 70)
                }
                val devices = stub<DeviceRepository> { name, _ ->
                    check(name == "getBatteryLevel")
                    70
                }
                val vm = ParentEmergencyAlertViewModel(events, object : LocationRepository {
                    override suspend fun getCurrentLocation(): GeoLocation {
                        locations++
                        return GeoLocation(37.0, 127.0)
                    }
                }, devices, ParentSharingGuard(
                    load = { SeniorPermissionSettings(1L, enabled, true) },
                    save = { error("unused") },
                ))
                vm.sendEmergencyAlert()
                advanceUntilIdle()
                assertEquals(if (enabled) 1 else 0, locations)
                assertEquals(listOf(if (enabled) 37.0 else null, if (enabled) 127.0 else null, 70), sent)
                assertEquals(ParentEmergencyAlertStatus.Sent, vm.uiState.value.status)
                vm.reset()
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    private inline fun <reified T> stub(crossinline call: (String, Array<out Any?>) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, args ->
            call(method.name, args ?: emptyArray())
        } as T
}
