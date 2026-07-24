package com.example.senior_on.data.repository

import com.example.senior_on.domain.model.DisplayDevice
import com.example.senior_on.domain.model.DisplayDeviceConnectionStatus
import com.example.senior_on.domain.model.DisplayOverview
import com.example.senior_on.domain.model.SeniorFontSize
import com.example.senior_on.domain.model.SeniorHomeButtonType
import com.example.senior_on.domain.model.SeniorScreenConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface DisplayRepository {
    val overview: StateFlow<DisplayOverview>

    fun updateFontSize(fontSize: SeniorFontSize)

    fun updateButtons(
        buttons: List<SeniorHomeButtonType>,
        customButtonLabels: Map<SeniorHomeButtonType, String>,
    )

    fun disconnectDevice()
}

enum class MockDisplayScenario {
    Connected,
    Offline,
    NotConnected,
}

class MockDisplayRepository(
    initialScenario: MockDisplayScenario = MockDisplayScenario.Connected,
) : DisplayRepository {
    private val _overview = MutableStateFlow(MockDisplayFixtures.overview(initialScenario))
    override val overview: StateFlow<DisplayOverview> = _overview.asStateFlow()

    override fun updateFontSize(fontSize: SeniorFontSize) {
        _overview.update { current ->
            current.copy(
                screenConfiguration = current.screenConfiguration.copy(fontSize = fontSize)
            )
        }
    }

    override fun updateButtons(
        buttons: List<SeniorHomeButtonType>,
        customButtonLabels: Map<SeniorHomeButtonType, String>,
    ) {
        val uniqueButtons = buttons.distinct()
        _overview.update { current ->
            current.copy(
                screenConfiguration = current.screenConfiguration.copy(
                    buttons = uniqueButtons,
                    customButtonLabels = customButtonLabels
                        .filterKeys(uniqueButtons::contains)
                        .mapValues { (_, label) -> label.trim() }
                        .filterValues(String::isNotEmpty),
                )
            )
        }
    }

    override fun disconnectDevice() {
        _overview.update { current -> current.copy(device = null) }
    }

    fun setScenario(scenario: MockDisplayScenario) {
        _overview.update { current ->
            MockDisplayFixtures.overview(scenario).copy(
                screenConfiguration = current.screenConfiguration
            )
        }
    }
}

object MockDisplayFixtures {
    val defaultScreenConfiguration = SeniorScreenConfiguration()

    fun overview(scenario: MockDisplayScenario): DisplayOverview = DisplayOverview(
        device = when (scenario) {
            MockDisplayScenario.Connected -> DisplayDevice(
                id = "parent-device-galaxy-s24",
                name = "Galaxy S24",
                connectionStatus = DisplayDeviceConnectionStatus.Online,
                batteryLevelPercent = 72,
                lastLocationUpdatedAtLabel = "1분 전",
            )
            MockDisplayScenario.Offline -> DisplayDevice(
                id = "parent-device-galaxy-s24",
                name = "Galaxy S24",
                connectionStatus = DisplayDeviceConnectionStatus.Offline,
                batteryLevelPercent = 72,
                lastConnectedAtLabel = "5월 9일 15:12",
                lastLocationUpdatedAtLabel = "오후 3:12",
            )
            MockDisplayScenario.NotConnected -> null
        },
        screenConfiguration = defaultScreenConfiguration,
    )
}
