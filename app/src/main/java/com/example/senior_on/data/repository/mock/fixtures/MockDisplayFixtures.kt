package com.example.senior_on.data.repository.mock.fixtures

import com.example.senior_on.data.repository.mock.display.MockDisplayScenario
import com.example.senior_on.domain.model.display.DisplayDevice
import com.example.senior_on.domain.model.display.DisplayDeviceConnectionStatus
import com.example.senior_on.domain.model.display.DisplayOverview
import com.example.senior_on.domain.model.display.SeniorScreenConfiguration

object MockDisplayFixtures {
    const val CONNECTED_DEVICE_ID = "parent-device-galaxy-s24"
    const val CONNECTED_DEVICE_NAME = "Galaxy S24"

    val defaultScreenConfiguration = SeniorScreenConfiguration()

    fun overview(scenario: MockDisplayScenario): DisplayOverview = DisplayOverview(
        device = when (scenario) {
            MockDisplayScenario.Connected -> DisplayDevice(
                id = CONNECTED_DEVICE_ID,
                name = CONNECTED_DEVICE_NAME,
                connectionStatus = DisplayDeviceConnectionStatus.Online,
                batteryLevelPercent = 72,
                lastLocationUpdatedAtLabel = "1분 전",
            )

            MockDisplayScenario.Offline -> DisplayDevice(
                id = CONNECTED_DEVICE_ID,
                name = CONNECTED_DEVICE_NAME,
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
