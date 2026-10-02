package com.example.senior_on.ui.child.family

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyMemberSettingsPolicyTest {
    @Test
    fun `시니어 연결 관리는 현재 사용자가 주 담당자일 때만 허용한다`() {
        val primaryState = FamilyTabUiState(
            members = listOf(
                familyMember("1", FamilyCaregiverRole.Primary, false, isCurrentUser = true),
            ),
        )
        val assistantState = FamilyTabUiState(
            members = listOf(
                familyMember("2", FamilyCaregiverRole.Assistant, true, isCurrentUser = true),
            ),
        )

        assertTrue(primaryState.canManageSeniorConnections)
        assertFalse(assistantState.canManageSeniorConnections)
    }

    @Test
    fun `구성원을 선택하기 전에는 변경과 삭제가 로딩 상태가 아니다`() {
        assertFalse(isSelectedMemberMutationLoading(null, null))
    }

    @Test
    fun `선택한 구성원의 작업만 로딩 상태로 표시한다`() {
        assertFalse(isSelectedMemberMutationLoading("2", "3"))
        assertEquals(true, isSelectedMemberMutationLoading("2", "2"))
    }

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
    isCurrentUser: Boolean = false,
) = FamilyMemberUiModel(
    id = id,
    name = "구성원$id",
    role = role,
    canBecomePrimary = canBecomePrimary,
    isCurrentUser = isCurrentUser,
)
