package com.example.senior_on.ui.onboarding

import com.example.senior_on.domain.model.auth.FoundLoginId
import com.example.senior_on.domain.model.auth.PasswordResetVerificationDelivery
import com.example.senior_on.domain.repository.auth.AccountRecoveryRepository
import com.example.senior_on.ui.onboarding.viewmodel.AccountRecoveryViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class AccountRecoveryStateTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    private class Repository : AccountRecoveryRepository {
        var fail = false
        var deliveries = 0
        var waitFor: CompletableDeferred<Unit>? = null
        override suspend fun findLoginId(name: String, email: String): FoundLoginId {
            // Deliberately ignore cancellation to exercise the post-response guard.
            waitFor?.let { withContext(NonCancellable) { it.await() } }
            if (fail) error("조회 실패")
            return FoundLoginId(name, "2026-10-12T12:00:00")
        }
        override suspend fun sendPasswordResetVerificationCode(name: String, loginId: String): PasswordResetVerificationDelivery {
            deliveries++
            if (fail) error("전송 실패")
            return PasswordResetVerificationDelivery(true, 10L)
        }
        override suspend fun verifyPasswordResetVerificationCode(verificationId: Long, verificationCode: String) = true
        override suspend fun resetPassword(verificationId: Long, newPassword: String, newPasswordCheck: String) = true
    }

    @Test fun returningToVerificationDoesNotResendButResendButtonDoes() = runTest(dispatcher) {
        val repo = Repository()
        val vm = AccountRecoveryViewModel(repo)
        vm.continuePasswordRecovery("name", "id") {}
        advanceUntilIdle()
        var navigated = false
        vm.cancelPendingRequest()
        vm.continuePasswordRecovery("name", "id") { navigated = it }
        advanceUntilIdle()
        assertTrue(navigated)
        assertEquals(1, repo.deliveries)
        vm.resendPasswordResetVerificationCode()
        advanceUntilIdle()
        assertEquals(2, repo.deliveries)
        vm.resetFlow()
        vm.continuePasswordRecovery("other", "other") {}
        advanceUntilIdle()
        assertEquals(3, repo.deliveries)
    }

    @Test fun freshFlowResumesDeliveryWithoutRestoringVerifiedState() = runTest(dispatcher) {
        val repo = Repository()
        val vm = AccountRecoveryViewModel(repo)
        vm.continuePasswordRecovery("name", "id") {}
        advanceUntilIdle()
        vm.verifyPasswordResetCode("123456") {}
        advanceUntilIdle()
        assertTrue(vm.uiState.value.passwordResetVerified)
        vm.resetFlow() // Return to Login, then start a new recovery flow.
        assertNull(vm.uiState.value.verificationId)
        var navigated = false
        vm.continuePasswordRecovery("name", "id") { navigated = it }
        advanceUntilIdle()
        assertTrue(navigated)
        assertEquals(1, repo.deliveries)
        assertEquals(10L, vm.uiState.value.verificationId)
        assertFalse(vm.uiState.value.passwordResetVerified)
    }

    @Test fun differentNameCannotResumeOldDelivery() = runTest(dispatcher) {
        val repo = Repository()
        val vm = AccountRecoveryViewModel(repo)
        vm.continuePasswordRecovery("name", "id") {}
        advanceUntilIdle()
        vm.resetFlow()
        repo.fail = true
        var navigated = false
        vm.continuePasswordRecovery("wrong name", "id") { navigated = it }
        advanceUntilIdle()
        assertFalse(navigated)
        assertNull(vm.uiState.value.verificationId)
        assertEquals(2, repo.deliveries)
    }

    @Test fun expiredAndConsumedDeliveriesAreNotReused() = runTest(dispatcher) {
        var now = 0L
        val repo = Repository()
        val vm = AccountRecoveryViewModel(repo, nowMillis = { now })
        vm.continuePasswordRecovery("name", "id") {}
        advanceUntilIdle()
        vm.resetFlow()
        now = 300_000L
        vm.continuePasswordRecovery("name", "id") {}
        advanceUntilIdle()
        assertEquals(2, repo.deliveries)
        vm.verifyPasswordResetCode("123456") {}
        advanceUntilIdle()
        vm.resetPassword("newPassword1", "newPassword1") {}
        advanceUntilIdle()
        vm.resetFlow()
        vm.continuePasswordRecovery("name", "id") {}
        advanceUntilIdle()
        assertEquals(3, repo.deliveries)
    }

    @Test fun failedLookupDoesNotRetainPreviousId() = runTest(dispatcher) {
        val repo = Repository()
        val vm = AccountRecoveryViewModel(repo)
        vm.findLoginId("first", "a") {}
        advanceUntilIdle()
        assertEquals("first", vm.uiState.value.foundLoginId)
        repo.fail = true
        var result: FoundLoginId? = FoundLoginId("stale", "")
        vm.findLoginId("wrong", "b") { result = it }
        advanceUntilIdle()
        assertNull(result)
        assertEquals("", vm.uiState.value.foundLoginId)
    }

    @Test fun failedNewDeliveryInvalidatesOldVerification() = runTest(dispatcher) {
        val repo = Repository()
        val vm = AccountRecoveryViewModel(repo)
        vm.sendPasswordResetVerificationCode("a", "a") {}
        advanceUntilIdle()
        vm.verifyPasswordResetCode("123456") {}
        advanceUntilIdle()
        assertTrue(vm.uiState.value.passwordResetVerified)
        repo.fail = true
        vm.sendPasswordResetVerificationCode("b", "b") {}
        advanceUntilIdle()
        assertNull(vm.uiState.value.verificationId)
        assertFalse(vm.uiState.value.passwordResetVerified)
    }

    @Test fun resetBlocksLateResponseAndNavigation() = runTest(dispatcher) {
        val repo = Repository().apply { waitFor = CompletableDeferred() }
        val vm = AccountRecoveryViewModel(repo)
        var callbacks = 0
        vm.findLoginId("old", "a") { callbacks++ }
        runCurrent()
        vm.resetFlow()
        repo.waitFor!!.complete(Unit)
        advanceUntilIdle()
        assertEquals(0, callbacks)
        assertEquals("", vm.uiState.value.foundLoginId)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test fun newerRequestWinsEvenWhenOldResponseArrivesLater() = runTest(dispatcher) {
        val pending = CompletableDeferred<Unit>()
        val repo = Repository().apply { waitFor = pending }
        val vm = AccountRecoveryViewModel(repo)
        var oldCallbacks = 0
        vm.findLoginId("old", "a") { oldCallbacks++ }
        runCurrent()
        repo.waitFor = null
        vm.findLoginId("new", "b") {}
        runCurrent()
        pending.complete(Unit)
        advanceUntilIdle()
        assertEquals("new", vm.uiState.value.foundLoginId)
        assertEquals(0, oldCallbacks)
    }

    @Test fun backCancellationPreservesCompletedVerificationButResetClearsIt() = runTest(dispatcher) {
        val vm = AccountRecoveryViewModel(Repository())
        vm.sendPasswordResetVerificationCode("a", "a") {}
        advanceUntilIdle()
        vm.verifyPasswordResetCode("123456") {}
        advanceUntilIdle()
        vm.cancelPendingRequest()
        assertTrue(vm.uiState.value.passwordResetVerified)
        vm.resetFlow()
        assertFalse(vm.uiState.value.passwordResetVerified)
        assertNull(vm.uiState.value.verificationId)
    }
}
