package com.example.senior_on.ui.child.notification

internal fun locationUnavailableMessage(
    sharingStatusKnown: Boolean,
    locationSharingEnabled: Boolean,
    latitude: Double?,
    longitude: Double?,
): String? = when {
    !sharingStatusKnown -> "공유 상태를 확인하지 못했어요.\n다시 시도해 주세요."
    !locationSharingEnabled -> "위치 정보 공유가 중단되었어요."
    latitude == null || longitude == null ||
        latitude !in -90.0..90.0 || longitude !in -180.0..180.0 ->
        "저장된 위치 정보가 없어요."
    else -> null
}

/** This is not an Android permission or the guardian's notification toggle state. */
internal fun NotificationScreenUiState.accessWarningPanel(): NotificationFooterPanelUiState? {
    val senior = seniorDisplayName.ifBlank { "시니어" }
    val disabledSettings = buildList {
        if (gpsEnabled == false) add("휴대폰 위치 기능(GPS)")
        if (locationPermissionGranted == false) add("시니어On 위치 접근 권한")
        if (sharingStatusKnown && (isSeniorSharingRevoked || !locationSharingEnabled)) add("앱 내 위치 정보 공유")
        if (sharingStatusKnown && (isSeniorSharingRevoked || !inactivitySharingEnabled)) add("앱 내 무활동 감지 공유")
    }
    return when {
        !isParentPhoneRegistered -> NotificationFooterPanelUiState(
            tone = NotificationFooterTone.Warning,
            title = "$senior 폰이 연결되지 않았어요.",
        )
        disabledSettings.isNotEmpty() -> NotificationFooterPanelUiState(
            tone = NotificationFooterTone.Warning,
            title = "${senior} 폰에서 꺼진 설정이 있어요.",
            description = disabledSettings.mapIndexed { index, setting -> "${index + 1}. $setting" }.joinToString("\n"),
        )
        !sharingStatusKnown -> NotificationFooterPanelUiState(
            tone = NotificationFooterTone.Warning,
            title = "공유 상태를 확인하지 못했어요.",
            description = "당겨서 새로고침해 다시 확인해 주세요.",
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
    else message.copy(
        // SOS summaries may contain an address even before event details are loaded.
        title = if (message.category == NotificationCategory.Sos) "긴급 도움 요청" else message.title,
        address = null, latitude = null, longitude = null,
        lastLocationUpdatedAtMillis = null, detail = null, eventMessage = null)
    return if (canAccess(NotificationCategory.Inactivity)) visible else visible.copy(lastSeenAt = null)
}

internal fun NotificationScreenUiState.canUseToggle(category: NotificationCategory): Boolean =
    canAccess(category) && (category != NotificationCategory.Outing ||
        (gpsEnabled != false && locationPermissionGranted != false))

internal fun NotificationScreenUiState.displaySection(section: NotificationSectionUiState) = when {
    !canUseToggle(section.category) -> section.copy(enabled = false, messages = emptyList(), detectionStandardTime = null)
    section.category == NotificationCategory.Sos -> section.copy(enabled = true)
    else -> section
}

internal fun NotificationScreenUiState.enforceAccess(): NotificationScreenUiState = copy(
    sections = sections.map { section ->
        if (!canAccess(section.category)) section.copy(messages = emptyList(), detectionStandardTime = null)
        else section.copy(messages = section.messages.map(::visibleMessage))
    },
)
