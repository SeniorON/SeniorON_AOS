package com.example.senior_on.core.time

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

val KoreaZoneId: ZoneId = ZoneId.of("Asia/Seoul")

fun koreaToday(): LocalDate = LocalDate.now(KoreaZoneId)

fun koreaNow(): LocalDateTime = LocalDateTime.now(KoreaZoneId)

fun koreaYearMonth(): YearMonth = YearMonth.from(koreaToday())
