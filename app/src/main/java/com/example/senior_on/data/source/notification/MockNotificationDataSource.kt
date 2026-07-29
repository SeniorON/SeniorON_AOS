package com.example.senior_on.data.source.notification

import com.example.senior_on.data.remote.dto.InactivitySettingRequest
import com.example.senior_on.data.remote.dto.InactivitySettingResponse
import com.example.senior_on.data.remote.dto.NotificationHomeListResponse
import com.example.senior_on.data.remote.dto.NotificationHomeResponse
import com.example.senior_on.data.remote.dto.NotificationItem
import com.example.senior_on.data.remote.dto.NotificationListResponse
import com.example.senior_on.data.remote.dto.NotificationSettingRequest
import com.example.senior_on.data.remote.dto.NotificationSettingResponse
import com.example.senior_on.data.remote.dto.ParentDeviceStatusResponse
import com.example.senior_on.data.source.mock.fixtures.MockUserFixtures
import java.time.Instant
import java.time.temporal.ChronoUnit

class MockNotificationDataSource(
    private val scenario: MockNotificationScenario =
        MockNotificationScenario.MultipleRecentAlarms,
) : NotificationDataSource {
    private val enabledSettings = mutableMapOf(
        "SOS" to true,
        "INACTIVITY" to true,
        "RISK_LINK" to true,
        "OUTING_RETURN" to true,
    )
    private val readNotificationIds = mutableSetOf<Long>()
    private val deletedNotificationIds = mutableSetOf<Long>()
    private var inactivityHours = DefaultInactivityHours

    override suspend fun getNotifications(
        type: String,
        cursor: Long?,
        size: Int?,
    ): NotificationListResponse {
        val pageSize = size ?: DefaultPageSize
        val items = history(type.uppercase())
            .filterNot { it.notificationId in deletedNotificationIds }
            .filter { cursor == null || (it.notificationId ?: 0L) < cursor }
            .take(pageSize)
            .map { item ->
                item.copy(read = item.notificationId in readNotificationIds)
            }
        return NotificationListResponse(
            totalCount = history(type.uppercase())
                .count { it.notificationId !in deletedNotificationIds },
            items = items,
            nextCursor = items.lastOrNull()?.notificationId
                ?.takeIf { items.size == pageSize },
        )
    }

    override suspend fun markRead(id: Long) {
        readNotificationIds += id
    }

    override suspend fun delete(id: Long) {
        deletedNotificationIds += id
    }

    override suspend fun getSettings(): NotificationHomeListResponse {
        val items = NotificationTypes.map { type ->
            val latest = history(type)
                .filterNot { it.notificationId in deletedNotificationIds }
                .firstOrNull()
            NotificationHomeResponse(
                type = type,
                enabled = enabledSettings[type] == true,
                hasAlert = latest != null,
                occurredAt = latest?.occurredAt,
                dateTimeLabel = latest?.occurredAt?.let { "최근 알림" },
                summary = latest?.summary,
                senderId = MockParentUserId,
                senderName = "어머니",
                deviceBattery = 78,
                address = if (type == "SOS") "서울특별시 강동구" else null,
                linkUrl = if (type == "RISK_LINK") "http://fake-bank.xyz" else null,
                phase = if (type == "OUTING_RETURN") "OUTING" else null,
                emptyMessage = if (latest == null) "최근 알림이 없습니다." else null,
            )
        }
        return NotificationHomeListResponse(
            enabledCount = items.count { it.enabled == true },
            items = items,
        )
    }

    override suspend fun updateSetting(
        type: String,
        request: NotificationSettingRequest,
    ): NotificationSettingResponse {
        enabledSettings[type.uppercase()] = request.enabled
        return NotificationSettingResponse(
            enabled = request.enabled,
            type = type.uppercase(),
        )
    }

    override suspend fun getParentDeviceStatus(): ParentDeviceStatusResponse {
        return ParentDeviceStatusResponse(
            online = scenario != MockNotificationScenario.ParentPhoneOffline &&
                scenario != MockNotificationScenario.ParentPhoneNotRegistered,
        )
    }

    override suspend fun getInactivitySetting(
        userId: Long,
    ): InactivitySettingResponse {
        return InactivitySettingResponse(
            usersId = userId,
            thresholdHours = inactivityHours,
            isEnabled = enabledSettings["INACTIVITY"] == true,
        )
    }

    override suspend fun updateInactivitySetting(
        userId: Long,
        request: InactivitySettingRequest,
    ): InactivitySettingResponse {
        inactivityHours = request.thresholdHours.coerceIn(1, 24)
        return getInactivitySetting(userId)
    }

    private fun history(type: String): List<NotificationItem> {
        if (scenario in EmptyScenarios) return emptyList()

        val now = Instant.now()
        val baseId = when (type) {
            "SOS" -> 400L
            "INACTIVITY" -> 300L
            "RISK_LINK" -> 200L
            "OUTING_RETURN" -> 100L
            else -> return emptyList()
        }
        val summaries = when (type) {
            "SOS" -> listOf("어머니 · 서울특별시 강동구", "어머니 · 서울특별시 송파구")
            "INACTIVITY" -> listOf("4시간 동안 활동이 감지되지 않았어요.")
            "RISK_LINK" -> listOf("http://fake-bank.xyz")
            "OUTING_RETURN" -> listOf("외출하셨어요.", "귀가하셨어요.")
            else -> emptyList()
        }
        return summaries.mapIndexed { index, summary ->
            val hoursAgo = if (
                scenario == MockNotificationScenario.ExpiredAlarms
            ) {
                49L + index
            } else {
                1L + index * 2L
            }
            NotificationItem(
                notificationId = baseId - index,
                eventId = baseId + 1_000L - index,
                title = summary,
                summary = summary,
                occurredAt = now.minus(hoursAgo, ChronoUnit.HOURS).toString(),
                read = false,
            )
        }
    }

    companion object {
        fun scenarioForUserId(userId: String): MockNotificationScenario {
            return when (userId.trim().lowercase()) {
                MockUserFixtures.ASSISTANT_CAREGIVER_USER_ID ->
                    MockNotificationScenario.NoAlarm
                else -> MockNotificationScenario.MultipleRecentAlarms
            }
        }

        private val NotificationTypes = listOf(
            "SOS",
            "INACTIVITY",
            "RISK_LINK",
            "OUTING_RETURN",
        )
        private val EmptyScenarios = setOf(
            MockNotificationScenario.NoAlarm,
            MockNotificationScenario.HomeAddressMissing,
            MockNotificationScenario.ParentPhoneOffline,
            MockNotificationScenario.ParentPhoneNotRegistered,
        )
        private const val DefaultInactivityHours = 4
        private const val DefaultPageSize = 20
        private const val MockParentUserId = 101L
    }
}

enum class MockNotificationScenario {
    NoAlarm,
    RecentAlarms,
    ExpiredAlarms,
    MultipleRecentAlarms,
    HomeAddressMissing,
    ParentPhoneOffline,
    ParentPhoneNotRegistered,
}
