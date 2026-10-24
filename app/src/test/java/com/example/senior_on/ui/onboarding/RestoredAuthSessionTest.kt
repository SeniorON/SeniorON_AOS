package com.example.senior_on.ui.onboarding

import com.example.senior_on.domain.model.auth.*
import com.example.senior_on.domain.repository.auth.*
import com.example.senior_on.domain.repository.device.DeviceRegistrationRepository
import com.example.senior_on.ui.onboarding.viewmodel.AuthViewModel
import java.lang.reflect.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RestoredAuthSessionTest {
    @Test fun savedSessionRestoresRegistrationCredentialsWithoutLoggingOut() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val session = AuthSession(AppUserMode.Child, "child", 17L)
            val vm = createViewModel { session }
            var result: AuthSession? = null
            vm.loadSavedSession { result = it }
            advanceUntilIdle()
            assertEquals(session, result)
            assertEquals(17L, vm.authenticatedUsersId)
            assertEquals("restored-token", vm.accessToken)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun missingSessionDoesNotReuseToken() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            var session: AuthSession? = AuthSession(AppUserMode.Child, "child", 17L)
            val vm = createViewModel { session }
            vm.loadSavedSession { }
            advanceUntilIdle()
            session = null
            vm.loadSavedSession { assertNull(it) }
            advanceUntilIdle()
            assertNull(vm.accessToken)
            assertNull(vm.authenticatedUsersId)
        } finally { Dispatchers.resetMain() }
    }

    private fun createViewModel(session: () -> AuthSession?) = AuthViewModel(
        stub<AuthRepository> { error("Unexpected auth call: $it") },
        stub<SocialAuthRepository> { error("Unexpected social call: $it") },
        stub<SessionRepository> { method ->
            check(method == "validateSavedSession") { "Must not mutate session: $method" }
            session()
        },
        stub<DeviceRegistrationRepository> { error("Unexpected device call: $it") },
        currentAccessToken = { "restored-token" },
    )

    private inline fun <reified T> stub(crossinline call: (String) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, _ ->
            call(method.name)
        } as T
}
