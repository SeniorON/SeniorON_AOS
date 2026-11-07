package com.example.senior_on.ui.child.settings

import com.example.senior_on.domain.model.inquiry.*
import com.example.senior_on.domain.repository.inquiry.InquiryRepository
import com.example.senior_on.ui.child.settings.viewmodel.InquiryViewModel
import com.example.senior_on.ui.child.settings.viewmodel.InquiryDraft
import com.example.senior_on.ui.child.settings.viewmodel.InquiryNotice
import com.example.senior_on.domain.repository.inquiry.InquiryResultUnknownException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InquiryViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setMain() = Dispatchers.setMain(dispatcher)
    @After fun resetMain() = Dispatchers.resetMain()

    @Test fun doubleTapSendsOnceEvenIfSuccessArrivesBeforeDialog() = runTest(dispatcher) {
        val repo = FakeRepository()
        val vm = InquiryViewModel(repo) { _, _ -> }
        fun submit() = vm.submitInquiry("제목", "내용", emptyList())
        vm.setWriteVisible(true)
        submit()
        submit()
        assertTrue(vm.uiState.value.isSubmitting)
        advanceUntilIdle()
        submit()
        advanceUntilIdle()
        assertEquals(1, repo.posts)
        vm.consumeSubmitCompleted()
        submit()
        advanceUntilIdle()
        assertEquals(2, repo.posts)
    }

    @Test fun failedPostAllowsManualRetry() = runTest(dispatcher) {
        val repo = FakeRepository().apply { fail = true }
        val vm = InquiryViewModel(repo) { _, _ -> }
        vm.setWriteVisible(true)
        vm.submitInquiry("제목", "내용", emptyList())
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isSubmitting)
        assertFalse(vm.uiState.value.submitCompleted)
        repo.fail = false
        vm.submitInquiry("제목", "내용", emptyList())
        advanceUntilIdle()
        assertTrue(vm.uiState.value.submitCompleted)
        assertEquals(2, repo.posts)
    }

    @Test fun overlappingRefreshesLoadOnceAndFailurePreservesExistingHistory() = runTest(dispatcher) {
        val repo = FakeRepository()
        val vm = InquiryViewModel(repo) { _, _ -> }
        vm.loadInquiries()
        vm.loadInquiries()
        advanceUntilIdle()
        assertEquals(1, repo.loads)
        repo.fail = true
        vm.loadInquiries()
        advanceUntilIdle()
        assertEquals("제목", vm.uiState.value.historyItems.single().question)
        assertNotNull(vm.uiState.value.historyErrorMessage)
        repo.fail = false
        repo.items = emptyList()
        vm.loadInquiries()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.historyItems.isEmpty())
        assertNull(vm.uiState.value.historyErrorMessage)
    }

    @Test fun leavingWriteWhileSendingShowsSuccessNoticeWithoutReposting() = runTest(dispatcher) {
        val repo = FakeRepository()
        val vm = InquiryViewModel(repo) { _, _ -> }
        vm.setWriteVisible(true)
        vm.submitInquiry("제목", "내용", listOf("photo"))
        vm.setWriteVisible(false)
        advanceUntilIdle()
        assertEquals(InquiryNotice.Success, vm.uiState.value.notice)
        assertFalse(vm.uiState.value.submitCompleted)
        assertEquals(InquiryDraft(), vm.uiState.value.draft)
        assertEquals(1, repo.posts)
    }

    @Test fun uncertainResultKeepsDraftUntilExplicitRetryAfterSuccessfulHistoryRead() = runTest(dispatcher) {
        val repo = FakeRepository().apply { uncertain = true }
        val vm = InquiryViewModel(repo) { _, _ -> }
        fun submit() = vm.submitInquiry("제목", "내용", listOf("photo"))
        submit()
        advanceUntilIdle()
        assertEquals(InquiryNotice.Unknown, vm.uiState.value.notice)
        assertEquals(InquiryDraft("제목", "내용", listOf("photo")), vm.uiState.value.draft)
        submit()
        repo.fail = true
        vm.loadInquiries()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.needsHistoryCheck)
        repo.fail = false
        vm.loadInquiries()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.needsHistoryCheck)
        assertEquals(1, repo.posts)
        assertEquals(2, repo.loads)
        repo.uncertain = false
        submit()
        advanceUntilIdle()
        assertEquals(2, repo.posts)
    }

    private class FakeRepository : InquiryRepository {
        var posts = 0
        var loads = 0
        var fail = false
        var uncertain = false
        var items = listOf(InquirySummary(1, "제목", InquiryStatus.Waiting, ""))
        override suspend fun getInquiries(): List<InquirySummary> {
            loads++
            check(!fail)
            return items
        }
        override suspend fun getInquiry(inquiryId: Long): InquiryDetail = error("unused")
        override suspend fun createInquiry(title: String, content: String, imageUris: List<String>): InquiryCreateResult {
            posts++
            if (uncertain) throw InquiryResultUnknownException(java.net.SocketTimeoutException())
            check(!fail)
            return InquiryCreateResult(1, title, InquiryStatus.Waiting, "", emptyList())
        }
    }
}
