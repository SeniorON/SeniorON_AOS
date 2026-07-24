package com.example.senior_on.data.repository

import com.example.senior_on.domain.model.DisplayDeviceConnectionStatus
import com.example.senior_on.domain.model.ParentInfo
import com.example.senior_on.domain.model.SeniorFontSize
import com.example.senior_on.domain.model.SeniorHomeButtonType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DisplayRepositoryTest {
    @Test
    fun connectedScenarioProvidesOnlineParentDevice() {
        val repository = MockDisplayRepository(MockDisplayScenario.Connected)

        val device = repository.overview.value.device

        assertNotNull(device)
        assertEquals(DisplayDeviceConnectionStatus.Online, device?.connectionStatus)
        assertEquals(72, device?.batteryLevelPercent)
    }

    @Test
    fun notConnectedScenarioProvidesNoDevice() {
        val repository = MockDisplayRepository(MockDisplayScenario.NotConnected)

        assertNull(repository.overview.value.device)
    }

    @Test
    fun screenConfigurationUpdatesWithoutBackend() {
        val repository = MockDisplayRepository()

        repository.updateFontSize(SeniorFontSize.Normal)

        assertEquals(
            SeniorFontSize.Normal,
            repository.overview.value.screenConfiguration.fontSize,
        )
    }

    @Test
    fun buttonOrderUpdatesWithoutBeingReSorted() {
        val repository = MockDisplayRepository()
        val orderedButtons = listOf(
            SeniorHomeButtonType.Schedule,
            SeniorHomeButtonType.Message,
            SeniorHomeButtonType.Call,
            SeniorHomeButtonType.Camera,
        )

        repository.updateButtons(
            buttons = orderedButtons,
            customButtonLabels = emptyMap(),
        )

        assertEquals(
            orderedButtons,
            repository.overview.value.screenConfiguration.buttons,
        )
    }

    @Test
    fun customButtonLabelsAreSavedAndRemovedWithDeletedButtons() {
        val repository = MockDisplayRepository()

        repository.updateButtons(
            buttons = listOf(
                SeniorHomeButtonType.Schedule,
                SeniorHomeButtonType.Call,
                SeniorHomeButtonType.Message,
            ),
            customButtonLabels = mapOf(
                SeniorHomeButtonType.Call to "엄마전화",
                SeniorHomeButtonType.Message to "  문자  ",
                SeniorHomeButtonType.Camera to "카메라",
            ),
        )

        assertEquals(
            mapOf(
                SeniorHomeButtonType.Call to "엄마전화",
                SeniorHomeButtonType.Message to "문자",
            ),
            repository.overview.value.screenConfiguration.customButtonLabels,
        )
    }

    @Test
    fun disconnectingDeviceKeepsScreenConfigurationAndClearsDevice() {
        val repository = MockDisplayRepository(MockDisplayScenario.Connected)

        repository.disconnectDevice()

        assertNull(repository.overview.value.device)
        assertEquals(
            MockDisplayFixtures.defaultScreenConfiguration,
            repository.overview.value.screenConfiguration,
        )
    }

    @Test
    fun savedParentInfoIsSharedWithObservers() {
        val repository = MockParentInfoRepository(MockParentInfoFixtures.mother)
        val savedParentInfo = ParentInfo(
            name = "김영희",
            relationshipLabel = "아버지",
            birthDate = LocalDate.of(1960, 1, 2),
            phoneNumber = "010-9876-5432",
            address = "서울시 강동구",
        )

        repository.saveParentInfo(savedParentInfo)

        assertEquals(savedParentInfo, repository.parentInfo.value)
    }
}
