package com.example.senior_on.ui.parent.settings

import com.example.senior_on.ui.parent.permission.*
import org.junit.Assert.*
import org.junit.Test

class ParentPermissionPolicyTest {
    @Test fun freshGuideOffersDeferredButStillMissingPermissionsAgain() {
        val status: (ParentPermissionStep) -> ParentPermissionStatus = {
            if (it == ParentPermissionStep.Notification) ParentPermissionStatus.Granted
            else ParentPermissionStatus.Required
        }
        val previousGuideDeferrals = ParentPermissionStep.entries.toSet()
        assertFalse(shouldOfferPermissionGuide(previousGuideDeferrals, status))
        assertTrue(shouldOfferPermissionGuide(status = status))
        assertEquals(ParentPermissionStep.BatteryOptimization, firstMissingPermissionStep(status = status))
        assertEquals(ParentPermissionStep.ForegroundLocation,
            ParentPermissionStep.BatteryOptimization.nextRequired(status = status))
    }

    @Test fun backFromFiveDoesNotAdvanceToSixWhenEarlierStepsAreComplete() {
        val status: (ParentPermissionStep) -> ParentPermissionStatus = {
            when (it) {
                ParentPermissionStep.SleepingApps -> ParentPermissionStatus.Manual
                ParentPermissionStep.DefaultHome -> ParentPermissionStatus.Required
                else -> ParentPermissionStatus.Granted
            }
        }
        assertNull(ParentPermissionStep.SleepingApps.previousRequired(status = status))
        assertEquals(ParentPermissionStep.SleepingApps,
            ParentPermissionStep.DefaultHome.previousRequired(status = status))
    }

    @Test fun backSelectsNearestPendingStepAndSkipsNotApplicableSteps() {
        assertEquals(ParentPermissionStep.BackgroundLocation,
            ParentPermissionStep.DefaultHome.previousRequired {
                when (it) {
                    ParentPermissionStep.SleepingApps -> ParentPermissionStatus.NotApplicable
                    else -> ParentPermissionStatus.Required
                }
            })
        assertNull(ParentPermissionStep.BatteryOptimization.previousRequired { ParentPermissionStatus.Required })
    }

    @Test fun backFromFourReturnsToThreeEvenWhenFirstThreeWereDeferred() {
        val dismissed = setOf(ParentPermissionStep.BatteryOptimization,
            ParentPermissionStep.Notification, ParentPermissionStep.ForegroundLocation)
        val status: (ParentPermissionStep) -> ParentPermissionStatus = { ParentPermissionStatus.Required }
        val firstStep = firstMissingPermissionStep(dismissed, status)
        assertEquals(ParentPermissionStep.BackgroundLocation, firstStep)
        assertEquals(ParentPermissionStep.ForegroundLocation, firstStep!!.previousRequired(status))
        assertEquals(ParentPermissionStep.Notification, ParentPermissionStep.ForegroundLocation.previousRequired(status))
        assertEquals(ParentPermissionStep.BatteryOptimization, ParentPermissionStep.Notification.previousRequired(status))
        assertEquals(ParentPermissionStep.BatteryOptimization,
            ParentPermissionStep.BackgroundLocation.previousRequired {
                if (it == ParentPermissionStep.BatteryOptimization) ParentPermissionStatus.Required
                else ParentPermissionStatus.Granted
            })
    }

    @Test fun currentMissingHomeIsOfferedOnNewInstallation() {
        val status: (ParentPermissionStep) -> ParentPermissionStatus = {
            if (it == ParentPermissionStep.DefaultHome) ParentPermissionStatus.Required else ParentPermissionStatus.Granted
        }
        assertTrue(shouldOfferPermissionGuide(emptySet(), status))
        assertFalse(shouldOfferPermissionGuide(setOf(ParentPermissionStep.DefaultHome), status))
        assertEquals(ParentPermissionStep.DefaultHome, firstMissingPermissionStep(status = status))
    }

    @Test fun completedPermissionsAndUnverifiableManualStepDoNotForceGuide() {
        assertFalse(shouldOfferPermissionGuide { ParentPermissionStatus.Granted })
        assertFalse(shouldOfferPermissionGuide { ParentPermissionStatus.Manual })
        assertNull(firstMissingPermissionStep { ParentPermissionStatus.NotApplicable })
    }

    @Test fun decliningBatteryDoesNotHideMissingHome() {
        val dismissed = setOf(ParentPermissionStep.BatteryOptimization)
        val status: (ParentPermissionStep) -> ParentPermissionStatus = {
            if (it == ParentPermissionStep.BatteryOptimization || it == ParentPermissionStep.DefaultHome)
                ParentPermissionStatus.Required else ParentPermissionStatus.Granted
        }
        assertTrue(shouldOfferPermissionGuide(dismissed, status))
        assertEquals(ParentPermissionStep.DefaultHome, firstMissingPermissionStep(dismissed, status))
    }

    @Test fun nextStepSkipsOnlyExplicitlyDeferredPermissions() {
        val dismissed = setOf(ParentPermissionStep.Notification, ParentPermissionStep.ForegroundLocation)
        assertEquals(ParentPermissionStep.BackgroundLocation,
            ParentPermissionStep.BatteryOptimization.nextRequired(dismissed) { ParentPermissionStatus.Required })
        assertNull(firstMissingPermissionStep(ParentPermissionStep.entries.toSet()) { ParentPermissionStatus.Required })
    }

    @Test fun grantClearsRefusalSoLaterRevocationIsOfferedAgain() {
        val dismissed = setOf(ParentPermissionStep.DefaultHome, ParentPermissionStep.Notification)
        val remaining = remainingPermissionDismissals(dismissed) {
            if (it == ParentPermissionStep.DefaultHome) ParentPermissionStatus.Granted else ParentPermissionStatus.Required
        }
        assertEquals(setOf(ParentPermissionStep.Notification), remaining)
        assertTrue(shouldOfferPermissionGuide(remaining) {
            if (it == ParentPermissionStep.DefaultHome) ParentPermissionStatus.Required else ParentPermissionStatus.Granted
        })
    }

    @Test fun guideContainsExactlySixStepsInOrder() {
        assertEquals(listOf("BatteryOptimization", "Notification", "ForegroundLocation", "BackgroundLocation", "SleepingApps", "DefaultHome"),
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
        assertNull(ParentPermissionStep.DefaultHome.nextRequired { ParentPermissionStatus.Manual })
    }

    @Test fun sleepingAppsComesBeforeFinalHomeStep() {
        assertEquals(ParentPermissionStep.SleepingApps, ParentPermissionStep.BackgroundLocation.next())
        assertEquals(ParentPermissionStep.DefaultHome, ParentPermissionStep.SleepingApps.next())
        assertEquals(ParentPermissionStep.SleepingApps, ParentPermissionStep.DefaultHome.previous())
        assertEquals(ParentPermissionStep.DefaultHome,
            ParentPermissionStep.SleepingApps.nextRequired { ParentPermissionStatus.Required })
        assertNull(ParentPermissionStep.DefaultHome.next())
    }

    @Test fun backgroundLocationExplainsConsentAndHomeDoesNotRequestKnox() {
        assertEquals("동의하고 설정하기", ParentPermissionStep.BackgroundLocation.guideContent().button)
        assertTrue(ParentPermissionStep.BackgroundLocation.guideContent().description.contains("‘항상 허용’"))
        assertEquals("시니어On", ParentPermissionStep.DefaultHome.guideContent().emphasis)
    }
}
