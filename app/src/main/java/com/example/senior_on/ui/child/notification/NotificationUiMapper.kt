package com.example.senior_on.ui.child.notification

import com.example.senior_on.domain.model.server.AppNotification
import com.example.senior_on.domain.model.server.NotificationHome
import com.example.senior_on.domain.model.server.NotificationHomeItem
import com.example.senior_on.domain.model.server.SafetyEvent
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

internal fun emptyNotificationScreenUiState(): NotificationScreenUiState =
    NotificationScreenUiState(
        sections = NotificationCategory.entries.map { category ->
            NotificationSectionUiState(
                category = category,
                enabled = category == NotificationCategory.Sos,
            )
        },
    )

internal fun parentNotConnectedNotificationScreenUiState(): NotificationScreenUiState =
    NotificationScreenUiState(
        sections = NotificationCategory.entries.map { category ->
            NotificationSectionUiState(
                category = category,
                enabled = false,
            )
        },
        footerPanel = NotificationFooterPanelUiState(
            tone = NotificationFooterTone.Warning,
        ),
        isParentPhoneRegistered = false,
        isParentPhoneInternetConnected = false,
    )

internal fun NotificationHome.toUiState(
    isParentDeviceOnline: Boolean,
    hasHomeAddress: Boolean,
): NotificationScreenUiState {
    val itemsByCategory = items.mapNotNull { item ->
        item.type.toNotificationCategory()?.let { category -> category to item }
    }.toMap()

    return NotificationScreenUiState(
        sections = NotificationCategory.entries.map { category ->
            val item = itemsByCategory[category]
            NotificationSectionUiState(
                category = category,
                enabled = if (category == NotificationCategory.Sos) {
                    true
                } else {
                    item?.enabled == true
                },
                messages = item
                    ?.takeIf(NotificationHomeItem::hasAlert)
                    ?.let { listOf(it.toMessageUiState(category)) }
                    .orEmpty(),
            )
        },
        footerPanel = null,
        isParentPhoneRegistered = true,
        hasHomeAddress = hasHomeAddress,
        isParentPhoneInternetConnected = isParentDeviceOnline,
    )
}

internal fun AppNotification.toUiState(
    category: NotificationCategory,
): NotificationMessageUiState =
    NotificationMessageUiState(
        time = occurredAt,
        title = summary.ifBlank { title },
        severity = category.severity,
        tintBackground = category != NotificationCategory.Outing,
        occurredAtMillis = occurredAt.toEpochMillisOrNull(),
        movementType = category.outingMovementFrom(summary.ifBlank { title }),
        notificationId = id,
        eventId = eventId,
        isRead = read,
    )

internal val NotificationCategory.apiType: String
    get() = when (this) {
        NotificationCategory.Sos -> "SOS"
        NotificationCategory.Inactivity -> "INACTIVITY"
        NotificationCategory.RiskLink -> "RISK_LINK"
        NotificationCategory.Outing -> "OUTING_RETURN"
    }

private fun NotificationHomeItem.toMessageUiState(
    category: NotificationCategory,
): NotificationMessageUiState {
    val messageTitle = when (category) {
        NotificationCategory.Sos -> address ?: summary
        NotificationCategory.RiskLink -> linkUrl ?: summary
        else -> summary
    }.orEmpty()

    return NotificationMessageUiState(
        time = dateTimeLabel ?: occurredAt.orEmpty(),
        title = messageTitle,
        detail = if (category == NotificationCategory.Inactivity) summary else null,
        severity = category.severity,
        tintBackground = category != NotificationCategory.Outing,
        occurredAtMillis = occurredAt.toEpochMillisOrNull(),
        movementType = category.outingMovementFrom(phase),
        notificationId = notificationId,
        eventId = eventId,
    )
}

internal fun SafetyEvent.toUiState(
    category: NotificationCategory,
    fallback: NotificationMessageUiState,
): NotificationMessageUiState {
    val resolvedTitle = when (category) {
        NotificationCategory.Sos -> address ?: message
        NotificationCategory.Inactivity -> message
        NotificationCategory.RiskLink -> linkUrl ?: message
        NotificationCategory.Outing -> message
    }.orEmpty().ifBlank { fallback.title }

    return fallback.copy(
        time = occurredAt ?: fallback.time,
        title = resolvedTitle,
        detail = when (category) {
            NotificationCategory.Inactivity -> message ?: lastSeenAt ?: fallback.detail
            else -> fallback.detail
        },
        severity = if (
            category == NotificationCategory.RiskLink && dangerous == false
        ) {
            NotificationSeverity.Normal
        } else {
            category.severity
        },
        occurredAtMillis = occurredAt.toEpochMillisOrNull()
            ?: fallback.occurredAtMillis,
        movementType = category.outingMovementFrom(phase)
            ?: fallback.movementType,
        eventId = id ?: fallback.eventId,
        eventMessage = message,
        senderName = senderName,
        address = address,
        latitude = latitude,
        longitude = longitude,
        deviceBattery = deviceBattery,
        lastSeenAt = lastSeenAt,
    )
}

private val NotificationCategory.severity: NotificationSeverity
    get() = if (this == NotificationCategory.Outing) {
        NotificationSeverity.Normal
    } else {
        NotificationSeverity.Danger
    }

private fun NotificationCategory.outingMovementFrom(
    value: String?,
): NotificationMovementType? {
    if (this != NotificationCategory.Outing) return null
    return when (value?.trim()?.uppercase()) {
        "RETURN", "RETURNED", "RETURN_HOME", "RETURNED_HOME", "IN" ->
            NotificationMovementType.ReturnedHome
        else -> NotificationMovementType.LeftHome
    }
}

private fun String?.toNotificationCategory(): NotificationCategory? =
    when (this?.trim()?.uppercase()) {
        "SOS" -> NotificationCategory.Sos
        "INACTIVITY" -> NotificationCategory.Inactivity
        "RISK_LINK" -> NotificationCategory.RiskLink
        "OUTING_RETURN" -> NotificationCategory.Outing
        else -> null
    }

private fun String?.toEpochMillisOrNull(): Long? =
    this?.let { value ->
        runCatching { Instant.parse(value).toEpochMilli() }
            .recoverCatching {
                LocalDateTime.parse(value)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }
            .getOrNull()
    }
