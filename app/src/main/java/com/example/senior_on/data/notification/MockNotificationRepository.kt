package com.example.senior_on.data.notification

import com.example.senior_on.ui.notification.NotificationCategory
import com.example.senior_on.ui.notification.NotificationFooterPanelUiState
import com.example.senior_on.ui.notification.NotificationFooterTone
import com.example.senior_on.ui.notification.NotificationMessageUiState
import com.example.senior_on.ui.notification.NotificationMovementType
import com.example.senior_on.ui.notification.NotificationScreenUiState
import com.example.senior_on.ui.notification.NotificationSectionUiState
import com.example.senior_on.ui.notification.NotificationSeverity

object MockNotificationRepository {
    fun getNotificationHistory(
        category: NotificationCategory
    ): List<NotificationMessageUiState> {
        val now = System.currentTimeMillis()

        return listOf(0L, 1L, 3L, 5L).mapIndexed { index, daysAgo ->
            NotificationMessageUiState(
                time = when {
                    category == NotificationCategory.Inactivity && index == 0 -> "오늘 오전 10:00부터"
                    index == 0 -> "오늘 오전 10:00"
                    category == NotificationCategory.Outing && index == 3 -> "오후 10:00"
                    else -> "오전 10:00"
                },
                title = when (category) {
                    NotificationCategory.Sos -> "어머니 · 경기도 하남시 창우동"
                    NotificationCategory.Inactivity -> "4시간 미사용 감지됨"
                    NotificationCategory.RiskLink -> "http://fake-bank.xyz"
                    NotificationCategory.Outing -> if (index == 3) {
                        "귀가하셨어요"
                    } else {
                        "외출하셨어요"
                    }
                },
                severity = if (category == NotificationCategory.Outing) {
                    NotificationSeverity.Normal
                } else {
                    NotificationSeverity.Danger
                },
                tintBackground = category != NotificationCategory.Outing,
                occurredAtMillis = now - daysAgo * ONE_DAY_MILLIS,
                movementType = when {
                    category != NotificationCategory.Outing -> null
                    index == 3 -> NotificationMovementType.ReturnedHome
                    else -> NotificationMovementType.LeftHome
                }
            )
        }
    }

    fun getNotificationState(
        scenario: MockNotificationScenario = MockNotificationScenario.NoAlarm
    ): NotificationScreenUiState {
        val now = System.currentTimeMillis()
        val isParentPhoneRegistered = scenario != MockNotificationScenario.ParentPhoneNotRegistered

        return NotificationScreenUiState(
            sections = buildSections(
                messagesByCategory = scenario.messagesByCategory(now),
                scenario = scenario,
                isParentPhoneRegistered = isParentPhoneRegistered
            ),
            footerPanel = if (scenario == MockNotificationScenario.ParentPhoneNotRegistered) {
                NotificationFooterPanelUiState(tone = NotificationFooterTone.Warning)
            } else {
                null
            },
            isParentPhoneRegistered = isParentPhoneRegistered,
            hasHomeAddress = scenario != MockNotificationScenario.HomeAddressMissing,
            isParentPhoneInternetConnected = scenario != MockNotificationScenario.ParentPhoneOffline
        )
    }

    private fun buildSections(
        messagesByCategory: Map<NotificationCategory, List<NotificationMessageUiState>>,
        scenario: MockNotificationScenario,
        isParentPhoneRegistered: Boolean
    ): List<NotificationSectionUiState> =
        NotificationCategory.entries.map { category ->
            NotificationSectionUiState(
                category = category,
                enabled = when {
                    !isParentPhoneRegistered -> false
                    category == NotificationCategory.Sos -> true
                    scenario == MockNotificationScenario.RecentAlarms ||
                        scenario == MockNotificationScenario.MultipleRecentAlarms -> true
                    else -> false
                },
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
    private const val ONE_DAY_MILLIS = 24L * ONE_HOUR_MILLIS
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
