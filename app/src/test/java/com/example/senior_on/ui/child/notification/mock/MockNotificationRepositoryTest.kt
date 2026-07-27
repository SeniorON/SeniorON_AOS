package com.example.senior_on.ui.child.notification.mock

import com.example.senior_on.data.repository.mock.fixtures.MockUserFixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class MockNotificationRepositoryTest {
    @Test
    fun `child account receives multiple alarms`() {
        assertEquals(
            MockNotificationScenario.MultipleRecentAlarms,
            MockNotificationRepository.scenarioForUserId(
                MockUserFixtures.PRIMARY_CAREGIVER_USER_ID
            )
        )
    }

    @Test
    fun `child01 account receives no alarms`() {
        assertEquals(
            MockNotificationScenario.NoAlarm,
            MockNotificationRepository.scenarioForUserId(
                MockUserFixtures.ASSISTANT_CAREGIVER_USER_ID
            )
        )
    }
}
