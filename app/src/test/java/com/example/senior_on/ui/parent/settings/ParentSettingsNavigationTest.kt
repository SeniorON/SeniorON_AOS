package com.example.senior_on.ui.parent.settings

import com.example.senior_on.ui.parent.permission.ParentPermissionStep
import com.example.senior_on.ui.parent.permission.guideContent
import org.junit.Assert.*
import org.junit.Test

class ParentSettingsNavigationTest {
    @Test fun guideStepsHaveBoundedNavigationAndDistinctIds() {
        assertEquals(6, ParentPermissionStep.entries.size)
        assertNull(ParentPermissionStep.entries.first().previous())
        assertNull(ParentPermissionStep.entries.last().next())
        ParentPermissionStep.entries.dropLast(1).forEach { assertEquals(it, it.next()?.previous()) }
    }

    @Test fun eachGuideContainsEmphasisAndIndependentMediaDimensions() {
        ParentPermissionStep.entries.forEach {
            val content = it.guideContent()
            assertTrue(content.title.contains(content.emphasis))
            assertTrue(content.description.isNotBlank())
            assertTrue(content.mediaAspectRatio > 0)
        }
        assertTrue(ParentPermissionStep.entries.map { it.guideContent().mediaAspectRatio }.distinct().size > 1)
    }

    @Test fun nestedSettingsReturnToTheirOwningScreen() {
        assertEquals(ParentSettingsDestination.Account, ParentSettingsDestination.ChangeName.back())
        assertEquals(ParentSettingsDestination.Account, ParentSettingsDestination.ChangePassword.back())
        assertEquals(ParentSettingsDestination.Main, ParentSettingsDestination.PermissionControl.back())
        assertEquals(ParentSettingsDestination.Main, ParentSettingsDestination.ShareCode.back())
    }

    @Test fun disconnectCopyMatchesDeviceConnectionDesign() {
        assertEquals("연결을 해제할까요?", ParentSettingsConfirmation.Disconnect.title)
        assertEquals("자녀와의 연결이 끊어져요.", ParentSettingsConfirmation.Disconnect.description)
    }
}
