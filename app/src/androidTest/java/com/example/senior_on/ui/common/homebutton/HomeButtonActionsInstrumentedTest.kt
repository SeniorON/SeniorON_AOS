package com.example.senior_on.ui.common.homebutton

import android.content.Intent
import android.content.pm.PackageManager
import android.provider.AlarmClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeButtonActionsInstrumentedTest {
    @Test
    fun calculatorUsesAndroidMainSelector() {
        val calculatorIntent = createDefaultHomeButtonIntents("CALCULATOR").single()

        assertEquals(Intent.ACTION_MAIN, calculatorIntent.action)
        assertTrue(
            calculatorIntent.selector
                ?.categories
                .orEmpty()
                .contains(Intent.CATEGORY_APP_CALCULATOR),
        )
    }

    @Test
    fun timerShowsTimersBeforeFallingBackToSetTimer() {
        assertEquals(
            listOf(AlarmClock.ACTION_SHOW_TIMERS, AlarmClock.ACTION_SET_TIMER),
            createDefaultHomeButtonIntents("TIMER").map(Intent::getAction),
        )
    }

    @Test
    fun alarmAndTimerPermissionIsDeclared() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val requestedPermissions = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS,
        ).requestedPermissions.orEmpty()

        assertTrue("com.android.alarm.permission.SET_ALARM" in requestedPermissions)
    }

    @Test
    fun memoSupportsModernAndLegacyNoteApps() {
        val memoIntents = createDefaultHomeButtonIntents("MEMO")

        assertEquals("android.intent.action.CREATE_NOTE", memoIntents[0].action)
        assertEquals("com.google.android.gms.actions.CREATE_NOTE", memoIntents[1].action)
        assertEquals("text/plain", memoIntents[1].type)
    }

    @Test
    fun internetSupportsWebLinksAndBrowserSelector() {
        val internetIntents = createDefaultHomeButtonIntents("INTERNET")

        assertEquals("https", internetIntents[0].data?.scheme)
        assertTrue(internetIntents[0].categories.contains(Intent.CATEGORY_BROWSABLE))
        assertTrue(
            internetIntents[1].selector
                ?.categories
                .orEmpty()
                .contains(Intent.CATEGORY_APP_BROWSER),
        )
    }
}
