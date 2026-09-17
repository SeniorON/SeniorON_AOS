package com.example.senior_on.ui.child.notification

import org.junit.Assert.*
import org.junit.Test

class NotificationAccessPresentationTest {
    @Test fun existingConnectedStateDoesNotShowAccessWarning() {
        assertNull(NotificationScreenUiState(emptyList()).accessWarningPanel())
    }

    @Test fun revokedAccessReusesWarningCardWithSeniorCopy() {
        val panel = NotificationScreenUiState(emptyList(), isSeniorSharingRevoked = true,
            seniorDisplayName = "어머니").accessWarningPanel()!!
        assertEquals(NotificationFooterTone.Warning, panel.tone)
        assertEquals("어머니가 권한을 해제했어요.", panel.title)
        assertTrue(panel.description.contains("권한을 다시 켜달라고"))
    }

    @Test fun disconnectedStateTakesPriorityOverRevocation() {
        val panel = NotificationScreenUiState(emptyList(), isParentPhoneRegistered = false,
            isSeniorSharingRevoked = true).accessWarningPanel()!!
        assertEquals("시니어 폰이 연결되지 않았어요.", panel.title)
        assertTrue(panel.description.contains("가족 코드"))
    }
}
