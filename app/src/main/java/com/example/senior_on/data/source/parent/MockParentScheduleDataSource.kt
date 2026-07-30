package com.example.senior_on.data.source.parent

import com.example.senior_on.domain.model.parent.ParentSchedule
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.delay

enum class MockParentScheduleScenario {
    TodayWithSchedules,
    TodayWithoutSchedules,
}

class MockParentScheduleDataSource(
    private val scenario: MockParentScheduleScenario =
        MockParentScheduleScenario.TodayWithSchedules,
) : ParentScheduleDataSource {

    override suspend fun getSchedules(date: LocalDate): List<ParentSchedule> {
        delay(250)

        return when (scenario) {
            MockParentScheduleScenario.TodayWithSchedules -> listOf(
                ParentSchedule(
                    id = "eye-clinic",
                    date = date,
                    time = LocalTime.of(10, 0),
                    title = "안과",
                ),
                ParentSchedule(
                    id = "internal-medicine",
                    date = date,
                    time = LocalTime.of(14, 0),
                    title = "서울대학교병원",
                    description = "내과",
                ),
                ParentSchedule(
                    id = "surgery",
                    date = date,
                    time = LocalTime.of(18, 0),
                    title = "서울대학교병원",
                    description = "외과",
                ),
            )

            MockParentScheduleScenario.TodayWithoutSchedules -> emptyList()
        }
    }
}
