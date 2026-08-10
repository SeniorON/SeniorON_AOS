package com.example.senior_on.ui.parent.launcher.viewmodel

import com.example.senior_on.domain.model.server.DeviceLocation
import com.example.senior_on.domain.model.server.SeniorHomeLocation
import com.example.senior_on.domain.repository.server.DeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParentDeviceStatusViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun disconnectedStatusStopsPollingAndRequestsHomeRefresh() = runTest(dispatcher) {
        val repository = DisconnectedDeviceRepository()
        val viewModel = ParentDeviceStatusViewModel(
            repository = repository,
            statusUpdateIntervalMillis = 1L,
        )

        viewModel.startForegroundUpdates()
        advanceUntilIdle()

        assertTrue(viewModel.isDeviceDisconnected.value)
        assertTrue(repository.updateCount == 1)
    }

    private class DisconnectedDeviceRepository : DeviceRepository {
        var updateCount = 0

        override suspend fun updateStatus(): Boolean {
            updateCount += 1
            return false
        }

        override suspend fun updateFcmToken(token: String) = Unit
        override suspend fun disconnect() = Unit
        override suspend fun getLatestLocation(): DeviceLocation =
            error("Not used in this test")
        override suspend fun updateLocation(latitude: Double, longitude: Double) = Unit
        override suspend fun getHomeLocation(): SeniorHomeLocation =
            error("Not used in this test")
        override fun getBatteryLevel(): Int = 100
    }
}
