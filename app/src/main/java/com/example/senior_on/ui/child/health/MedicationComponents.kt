package com.example.senior_on.ui.child.health

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.common.time.koreaNow
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

enum class MedicationDoseStatus(val label: String) {
    Taken("복용"),
    Missed("미복용"),
    Scheduled("예정")
}

data class RegisteredMedicationUiState(
    val id: String,
    val category: String,
    val name: String,
    val times: List<LocalTime>,
    val weekdays: Set<Int>,
    val startDate: LocalDate? = null,
    val repeat: MedicationRepeatSelection = MedicationRepeatSelection(),
) {
    val time: LocalTime
        get() = times.firstOrNull() ?: LocalTime.of(8, 0)

    val scheduleLabel: String
        get() = weekdays.toMedicationScheduleLabel()

    val startDateLabel: String
        get() = startDate?.toMedicationStartDateLabel().orEmpty()

    val isEveryday: Boolean
        get() = weekdays.size == MedicationWeekdayLabels.size

    fun isActiveOn(date: LocalDate): Boolean {
        if (startDate != null && date.isBefore(startDate)) return false

        // 서버가 확정한 종료일이 있으면 종료 유형과 관계없이 그 값을 우선한다.
        // 과거/혼합 응답에서 repeatEndType과 durationWeeks가 실제 endDate와
        // 다르더라도 다음 날까지 등록 약이 노출되지 않도록 하기 위함이다.
        val effectiveEndDate = repeat.endDate ?: when (repeat.duration) {
            MedicationRepeatDuration.Continuous -> null
            MedicationRepeatDuration.Period -> startDate
                ?.plusWeeks(repeat.periodValue.coerceAtLeast(1).toLong())
            MedicationRepeatDuration.Date -> null
        }

        return effectiveEndDate == null || !date.isAfter(effectiveEndDate)
    }

    fun isScheduledOn(date: LocalDate): Boolean {
        if (!isActiveOn(date)) return false

        val cycle = repeat.cycleValue.coerceAtLeast(1).toLong()
        val anchor = startDate ?: date
        val weekdayIndex = date.dayOfWeek.value % 7

        return when (repeat.frequency) {
            MedicationRepeatFrequency.Daily -> {
                val days = ChronoUnit.DAYS.between(anchor, date)
                days >= 0L && days % cycle == 0L
            }
            MedicationRepeatFrequency.Weekly -> {
                if (weekdays.isEmpty()) return false
                if (weekdayIndex !in weekdays) return false
                val anchorWeek = anchor.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                val targetWeek = date.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                val weeks = ChronoUnit.WEEKS.between(anchorWeek, targetWeek)
                weeks >= 0L && weeks % cycle == 0L
            }
        }
    }
}

internal val MedicationWeekdayLabels = listOf("일", "월", "화", "수", "목", "금", "토")

internal fun Set<Int>.toMedicationScheduleLabel(): String {
    if (isEmpty()) return ""
    if (size == MedicationWeekdayLabels.size) return "매일"
    return sorted().joinToString(", ") { MedicationWeekdayLabels[it] }
}

internal fun buildTodayMedicationsFromRegistered(
    date: LocalDate,
    registered: List<RegisteredMedicationUiState>,
    remoteSchedules: List<TodayMedicationUiState>? = null,
): List<TodayMedicationUiState> {
    if (remoteSchedules != null) {
        return remoteSchedules
            .asSequence()
            .filter { schedule -> schedule.date == date }
            .sortedWith(compareBy({ it.time }, { it.category }, { it.name }))
            .toList()
    }

    return registered
        .asSequence()
        .filter { medication -> medication.isScheduledOn(date) }
        .flatMap { medication ->
            val doseTimes = medication.times.ifEmpty { listOf(medication.time) }
            doseTimes.asSequence().map { doseTime ->
                TodayMedicationUiState(
                    date = date,
                    category = medication.category,
                    name = medication.name.ifBlank { medication.category },
                    time = doseTime,
                    status = defaultDoseStatus(date, doseTime),
                    medicationLogId = 0L,
                )
            }
        }
        .sortedWith(compareBy({ it.time }, { it.category }, { it.name }))
        .toList()
}

private fun defaultDoseStatus(date: LocalDate, time: LocalTime): MedicationDoseStatus {
    val dateTime = LocalDateTime.of(date, time)
    return if (dateTime.isBefore(koreaNow())) {
        MedicationDoseStatus.Missed
    } else {
        MedicationDoseStatus.Scheduled
    }
}

data class TodayMedicationUiState(
    val date: LocalDate,
    val category: String,
    val name: String,
    val time: LocalTime,
    val status: MedicationDoseStatus,
    val medicationLogId: Long = 0L,
    val takenTime: LocalTime? = null,
)

@Composable
internal fun TodayMedicationSection(
    selectedDate: LocalDate,
    todayMedications: List<TodayMedicationUiState>,
    showCalendar: Boolean,
    onYearClick: () -> Unit,
    onPreviousDayClick: () -> Unit,
    onNextDayClick: () -> Unit,
    onAddTodayMedicationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredMedications = todayMedications.filter { it.date == selectedDate }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SeniorOnColors.Primary600)
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp)
    ) {
        HealthYearSelector(
            year = selectedDate.year,
            expanded = showCalendar,
            onClick = onYearClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        HealthDateNavigator(
            selectedDate = selectedDate,
            onPreviousDayClick = onPreviousDayClick,
            onNextDayClick = onNextDayClick
        )
        Spacer(modifier = Modifier.height(12.dp))

        HealthGreenSectionTitle(
            title = "오늘 복약 현황",
            iconResId = R.drawable.ic_illust_medication,
            actionLabel = "오늘 복약 추가",
            onActionClick = onAddTodayMedicationClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        TodayMedicationStatusCard(
            medications = filteredMedications
        )
    }
}

@Composable
private fun HealthYearSelector(
    year: Int,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${year}년",
            style = SeniorOnTextStyles.BodySRegular,
            color = SeniorOnColors.SupportWhite100
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_sm_chevron_down_1),
            contentDescription = null,
            tint = SeniorOnColors.SupportWhite100,
            modifier = Modifier
                .size(18.dp)
                .rotate(if (expanded) 180f else 0f)
        )
    }
}

@Composable
private fun HealthDateNavigator(
    selectedDate: LocalDate,
    onPreviousDayClick: () -> Unit,
    onNextDayClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_next),
            contentDescription = "이전 날",
            tint = SeniorOnColors.SupportWhite100,
            modifier = Modifier
                .size(24.dp)
                .rotate(180f)
                .clickable(onClick = onPreviousDayClick)
        )
        Text(
            text = "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일",
            style = SeniorOnTextStyles.BodyLSemiBold,
            color = SeniorOnColors.SupportWhite100,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_next),
            contentDescription = "다음 날",
            tint = SeniorOnColors.SupportWhite100,
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onNextDayClick)
        )
    }
}

@Composable
internal fun HealthCalendarCard(
    displayedMonth: YearMonth,
    selectedDay: Int,
    markedDates: Set<LocalDate>,
    onDayClick: (Int) -> Unit,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Large)

    Surface(
        modifier = modifier
            .width(HealthCalendarCardWidth)
            .height(HealthCalendarCardHeight)
            .dropShadow(
                shape = shape,
                shadow = Shadow(
                    radius = 12.dp,
                    spread = 0.dp,
                    color = Color.Black.copy(alpha = 15f / 255f),
                    offset = DpOffset(x = 0.dp, y = 4.dp)
                )
            ),
        shape = shape,
        color = SeniorOnColors.SupportWhite100,
        shadowElevation = 0.dp
    ) {
        ScheduleCalendar(
            displayedMonth = displayedMonth,
            selectedDay = selectedDay,
            markedDays = markedDates
                .asSequence()
                .filter { YearMonth.from(it) == displayedMonth }
                .map(LocalDate::getDayOfMonth)
                .toSet(),
            onDayClick = onDayClick,
            onPreviousMonthClick = onPreviousMonthClick,
            onNextMonthClick = onNextMonthClick,
            mode = ScheduleCalendarMode.HealthOverlay,
            modifier = Modifier.padding(HealthCalendarCardPadding)
        )
    }
}

internal val HealthCalendarCardWidth = 296.dp
internal val HealthCalendarCardHeight = 320.dp
internal val HealthCalendarCardPadding = 16.dp
internal val HealthCalendarCardTopOffset = 52.dp

@Composable
private fun HealthGreenSectionTitle(
    title: String,
    @DrawableRes iconResId: Int,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = SeniorOnTextStyles.HeadingXS,
            color = SeniorOnColors.SupportWhite100,
            modifier = Modifier.weight(1f)
        )
        HealthGreenOutlineAddButton(
            label = actionLabel,
            onClick = onActionClick
        )
    }
}

@Composable
private fun HealthGreenOutlineAddButton(
    label: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(38.dp)
    Row(
        modifier = Modifier
            .width(120.dp)
            .height(28.dp)
            .clip(shape)
            .background(SeniorOnColors.SupportWhite100.copy(alpha = 0.2f))
            .clickable(onClick = onClick)
            .padding(start = 8.dp, top = 4.dp, end = 10.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_plus),
            contentDescription = null,
            tint = SeniorOnColors.SupportWhite100,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.SupportWhite100,
            maxLines = 1
        )
    }
}

@Composable
private fun TodayMedicationStatusCard(
    medications: List<TodayMedicationUiState>
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Large)
    val backgroundColor = if (medications.isEmpty()) {
        SeniorOnColors.SupportWhite80
    } else {
        SeniorOnColors.SupportWhite100
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(backgroundColor)
            .padding(
                start = 14.dp,
                top = 10.dp,
                end = 14.dp,
                bottom = 10.dp
            ),
        horizontalAlignment = Alignment.Start
    ) {
        if (medications.isEmpty()) {
            Text(
                text = "오늘 복용할 약이 없어요",
                modifier = Modifier.padding(vertical = 21.dp),
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.Gray500
            )
        } else {
            medications.forEach { medication ->
                TodayMedicationItemRow(
                    medication = medication,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun TodayMedicationItemRow(
    medication: TodayMedicationUiState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = medication.status.iconResId()),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = medication.category,
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.Gray800
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "${medication.time.toMedicationTime()} · ${medication.name}",
                style = SeniorOnTextStyles.BodySRegular,
                color = SeniorOnColors.Gray500
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = medication.status.label,
                style = SeniorOnTextStyles.BodySMedium,
                color = medication.status.labelColor(),
                textDecoration = TextDecoration.None,
            )
            if (medication.status == MedicationDoseStatus.Taken) {
                medication.takenTime?.let { takenTime ->
                    Text(
                        text = takenTime.toMedicationTime(),
                        style = SeniorOnTextStyles.BodySRegular,
                        color = SeniorOnColors.Gray500,
                    )
                }
            }
        }
    }
}

@Composable
internal fun RegisteredMedicationsSection(
    medications: List<RegisteredMedicationUiState>,
    onAddMedicationClick: () -> Unit,
    onMedicationClick: (RegisteredMedicationUiState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SeniorOnColors.SupportWhite100)
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "등록된 약",
                    style = SeniorOnTextStyles.HeadingM,
                    color = SeniorOnColors.Gray800
                )
                Spacer(modifier = Modifier.height(2.dp))
                RegisteredMedicationCountText(count = medications.size)
            }
            if (medications.isNotEmpty()) {
                RegisteredMedicationAddButton(
                    onClick = onAddMedicationClick
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (medications.isEmpty()) {
            EmptyRegisteredMedicationsContent(onAddMedicationClick = onAddMedicationClick)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                medications.forEach { medication ->
                    RegisteredMedicationCard(
                        medication = medication,
                        onClick = { onMedicationClick(medication) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RegisteredMedicationAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(38.dp)
    Row(
        modifier = modifier
            .width(96.dp)
            .height(32.dp)
            .clip(shape)
            .border(
                width = 1.dp,
                color = SeniorOnColors.Primary500,
                shape = shape,
            )
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_sm_plus),
            contentDescription = null,
            tint = SeniorOnColors.Primary500,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "복약 추가",
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Primary500,
        )
    }
}

@Composable
private fun RegisteredMedicationCountText(count: Int) {
    Text(
        text = buildAnnotatedString {
            append("현재 ")
            if (count > 0) {
                withStyle(
                    SeniorOnTextStyles.BodySSemiBold
                        .toSpanStyle()
                        .copy(color = SeniorOnColors.Primary700)
                ) {
                    append("${count}개의 약")
                }
            } else {
                append("${count}개의 약")
            }
            append("이 등록되어 있어요")
        },
        style = SeniorOnTextStyles.BodySMedium,
        color = SeniorOnColors.Gray500
    )
}

@Composable
private fun EmptyRegisteredMedicationsContent(
    onAddMedicationClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_illust_medication_scheduled),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(60.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "등록된 약이 없어요",
            style = SeniorOnTextStyles.BodyMSemiBold,
            color = SeniorOnColors.Gray600
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "복용 중인 약을 등록하면\n시간에 맞춰 부모님께 알림을 드려요",
            style = SeniorOnTextStyles.CaptionMedium,
            color = SeniorOnColors.Gray300,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        HospitalFilledActionButton(
            label = "약 추가하기",
            iconResId = R.drawable.ic_plus,
            onClick = onAddMedicationClick
        )
    }
}

@Composable
private fun RegisteredMedicationCard(
    medication: RegisteredMedicationUiState,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Medium)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(shape)
            .background(SeniorOnColors.Background1)
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = SeniorOnColors.Background4,
                shape = shape,
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.width(118.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = medication.category,
                style = SeniorOnTextStyles.BodySRegular,
                color = SeniorOnColors.Gray500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = medication.name,
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.Gray800,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .width(2.dp)
                .height(40.dp)
                .background(SeniorOnColors.Background4)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RegisteredMedicationMetaRow(
                iconResId = R.drawable.ic_clock_1,
                label = medication.time.toMedicationHourLabel()
            )
            RegisteredMedicationMetaRow(
                iconResId = R.drawable.ic_calendar,
                label = medication.scheduleLabel.ifBlank { medication.startDateLabel }
            )
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_next),
            contentDescription = null,
            tint = SeniorOnColors.Gray500,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun RegisteredMedicationMetaRow(
    @DrawableRes iconResId: Int,
    label: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = SeniorOnColors.Gray300,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = SeniorOnTextStyles.CaptionMedium,
            color = SeniorOnColors.Gray800,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@DrawableRes
private fun MedicationDoseStatus.iconResId(): Int = when (this) {
    MedicationDoseStatus.Taken -> R.drawable.ic_illust_taken
    MedicationDoseStatus.Missed -> R.drawable.ic_illust_missed
    MedicationDoseStatus.Scheduled -> R.drawable.ic_illust_scheduled
}

private fun MedicationDoseStatus.labelColor(): Color = when (this) {
    MedicationDoseStatus.Taken -> SeniorOnColors.Primary600
    MedicationDoseStatus.Missed -> SeniorOnColors.Red300
    MedicationDoseStatus.Scheduled -> SeniorOnColors.Gray400
}

internal fun LocalTime.toMedicationTime(): String =
    DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN).format(this)

internal fun LocalTime.toMedicationHourLabel(): String {
    val period = if (hour < 12) "오전" else "오후"
    val displayHour = (hour % 12).let { if (it == 0) 12 else it }
    return if (minute == 0) {
        "$period ${displayHour}시"
    } else {
        "$period ${displayHour}시 ${minute}분"
    }
}

internal fun LocalDate.toMedicationStartDateLabel(): String =
    DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN).format(this)

internal fun previewRegisteredMedications() = listOf(
    RegisteredMedicationUiState(
        id = "1",
        category = "혈압약",
        name = "아암로디핀",
        times = listOf(LocalTime.of(8, 0), LocalTime.of(14, 0)),
        weekdays = setOf(1, 2, 3, 4),
        startDate = LocalDate.of(2026, 8, 11)
    ),
    RegisteredMedicationUiState(
        id = "2",
        category = "혈압약",
        name = "아암로디핀",
        times = listOf(LocalTime.of(8, 0)),
        weekdays = (0..6).toSet(),
        startDate = LocalDate.of(2026, 6, 12)
    ),
    RegisteredMedicationUiState(
        id = "3",
        category = "혈압약",
        name = "아암로디핀",
        times = listOf(LocalTime.of(8, 0)),
        weekdays = setOf(1, 2, 3, 4, 5, 6),
        startDate = LocalDate.of(2026, 8, 11)
    )
)

internal fun previewTodayMedications() = listOf(
    TodayMedicationUiState(
        date = LocalDate.of(2026, 6, 12),
        category = "혈압약",
        name = "아암로디핀",
        time = LocalTime.of(8, 0),
        status = MedicationDoseStatus.Taken,
        takenTime = LocalTime.of(14, 0),
    ),
    TodayMedicationUiState(
        date = LocalDate.of(2026, 6, 12),
        category = "혈압약",
        name = "아암로디핀",
        time = LocalTime.of(18, 0),
        status = MedicationDoseStatus.Missed
    ),
    TodayMedicationUiState(
        date = LocalDate.of(2026, 6, 12),
        category = "혈압약",
        name = "아암로디핀",
        time = LocalTime.of(20, 0),
        status = MedicationDoseStatus.Scheduled
    )
)

internal fun previewMedicationMarkedDates() = setOf(
    LocalDate.of(2026, 6, 19),
    LocalDate.of(2026, 6, 27),
)

@Preview(name = "오늘 복약 - 복약 있음", showBackground = true, widthDp = 360)
@Composable
private fun TodayMedicationSectionPreview() {
    SENIOR_ONTheme {
        TodayMedicationSection(
            selectedDate = LocalDate.of(2026, 6, 12),
            todayMedications = previewTodayMedications(),
            showCalendar = false,
            onYearClick = {},
            onPreviousDayClick = {},
            onNextDayClick = {},
            onAddTodayMedicationClick = {}
        )
    }
}

@Preview(name = "오늘 복약 - 없음", showBackground = true, widthDp = 360)
@Composable
private fun EmptyTodayMedicationSectionPreview() {
    SENIOR_ONTheme {
        TodayMedicationSection(
            selectedDate = LocalDate.of(2026, 6, 12),
            todayMedications = emptyList(),
            showCalendar = false,
            onYearClick = {},
            onPreviousDayClick = {},
            onNextDayClick = {},
            onAddTodayMedicationClick = {}
        )
    }
}

@Preview(name = "등록된 약 - 있음", showBackground = true, widthDp = 360)
@Composable
private fun RegisteredMedicationsSectionPreview() {
    SENIOR_ONTheme {
        RegisteredMedicationsSection(
            medications = previewRegisteredMedications(),
            onAddMedicationClick = {},
            onMedicationClick = {}
        )
    }
}

@Preview(name = "등록된 약 - 없음", showBackground = true, widthDp = 360)
@Composable
private fun EmptyRegisteredMedicationsSectionPreview() {
    SENIOR_ONTheme {
        RegisteredMedicationsSection(
            medications = emptyList(),
            onAddMedicationClick = {},
            onMedicationClick = {}
        )
    }
}
