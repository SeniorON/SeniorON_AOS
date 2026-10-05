package com.example.senior_on.ui.child.notification

import com.example.senior_on.domain.model.server.SafetyEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NotificationUiMapperTest {
    @Test
    fun `SOS without coordinates does not reuse an old address as title`() {
        val event = SafetyEvent(42L, "SOS", null, "old address", null, null, 72)
        val actual = event.toUiState(NotificationCategory.Sos,
            NotificationMessageUiState("", "cached address", severity = NotificationSeverity.Danger))
        assertEquals("긴급 도움 요청", actual.title)
        assertEquals(NotificationCategory.Sos, actual.category)
    }

    @Test
    fun `SOS history summary is hidden when location sharing is off or unknown`() {
        val message = com.example.senior_on.domain.model.server.AppNotification(
            1L, 42L, "긴급 알림", "private address", "2026-10-01T12:00:00", false,
        ).toUiState(NotificationCategory.Sos)
        listOf(
            NotificationScreenUiState(emptyList(), locationSharingEnabled = false),
            NotificationScreenUiState(emptyList(), sharingStatusKnown = false),
        ).forEach { assertEquals("긴급 도움 요청", it.visibleMessage(message).title) }
    }

    @Test
    fun `SOS home address title is hidden before detail is fetched`() {
        val home = com.example.senior_on.domain.model.server.NotificationHome(1, listOf(
            com.example.senior_on.domain.model.server.NotificationHomeItem(
                "SOS", true, true, null, null, "private summary", null, null,
                null, "private address", null, null, null,
            ),
        )).toUiState(true, true)
        val visible = home.copy(locationSharingEnabled = false).enforceAccess()
        assertEquals("긴급 도움 요청", visible.sections.first { it.category == NotificationCategory.Sos }.messages.single().title)
    }

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
