package com.example.senior_on.ui.child.display.viewmodel

import com.example.senior_on.data.repository.impl.ParentInfoRepositoryImpl
import com.example.senior_on.data.source.display.MockDisplayScenario
import com.example.senior_on.data.source.mock.fixtures.MockDisplayFixtures
import com.example.senior_on.data.source.mock.fixtures.MockSeniorFixtures
import com.example.senior_on.data.source.parent.MockParentInfoDataSource
import com.example.senior_on.domain.model.display.DisplayDevice
import com.example.senior_on.domain.model.display.DisplayHomeButton
import com.example.senior_on.domain.model.display.DisplayOverview
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
    fun `최초 조회는 빈 서버 버튼을 안드로이드 기본값으로 저장하지 않는다`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = RecordingDisplayRepository().apply {
                overview = overview.copy(
                    screenConfiguration = SeniorScreenConfiguration(),
                    configuredButtonItems = emptyList(),
                    hasSavedButtonConfiguration = false,
                )
            }

            val viewModel = DisplayViewModel(
                parentInfoRepository = ParentInfoRepositoryImpl(
                    MockParentInfoDataSource(MockSeniorFixtures.mother)
                ),
                displayRepository = repository,
            )
            advanceUntilIdle()

            assertEquals(1, repository.overviewRequestCount)
            assertEquals(0, repository.buttonSaveRequestCount)
            assertEquals(
                emptyList<SeniorHomeButtonType>(),
                viewModel.uiState.value.screenConfiguration.buttons,
            )
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `시니어 관계 저장 성공 시 홈 상단 관계 상태를 즉시 갱신한다`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val parentInfoRepository = ParentInfoRepositoryImpl(
                MockParentInfoDataSource(MockSeniorFixtures.mother)
            )
            val viewModel = DisplayViewModel(
                parentInfoRepository = parentInfoRepository,
                displayRepository = RecordingDisplayRepository(),
            )
            advanceUntilIdle()
            val updatedParentInfo = MockSeniorFixtures.mother.copy(
                relationshipLabel = "이모",
            )

            viewModel.saveParentInfo(updatedParentInfo)
            advanceUntilIdle()

            assertEquals("이모", viewModel.uiState.value.relationshipLabel)
            assertEquals(updatedParentInfo, viewModel.uiState.value.parentInfo)
            assertEquals(updatedParentInfo, parentInfoRepository.parentInfo.value)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `화면 탭 재진입은 홈과 편집 권한을 갱신한다`() = runTest {
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

            assertEquals(1, repository.overviewRequestCount)
            assertEquals(1, repository.editPermissionRequestCount)
            assertEquals(0, repository.deviceRequestCount)
            assertEquals(listOf(MockSeniorFixtures.SENIOR_ID), repository.overviewSeniorIds)
            assertEquals(
                listOf(MockSeniorFixtures.SENIOR_ID),
                repository.editPermissionSeniorIds,
            )

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

            repository.canEditScreen = true
            viewModel.refreshOnScreenTabReentry()
            advanceUntilIdle()

            assertEquals(3, repository.overviewRequestCount)
            assertEquals(3, repository.editPermissionRequestCount)
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
            assertEquals(
                listOf(MockSeniorFixtures.SENIOR_ID),
                repository.deviceSeniorIds,
            )
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `당겨서 새로고침은 화면 개요와 권한을 한 번씩 갱신한다`() = runTest {
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
            assertFalse(viewModel.uiState.value.canEditScreen)
            assertNull(viewModel.uiState.value.device)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `시니어 전환은 새 ID로 다시 조회하고 이전 응답이 화면을 덮지 못한다`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val firstRequestGate = CompletableDeferred<Unit>()
            val repository = RecordingDisplayRepository().apply {
                overviewGate = firstRequestGate
            }
            val viewModel = DisplayViewModel(
                parentInfoRepository = ParentInfoRepositoryImpl(
                    MockParentInfoDataSource(MockSeniorFixtures.mother)
                ),
                displayRepository = repository,
            )
            runCurrent()

            val switchedParent = MockSeniorFixtures.mother.copy(
                seniorId = 2L,
                name = "두 번째 시니어",
                relationshipLabel = "아버지",
            )
            repository.overview = repository.overview.copy(parentInfo = switchedParent)
            repository.overviewGate = null

            viewModel.selectSenior(
                seniorId = 2L,
                relationshipLabel = "아버지",
                parentInfoSeed = switchedParent,
            )
            advanceUntilIdle()

            assertEquals(listOf(1L, 2L), repository.overviewSeniorIds)
            assertEquals(listOf(1L, 2L), repository.editPermissionSeniorIds)
            assertEquals(2L, viewModel.uiState.value.selectedSeniorId)
            assertEquals(switchedParent, viewModel.uiState.value.parentInfo)

            firstRequestGate.complete(Unit)
            advanceUntilIdle()

            assertEquals(2L, viewModel.uiState.value.selectedSeniorId)
            assertEquals("두 번째 시니어", viewModel.uiState.value.parentInfo?.name)
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private class RecordingDisplayRepository : DisplayRepository {
    var overviewRequestCount = 0
    var editPermissionRequestCount = 0
    var deviceRequestCount = 0
    var buttonSaveRequestCount = 0
    val overviewSeniorIds = mutableListOf<Long>()
    val editPermissionSeniorIds = mutableListOf<Long>()
    val deviceSeniorIds = mutableListOf<Long>()
    var canEditScreen = true
    var overviewGate: CompletableDeferred<Unit>? = null
    var overview: DisplayOverview = MockDisplayFixtures
        .overview(MockDisplayScenario.Connected)
        .copy(parentInfo = MockSeniorFixtures.mother)

    override suspend fun canCurrentUserEditScreen(seniorId: Long): Boolean {
        editPermissionRequestCount += 1
        editPermissionSeniorIds += seniorId
        return canEditScreen
    }

    override suspend fun getOverview(
        seniorId: Long,
        currentParentInfo: ParentInfo?,
    ): DisplayOverview {
        overviewRequestCount += 1
        overviewSeniorIds += seniorId
        overviewGate?.await()
        return overview
    }

    override suspend fun getSeniorScreenConfiguration(): SeniorScreenConfiguration =
        overview.screenConfiguration

    override suspend fun getDevice(seniorId: Long): DisplayDevice? {
        deviceRequestCount += 1
        deviceSeniorIds += seniorId
        return overview.device
    }

    override suspend fun updateSeniorProfile(parentInfo: ParentInfo): ParentInfo = parentInfo
    override suspend fun updateFontSize(seniorId: Long, fontSize: SeniorFontSize) = Unit

    override suspend fun saveButtons(
        seniorId: Long,
        buttons: List<SeniorHomeButtonType>,
        customButtonLabels: Map<SeniorHomeButtonType, String>,
    ) {
        buttonSaveRequestCount += 1
    }

    override suspend fun saveButtons(seniorId: Long, buttons: List<DisplayHomeButton>) {
        buttonSaveRequestCount += 1
    }

    override suspend fun disconnectDevice(seniorId: Long) = Unit
}
