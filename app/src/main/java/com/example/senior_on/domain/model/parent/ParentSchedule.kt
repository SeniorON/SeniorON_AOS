package com.example.senior_on.domain.model.parent

import java.time.LocalDate
import java.time.LocalTime

data class ParentSchedule(
    val id: String,
    val date: LocalDate,
    val time: LocalTime,
    val title: String,
    val description: String? = null,
)
