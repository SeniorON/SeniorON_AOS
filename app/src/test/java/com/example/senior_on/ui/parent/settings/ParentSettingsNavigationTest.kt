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

    @Test fun disconnectCopyExplainsAllDevicesAndPreservedFamilyMembership() {
        assertEquals("연결을 해제할까요?", ParentSettingsConfirmation.Disconnect.title)
        assertEquals("내 계정의 모든 기기 연결이 해제돼요.\n가족 관계는 유지돼요.", ParentSettingsConfirmation.Disconnect.description)
    }
}
