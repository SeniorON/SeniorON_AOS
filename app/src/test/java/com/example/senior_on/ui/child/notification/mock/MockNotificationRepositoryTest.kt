package com.example.senior_on.ui.child.notification.mock

import org.junit.Assert.assertEquals
import org.junit.Test

class MockNotificationRepositoryTest {
    @Test
    fun `child account receives multiple alarms`() {
        assertEquals(
            MockNotificationScenario.MultipleRecentAlarms,
            MockNotificationRepository.scenarioForUserId("child")
        )
    }

    @Test
    fun `child01 account receives no alarms`() {
        assertEquals(
            MockNotificationScenario.NoAlarm,
            MockNotificationRepository.scenarioForUserId("child01")
        )
    }
}
