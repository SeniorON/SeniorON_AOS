package com.example.senior_on.ui.child.settings

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsDeviceNavigationTest {
    @Test
    fun `loaded overview without a registered device opens the install guide`() {
        assertTrue(
            shouldOpenSeniorAppInstallGuide(
                hasLoadedOverview = true,
                hasRegisteredDevice = false,
            )
        )
    }

    @Test
    fun `offline registered device stays on the connected devices screen`() {
        assertFalse(
            shouldOpenSeniorAppInstallGuide(
                hasLoadedOverview = true,
                hasRegisteredDevice = true,
            )
        )
    }

    @Test
    fun `initial loading does not treat an unknown device as disconnected`() {
        assertFalse(
            shouldOpenSeniorAppInstallGuide(
                hasLoadedOverview = false,
                hasRegisteredDevice = false,
            )
        )
    }
}
