package com.example.senior_on.ui.common.time

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

internal fun String?.toRelativeTimeLabel(
    now: Instant = Instant.now(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): String? {
    val value = this?.trim()?.takeIf(String::isNotEmpty) ?: return null
    val timestamp = value.toInstantOrNull(zoneId) ?: return value
    val elapsedSeconds = Duration.between(timestamp, now).seconds.coerceAtLeast(0L)

    return when {
        elapsedSeconds < 60L -> "방금 전"
        elapsedSeconds < 60L * 60L -> "${elapsedSeconds / 60L}분 전"
        elapsedSeconds < 24L * 60L * 60L -> "${elapsedSeconds / (60L * 60L)}시간 전"
        else -> "${elapsedSeconds / (24L * 60L * 60L)}일 전"
    }
}

private fun String.toInstantOrNull(zoneId: ZoneId): Instant? =
    runCatching { OffsetDateTime.parse(this).toInstant() }
        .recoverCatching { Instant.parse(this) }
        .recoverCatching { LocalDateTime.parse(this).atZone(zoneId).toInstant() }
        .getOrNull()
