package com.example.senior_on.ui.child.display.viewmodel

import com.example.senior_on.data.repository.impl.DisplayRepositoryImpl
import com.example.senior_on.data.repository.impl.ParentInfoRepositoryImpl
import com.example.senior_on.data.source.display.MockDisplayDataSource
import com.example.senior_on.data.source.display.MockDisplayScenario
import com.example.senior_on.data.source.mock.fixtures.MockDisplayFixtures
import com.example.senior_on.data.source.mock.fixtures.MockSeniorFixtures
import com.example.senior_on.data.source.parent.MockParentInfoDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DisplayViewModelDisconnectTest {
    @Test
    fun `연결 해제 성공 시 서버의 연결 해제 홈 상태로 즉시 갱신한다`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val parentInfoDataSource = MockParentInfoDataSource(MockSeniorFixtures.mother)
            val viewModel = DisplayViewModel(
                parentInfoRepository = ParentInfoRepositoryImpl(parentInfoDataSource),
                displayRepository = DisplayRepositoryImpl(
                    MockDisplayDataSource(MockDisplayScenario.Connected)
                ),
            )
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.canEditScreen)

            var onSuccessCalled = false
            viewModel.disconnectDevice(onSuccess = { onSuccessCalled = true })
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(onSuccessCalled)
            assertNull(state.device)
            assertEquals(MockSeniorFixtures.mother, state.parentInfo)
            assertEquals(
                MockSeniorFixtures.mother.relationshipLabel,
                state.relationshipLabel,
            )
            assertTrue(state.canEditScreen)
            assertEquals(
                MockSeniorFixtures.mother,
                parentInfoDataSource.parentInfo.value,
            )
            assertEquals(
                MockDisplayFixtures.disconnectedScreenConfiguration,
                state.screenConfiguration,
            )
        } finally {
            Dispatchers.resetMain()
        }
    }
}
