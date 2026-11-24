package com.example.senior_on.domain.model.auth

import com.example.senior_on.core.time.koreaToday
import java.time.LocalDate
import java.time.format.DateTimeParseException

internal object SignupAgePolicy {
    const val ageMessage = "만 14세 이상만 가입할 수 있어요."

    fun error(birth: String, today: LocalDate = koreaToday()): String? {
        val date = try {
            LocalDate.parse(birth.replace('.', '-'))
        } catch (_: DateTimeParseException) {
            return "올바른 생년월일을 선택해 주세요."
        }
        if (date.year !in 1900..today.year || date > today) return "올바른 생년월일을 선택해 주세요."
        // Match the social signup endpoint, including leap-day birthdays.
        return ageMessage.takeIf { date.plusYears(14) > today }
    }
}
