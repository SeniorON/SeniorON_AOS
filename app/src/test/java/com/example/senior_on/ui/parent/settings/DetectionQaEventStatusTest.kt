package com.example.senior_on.ui.parent.settings

import com.example.senior_on.device.PendingInactivity
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class DetectionQaEventStatusTest {
    @Test fun `대기와 활동 재개 만료 완료 상태를 구분한다`() {
        val event = PendingInactivity("a", 100, 200)
        assertTrue(qaInactivityEventStatus(event, 100, 300).startsWith("대기 중"))
        assertTrue(qaInactivityEventStatus(event, 250, 300).startsWith("활동 재개"))
        assertTrue(qaInactivityEventStatus(event, 100, 200 + TimeUnit.HOURS.toMillis(48)).startsWith("대기 기한 경과"))
        assertTrue(qaInactivityEventStatus(event.copy(status = "sent"), 250, 300).startsWith("모의 전송 완료"))
        assertTrue(qaInactivityEventStatus(event.copy(status = "expired"), 100, 300).startsWith("만료 처리 완료"))
    }
}
