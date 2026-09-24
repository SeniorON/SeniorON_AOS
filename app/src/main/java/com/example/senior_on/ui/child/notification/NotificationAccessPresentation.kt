package com.example.senior_on.ui.child.notification

/** This is not an Android permission or the guardian's notification toggle state. */
internal fun NotificationScreenUiState.accessWarningPanel(): NotificationFooterPanelUiState? {
    val senior = seniorDisplayName.ifBlank { "시니어" }
    return when {
        !isParentPhoneRegistered -> NotificationFooterPanelUiState(
            tone = NotificationFooterTone.Warning,
            title = "$senior 폰이 연결되지 않았어요.",
        )
        !sharingStatusKnown -> NotificationFooterPanelUiState(
            tone = NotificationFooterTone.Warning,
            title = "공유 상태를 확인하지 못했어요.",
            description = "당겨서 새로고침해 다시 확인해 주세요.",
        )
        isSeniorSharingRevoked || !locationSharingEnabled || !inactivitySharingEnabled -> NotificationFooterPanelUiState(
            tone = NotificationFooterTone.Warning,
            title = "${senior}가 권한을 해제했어요.",
            description = listOfNotNull(
                "무응답 감지".takeIf { isSeniorSharingRevoked || !inactivitySharingEnabled },
                "위치 알림".takeIf { isSeniorSharingRevoked || !locationSharingEnabled },
            ).joinToString(", ") + " 권한이 꺼져있어요.\n${senior}께 권한을 다시 켜달라고 요청해보세요.",
        )
        else -> null
    }
}

internal fun NotificationScreenUiState.canAccess(category: NotificationCategory): Boolean =
    isParentPhoneRegistered && when (category) {
        NotificationCategory.Inactivity -> sharingStatusKnown && !isSeniorSharingRevoked && inactivitySharingEnabled
        NotificationCategory.Outing -> sharingStatusKnown && !isSeniorSharingRevoked && locationSharingEnabled
        else -> true
    }

internal fun NotificationScreenUiState.visibleMessage(message: NotificationMessageUiState): NotificationMessageUiState {
    val visible = if (isParentPhoneRegistered && sharingStatusKnown && locationSharingEnabled && !isSeniorSharingRevoked) message
    else message.copy(address = null, latitude = null, longitude = null,
        lastLocationUpdatedAtMillis = null, detail = null, eventMessage = null)
    return if (canAccess(NotificationCategory.Inactivity)) visible else visible.copy(lastSeenAt = null)
}

internal fun NotificationScreenUiState.enforceAccess(): NotificationScreenUiState = copy(
    sections = sections.map { section ->
        if (!canAccess(section.category)) section.copy(enabled = false, messages = emptyList(), detectionStandardTime = null)
        else section.copy(messages = section.messages.map(::visibleMessage))
    },
)
