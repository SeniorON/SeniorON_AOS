package com.example.senior_on.ui.child

import androidx.lifecycle.ViewModelStore
import com.example.senior_on.domain.repository.senior.SelectedSeniorRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SelectedSeniorViewModelTest {
    @Test fun validatesRestoredSelectionAndHandlesSwitchRemovalAndEmptyList() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        val saved = MutableStateFlow<Long?>(77)
        val repo = object : SelectedSeniorRepository {
            override fun observe(accountId: String) = saved
            override suspend fun select(accountId: String, seniorId: Long?) { saved.value = seniorId }
        }
        try {
            val vm = SelectedSeniorViewModel("account", repo).also { store.put("selection", it) }
            advanceUntilIdle()
            assertTrue(vm.state.value.isLoading)
            assertNull(vm.state.value.seniorId)
            vm.reconcile(listOf(42, 77))
            advanceUntilIdle()
            assertEquals(77L, vm.state.value.seniorId)
            vm.select(42)
            advanceUntilIdle()
            assertEquals(42L, vm.state.value.seniorId)
            assertEquals(42L, saved.value)
            vm.select(999)
            advanceUntilIdle()
            assertEquals(42L, saved.value)
            vm.reconcile(listOf(77))
            advanceUntilIdle()
            assertEquals(77L, vm.state.value.seniorId)
            vm.reconcile(emptyList())
            advanceUntilIdle()
            assertNull(vm.state.value.seniorId)
            assertNull(saved.value)
        } finally { store.clear(); Dispatchers.resetMain() }
    }
}
