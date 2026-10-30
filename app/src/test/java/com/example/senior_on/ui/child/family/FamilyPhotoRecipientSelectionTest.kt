package com.example.senior_on.ui.child.family

import com.example.senior_on.domain.model.server.ServerPhotoTarget
import org.junit.Assert.assertEquals
import org.junit.Test

class FamilyPhotoRecipientSelectionTest {
    @Test
    fun `시니어를 선택하면 기존 선택 목록에 추가한다`() {
        assertEquals(
            listOf(1L, 2L),
            togglePhotoRecipientSelection(listOf(1L), 2L),
        )
    }

    @Test
    fun `이미 선택한 시니어를 누르면 선택 목록에서 제거한다`() {
        assertEquals(
            listOf(2L),
            togglePhotoRecipientSelection(listOf(1L, 2L), 1L),
        )
    }

    @Test
    fun `선택 수에 따라 공유 버튼 문구를 만든다`() {
        assertEquals("받을 분을 선택해 주세요", recipientShareButtonText(0))
        assertEquals("2명에게 공유하기", recipientShareButtonText(2))
    }

    @Test
    fun `사진 대상 응답의 중복 시니어를 한 번만 표시한다`() {
        val first = recipient(seniorId = 7L)
        val duplicate = recipient(seniorId = 7L)

        val recipients = familyPhotoRecipients(listOf(first, duplicate))

        assertEquals(listOf(7L), recipients.map { it.seniorId })
        assertEquals(7L, recipients.single().seniorId)
    }

    private fun recipient(seniorId: Long) = ServerPhotoTarget(
        seniorId = seniorId,
        name = "시니어",
        relationshipLabel = "가족",
    )
}
