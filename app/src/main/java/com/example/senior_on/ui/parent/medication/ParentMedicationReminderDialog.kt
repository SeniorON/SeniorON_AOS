package com.example.senior_on.ui.parent.medication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.senior_on.R
import com.example.senior_on.domain.model.parent.ParentMedication
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.LocalTime

@Composable
fun ParentMedicationReminderDialog(
    medication: ParentMedication,
    onConfirmClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            ParentMedicationReminderContent(
                medication = medication,
                onConfirmClick = onConfirmClick,
            )
        }
    }
}

@Composable
private fun ParentMedicationReminderContent(
    medication: ParentMedication,
    onConfirmClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .size(width = 263.dp, height = 400.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.XLarge))
            .background(SeniorOnColors.SupportWhite100)
            .padding(start = 16.dp, top = 32.dp, end = 16.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ParentMedicationReminderIcon()

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "약 드실\n시간이에요",
            style = SeniorOnTextStyles.HeadingXL,
            color = SeniorOnColors.Gray800,
            textAlign = TextAlign.Center,
        )

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = SeniorOnColors.Primary700)) {
                    append(medication.scheduledTime.toMedicationTime())
                    append(" ${medication.name}")
                }
                append("을\n복용해 주세요.")
            },
            modifier = Modifier.padding(top = 18.dp),
            style = SeniorOnTextStyles.HeadingXS,
            color = SeniorOnColors.Gray500,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(38.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(SeniorOnRadius.Medium))
                .background(SeniorOnColors.Primary600)
                .clickable(onClick = onConfirmClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "복약 확인하기",
                style = SeniorOnTextStyles.HeadingS,
                color = SeniorOnColors.SupportWhite100,
            )
        }
    }
}

@Composable
private fun ParentMedicationReminderIcon() {
    Box(
        modifier = Modifier
            .size(58.dp)
            .dropShadow(
                shape = CircleShape,
                shadow = Shadow(
                    radius = 6.69.dp,
                    spread = 0.dp,
                    color = Color.Black.copy(alpha = 0.08f),
                    offset = DpOffset(x = 0.dp, y = 1.12.dp),
                ),
            )
            .clip(CircleShape)
            .background(SeniorOnColors.SupportWhite100),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_illust_medication),
            contentDescription = null,
            modifier = Modifier.size(34.dp),
            tint = Color.Unspecified,
        )
    }
}

private fun LocalTime.toMedicationTime(): String {
    val period = if (hour < 12) "오전" else "오후"
    val displayHour = (hour % 12).let { if (it == 0) 12 else it }
    return if (minute == 0) {
        "$period ${displayHour}시"
    } else {
        "$period ${displayHour}시 ${minute}분"
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ParentMedicationReminderDialogPreview() {
    SENIOR_ONTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Black.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center,
        ) {
            ParentMedicationReminderContent(
                medication = ParentMedication(
                    id = "preview",
                    name = "혈압약",
                    scheduledTime = LocalTime.of(14, 0),
                ),
                onConfirmClick = {},
            )
        }
    }
}
