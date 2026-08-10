package com.example.senior_on.ui.child.family

import org.junit.Assert.assertEquals
import org.junit.Test

class FamilyInvitationShareContentTest {
    @Test
    fun `초대 공유 문구에 가족 코드와 안내를 포함한다`() {
        val content = familyInvitationShareContent("40B6-4571")

        assertEquals(
            """
            SeniorON 가족으로 초대했어요.
            가족 공유 코드: 40B6-4571
            앱에서 코드를 입력해 가족을 연결해주세요.
            """.trimIndent(),
            content.text,
        )
        assertEquals(
            mapOf("familyCode" to "40B6-4571"),
            content.androidExecutionParams,
        )
    }
}
