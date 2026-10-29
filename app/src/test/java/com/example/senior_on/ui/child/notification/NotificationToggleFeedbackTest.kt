package com.example.senior_on.ui.child.notification

import com.example.senior_on.domain.model.auth.RemoteRequestException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationToggleFeedbackTest {
    private fun error(code: String) = RemoteRequestException(404, code, null, "server message", Exception())

    @Test
    fun `only missing home location on outing toggle produces toast`() {
        assertEquals(HomeLocationMissingMessage,
            homeLocationToggleFeedback(NotificationCategory.Outing, error("SENIOR_HOME_LOCATION_NOT_FOUND")))
        assertNull(homeLocationToggleFeedback(NotificationCategory.Outing, error("OTHER_ERROR")))
        assertNull(homeLocationToggleFeedback(NotificationCategory.Outing, java.io.IOException()))
        assertNull(homeLocationToggleFeedback(NotificationCategory.Inactivity, error("SENIOR_HOME_LOCATION_NOT_FOUND")))
    }
}
