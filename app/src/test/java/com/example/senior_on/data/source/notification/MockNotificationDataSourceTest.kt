package com.example.senior_on.data.source.notification

import com.example.senior_on.data.source.mock.fixtures.MockUserFixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class MockNotificationDataSourceTest {
    @Test
    fun `primary child account receives multiple alarms`() {
        assertEquals(
            MockNotificationScenario.MultipleRecentAlarms,
            MockNotificationDataSource.scenarioForUserId(
                MockUserFixtures.PRIMARY_CAREGIVER_USER_ID
            )
        )
    }

    @Test
    fun `assistant child account receives no alarms`() {
        assertEquals(
            MockNotificationScenario.NoAlarm,
            MockNotificationDataSource.scenarioForUserId(
                MockUserFixtures.ASSISTANT_CAREGIVER_USER_ID
            )
        )
    }
}
