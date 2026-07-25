package com.example.senior_on.data.repository

import com.example.senior_on.domain.model.ParentSchedule
import java.time.LocalDate

interface ParentScheduleRepository {
    suspend fun getSchedules(date: LocalDate): List<ParentSchedule>
}
