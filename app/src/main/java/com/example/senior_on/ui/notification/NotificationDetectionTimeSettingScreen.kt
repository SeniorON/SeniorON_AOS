package com.example.senior_on.ui.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnFontFamily
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun NotificationDetectionTimeSettingScreen(
    initialHours: Int = 12,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSaveClick: (Int) -> Unit = {}
) {
    var selectedHours by rememberSaveable { mutableFloatStateOf(initialHours.toFloat()) }
    val hours = selectedHours.toInt()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.Background2)
            .statusBarsPadding()
    ) {
        DetectionTimeTopBar(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            DetectionTimeBadge(
                modifier = Modifier.padding(start = 10.dp)
            )

            Spacer(modifier = Modifier.height(118.dp))

            DetectionTimeValue(hours = hours)

            Spacer(modifier = Modifier.height(30.dp))

            DetectionTimeDescriptionChip(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(108.dp))

            DetectionTimeSliderCard(
                selectedHours = selectedHours,
                onHoursChange = { selectedHours = it }
            )

            Spacer(modifier = Modifier.weight(1f))

            DetectionTimeSaveButton(
                onClick = { onSaveClick(hours) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DetectionTimeTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(SeniorOnColors.SupportWhite100),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .size(26.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBackClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.Gray800
            )
        }

        Text(
            text = "감지 시간 설정",
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Gray800
        )
    }
}

@Composable
private fun DetectionTimeBadge(
    modifier: Modifier = Modifier
) {
    Text(
        text = "설정된 시간",
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomEnd = SeniorOnRadius.Small,
                    bottomStart = SeniorOnRadius.Small
                )
            )
            .background(SeniorOnColors.Primary700)
            .padding(horizontal = 11.dp, vertical = 10.dp),
        style = SeniorOnTextStyles.BodySSemiBold,
        color = SeniorOnColors.SupportWhite100
    )
}

@Composable
private fun DetectionTimeValue(
    hours: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(66.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = hours.toString(),
                style = TextStyle(
                    fontFamily = SeniorOnFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 66.sp,
                    lineHeight = 54.sp,
                    letterSpacing = 0.sp
                ),
                color = SeniorOnColors.Primary700
            )

            Text(
                text = "시간",
                modifier = Modifier.offset(y = 16.dp),
                style = SeniorOnTextStyles.HeadingL,
                color = SeniorOnColors.Gray800
            )
        }
    }
}

@Composable
private fun DetectionTimeDescriptionChip(
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(213.dp)

    Text(
        text = "설정 시간이 지나면 무활동으로 감지해요",
        modifier = modifier
            .clip(shape)
            .border(width = 1.dp, color = SeniorOnColors.Primary600, shape = shape)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        style = SeniorOnTextStyles.CaptionRegular,
        color = SeniorOnColors.Primary600
    )
}

@Composable
private fun DetectionTimeSliderCard(
    selectedHours: Float,
    onHoursChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SeniorOnRadius.Medium),
        color = SeniorOnColors.SupportWhite100,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 20.dp, bottom = 13.dp)
        ) {
            Slider(
                value = selectedHours,
                onValueChange = { value -> onHoursChange(value.coerceIn(1f, 24f)) },
                valueRange = 1f..24f,
                steps = 22,
                colors = SliderDefaults.colors(
                    thumbColor = SeniorOnColors.Primary600,
                    activeTrackColor = SeniorOnColors.Primary600,
                    inactiveTrackColor = SeniorOnColors.Gray200,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DetectionTimeSliderLabel(text = "1시간")
                Spacer(modifier = Modifier.weight(1f))
                DetectionTimeSliderLabel(text = "12시간")
                Spacer(modifier = Modifier.weight(1f))
                DetectionTimeSliderLabel(text = "24시간")
            }
        }
    }
}

@Composable
private fun DetectionTimeSliderLabel(
    text: String
) {
    Text(
        text = text,
        style = SeniorOnTextStyles.CaptionMedium,
        color = SeniorOnColors.Gray500
    )
}

@Composable
private fun DetectionTimeSaveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(SeniorOnColors.Primary600)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "저장",
            style = SeniorOnTextStyles.ButtonM,
            color = SeniorOnColors.SupportWhite100
        )
    }
}

@Preview(
    name = "Detection Time Setting",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun NotificationDetectionTimeSettingScreenPreview() {
    SENIOR_ONTheme {
        NotificationDetectionTimeSettingScreen()
    }
}
