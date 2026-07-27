package com.example.senior_on.data.repository.mock.display

import com.example.senior_on.data.repository.mock.fixtures.MockDisplayFixtures
import com.example.senior_on.domain.model.display.DisplayOverview
import com.example.senior_on.domain.model.display.SeniorFontSize
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.domain.repository.display.DisplayRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
