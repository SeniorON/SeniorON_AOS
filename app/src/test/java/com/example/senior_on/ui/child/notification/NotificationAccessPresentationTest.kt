package com.example.senior_on.ui.child.notification

import org.junit.Assert.*
import org.junit.Test

class NotificationAccessPresentationTest {
    @Test fun locationRevocationDoesNotDisableInactivityOrSos() {
        val state = NotificationScreenUiState(emptyList(), locationSharingEnabled = false)
        assertFalse(state.canAccess(NotificationCategory.Outing))
        assertTrue(state.canAccess(NotificationCategory.Inactivity))
        assertTrue(state.canAccess(NotificationCategory.Sos))
        assertFalse(state.accessWarningPanel()!!.description.contains("무응답 감지"))
    }

    @Test fun inactivityRevocationDoesNotDisableLocation() {
        val state = NotificationScreenUiState(emptyList(), inactivitySharingEnabled = false)
        assertFalse(state.canAccess(NotificationCategory.Inactivity))
        assertTrue(state.canAccess(NotificationCategory.Outing))
    }

    @Test fun unknownPermissionsHideSensitiveInformationWithoutClaimingDisconnection() {
        val state = NotificationScreenUiState(emptyList(), sharingStatusKnown = false)
        assertFalse(state.canAccess(NotificationCategory.Outing))
        assertFalse(state.canAccess(NotificationCategory.Inactivity))
        assertTrue(state.isParentPhoneRegistered)
        assertEquals("공유 상태를 확인하지 못했어요.", state.accessWarningPanel()!!.title)
    }

    @Test fun revokedLocationSanitizesCachedSosMessage() {
        val state = NotificationScreenUiState(emptyList(), locationSharingEnabled = false)
        val message = NotificationMessageUiState("", "긴급 알림", severity = NotificationSeverity.Danger,
            address = "집 주소", latitude = 37.0, longitude = 127.0, detail = "집 주소")
        val visible = state.visibleMessage(message)
        assertNull(visible.address)
        assertNull(visible.latitude)
        assertNull(visible.longitude)
        assertNull(visible.detail)
        assertEquals("긴급 알림", visible.title)
    }

    @Test fun disconnectedDisablesEveryCategoryEvenWithSharingAllowed() {
        val state = NotificationScreenUiState(emptyList(), isParentPhoneRegistered = false)
        NotificationCategory.entries.forEach { assertFalse(state.canAccess(it)) }
    }

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
