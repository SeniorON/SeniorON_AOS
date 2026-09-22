package com.example.senior_on.ui.parent.launcher

import androidx.lifecycle.ViewModelStore
import com.example.senior_on.domain.model.auth.CareManagerType
import com.example.senior_on.domain.model.auth.OnboardingStatus
import com.example.senior_on.domain.repository.auth.AuthRepository
import com.example.senior_on.ui.parent.launcher.viewmodel.ParentFamilyMembershipStatus
import com.example.senior_on.ui.parent.launcher.viewmodel.ParentFamilyMembershipViewModel
import java.io.IOException
import java.lang.reflect.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParentFamilyMembershipViewModelTest {
    @Test
    fun membershipUsesHasFamilyEvenWhenSeniorIdIsMissing() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        var hasFamily = true
        var calls = 0
        val auth = authStub {
            calls++
            OnboardingStatus(hasFamily, CareManagerType.None, null, false, false, false)
        }
        try {
            val vm = ParentFamilyMembershipViewModel(auth).also { store.put("membership", it) }
            advanceUntilIdle()
            assertEquals(ParentFamilyMembershipStatus.Connected, vm.uiState.value.status)
            hasFamily = false
            vm.checkFamilyMembership()
            advanceUntilIdle()
            assertEquals(ParentFamilyMembershipStatus.NotConnected, vm.uiState.value.status)
            assertEquals(2, calls)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun lookupFailureIsNotTreatedAsDisconnectedAndCanRetry() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        var fail = true
        val auth = authStub {
            if (fail) throw IOException("timeout")
            OnboardingStatus(true, CareManagerType.None, 3L, true, true, true)
        }
        try {
            val vm = ParentFamilyMembershipViewModel(auth).also { store.put("membership", it) }
            advanceUntilIdle()
            assertEquals(ParentFamilyMembershipStatus.Error, vm.uiState.value.status)
            fail = false
            vm.checkFamilyMembership()
            advanceUntilIdle()
            assertEquals(ParentFamilyMembershipStatus.Connected, vm.uiState.value.status)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    private fun authStub(status: () -> OnboardingStatus): AuthRepository =
        Proxy.newProxyInstance(
            AuthRepository::class.java.classLoader,
            arrayOf(AuthRepository::class.java),
        ) { _, method, _ ->
            check(method.name == "getOnboardingStatus") { "Unexpected call: ${method.name}" }
            status()
        } as AuthRepository
}
