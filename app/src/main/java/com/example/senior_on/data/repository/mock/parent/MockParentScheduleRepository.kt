package com.example.senior_on.data.repository.mock.parent

import com.example.senior_on.domain.model.parent.ParentSchedule
import com.example.senior_on.domain.repository.parent.ParentScheduleRepository
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.delay

enum class MockParentScheduleScenario {
    TodayWithSchedules,
    TodayWithoutSchedules
}

class MockParentScheduleRepository(
    private val scenario: MockParentScheduleScenario =
        MockParentScheduleScenario.TodayWithSchedules
) : ParentScheduleRepository {

    override suspend fun getSchedules(date: LocalDate): List<ParentSchedule> {
        delay(250)

        return when (scenario) {
            MockParentScheduleScenario.TodayWithSchedules -> listOf(
                ParentSchedule(
                    id = "medicine",
                    date = date,
                    time = LocalTime.of(10, 0),
                    title = "OOO 안과"
                ),
                ParentSchedule(
                    id = "hospital",
                    date = date,
                    time = LocalTime.of(15, 0),
                    title = "연세세브란스병원"
                )
            )

            MockParentScheduleScenario.TodayWithoutSchedules -> emptyList()
        }
    }
}
