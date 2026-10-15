package com.example.senior_on.ui.onboarding

import com.example.senior_on.domain.model.auth.*
import com.example.senior_on.domain.repository.auth.*
import com.example.senior_on.domain.repository.device.DeviceRegistrationRepository
import com.example.senior_on.ui.onboarding.viewmodel.AuthViewModel
import com.example.senior_on.ui.onboarding.viewmodel.SignupAgreements
import java.lang.reflect.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignupDraftReplacementTest {
    @Test fun editedNameAndEmailAreUsedAfterReturningFromVerification() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            var submitted: SignupCredentials? = null
            val auth = stub<AuthRepository> { method, args -> when (method) {
                "sendSignupEmailVerificationCode" -> true
                "signup" -> {
                    val input = args[0] as SignupCredentials
                    submitted = input
                    SignupResult(1L, input.name, input.loginId, input.mode)
                }
                else -> error(method)
            } }
            val vm = AuthViewModel(auth, stub<SocialAuthRepository> { _, _ -> error("unused") },
                stub<SessionRepository> { _, _ -> Unit },
                stub<DeviceRegistrationRepository> { _, _ -> error("unused") })
            vm.saveNameAndBirth("신한석", "1950.01.01")
            vm.sendSignupEmailVerificationCode("first@example.com") {}
            advanceUntilIdle()
            vm.saveNameAndBirth("최원재", "1960.02.02")
            vm.saveVerifiedEmail("second@example.com")
            vm.saveAccountInfo("second", "Password1", "Password1")
            var succeeded = false
            vm.completeSignup(AppUserMode.Senior, SignupAgreements(true, true, true, false)) { succeeded = it }
            advanceUntilIdle()
            assertTrue(succeeded)
            assertEquals("최원재", submitted?.name)
            assertEquals("second@example.com", submitted?.email)
        } finally { Dispatchers.resetMain() }
    }

    private inline fun <reified T> stub(crossinline call: (String, Array<out Any?>) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, args ->
            call(method.name, args ?: emptyArray())
        } as T
}
