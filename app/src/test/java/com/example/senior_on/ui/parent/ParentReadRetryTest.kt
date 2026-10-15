package com.example.senior_on.ui.parent

import androidx.lifecycle.ViewModelStore
import com.example.senior_on.domain.repository.parent.ParentHomeUpdatesRepository
import com.example.senior_on.domain.repository.parent.ParentSeniorProfileRepository
import com.example.senior_on.domain.repository.server.MedicationRepository
import com.example.senior_on.domain.repository.server.HospitalRepository
import com.example.senior_on.ui.parent.medication.viewmodel.ParentMedicationViewModel
import com.example.senior_on.ui.parent.medication.viewmodel.ParentMedicationContent
import com.example.senior_on.ui.parent.schedule.viewmodel.ParentScheduleViewModel
import java.lang.reflect.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParentReadRetryTest {
    @Test fun medicationReadFailureUsesPersistentRetryInsteadOfEmptyOrToast() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        var fail = true
        var reads = 0
        val repository = stub<MedicationRepository> {
            check(it == "getMySchedules")
            reads++
            if (fail) error("offline")
            emptyList<Any>()
        }
        val vm = ParentMedicationViewModel(repository, stub<ParentHomeUpdatesRepository> { error(it) })
        store.put("medication", vm)
        try {
            vm.loadMedication()
            advanceUntilIdle()
            assertNotNull(vm.uiState.value.queryError)
            assertNull(vm.uiState.value.message)
            vm.consumeMessage()
            assertNotNull(vm.uiState.value.queryError)
            fail = false
            vm.refresh()
            advanceTimeBy(151)
            advanceUntilIdle()
            assertNull(vm.uiState.value.queryError)
            assertEquals(ParentMedicationContent.Empty, vm.uiState.value.content)
            assertFalse(vm.uiState.value.isQueryLoading)
            assertEquals(2, reads)
        } finally { store.clear(); Dispatchers.resetMain() }
    }

    @Test fun scheduleRefreshFailureStaysVisibleUntilSuccessfulRead() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        var fail = false
        val vm = ParentScheduleViewModel(
            stub<HospitalRepository> {
                check(it == "getDaily")
                if (fail) error("offline")
                emptyList<Any>()
            },
            stub<ParentHomeUpdatesRepository> { error(it) },
            stub<ParentSeniorProfileRepository> { 3L },
        )
        store.put("schedule", vm)
        try {
            vm.refresh()
            advanceUntilIdle()
            assertNull(vm.uiState.value.errorMessage)
            fail = true
            vm.refresh()
            advanceUntilIdle()
            assertNotNull(vm.uiState.value.errorMessage)
            fail = false
            vm.refresh()
            assertNotNull(vm.uiState.value.errorMessage)
            advanceUntilIdle()
            assertNull(vm.uiState.value.errorMessage)
            assertFalse(vm.uiState.value.isLoading)
        } finally { store.clear(); Dispatchers.resetMain() }
    }

    private inline fun <reified T> stub(crossinline result: (String) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, _ ->
            result(method.name)
        } as T
}
