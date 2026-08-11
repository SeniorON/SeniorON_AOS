package com.example.senior_on.ui.child.display.viewmodel

import com.example.senior_on.data.repository.impl.ParentInfoRepositoryImpl
import com.example.senior_on.data.source.display.MockDisplayScenario
import com.example.senior_on.data.source.mock.fixtures.MockDisplayFixtures
import com.example.senior_on.data.source.mock.fixtures.MockSeniorFixtures
import com.example.senior_on.data.source.parent.MockParentInfoDataSource
import com.example.senior_on.domain.model.display.DisplayDevice
import com.example.senior_on.domain.model.display.DisplayHomeButton
import com.example.senior_on.domain.model.display.DisplayOverview
import com.example.senior_on.domain.model.display.DisplayWeather
import com.example.senior_on.domain.model.display.SeniorFontSize
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.domain.model.display.SeniorScreenConfiguration
import com.example.senior_on.domain.model.parent.ParentInfo
import com.example.senior_on.domain.repository.display.DisplayRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DisplayViewModelRefreshTest {
    @Test
    fun `화면 탭 재진입은 홈과 편집 권한을 갱신하고 날씨는 10분 동안 재사용한다`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            var currentTimeMillis = 0L
            val repository = RecordingDisplayRepository()
            val viewModel = DisplayViewModel(
                parentInfoRepository = ParentInfoRepositoryImpl(
                    MockParentInfoDataSource(MockSeniorFixtures.mother)
                ),
                displayRepository = repository,
                currentTimeMillis = { currentTimeMillis },
            )
            advanceUntilIdle()

            assertEquals(1, repository.overviewRequestCount)
            assertEquals(1, repository.editPermissionRequestCount)
            assertEquals(1, repository.weatherRequestCount)
            assertEquals(0, repository.deviceRequestCount)

            repository.overview = MockDisplayFixtures
                .overview(MockDisplayScenario.NotConnected)
                .copy(parentInfo = MockSeniorFixtures.mother)
            repository.canEditScreen = false
            viewModel.refreshOnScreenTabReentry()
            viewModel.refreshOnScreenTabReentry()

            assertFalse(viewModel.uiState.value.isLoading)
            advanceUntilIdle()

            assertEquals(2, repository.overviewRequestCount)
            assertEquals(2, repository.editPermissionRequestCount)
            assertEquals(1, repository.weatherRequestCount)
            assertFalse(viewModel.uiState.value.canEditScreen)
            assertNull(viewModel.uiState.value.device)
            assertEquals(
                MockSeniorFixtures.mother,
                viewModel.uiState.value.parentInfo,
            )
            assertEquals(
                MockDisplayFixtures.disconnectedScreenConfiguration,
                viewModel.uiState.value.screenConfiguration,
            )

            currentTimeMillis = 10 * 60 * 1_000L + 1L
            repository.canEditScreen = true
            viewModel.refreshOnScreenTabReentry()
            advanceUntilIdle()

            assertEquals(3, repository.overviewRequestCount)
            assertEquals(3, repository.editPermissionRequestCount)
            assertEquals(2, repository.weatherRequestCount)
            assertEquals(true, viewModel.uiState.value.canEditScreen)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `기기 상세 갱신 요청은 진행 중 중복 호출을 막는다`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = RecordingDisplayRepository()
            val viewModel = DisplayViewModel(
                parentInfoRepository = ParentInfoRepositoryImpl(
                    MockParentInfoDataSource(MockSeniorFixtures.mother)
                ),
                displayRepository = repository,
            )
            advanceUntilIdle()

            viewModel.refreshDevice()
            viewModel.refreshDevice()
            advanceUntilIdle()

            assertEquals(1, repository.deviceRequestCount)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `당겨서 새로고침은 화면 개요와 권한과 날씨를 한 번씩 갱신한다`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = RecordingDisplayRepository()
            val viewModel = DisplayViewModel(
                parentInfoRepository = ParentInfoRepositoryImpl(
                    MockParentInfoDataSource(MockSeniorFixtures.mother)
                ),
                displayRepository = repository,
            )
            advanceUntilIdle()

            repository.overview = MockDisplayFixtures
                .overview(MockDisplayScenario.NotConnected)
                .copy(parentInfo = MockSeniorFixtures.mother)
            repository.canEditScreen = false
            val refreshGate = CompletableDeferred<Unit>()
            repository.overviewGate = refreshGate

            viewModel.refreshOverview()
            viewModel.refreshOverview()
            runCurrent()

            assertTrue(viewModel.uiState.value.isRefreshing)
            refreshGate.complete(Unit)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isRefreshing)
            assertEquals(2, repository.overviewRequestCount)
            assertEquals(2, repository.editPermissionRequestCount)
            assertEquals(2, repository.weatherRequestCount)
            assertFalse(viewModel.uiState.value.canEditScreen)
            assertNull(viewModel.uiState.value.device)
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private class RecordingDisplayRepository : DisplayRepository {
    var overviewRequestCount = 0
    var editPermissionRequestCount = 0
    var weatherRequestCount = 0
    var deviceRequestCount = 0
    var canEditScreen = true
    var overviewGate: CompletableDeferred<Unit>? = null
    var overview: DisplayOverview = MockDisplayFixtures
        .overview(MockDisplayScenario.Connected)
        .copy(parentInfo = MockSeniorFixtures.mother)

    override suspend fun canCurrentUserEditScreen(): Boolean {
        editPermissionRequestCount += 1
        return canEditScreen
    }

    override suspend fun getOverview(currentParentInfo: ParentInfo?): DisplayOverview {
        overviewRequestCount += 1
        overviewGate?.await()
        return overview
    }

    override suspend fun getSeniorScreenConfiguration(): SeniorScreenConfiguration =
        overview.screenConfiguration

    override suspend fun getWeather(
        latitude: Double,
        longitude: Double,
    ): DisplayWeather {
        weatherRequestCount += 1
        return DisplayWeather(
            temperatureCelsius = 20,
            status = "CLEAR",
            description = "맑음",
            observedAt = null,
        )
    }

    override suspend fun getDevice(): DisplayDevice? {
        deviceRequestCount += 1
        return overview.device
    }

    override suspend fun updateSeniorProfile(parentInfo: ParentInfo): ParentInfo = parentInfo
    override suspend fun updateFontSize(fontSize: SeniorFontSize) = Unit

    override suspend fun saveButtons(
        buttons: List<SeniorHomeButtonType>,
        customButtonLabels: Map<SeniorHomeButtonType, String>,
    ) = Unit

    override suspend fun saveButtons(buttons: List<DisplayHomeButton>) = Unit

    override suspend fun disconnectDevice() = Unit
}
