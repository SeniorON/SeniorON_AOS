package com.example.senior_on.ui.child.notification

internal fun List<NotificationMessageUiState>.distinctHistoryNotifications(): List<NotificationMessageUiState> {
    val seenIds = mutableSetOf<Long>()
    // Keep the first occurrence of a server notification. Equal titles/times or event IDs
    // do not prove that two notifications are the same. Preserve ID-less entries.
    return filter { message ->
        val id = message.notificationId?.takeIf { it > 0 }
        id == null || seenIds.add(id)
    }
}

internal fun NotificationMessageUiState.historyItemKey(date: String, index: Int): String {
    val id = notificationId?.takeIf { it > 0 }
    // The fallback is positional only within its date group; never reuse it for a server ID.
    return if (id != null) "notification-$id" else "notification-missing-$date-$index"
}
