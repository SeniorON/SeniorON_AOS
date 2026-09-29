package com.example.senior_on.ui.parent.medication

import androidx.lifecycle.ViewModelStore
import com.example.senior_on.domain.model.server.MedicationSchedule
import com.example.senior_on.domain.repository.parent.ParentHomeUpdatesRepository
import com.example.senior_on.domain.repository.server.MedicationRepository
import com.example.senior_on.ui.parent.medication.viewmodel.ParentMedicationViewModel
import java.lang.reflect.Proxy
import java.time.LocalDateTime
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParentMedicationDeadlineStateTest {
    @Test fun deadlinesRestartOnListChangeAndScreenResumeWithoutPolling() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val start = LocalDateTime.of(2026, 9, 29, 9, 59)
            var clockReads = 0
            var schedules = listOf(MedicationSchedule(1, "약", "08:00", false, plannedDate = "2026-09-29"))
            val repository = proxy<MedicationRepository> { name ->
                check(name == "getMySchedules")
                schedules
            }
            val vm = ParentMedicationViewModel(repository, proxy<ParentHomeUpdatesRepository> { error(it) }) {
                clockReads++
                start.plusNanos(testScheduler.currentTime * 1_000_000)
            }
            store.put("vm", vm)
            var observer = backgroundScope.launch { vm.observeTakingDeadlines() }
            vm.loadMedication()
            advanceTimeBy(151); runCurrent()
            assertTrue(vm.uiState.value.expiredMedicationIds.isEmpty())
            val reads = clockReads
            advanceTimeBy(30_000); runCurrent()
            assertEquals(reads, clockReads)
            advanceTimeBy(29_849); runCurrent()
            assertTrue(vm.uiState.value.expiredMedicationIds.isEmpty())
            advanceTimeBy(1); runCurrent()
            assertEquals(setOf("1"), vm.uiState.value.expiredMedicationIds)

            schedules = listOf(MedicationSchedule(2, "새 약", "08:01", false, plannedDate = "2026-09-29"))
            vm.refresh()
            advanceTimeBy(151); runCurrent()
            assertTrue(vm.uiState.value.expiredMedicationIds.isEmpty())
            observer.cancelAndJoin()
            advanceTimeBy(60_000); runCurrent()
            assertTrue(vm.uiState.value.expiredMedicationIds.isEmpty())
            observer = backgroundScope.launch { vm.observeTakingDeadlines() }
            runCurrent()
            assertEquals(setOf("2"), vm.uiState.value.expiredMedicationIds)
            val afterExpiry = clockReads
            advanceTimeBy(60_000); runCurrent()
            assertEquals(afterExpiry, clockReads)
            observer.cancelAndJoin()
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    private inline fun <reified T> proxy(crossinline call: (String) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, _ ->
            call(method.name)
        } as T
}
