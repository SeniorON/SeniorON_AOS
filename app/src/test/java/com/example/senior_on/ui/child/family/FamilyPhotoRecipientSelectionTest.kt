package com.example.senior_on.ui.child.family

import com.example.senior_on.domain.model.server.ServerConnectedSenior
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
    fun `현재 시니어의 기본 그룹을 연결된 시니어보다 앞에 표시한다`() {
        val currentSenior = recipient(photoGroupId = 10L, seniorId = 7L)
        val connectedSenior = recipient(photoGroupId = 20L, seniorId = 8L)

        val recipients = familyPhotoRecipients(
            currentSenior = currentSenior,
            connectedSeniors = listOf(connectedSenior),
        )

        assertEquals(listOf(10L, 20L), recipients.map { it.photoGroupId })
    }

    private fun recipient(photoGroupId: Long, seniorId: Long) = ServerConnectedSenior(
        photoGroupId = photoGroupId,
        seniorId = seniorId,
        name = "시니어",
        relationshipLabel = "가족",
        connectedAt = "",
    )
}
