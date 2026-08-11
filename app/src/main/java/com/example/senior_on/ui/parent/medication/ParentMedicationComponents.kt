package com.example.senior_on.ui.parent.medication

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.domain.model.parent.ParentMedication
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import java.time.LocalTime

private val ParentMedicationCardShadow = Shadow(
    radius = 11.dp,
    spread = 0.dp,
    color = Color(0x14000000),
    offset = DpOffset.Zero,
)

private val ParentMedicationReturnButtonShadow = Shadow(
    radius = 26.dp,
    spread = 0.dp,
    color = Color(0x38000000),
    offset = DpOffset(x = 0.dp, y = 14.dp),
)

private val ParentMedicationReturnButtonInnerShadow = Shadow(
    radius = 10.dp,
    spread = 0.dp,
    color = Color(0x40FFFFFF),
    offset = DpOffset(x = 0.dp, y = 4.dp),
)

@Composable
internal fun ParentMedicationSectionTitle(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_big_medicine),
            contentDescription = null,
            modifier = Modifier.size(34.dp),
            tint = Color.Unspecified,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "오늘 드실 약",
            style = SeniorOnTextStyles.HeadingL,
            color = SeniorOnColors.Gray800,
        )
    }
}

@Composable
internal fun ParentMedicationCard(
    medication: ParentMedication,
    highlighted: Boolean,
    isSubmitting: Boolean,
    onTakenClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Large)
    val isTaken = medication.takenAt != null
    Row(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(
                shape = shape,
                shadow = ParentMedicationCardShadow,
            )
            .clip(shape)
            .background(
                if (isTaken) SeniorOnColors.Background4 else SeniorOnColors.SupportWhite100,
            )
            .then(
                if (highlighted) {
                    Modifier.border(2.dp, SeniorOnColors.Primary600, shape)
                } else {
                    Modifier
                },
            )
            .defaultMinSize(minHeight = if (highlighted) 142.dp else 108.dp)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = medication.scheduledTime.toMedicationDisplayTime(),
                style = SeniorOnTextStyles.HeadingXXS,
                color = if (isTaken) SeniorOnColors.Gray500 else SeniorOnColors.Gray600,
            )
            Text(
                text = medication.name,
                modifier = Modifier.padding(top = 2.dp),
                style = SeniorOnTextStyles.HeadingXL,
                color = if (isTaken) SeniorOnColors.Gray500 else SeniorOnColors.Gray800,
            )
        }

        if (isTaken) {
            Text(
                text = "먹었어요",
                style = SeniorOnTextStyles.HeadingXXS,
                color = SeniorOnColors.Gray400,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(width = 96.dp, height = 60.dp)
                    .clip(RoundedCornerShape(SeniorOnRadius.Large))
                    .background(
                        if (isSubmitting) SeniorOnColors.Gray400 else SeniorOnColors.Primary600,
                    )
                    .clickable(enabled = !isSubmitting, onClick = onTakenClick),
                contentAlignment = Alignment.Center,
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = SeniorOnColors.SupportWhite100,
                        strokeWidth = 3.dp,
                    )
                } else {
                    Text(
                        text = "복용하기",
                        style = SeniorOnTextStyles.HeadingXXS,
                        color = SeniorOnColors.SupportWhite100,
                    )
                }
            }
        }
    }
}

@Composable
internal fun ParentMedicationEmptyContent(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 57.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(94.dp))
        Text(
            text = "등록된 약이\n없어요",
            style = SeniorOnTextStyles.Display,
            color = SeniorOnColors.Gray800,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(33.dp))
        ParentMedicationReturnButton(onClick = onBackClick)
        Spacer(modifier = Modifier.height(35.dp))
        Text(
            text = buildAnnotatedString {
                append("보호자에게 ")
                withStyle(
                    SpanStyle(color = SeniorOnColors.Primary600)
                ){
                    append("약 등록")
                }

                append("을\n요청해 보세요")
            },
            style = SeniorOnTextStyles.HeadingS,
            color = SeniorOnColors.Gray600,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun ParentMedicationCompletedContent(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(94.dp))
        Text(
            text = "잘 하셨어요!",
            style = SeniorOnTextStyles.Display,
            color = SeniorOnColors.Primary700,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "보호자에게 알림이 전송됐어요",
            modifier = Modifier.padding(top = 9.dp),
            style = SeniorOnTextStyles.HeadingS,
            color = SeniorOnColors.Gray600,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(46.dp))
        ParentMedicationReturnButton(onClick = onBackClick)
    }
}

@Composable
private fun ParentMedicationReturnButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .size(246.dp)
            .dropShadow(
                shape = CircleShape,
                shadow = ParentMedicationReturnButtonShadow,
            )
            .clip(CircleShape)
            .background(SeniorOnColors.Gray400)
            .innerShadow(
                shape = CircleShape,
                shadow = ParentMedicationReturnButtonInnerShadow,
            )
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_big_back_arrow),
            contentDescription = null,
            modifier = Modifier.size(50.dp),
            tint = SeniorOnColors.SupportWhite100,
        )
        Text(
            text = "돌아가기",
            modifier = Modifier.padding(top = 6.dp),
            style = SeniorOnTextStyles.HeadingXL,
            color = SeniorOnColors.SupportWhite100,
        )
    }
}

internal fun LocalTime.toMedicationDisplayTime(): String {
    val period = if (hour < 12) "오전" else "오후"
    val displayHour = (hour % 12).let { if (it == 0) 12 else it }
    return "%s %d:%02d".format(period, displayHour, minute)
}

@Preview(
    name = "Parent medication - Card",
    showBackground = true,
    backgroundColor = 0xFFF7F8F5,
    widthDp = 360,
)
@Composable
private fun ParentMedicationCardPreview() {
    SENIOR_ONTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SeniorOnColors.Background3)
                .padding(16.dp),
        ) {
            ParentMedicationCard(
                medication = ParentMedication(
                    id = "preview",
                    name = "혈압약",
                    scheduledTime = LocalTime.of(14, 0),
                ),
                highlighted = false,
                isSubmitting = false,
                onTakenClick = {},
            )
        }
    }
}

@Preview(
    name = "Parent medication - Empty",
    showBackground = true,
    widthDp = 360,
    heightDp = 658,
)
@Composable
private fun ParentMedicationEmptyContentPreview() {
    SENIOR_ONTheme {
        ParentMedicationEmptyContent(onBackClick = {})
    }
}

@Preview(
    name = "Parent medication - Completed",
    showBackground = true,
    widthDp = 360,
    heightDp = 658,
)
@Composable
private fun ParentMedicationCompletedContentPreview() {
    SENIOR_ONTheme {
        ParentMedicationCompletedContent(onBackClick = {})
    }
}
