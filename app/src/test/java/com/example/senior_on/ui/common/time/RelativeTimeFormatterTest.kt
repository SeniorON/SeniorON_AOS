package com.example.senior_on.ui.common.time

import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RelativeTimeFormatterTest {
    private val now = Instant.parse("2026-08-01T00:10:00Z")

    @Test
    fun `서버 연결 시각을 분 단위 상대시간으로 표시한다`() {
        val result = "2026-08-01T00:09:00Z".toRelativeTimeLabel(now)

        assertEquals("1분 전", result)
    }

    @Test
    fun `시간대 없는 서버 연결 시각도 상대시간으로 표시한다`() {
        val result = "2026-08-01T09:08:00".toRelativeTimeLabel(
            now = now,
            zoneId = ZoneId.of("Asia/Seoul"),
        )

        assertEquals("2분 전", result)
    }

    @Test
    fun `이미 가공된 문구는 그대로 유지한다`() {
        assertEquals("1분 전", "1분 전".toRelativeTimeLabel(now))
    }

    @Test
    fun `값이 없으면 null을 반환한다`() {
        assertNull(null.toRelativeTimeLabel(now))
    }
}
