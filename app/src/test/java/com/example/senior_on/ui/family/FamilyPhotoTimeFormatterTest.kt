package com.example.senior_on.ui.family

import java.time.Instant
import java.time.temporal.ChronoUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class FamilyPhotoTimeFormatterTest {
    private val now = Instant.parse("2026-07-17T08:30:00Z")

    @Test
    fun `업로드 후 4분이면 4분전으로 표시한다`() {
        val createdAt = now.minus(4, ChronoUnit.MINUTES)

        assertEquals("4분전", createdAt.toRelativeTime(now))
    }

    @Test
    fun `업로드 후 2시간이면 2시간전으로 표시한다`() {
        val createdAt = now.minus(2, ChronoUnit.HOURS)

        assertEquals("2시간전", createdAt.toRelativeTime(now))
    }
}
