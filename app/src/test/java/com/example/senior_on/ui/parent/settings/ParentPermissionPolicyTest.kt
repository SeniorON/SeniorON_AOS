package com.example.senior_on.ui.parent.settings

import com.example.senior_on.ui.parent.permission.*
import org.junit.Assert.*
import org.junit.Test

class ParentPermissionPolicyTest {
    @Test fun orderMatchesUpdatedGuideWithoutUnspecifiedSeventhPermission() {
        assertEquals(listOf("BatteryOptimization", "Notification", "ForegroundLocation", "BackgroundLocation", "DefaultHome", "SleepingApps"),
            ParentPermissionStep.entries.map { it.name })
    }

    @Test fun alreadyGrantedPermissionsAreSkippedButManualIsNot() {
        assertEquals(ParentPermissionStep.SleepingApps, ParentPermissionStep.BatteryOptimization.nextRequired {
            if (it == ParentPermissionStep.SleepingApps) ParentPermissionStatus.Manual else ParentPermissionStatus.Granted
        })
    }

    @Test fun deniedPermissionIsNextAndNotApplicableDoesNotBlock() {
        assertEquals(ParentPermissionStep.ForegroundLocation, ParentPermissionStep.BatteryOptimization.nextRequired {
            if (it == ParentPermissionStep.ForegroundLocation) ParentPermissionStatus.Required else ParentPermissionStatus.NotApplicable
        })
        assertNull(ParentPermissionStep.BatteryOptimization.nextRequired { ParentPermissionStatus.Granted })
        assertNull(ParentPermissionStep.SleepingApps.nextRequired { ParentPermissionStatus.Manual })
    }

    @Test fun backgroundLocationExplainsConsentAndHomeDoesNotRequestKnox() {
        assertEquals("동의하고 설정하기", ParentPermissionStep.BackgroundLocation.guideContent().button)
        assertTrue(ParentPermissionStep.BackgroundLocation.guideContent().description.contains("‘항상 허용’"))
        assertEquals("시니어On", ParentPermissionStep.DefaultHome.guideContent().emphasis)
    }
}
