package com.example.senior_on.ui.child.notification

import com.example.senior_on.domain.model.server.SafetyEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NotificationUiMapperTest {
    @Test
    fun `SOS event detail keeps location and device information`() {
        val fallback = NotificationMessageUiState(
            time = "fallback",
            title = "fallback",
            severity = NotificationSeverity.Danger,
            eventId = 42L,
        )
        val event = SafetyEvent(
            id = 42L,
            type = "SOS",
            occurredAt = "2026-07-31T11:20:00Z",
            address = "서울특별시 성동구",
            latitude = 37.5634,
            longitude = 127.0369,
            deviceBattery = 72,
            message = "도움이 필요해요",
            senderName = "어머니",
        )

        val actual = event.toUiState(NotificationCategory.Sos, fallback)

        assertEquals("도움이 필요해요", actual.eventMessage)
        assertEquals("어머니", actual.senderName)
        assertEquals("서울특별시 성동구", actual.address)
        assertEquals(37.5634, actual.latitude!!, 0.0)
        assertEquals(127.0369, actual.longitude!!, 0.0)
        assertEquals(72, actual.deviceBattery)
    }

    @Test
    fun `SOS event detail parses server local date time`() {
        val fallback = NotificationMessageUiState(
            time = "fallback",
            title = "fallback",
            severity = NotificationSeverity.Danger,
            eventId = 41L,
        )
        val event = SafetyEvent(
            id = 41L,
            type = "SOS",
            occurredAt = "2026-07-31T21:32:43.646812",
            address = "서울 성동구 행당동 7",
            latitude = 37.563427,
            longitude = 127.0369339,
            deviceBattery = 100,
        )

        val actual = event.toUiState(NotificationCategory.Sos, fallback)

        assertNotNull(actual.occurredAtMillis)
    }
}
