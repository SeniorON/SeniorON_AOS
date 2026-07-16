package com.example.senior_on.data.notification

import com.example.senior_on.ui.notification.NotificationCategory
import com.example.senior_on.ui.notification.NotificationFooterPanelUiState
import com.example.senior_on.ui.notification.NotificationFooterTone
import com.example.senior_on.ui.notification.NotificationMessageUiState
import com.example.senior_on.ui.notification.NotificationScreenUiState
import com.example.senior_on.ui.notification.NotificationSectionUiState
import com.example.senior_on.ui.notification.NotificationSeverity

object MockNotificationRepository {
    fun getNotificationState(
        scenario: MockNotificationScenario = MockNotificationScenario.NoAlarm
    ): NotificationScreenUiState {
        val now = System.currentTimeMillis()

        return NotificationScreenUiState(
            sections = buildSections(messagesByCategory = scenario.messagesByCategory(now)),
            footerPanel = if (scenario == MockNotificationScenario.ParentPhoneNotRegistered) {
                NotificationFooterPanelUiState(tone = NotificationFooterTone.Warning)
            } else {
                null
            },
            hasHomeAddress = scenario != MockNotificationScenario.HomeAddressMissing,
            isParentPhoneInternetConnected = scenario != MockNotificationScenario.ParentPhoneOffline
        )
    }

    private fun buildSections(
        messagesByCategory: Map<NotificationCategory, List<NotificationMessageUiState>>
    ): List<NotificationSectionUiState> =
        NotificationCategory.entries.map { category ->
            NotificationSectionUiState(
                category = category,
                enabled = false,
                detectionStandardTime = if (category == NotificationCategory.Inactivity) {
                    "4시간"
                } else {
                    null
                },
                messages = messagesByCategory[category].orEmpty()
            )
        }

    private fun MockNotificationScenario.messagesByCategory(
        now: Long
    ): Map<NotificationCategory, List<NotificationMessageUiState>> =
        when (this) {
            MockNotificationScenario.NoAlarm,
            MockNotificationScenario.HomeAddressMissing,
            MockNotificationScenario.ParentPhoneOffline,
            MockNotificationScenario.ParentPhoneNotRegistered -> emptyMap()

            MockNotificationScenario.RecentAlarms -> recentAlarms(now)

            MockNotificationScenario.ExpiredAlarms -> recentAlarms(now).mapValues { (_, messages) ->
                messages.map { message ->
                    message.copy(occurredAtMillis = now - 49L * ONE_HOUR_MILLIS)
                }
            }

            MockNotificationScenario.MultipleRecentAlarms -> multipleRecentAlarms(now)
        }

    private fun recentAlarms(now: Long): Map<NotificationCategory, List<NotificationMessageUiState>> =
        mapOf(
            NotificationCategory.Sos to listOf(
                NotificationMessageUiState(
                    time = "오늘 오전 10:00",
                    title = "어머니 · 경기도 하남시 창우동",
                    severity = NotificationSeverity.Danger,
                    occurredAtMillis = now - ONE_HOUR_MILLIS
                )
            ),
            NotificationCategory.Inactivity to listOf(
                NotificationMessageUiState(
                    time = "어제 오전 10:00부터",
                    title = "4시간 미사용 감지됨",
                    severity = NotificationSeverity.Danger,
                    occurredAtMillis = now - 25L * ONE_HOUR_MILLIS
                )
            ),
            NotificationCategory.RiskLink to listOf(
                NotificationMessageUiState(
                    time = "오늘 오전 10:00",
                    title = "http://fake-bank.xyz",
                    severity = NotificationSeverity.Danger,
                    occurredAtMillis = now - ONE_HOUR_MILLIS
                )
            ),
            NotificationCategory.Outing to listOf(
                NotificationMessageUiState(
                    time = "오늘 오전 10:00",
                    title = "외출 나가셨어요",
                    severity = NotificationSeverity.Normal,
                    tintBackground = false,
                    occurredAtMillis = now - ONE_HOUR_MILLIS
                )
            )
        )

    private fun multipleRecentAlarms(
        now: Long
    ): Map<NotificationCategory, List<NotificationMessageUiState>> =
        mapOf(
            NotificationCategory.Sos to listOf(
                NotificationMessageUiState(
                    time = "오늘 오전 10:00",
                    title = "어머니 · 경기도 하남시 창우동",
                    severity = NotificationSeverity.Danger,
                    occurredAtMillis = now - ONE_HOUR_MILLIS
                ),
                NotificationMessageUiState(
                    time = "오늘 오전 8:00",
                    title = "어머니 · 서울시 강동구",
                    severity = NotificationSeverity.Danger,
                    occurredAtMillis = now - 3L * ONE_HOUR_MILLIS
                )
            ),
            NotificationCategory.Inactivity to listOf(
                NotificationMessageUiState(
                    time = "오늘 오전 9:30부터",
                    title = "4시간 미사용 감지됨",
                    severity = NotificationSeverity.Danger,
                    occurredAtMillis = now - 2L * ONE_HOUR_MILLIS
                ),
                NotificationMessageUiState(
                    time = "3일 전 오전 10:00부터",
                    title = "4시간 미사용 감지됨",
                    severity = NotificationSeverity.Danger,
                    occurredAtMillis = now - 72L * ONE_HOUR_MILLIS
                )
            ),
            NotificationCategory.RiskLink to listOf(
                NotificationMessageUiState(
                    time = "오늘 오전 10:00",
                    title = "http://fake-bank.xyz",
                    severity = NotificationSeverity.Danger,
                    occurredAtMillis = now - ONE_HOUR_MILLIS
                )
            ),
            NotificationCategory.Outing to listOf(
                NotificationMessageUiState(
                    time = "오늘 오전 10:00",
                    title = "외출 나가셨어요",
                    severity = NotificationSeverity.Normal,
                    tintBackground = false,
                    occurredAtMillis = now - ONE_HOUR_MILLIS
                )
            )
        )

    private const val ONE_HOUR_MILLIS = 60L * 60L * 1000L
}

enum class MockNotificationScenario {
    NoAlarm,
    RecentAlarms,
    ExpiredAlarms,
    MultipleRecentAlarms,
    HomeAddressMissing,
    ParentPhoneOffline,
    ParentPhoneNotRegistered
}
