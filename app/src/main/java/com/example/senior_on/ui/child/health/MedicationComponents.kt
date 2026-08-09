package com.example.senior_on.ui.child.health

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
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
    val startDate: LocalDate? = null
) {
    val time: LocalTime
        get() = times.firstOrNull() ?: LocalTime.of(8, 0)

    val scheduleLabel: String
        get() = weekdays.toMedicationScheduleLabel()

    val startDateLabel: String
        get() = startDate?.toMedicationStartDateLabel().orEmpty()

    val isEveryday: Boolean
        get() = weekdays.size == MedicationWeekdayLabels.size

    fun isScheduledOn(date: LocalDate): Boolean {
        if (weekdays.isEmpty()) return false
        if (startDate != null && date.isBefore(startDate)) return false
        val weekdayIndex = date.dayOfWeek.value % 7
        return weekdayIndex in weekdays
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
    remoteSchedules: List<TodayMedicationUiState> = emptyList(),
): List<TodayMedicationUiState> {
    return registered
        .asSequence()
        .filter { medication -> medication.isScheduledOn(date) }
        .flatMap { medication ->
            val doseTimes = medication.times.ifEmpty { listOf(medication.time) }
            doseTimes.asSequence().map { doseTime ->
                val remote = remoteSchedules.find { schedule ->
                    schedule.date == date &&
                        schedule.category == medication.category &&
                        schedule.name == medication.name &&
                        schedule.time == doseTime
                }
                TodayMedicationUiState(
                    date = date,
                    category = medication.category,
                    name = medication.name.ifBlank { medication.category },
                    time = doseTime,
                    status = remote?.status ?: defaultDoseStatus(date, doseTime),
                    medicationLogId = remote?.medicationLogId ?: 0L,
                )
            }
        }
        .sortedWith(compareBy({ it.time }, { it.category }, { it.name }))
        .toList()
}

private fun defaultDoseStatus(date: LocalDate, time: LocalTime): MedicationDoseStatus {
    val dateTime = LocalDateTime.of(date, time)
    return if (dateTime.isBefore(LocalDateTime.now())) {
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
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 24.dp)
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
            .padding(horizontal = 8.dp, vertical = 4.dp),
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
            painter = painterResource(id = R.drawable.ic_sm_arrow_left),
            contentDescription = "이전 날",
            tint = SeniorOnColors.SupportWhite100,
            modifier = Modifier
                .size(18.dp)
                .clickable(onClick = onPreviousDayClick)
        )
        Text(
            text = "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일",
            style = SeniorOnTextStyles.BodyLSemiBold,
            color = SeniorOnColors.SupportWhite100,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_sm_arrow_right),
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
internal val HealthCalendarCardStartOffset = 32.dp

@Composable
private fun HealthGreenSectionTitle(
    title: String,
    @DrawableRes iconResId: Int,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
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
            style = SeniorOnTextStyles.BodyLBold,
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
    val shape = RoundedCornerShape(22.dp)
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
            modifier = Modifier.size(18.dp)
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
        SeniorOnColors.Primary100
    } else {
        SeniorOnColors.SupportWhite100
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(backgroundColor)
            .padding(
                start = if (medications.isEmpty()) 14.dp else 16.dp,
                top = if (medications.isEmpty()) 31.dp else 16.dp,
                end = if (medications.isEmpty()) 14.dp else 16.dp,
                bottom = if (medications.isEmpty()) 31.dp else 16.dp
            ),
        horizontalAlignment = Alignment.Start
    ) {
        if (medications.isEmpty()) {
            Text(
                text = "오늘 복용할 약이 없어요",
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.Gray500
            )
        } else {
            medications.forEachIndexed { index, medication ->
                TodayMedicationItemRow(medication = medication)
                if (index != medications.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun TodayMedicationItemRow(
    medication: TodayMedicationUiState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            Text(
                text = "${medication.time.toMedicationTime()} · ${medication.name}",
                style = SeniorOnTextStyles.BodySRegular,
                color = SeniorOnColors.Gray500
            )
        }
        Text(
            text = medication.status.label,
            style = SeniorOnTextStyles.BodySMedium,
            color = medication.status.labelColor()
        )
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
                    style = SeniorOnTextStyles.BodyLBold,
                    color = SeniorOnColors.Gray800
                )
                Spacer(modifier = Modifier.height(2.dp))
                RegisteredMedicationCountText(count = medications.size)
            }
            if (medications.isNotEmpty()) {
                OutlineAddButton(
                    label = "복약 추가",
                    onClick = onAddMedicationClick
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
private fun RegisteredMedicationCountText(count: Int) {
    Text(
        text = buildAnnotatedString {
            append("현재 ")
            if (count > 0) {
                withStyle(
                    SpanStyle(
                        color = SeniorOnColors.Primary700,
                        fontWeight = FontWeight.SemiBold
                    )
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
            .padding(top = 24.dp, bottom = 8.dp),
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

        Spacer(modifier = Modifier.height(36.dp))

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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SeniorOnRadius.Large))
            .background(SeniorOnColors.Gray50)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.width(88.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = medication.category,
                style = SeniorOnTextStyles.BodySRegular,
                color = SeniorOnColors.Gray500
            )
            Text(
                text = medication.name,
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.Gray800
            )
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .width(2.dp)
                .height(40.dp)
                .background(SeniorOnColors.Gray100)
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
            painter = painterResource(id = R.drawable.ic_sm_arrow_right),
            contentDescription = null,
            tint = SeniorOnColors.Gray300,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun RegisteredMedicationMetaRow(
    @DrawableRes iconResId: Int,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
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
            color = SeniorOnColors.Gray800
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
        status = MedicationDoseStatus.Taken
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
