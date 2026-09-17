package com.example.senior_on.ui.child.notification

/** This is not an Android permission or the guardian's notification toggle state. */
internal fun NotificationScreenUiState.accessWarningPanel(): NotificationFooterPanelUiState? {
    val senior = seniorDisplayName.ifBlank { "시니어" }
    return when {
        !isParentPhoneRegistered -> NotificationFooterPanelUiState(
            tone = NotificationFooterTone.Warning,
            title = "$senior 폰이 연결되지 않았어요.",
        )
        isSeniorSharingRevoked -> NotificationFooterPanelUiState(
            tone = NotificationFooterTone.Warning,
            title = "${senior}가 권한을 해제했어요.",
            description = "무응답 감지, 위치 알림 권한이 꺼져있어요.\n${senior}께 권한을 다시 켜달라고 요청해보세요.",
        )
        else -> null
    }
}
