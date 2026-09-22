package com.example.senior_on.ui.parent.realtime

import androidx.lifecycle.ViewModelStore
import com.example.senior_on.domain.repository.auth.AuthRepository
import com.example.senior_on.domain.model.auth.OnboardingStatus
import com.example.senior_on.domain.model.auth.CareManagerType
import com.example.senior_on.core.time.koreaNow
import com.example.senior_on.core.time.koreaToday
import com.example.senior_on.domain.model.server.*
import com.example.senior_on.domain.repository.parent.*
import com.example.senior_on.domain.repository.server.*
import com.example.senior_on.ui.parent.medication.viewmodel.*
import com.example.senior_on.ui.parent.schedule.viewmodel.ParentScheduleViewModel
import java.lang.reflect.Proxy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParentDetailRealtimeTest {
    private val store = ViewModelStore()
    @Before fun setup() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun cleanup() { store.clear(); Dispatchers.resetMain() }

    @Test fun scheduleUpdatesCoalesceAndOtherEventsAreIgnored() = runTest {
        val repo = FakeSchedule()
        val events = Updates()
        val vm = ParentScheduleViewModel(repo, events, FakeAuth()).also { store.put("schedule", it) }
        backgroundScope.launch { vm.observeUpdates() }
        runCurrent(); advanceUntilIdle()
        assertEquals(1, repo.calls)
        events.events.emit(ParentHomeUpdateEvent.MedicationUpdated)
        runCurrent(); advanceUntilIdle()
        assertEquals(1, repo.calls)
        repo.data = emptyList()
        repeat(10) { events.events.emit(ParentHomeUpdateEvent.ScheduleUpdated) }
        runCurrent(); advanceUntilIdle()
        assertEquals(2, repo.calls)
        assertTrue(vm.uiState.value.schedules.isEmpty())
        assertFalse(vm.uiState.value.isRefreshing)
    }

    @Test fun scheduleChangeDuringFetchTriggersTrailingReadAndFailureKeepsData() = runTest {
        val repo = FakeSchedule()
        val events = Updates()
        val vm = ParentScheduleViewModel(repo, events, FakeAuth()).also { store.put("schedule", it) }
        backgroundScope.launch { vm.observeUpdates() }
        runCurrent(); advanceUntilIdle()
        val initial = vm.uiState.value.schedules
        repo.fetch = { error("offline") }
        events.events.emit(ParentHomeUpdateEvent.ScheduleUpdated)
        runCurrent(); advanceUntilIdle()
        assertEquals(initial, vm.uiState.value.schedules)
        val delayed = CompletableDeferred<List<TodayHospitalSchedule>>()
        repo.fetch = { delayed.await() }
        events.events.emit(ParentHomeUpdateEvent.ScheduleUpdated)
        runCurrent(); advanceTimeBy(150); runCurrent()
        events.events.emit(ParentHomeUpdateEvent.ScheduleUpdated)
        runCurrent()
        repo.fetch = { emptyList() }
        delayed.complete(repo.data)
        advanceUntilIdle()
        assertEquals(4, repo.calls)
        assertTrue(vm.uiState.value.schedules.isEmpty())
    }

    @Test fun observationStopsAndResumeAndReconnectReconcile() = runTest {
        val repo = FakeSchedule()
        val events = Updates()
        val vm = ParentScheduleViewModel(repo, events, FakeAuth()).also { store.put("schedule", it) }
        val first = backgroundScope.launch { vm.observeUpdates() }
        runCurrent(); advanceUntilIdle()
        first.cancelAndJoin()
        events.events.emit(ParentHomeUpdateEvent.ScheduleUpdated)
        runCurrent(); advanceUntilIdle()
        assertEquals(1, repo.calls)
        backgroundScope.launch { vm.observeUpdates() }
        runCurrent(); advanceUntilIdle()
        assertEquals(2, repo.calls)
        events.events.emit(ParentHomeUpdateEvent.Subscribed)
        runCurrent(); advanceUntilIdle()
        assertEquals(3, repo.calls)
    }

    @Test fun medicationRefreshPreservesHighlightAndHandlesEmptyAndErrors() = runTest {
        val repo = FakeMedication()
        val events = Updates()
        val vm = ParentMedicationViewModel(repo, events).also { store.put("medication", it) }
        vm.loadMedication(1)
        backgroundScope.launch { vm.observeUpdates() }
        runCurrent(); advanceUntilIdle()
        assertEquals(1, repo.calls)
        repo.data = listOf(dose().copy(name = "변경된 약"))
        repeat(5) { events.events.emit(ParentHomeUpdateEvent.MedicationUpdated) }
        runCurrent(); advanceUntilIdle()
        assertEquals(2, repo.calls)
        assertEquals("변경된 약", vm.uiState.value.medications.single().name)
        assertEquals("1", vm.uiState.value.highlightedMedicationId)
        events.events.emit(ParentHomeUpdateEvent.ScheduleUpdated)
        runCurrent(); advanceUntilIdle()
        assertEquals(2, repo.calls)
        repo.fetch = { error("offline") }
        events.events.emit(ParentHomeUpdateEvent.MedicationUpdated)
        runCurrent(); advanceUntilIdle()
        assertEquals("변경된 약", vm.uiState.value.medications.single().name)
        repo.fetch = { emptyList() }
        events.events.emit(ParentHomeUpdateEvent.MedicationUpdated)
        runCurrent(); advanceUntilIdle()
        assertEquals(ParentMedicationContent.Empty, vm.uiState.value.content)
    }

    @Test fun ownSocketEventDoesNotDismissCompletionOrUnlockSubmission() = runTest {
        val repo = FakeMedication()
        val events = Updates()
        val vm = ParentMedicationViewModel(repo, events).also { store.put("medication", it) }
        backgroundScope.launch { vm.observeUpdates() }
        runCurrent(); advanceUntilIdle()
        val written = CompletableDeferred<MedicationSchedule>()
        repo.write = { written.await() }
        vm.markAsTaken("1")
        vm.markAsTaken("1")
        runCurrent()
        events.events.emit(ParentHomeUpdateEvent.MedicationUpdated)
        runCurrent(); advanceTimeBy(150); runCurrent()
        assertEquals("1", vm.uiState.value.submittingMedicationId)
        assertEquals(1, repo.writes)
        assertEquals(1, repo.calls)
        repo.data = listOf(dose().copy(taken = true))
        written.complete(repo.data.single())
        advanceUntilIdle()
        assertEquals(ParentMedicationContent.Completed, vm.uiState.value.content)
        assertNull(vm.uiState.value.submittingMedicationId)
        assertNotNull(vm.uiState.value.medications.single().takenAt)
        events.events.emit(ParentHomeUpdateEvent.Subscribed)
        runCurrent(); advanceUntilIdle()
        assertEquals(ParentMedicationContent.Completed, vm.uiState.value.content)
    }

    @Test fun staleReadCannotUndoDoseSubmission() = runTest {
        val repo = FakeMedication()
        val events = Updates()
        val vm = ParentMedicationViewModel(repo, events).also { store.put("medication", it) }
        backgroundScope.launch { vm.observeUpdates() }
        runCurrent(); advanceUntilIdle()
        val oldData = repo.data
        val delayed = CompletableDeferred<List<MedicationSchedule>>()
        repo.fetch = { delayed.await() }
        events.events.emit(ParentHomeUpdateEvent.MedicationUpdated)
        runCurrent(); advanceTimeBy(150); runCurrent()
        vm.markAsTaken("1")
        runCurrent()
        repo.data = listOf(dose().copy(taken = true))
        repo.fetch = { repo.data }
        delayed.complete(oldData)
        advanceUntilIdle()
        assertEquals(3, repo.calls)
        assertEquals(ParentMedicationContent.Completed, vm.uiState.value.content)
        assertNotNull(vm.uiState.value.medications.single().takenAt)
    }

    @Test fun externalCompletionUpdatesListAndObservationRestartsAfterReset() = runTest {
        val repo = FakeMedication()
        val events = Updates()
        val vm = ParentMedicationViewModel(repo, events).also { store.put("medication", it) }
        val observation = backgroundScope.launch { vm.observeUpdates() }
        runCurrent(); advanceUntilIdle()
        repo.data = listOf(dose().copy(taken = true))
        events.events.emit(ParentHomeUpdateEvent.MedicationUpdated)
        runCurrent(); advanceUntilIdle()
        assertEquals(ParentMedicationContent.List, vm.uiState.value.content)
        assertNotNull(vm.uiState.value.medications.single().takenAt)
        observation.cancelAndJoin()
        vm.reset()
        events.events.emit(ParentHomeUpdateEvent.MedicationUpdated)
        runCurrent(); advanceUntilIdle()
        assertEquals(2, repo.calls)
        assertEquals(ParentMedicationContent.Loading, vm.uiState.value.content)
        repo.data = emptyList()
        backgroundScope.launch { vm.observeUpdates() }
        runCurrent(); advanceUntilIdle()
        assertEquals(3, repo.calls)
        assertEquals(ParentMedicationContent.Empty, vm.uiState.value.content)
    }

    @Test fun medicationEventsDuringReadAreNotLost() = runTest {
        val repo = FakeMedication()
        val events = Updates()
        val vm = ParentMedicationViewModel(repo, events).also { store.put("medication", it) }
        backgroundScope.launch { vm.observeUpdates() }
        runCurrent(); advanceUntilIdle()
        val delayed = CompletableDeferred<List<MedicationSchedule>>()
        repo.fetch = { delayed.await() }
        events.events.emit(ParentHomeUpdateEvent.MedicationUpdated)
        runCurrent(); advanceTimeBy(150); runCurrent()
        repeat(5) { events.events.emit(ParentHomeUpdateEvent.MedicationUpdated) }
        runCurrent()
        repo.fetch = { listOf(dose().copy(name = "최종 변경")) }
        delayed.complete(repo.data)
        advanceUntilIdle()
        assertEquals(3, repo.calls)
        assertEquals("최종 변경", vm.uiState.value.medications.single().name)
    }

    private class Updates : ParentHomeUpdatesRepository {
        val events = MutableSharedFlow<ParentHomeUpdateEvent>(extraBufferCapacity = 32)
        override fun observeUpdates() = events
    }
    @Test fun dailySchedulesShowAllItemsInTimeOrder() = runTest {
        val repo = FakeSchedule()
        val first = repo.data.single()
        repo.data = listOf(
            first.copy(id = 2, time = java.time.LocalTime.of(15, 0)),
            first.copy(id = 1, time = java.time.LocalTime.of(9, 0)),
            first.copy(id = 3, time = java.time.LocalTime.of(17, 0)),
        )
        val vm = ParentScheduleViewModel(repo, Updates(), FakeAuth()).also { store.put("schedule", it) }
        vm.loadTodaySchedules()
        advanceUntilIdle()
        assertEquals(listOf("1", "2", "3"), vm.uiState.value.schedules.map { it.id })
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test fun missingSeniorDoesNotRequestSchedulesAndRetryWorks() = runTest {
        val repo = FakeSchedule()
        val auth = FakeAuth(null)
        val vm = ParentScheduleViewModel(repo, Updates(), auth).also { store.put("schedule", it) }
        vm.loadTodaySchedules()
        advanceUntilIdle()
        assertEquals(0, repo.calls)
        assertNotNull(vm.uiState.value.errorMessage)
        auth.seniorId = 42L
        vm.refresh()
        advanceUntilIdle()
        assertEquals(1, repo.calls)
        assertEquals(1, vm.uiState.value.schedules.size)
        assertNull(vm.uiState.value.errorMessage)
    }

    private class FakeAuth(var seniorId: Long? = 42L) : AuthRepository by unused(AuthRepository::class.java) {
        override suspend fun getOnboardingStatus() =
            OnboardingStatus(true, CareManagerType.None, seniorId, true, true, true)
    }

    private class FakeSchedule : HospitalRepository by unused(HospitalRepository::class.java) {
        var calls = 0
        var data = listOf(TodayHospitalSchedule(1, "병원", "내과", koreaToday(), koreaNow().toLocalTime(), null, null))
        var fetch: suspend () -> List<TodayHospitalSchedule> = { data }
        override suspend fun getDaily(parentId: Long, date: String): List<HospitalAppointment> {
            assertEquals(42L, parentId)
            assertEquals(koreaToday().toString(), date)
            calls++
            return fetch().map {
                HospitalAppointment(it.id, it.hospitalName, it.department.orEmpty(), date, it.time.toString(), "")
            }
        }
    }
    private class FakeMedication : MedicationRepository by unused(MedicationRepository::class.java) {
        var calls = 0
        var writes = 0
        var data = listOf(dose())
        var fetch: suspend () -> List<MedicationSchedule> = { data }
        var write: suspend () -> MedicationSchedule = { dose().copy(taken = true) }
        override suspend fun getMySchedules(date: String): List<MedicationSchedule> { calls++; return fetch() }
        override suspend fun markTaken(medicationLogId: Long): MedicationSchedule { writes++; return write() }
    }
    companion object {
        private fun dose() = MedicationSchedule(1, "약", koreaNow().toLocalTime().toString(), false)
        private fun <T : Any> unused(type: Class<T>): T = requireNotNull(type.cast(Proxy.newProxyInstance(type.classLoader, arrayOf(type)) {
            _, method, _ -> error("Unexpected call: ${method.name}")
        }))
    }
}
