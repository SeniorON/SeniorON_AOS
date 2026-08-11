package com.example.senior_on.domain.model.parent

import java.time.Instant
import java.time.LocalTime

data class ParentMedication(
    val id: String,
    val name: String,
    val scheduledTime: LocalTime,
    val takenAt: Instant? = null
)
