package com.example.senior_on.ui.parent.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ParentHomeRoutePolicyTest {
    @Test
    fun failedDefaultAppLaunchIsReportedAsUnavailable() {
        var launchAttempted = false

        val result = evaluateDefaultAppLaunch(
            actionType = "DEFAULT",
            actionValue = "MEMO",
            launch = {
                launchAttempted = true
                false
            },
        )

        assertTrue(launchAttempted)
        assertEquals(DefaultAppLaunchResult.Unavailable, result)
    }

    @Test
    fun successfulDefaultAppLaunchIsReportedAsLaunched() {
        val result = evaluateDefaultAppLaunch(
            actionType = "DEFAULT",
            actionValue = "MEMO",
            launch = { true },
        )

        assertEquals(DefaultAppLaunchResult.Launched, result)
    }

    @Test
    fun installedAppDoesNotUseDefaultAppLaunchPolicy() {
        var launchAttempted = false

        val result = evaluateDefaultAppLaunch(
            actionType = "APP",
            actionValue = "com.example.notes",
            launch = {
                launchAttempted = true
                false
            },
        )

        assertFalse(launchAttempted)
        assertEquals(DefaultAppLaunchResult.NotApplicable, result)
    }
}
