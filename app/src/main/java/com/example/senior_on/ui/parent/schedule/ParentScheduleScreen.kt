package com.example.senior_on.ui.parent.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.domain.model.parent.ParentSchedule
import com.example.senior_on.ui.parent.component.ParentDetailTopBar
import com.example.senior_on.ui.parent.schedule.viewmodel.ParentScheduleUiState
import com.example.senior_on.ui.parent.schedule.viewmodel.toParentDisplayTime
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun ParentScheduleScreen(
    uiState: ParentScheduleUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.Background1)
            .safeDrawingPadding(),
    ) {
        ParentDetailTopBar(
            title = "일정",
            onBackClick = onBackClick,
        )

        when {
            uiState.isLoading -> ParentScheduleLoadingContent()
            else -> ParentScheduleListContent(schedules = uiState.schedules)
        }
    }
}

@Composable
private fun ParentScheduleLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = SeniorOnColors.Primary700)
    }
}

@Composable
private fun ParentScheduleListContent(
    schedules: List<ParentSchedule>,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 24.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ParentScheduleSummary(count = schedules.size)
        }

        if (schedules.isEmpty()) {
            item {
                ParentScheduleEmptyCard()
            }
        } else {
            itemsIndexed(
                items = schedules,
                key = { _, schedule -> schedule.id },
            ) { index, schedule ->
                ParentScheduleCard(
                    number = index + 1,
                    schedule = schedule,
                )
            }
        }
    }
}

@Composable
private fun ParentScheduleSummary(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_big_schedule),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = SeniorOnColors.Schedule,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = buildAnnotatedString {
                append("오늘 일정 ")
                withStyle(SpanStyle(color = SeniorOnColors.Schedule)) {
                    append("${count}개")
                }
                append(" 있어요")
            },
            style = SeniorOnTextStyles.HeadingM,
            color = SeniorOnColors.Gray800,
        )
    }
}

@Composable
private fun ParentScheduleCard(
    number: Int,
    schedule: ParentSchedule,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Large)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .dropShadow(
                shape = shape,
                shadow = Shadow(
                    radius = 11.dp,
                    color = Color(0x14000000),
                    offset = DpOffset.Zero,
                ),
            )
            .clip(shape)
            .background(SeniorOnColors.SupportWhite100)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(54.dp)
                .height(64.dp)
                .clip(RoundedCornerShape(SeniorOnRadius.Medium))
                .background(SeniorOnColors.Background3),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number.toString(),
                style = SeniorOnTextStyles.HeadingM,
                color = SeniorOnColors.Gray800,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = schedule.time.toParentDisplayTime(),
                style = SeniorOnTextStyles.HeadingL,
                color = SeniorOnColors.Gray800,
                maxLines = 1,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = schedule.displayDescription(),
                style = SeniorOnTextStyles.BodyLMedium,
                color = SeniorOnColors.Gray500,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ParentScheduleEmptyCard() {
    val shape = RoundedCornerShape(SeniorOnRadius.Large)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .dropShadow(
                shape = shape,
                shadow = Shadow(
                    radius = 11.dp,
                    color = Color(0x14000000),
                    offset = DpOffset.Zero,
                ),
            )
            .clip(shape)
            .background(SeniorOnColors.SupportWhite100),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "오늘은 일정이 없어요",
            style = SeniorOnTextStyles.HeadingS,
            color = SeniorOnColors.Gray300,
        )
    }
}

private fun ParentSchedule.displayDescription(): String =
    description
        ?.takeIf { it.isNotBlank() }
        ?.let { "$title · $it" }
        ?: title

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ParentScheduleScreenPreview() {
    SENIOR_ONTheme {
        ParentScheduleScreen(
            uiState = ParentScheduleUiState(
                isLoading = false,
                schedules = previewSchedules(),
            ),
            onBackClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ParentScheduleEmptyPreview() {
    SENIOR_ONTheme {
        ParentScheduleScreen(
            uiState = ParentScheduleUiState(isLoading = false),
            onBackClick = {},
        )
    }
}

private fun previewSchedules(): List<ParentSchedule> = listOf(
    ParentSchedule(
        id = "1",
        date = LocalDate.now(),
        time = LocalTime.of(10, 0),
        title = "안과",
    ),
    ParentSchedule(
        id = "2",
        date = LocalDate.now(),
        time = LocalTime.of(14, 0),
        title = "서울대학교병원",
        description = "내과",
    ),
    ParentSchedule(
        id = "3",
        date = LocalDate.now(),
        time = LocalTime.of(18, 0),
        title = "서울대학교병원",
        description = "외과",
    ),
)
