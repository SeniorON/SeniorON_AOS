package com.example.senior_on.ui.child.notification.mock

import com.example.senior_on.data.source.mock.fixtures.MockUserFixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class MockNotificationUiStateFactoryTest {
    @Test
    fun `child account receives multiple alarms`() {
        assertEquals(
            MockNotificationScenario.MultipleRecentAlarms,
            MockNotificationUiStateFactory.scenarioForUserId(
                MockUserFixtures.PRIMARY_CAREGIVER_USER_ID
            )
        )
    }

    @Test
    fun `child01 account receives no alarms`() {
        assertEquals(
            MockNotificationScenario.NoAlarm,
            MockNotificationUiStateFactory.scenarioForUserId(
                MockUserFixtures.ASSISTANT_CAREGIVER_USER_ID
            )
        )
    }
}
