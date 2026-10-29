package com.example.senior_on.ui.child.notification

import com.example.senior_on.domain.model.server.SafetyEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class OutingDetailPresentationTest {
    private fun detail(phase: String, detectionType: String?, message: String? = null) =
        SafetyEvent(
            1L, "OUTING_RETURN", "2026-10-26T12:00:00+09:00", null, null, null, null,
            phase = phase, message = message, detectionType = detectionType,
        ).toUiState(NotificationCategory.Outing,
            NotificationMessageUiState("", "", severity = NotificationSeverity.Normal))

    @Test
    fun `reconnection keeps server message and shows confirmation time`() {
        listOf("OUTING" to "집 밖에 있는 것이 확인됐어요", "RETURN" to "집에 있는 것이 확인됐어요")
            .forEach { (phase, text) ->
                val actual = detail(phase, "RECONNECTION", text)
                assertEquals("RECONNECTION", actual.detectionType)
                assertEquals(text, actual.outingDetailTitle())
                assertEquals("확인 시간: ", actual.outingDetailTimeLabel())
            }
    }

    @Test
    fun `transition and legacy details retain movement time labels`() {
        listOf("TRANSITION", null).forEach { type ->
            assertEquals("외출 시간: ", detail("OUTING", type).outingDetailTimeLabel())
            assertEquals("귀가 시간: ", detail("RETURN", type).outingDetailTimeLabel())
        }
    }

    @Test
    fun `missing message uses detection aware fallback`() {
        assertEquals("집 밖에 있는 것이 확인됐어요", detail("OUTING", "RECONNECTION", " ").outingDetailTitle())
        assertEquals("집에 있는 것이 확인됐어요", detail("RETURN", "RECONNECTION").outingDetailTitle())
        assertEquals("외출하셨어요", detail("OUTING", null).outingDetailTitle())
        assertEquals("귀가하셨어요", detail("RETURN", null).outingDetailTitle())
    }
}
