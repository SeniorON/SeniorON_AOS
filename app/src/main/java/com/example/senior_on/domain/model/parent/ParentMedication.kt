package com.example.senior_on.domain.model.parent

import java.time.Instant
import java.time.LocalTime
import java.time.LocalDate
import java.time.LocalDateTime
import com.example.senior_on.core.time.koreaToday

data class ParentMedication(
    val id: String,
    val name: String,
    val scheduledTime: LocalTime,
    val takenAt: Instant? = null,
    val scheduledDate: LocalDate = koreaToday(),
)

fun ParentMedication.isTakingDeadlineReached(now: LocalDateTime): Boolean =
    !now.isBefore(scheduledDate.atTime(scheduledTime).plusHours(2))
