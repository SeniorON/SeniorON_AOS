package com.example.senior_on.ui.child.display

import com.example.senior_on.domain.model.display.DisplayDeviceConnectionStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DisplayConnectionPolicyTest {
    @Test
    fun `online and offline devices allow preview and editing`() {
        assertTrue(DisplayDeviceConnectionStatus.Online.allowsDisplayPreviewAndEditing())
        assertTrue(DisplayDeviceConnectionStatus.Offline.allowsDisplayPreviewAndEditing())
    }

    @Test
    fun `login expired and disconnected devices block preview and editing`() {
        assertFalse(DisplayDeviceConnectionStatus.LoginExpired.allowsDisplayPreviewAndEditing())
        assertFalse(null.allowsDisplayPreviewAndEditing())
    }

    @Test
    fun `only offline saves require the delayed apply notice`() {
        assertTrue(DisplayDeviceConnectionStatus.Offline.requiresDelayedApplyNotice())
        assertFalse(DisplayDeviceConnectionStatus.Online.requiresDelayedApplyNotice())
        assertFalse(DisplayDeviceConnectionStatus.LoginExpired.requiresDelayedApplyNotice())
        assertFalse(null.requiresDelayedApplyNotice())
    }
}
