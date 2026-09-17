package com.example.senior_on.ui.parent.home.viewmodel

import androidx.lifecycle.ViewModelStore
import com.example.senior_on.domain.model.server.*
import com.example.senior_on.domain.repository.parent.*
import com.example.senior_on.domain.repository.server.HomeServerRepository
import java.io.IOException
import java.lang.reflect.Proxy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParentHomeRealtimeTest {
    @Test fun burstOnlyRefreshesHomeAndPreservesScreenWhileLoading() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val repo = FakeHome()
            val updates = FakeUpdates()
            val vm = ParentHomeViewModel(repo, updates)
            store.put("home", vm)
            backgroundScope.launch { vm.observeHomeUpdates() }
            advanceUntilIdle()
            assertEquals(1, repo.homeCalls)
            assertEquals(1, repo.weatherCalls)
            val initialButtons = vm.uiState.value.buttons
            val waiting = CompletableDeferred<SeniorHomeSnapshot>()
            repo.next = { waiting.await() }
            repeat(10) { updates.events.emit(ParentHomeUpdateEvent.HomeUpdated) }
            runCurrent()
            advanceTimeBy(150)
            runCurrent()
            assertEquals(2, repo.homeCalls)
            assertEquals(initialButtons, vm.uiState.value.buttons)
            assertFalse(vm.uiState.value.isLoading)
            assertFalse(vm.uiState.value.isRefreshing)

            // The event arriving DURING the request must schedule one trailing fetch.
            updates.events.emit(ParentHomeUpdateEvent.HomeUpdated)
            runCurrent()
            repo.next = { snapshot("latest") }
            waiting.complete(snapshot("intermediate"))
            advanceUntilIdle()
            assertEquals(3, repo.homeCalls)
            assertEquals("latest", vm.uiState.value.buttons.single().label)
            assertEquals(1, repo.weatherCalls)
            assertEquals(1, repo.scheduleCalls)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test fun reconnectRefreshesAndFailureKeepsExistingButtons() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val repo = FakeHome()
            val updates = FakeUpdates()
            val vm = ParentHomeViewModel(repo, updates)
            store.put("home", vm)
            val observation = backgroundScope.launch { vm.observeHomeUpdates() }
            advanceUntilIdle()
            val before = vm.uiState.value.buttons
            repo.next = { throw IOException("offline") }
            updates.events.emit(ParentHomeUpdateEvent.Subscribed)
            runCurrent()
            advanceUntilIdle()
            assertEquals(before, vm.uiState.value.buttons)
            assertEquals(2, repo.homeCalls)
            observation.cancelAndJoin()
            repo.next = { snapshot("after resume") }
            backgroundScope.launch { vm.observeHomeUpdates() }
            runCurrent()
            advanceUntilIdle()
            assertEquals("after resume", vm.uiState.value.buttons.single().label)
            assertEquals(2, repo.weatherCalls)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    private class FakeUpdates : ParentHomeUpdatesRepository {
        val events = MutableSharedFlow<ParentHomeUpdateEvent>(extraBufferCapacity = 32)
        override fun observeUpdates() = events
    }

    @Test fun scheduleEventRefreshesOnlyScheduleCard() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val repo = FakeHome()
            val events = FakeUpdates()
            val vm = ParentHomeViewModel(repo, events).also { store.put("home", it) }
            backgroundScope.launch { vm.observeHomeUpdates() }
            runCurrent(); advanceUntilIdle()
            repeat(5) { events.events.emit(ParentHomeUpdateEvent.ScheduleUpdated) }
            runCurrent(); advanceUntilIdle()
            assertEquals(2, repo.scheduleCalls)
            assertEquals(1, repo.homeCalls)
            assertEquals(1, repo.weatherCalls)
            events.events.emit(ParentHomeUpdateEvent.MedicationUpdated)
            runCurrent(); advanceUntilIdle()
            assertEquals(2, repo.scheduleCalls)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    private class FakeHome : HomeServerRepository by unusedRepository() {
        var homeCalls = 0
        var weatherCalls = 0
        var scheduleCalls = 0
        var next: suspend () -> SeniorHomeSnapshot = { snapshot("initial") }
        override suspend fun getSeniorHome(): SeniorHomeSnapshot { homeCalls++; return next() }
        override suspend fun getTodayHospitalSchedules(): List<TodayHospitalSchedule> { scheduleCalls++; return emptyList() }
        override suspend fun getWeather(latitude: Double, longitude: Double): WeatherInfo {
            weatherCalls++
            return WeatherInfo(20, "CLEAR", "맑음", null)
        }
    }

    companion object {
        private fun snapshot(label: String) = SeniorHomeSnapshot(
            buttons = listOf(ServerButton(1, order = 0, name = label, icon = null, actionType = "DEFAULT", actionValue = "PHONE")),
            fontSize = "MEDIUM", musicCard = null, todaySchedule = null,
        )
        private fun unusedRepository() = Proxy.newProxyInstance(
            HomeServerRepository::class.java.classLoader, arrayOf(HomeServerRepository::class.java),
        ) { _, method, _ -> error("Unexpected call: ${method.name}") } as HomeServerRepository
    }
}
