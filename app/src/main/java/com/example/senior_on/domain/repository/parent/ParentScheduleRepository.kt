package com.example.senior_on.domain.repository.parent

import com.example.senior_on.domain.model.parent.ParentSchedule
import java.time.LocalDate

interface ParentScheduleRepository {
    suspend fun getSchedules(date: LocalDate): List<ParentSchedule>
}
