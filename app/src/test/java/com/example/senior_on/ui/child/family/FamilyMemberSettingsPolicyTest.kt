package com.example.senior_on.ui.child.family

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FamilyMemberSettingsPolicyTest {
    @Test
    fun `승격할 수 없는 보조 담당자도 구성원 설정 목록에 표시한다`() {
        val members = listOf(
            familyMember(
                id = "1",
                role = FamilyCaregiverRole.Primary,
                canBecomePrimary = false,
            ),
            familyMember(
                id = "2",
                role = FamilyCaregiverRole.Assistant,
                canBecomePrimary = true,
            ),
            familyMember(
                id = "3",
                role = FamilyCaregiverRole.Assistant,
                canBecomePrimary = false,
            ),
        )

        val assistants = members.assistantMembersForSettings()

        assertEquals(listOf("2", "3"), assistants.map(FamilyMemberUiModel::id))
        assertFalse(assistants.last().canBecomePrimary)
    }
}

private fun familyMember(
    id: String,
    role: FamilyCaregiverRole,
    canBecomePrimary: Boolean,
) = FamilyMemberUiModel(
    id = id,
    name = "구성원$id",
    role = role,
    canBecomePrimary = canBecomePrimary,
)
