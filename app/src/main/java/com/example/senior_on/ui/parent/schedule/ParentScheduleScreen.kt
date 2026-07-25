package com.example.senior_on.ui.parent.schedule

import com.example.senior_on.ui.parent.schedule.viewmodel.toParentDisplayTime

import com.example.senior_on.ui.parent.schedule.viewmodel.ParentScheduleUiState

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.domain.model.parent.ParentSchedule
import com.example.senior_on.ui.parent.component.ParentDetailTopBar
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .safeDrawingPadding()
    ) {
        ParentDetailTopBar(
            title = "일정",
            onBackClick = onBackClick
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SeniorOnColors.Primary600)
                }
            }

            uiState.schedules.isEmpty() -> {
                ParentScheduleEmptyContent()
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    uiState.schedules.forEach { schedule ->
                        ParentScheduleCard(schedule = schedule)
                    }
                }
            }
        }
    }
}

@Composable
private fun ParentScheduleCard(
    schedule: ParentSchedule,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Large))
            .background(SeniorOnColors.Background3)
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Text(
            text = schedule.time.toParentDisplayTime(),
            style = SeniorOnTextStyles.HeadingM,
            color = SeniorOnColors.Gray800
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = schedule.title,
            style = SeniorOnTextStyles.BodyLMedium,
            color = SeniorOnColors.Gray500
        )
    }
}

@Composable
private fun ParentScheduleEmptyContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(SeniorOnRadius.Large))
                .background(SeniorOnColors.Background3)
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "오늘은 일정이 없어요",
                style = SeniorOnTextStyles.BodyLBold,
                color = SeniorOnColors.Gray300
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ParentScheduleScreenPreview() {
    SENIOR_ONTheme {
        ParentScheduleScreen(
            uiState = ParentScheduleUiState(
                isLoading = false,
                schedules = listOf(
                    ParentSchedule(
                        id = "1",
                        date = LocalDate.now(),
                        time = LocalTime.of(10, 0),
                        title = "OOO 안과"
                    ),
                    ParentSchedule(
                        id = "2",
                        date = LocalDate.now(),
                        time = LocalTime.of(15, 0),
                        title = "연세세브란스병원"
                    )
                )
            ),
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ParentScheduleEmptyPreview() {
    SENIOR_ONTheme {
        ParentScheduleScreen(
            uiState = ParentScheduleUiState(isLoading = false),
            onBackClick = {}
        )
    }
}
