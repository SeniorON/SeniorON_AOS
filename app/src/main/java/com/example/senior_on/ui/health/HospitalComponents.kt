package com.example.senior_on.ui.health

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

data class HospitalAppointmentUiState(
    val date: LocalDate,
    val hospitalName: String,
    val specialty: String,
    val time: LocalTime,
    val daysLeft: Int,
    val highlighted: Boolean = false,
    val reminder: HospitalReminder = HospitalReminder.DayBefore
) {
    val departmentAndTime: String
        get() = "$specialty · ${time.toKoreanTime()}"
}

@Composable
internal fun UpcomingAppointmentsSection(
    appointments: List<HospitalAppointmentUiState>,
    onAppointmentClick: (HospitalAppointmentUiState) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SeniorOnColors.Background1)
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScheduleSectionTitle(title = "다가오는 진료")
        if (appointments.isEmpty()) {
            EmptyUpcomingAppointmentCard()
        } else {
            appointments.take(2).forEach { appointment ->
                UpcomingAppointmentCard(
                    appointment = appointment,
                    onClick = { onAppointmentClick(appointment) }
                )
            }
        }
    }
}

@Composable
private fun EmptyUpcomingAppointmentCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(121.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Large))
            .background(SeniorOnColors.Gray100)
            .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom =36.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "다음 진료",
            style = SeniorOnTextStyles.BodyMSemiBold,
            color = SeniorOnColors.Gray500
        )
        Text(
            text = "예정된 진료가 없어요",
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Gray500
        )
    }
}

@Composable
private fun UpcomingAppointmentCard(
    appointment: HospitalAppointmentUiState,
    onClick: () -> Unit
) {
    val highlighted = appointment.highlighted
    val shape = RoundedCornerShape(SeniorOnRadius.Large)
    val primaryColor = if (highlighted) SeniorOnColors.SupportWhite100 else SeniorOnColors.Gray700
    val secondaryColor = if (highlighted) SeniorOnColors.SupportWhite100 else SeniorOnColors.Gray500

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .dropShadow(
                shape = shape,
                shadow = if (highlighted) {
                    Shadow(
                        radius = 14.dp,
                        spread = 0.dp,
                        color = Color(0xFFB1C595).copy(alpha = 229f / 255f),
                        offset = DpOffset(x = 0.dp, y = 4.dp)
                    )
                } else {
                    Shadow(
                        radius = 12.dp,
                        spread = 0.dp,
                        color = Color.Black.copy(alpha = 20f / 255f),
                        offset = DpOffset(x = 0.dp, y = 2.dp)
                    )
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = shape,
        color = if (highlighted) SeniorOnColors.Primary600 else SeniorOnColors.SupportWhite100,
        shadowElevation = 0.dp
    ) {
        if (highlighted) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "다음 진료",
                        style = SeniorOnTextStyles.BodyMSemiBold,
                        color = SeniorOnColors.White,
                        modifier = Modifier.weight(1f)
                    )
                    DaysLeftBadge(appointment.daysLeft, highlighted = true)
                }

                Spacer(modifier = Modifier.height(15.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppointmentDateBadge(appointment.date, highlighted = true)
                    Spacer(modifier = Modifier.width(12.dp))
                    AppointmentHospitalText(appointment, primaryColor, secondaryColor)
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppointmentDateBadge(appointment.date, false)
                Spacer(modifier = Modifier.width(12.dp))
                AppointmentHospitalText(
                    appointment,
                    primaryColor,
                    secondaryColor,
                    Modifier.weight(1f)
                )
                DaysLeftBadge(appointment.daysLeft, highlighted = false)
            }
        }
    }
}

@Composable
private fun AppointmentDateBadge(date: LocalDate, highlighted: Boolean) {
    Column(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (highlighted) {
                    SeniorOnColors.SupportWhite100.copy(alpha = 0.2f)
                } else {
                    SeniorOnColors.Background2
                }
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = (-2).dp,
            alignment = Alignment.CenterVertically
        )
    ) {
        Text(
            text = "${date.monthValue}월",
            style = SeniorOnTextStyles.CaptionMedium,
            color = if (highlighted) SeniorOnColors.SupportWhite100 else SeniorOnColors.Gray500
        )
        Text(
            text = date.dayOfMonth.toString(),
            style = SeniorOnTextStyles.BodyMBold,
            color = if (highlighted) SeniorOnColors.SupportWhite100 else SeniorOnColors.Gray800
        )
    }
}

@Composable
private fun AppointmentHospitalText(
    appointment: HospitalAppointmentUiState,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = appointment.hospitalName,
            style = SeniorOnTextStyles.BodyLBold,
            color = primaryColor
        )
        Text(
            text = appointment.departmentAndTime,
            style = SeniorOnTextStyles.BodySMedium,
            color = secondaryColor
        )
    }
}

@Composable
private fun DaysLeftBadge(daysLeft: Int, highlighted: Boolean) {
    Box(
        modifier = Modifier
            .width(55.dp)
            .height(25.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(
                if (highlighted) {
                    SeniorOnColors.SupportWhite100.copy(alpha = 0.2f)
                } else {
                    SeniorOnColors.Gray100
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "D-$daysLeft",
            style = SeniorOnTextStyles.CaptionMedium,
            color = if (highlighted) SeniorOnColors.SupportWhite100 else SeniorOnColors.Gray500
        )
    }
}

@Composable
internal fun HospitalScheduleSection(
    displayedMonth: YearMonth,
    selectedDay: Int,
    appointmentDays: Set<Int>,
    selectedAppointments: List<HospitalAppointmentUiState>,
    onDayClick: (Int) -> Unit,
    onAddAppointmentClick: () -> Unit,
    onEditAppointmentClick: (HospitalAppointmentUiState) -> Unit,
    onDeleteAppointmentClick: (HospitalAppointmentUiState) -> Unit,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 20.dp)
    ) {
        ScheduleSectionTitle(
            title = "병원 일정",
            iconResId = R.drawable.ic_illust_hospital_schedule,
            actionLabel = "진료 추가",
            onActionClick = onAddAppointmentClick
        )
        Spacer(modifier = Modifier.height(12.dp))
        HospitalCalendarCard(
            displayedMonth = displayedMonth,
            selectedDay = selectedDay,
            appointmentDays = appointmentDays,
            selectedAppointments = selectedAppointments,
            onDayClick = onDayClick,
            onAddAppointmentClick = onAddAppointmentClick,
            onEditAppointmentClick = onEditAppointmentClick,
            onDeleteAppointmentClick = onDeleteAppointmentClick,
            onPreviousMonthClick = onPreviousMonthClick,
            onNextMonthClick = onNextMonthClick
        )
    }
}

@Composable
private fun HospitalCalendarCard(
    displayedMonth: YearMonth,
    selectedDay: Int,
    appointmentDays: Set<Int>,
    selectedAppointments: List<HospitalAppointmentUiState>,
    onDayClick: (Int) -> Unit,
    onAddAppointmentClick: () -> Unit,
    onEditAppointmentClick: (HospitalAppointmentUiState) -> Unit,
    onDeleteAppointmentClick: (HospitalAppointmentUiState) -> Unit,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Large)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .dropShadow(
                shape = shape,
                shadow = Shadow(
                    radius = 12.dp,
                    spread = 0.dp,
                    color = Color.Black.copy(alpha = 15f / 255f),
                    offset = DpOffset(x = 0.dp, y = 0.dp)
                )
            ),
        shape = shape,
        color = SeniorOnColors.SupportWhite100,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScheduleCalendar(
                displayedMonth = displayedMonth,
                selectedDay = selectedDay,
                markedDays = appointmentDays,
                onDayClick = onDayClick,
                onPreviousMonthClick = onPreviousMonthClick,
                onNextMonthClick = onNextMonthClick,
                mode = ScheduleCalendarMode.Hospital
            )
            Spacer(modifier = Modifier.height(20.dp))
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(SeniorOnColors.Gray100)
            )
            Spacer(modifier = Modifier.height(18.dp))
            if (selectedAppointments.isEmpty()) {
                EmptyAppointmentContent(
                    displayedMonth = displayedMonth,
                    selectedDay = selectedDay,
                    onAddAppointmentClick = onAddAppointmentClick
                )
            } else {
                SelectedAppointmentsContent(
                    appointments = selectedAppointments,
                    onEditClick = onEditAppointmentClick,
                    onDeleteClick = onDeleteAppointmentClick
                )
            }
        }
    }
}

@Composable
private fun EmptyAppointmentContent(
    displayedMonth: YearMonth,
    selectedDay: Int,
    onAddAppointmentClick: () -> Unit
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_illust_hospital_schedule),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier.size(30.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "${displayedMonth.monthValue}월 ${selectedDay}일에는 예정된 진료가 없어요.",
        style = SeniorOnTextStyles.BodyMMedium,
        color = SeniorOnColors.Gray300,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(12.dp))
    HospitalFilledActionButton(
        label = "${displayedMonth.monthValue}월 ${selectedDay}일 추가하기",
        iconResId = R.drawable.ic_plus,
        onClick = onAddAppointmentClick
    )
}

@Composable
private fun SelectedAppointmentsContent(
    appointments: List<HospitalAppointmentUiState>,
    onEditClick: (HospitalAppointmentUiState) -> Unit,
    onDeleteClick: (HospitalAppointmentUiState) -> Unit
) {
    val firstAppointment = appointments.first()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${firstAppointment.date.monthValue}월 ${firstAppointment.date.dayOfMonth}일 진료",
            style = SeniorOnTextStyles.BodyMBold,
            color = SeniorOnColors.Primary600,
            modifier = Modifier.weight(1f)
        )
        DaysLeftBadge(firstAppointment.daysLeft, highlighted = false)
    }
    Spacer(modifier = Modifier.height(12.dp))
    appointments.forEachIndexed { index, appointment ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(SeniorOnRadius.Small))
                .background(SeniorOnColors.Gray50)
                .padding(12.dp)
        ) {
            Text(
                text = appointment.hospitalName,
                style = SeniorOnTextStyles.BodyLBold,
                color = SeniorOnColors.Gray800,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = appointment.departmentAndTime,
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray500,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HospitalOutlinedActionButton(
                    label = "삭제하기",
                    iconResId = R.drawable.ic_trash,
                    color = SeniorOnColors.Red300,
                    onClick = { onDeleteClick(appointment) },
                    modifier = Modifier.weight(1f)
                )
                HospitalScheduleEditButton(
                    onClick = { onEditClick(appointment) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (index != appointments.lastIndex) {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HospitalScheduleEditButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)

    Row(
        modifier = modifier
            .height(44.dp)
            .clip(shape)
            .background(SeniorOnColors.SupportWhite100)
            .border(
                width = 1.dp,
                color = SeniorOnColors.Gray200,
                shape = shape
            )
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_pencil),
            contentDescription = null,
            tint = SeniorOnColors.Gray800,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "수정하기",
            style = SeniorOnTextStyles.ButtonM,
            color = SeniorOnColors.Gray800
        )
    }
}

@Composable
internal fun HospitalFilledActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @androidx.annotation.DrawableRes iconResId: Int? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(SeniorOnColors.Primary600)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        iconResId?.let {
            Icon(
                painter = painterResource(id = it),
                contentDescription = null,
                tint = SeniorOnColors.SupportWhite100,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(text = label, style = SeniorOnTextStyles.ButtonM, color = SeniorOnColors.SupportWhite100)
    }
}

@Composable
internal fun HospitalOutlinedActionButton(
    label: String,
    @androidx.annotation.DrawableRes iconResId: Int,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .border(1.dp, color, RoundedCornerShape(SeniorOnRadius.Small))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = SeniorOnTextStyles.ButtonM, color = color)
    }
}

@Preview(
    name = "다가오는 진료",
    showBackground = true,
    backgroundColor = 0xFFF7F8F5,
    widthDp = 360
)
@Composable
private fun UpcomingAppointmentsSectionPreview() {
    SENIOR_ONTheme {
        UpcomingAppointmentsSection(
            appointments = previewHospitalAppointments(),
            onAppointmentClick = {}
        )
    }
}

@Preview(
    name = "다가오는 진료 - 진료 없음",
    showBackground = true,
    backgroundColor = 0xFFF7F8F5,
    widthDp = 360
)
@Composable
private fun EmptyUpcomingAppointmentsSectionPreview() {
    SENIOR_ONTheme {
        UpcomingAppointmentsSection(
            appointments = emptyList(),
            onAppointmentClick = {}
        )
    }
}

@Preview(
    name = "병원 일정 - 진료 없음",
    showBackground = true,
    backgroundColor = 0xFFF7F8F5,
    widthDp = 360
)
@Composable
private fun EmptyHospitalScheduleSectionPreview() {
    SENIOR_ONTheme {
        HospitalScheduleSection(
            displayedMonth = YearMonth.of(2026, 6),
            selectedDay = 17,
            appointmentDays = setOf(22, 26),
            selectedAppointments = emptyList(),
            onDayClick = {},
            onAddAppointmentClick = {},
            onEditAppointmentClick = {},
            onDeleteAppointmentClick = {},
            onPreviousMonthClick = {},
            onNextMonthClick = {}
        )
    }
}

@Preview(
    name = "병원 일정 - 진료 있음",
    showBackground = true,
    backgroundColor = 0xFFF7F8F5,
    widthDp = 360
)
@Composable
private fun SelectedHospitalScheduleSectionPreview() {
    val appointment = previewHospitalAppointments().first()

    SENIOR_ONTheme {
        HospitalScheduleSection(
            displayedMonth = YearMonth.from(appointment.date),
            selectedDay = appointment.date.dayOfMonth,
            appointmentDays = setOf(22, 26),
            selectedAppointments = listOf(appointment),
            onDayClick = {},
            onAddAppointmentClick = {},
            onEditAppointmentClick = {},
            onDeleteAppointmentClick = {},
            onPreviousMonthClick = {},
            onNextMonthClick = {}
        )
    }
}
