package com.example.senior_on.ui.parent.medication

import com.example.senior_on.ui.parent.medication.viewmodel.ParentMedicationUiState

import com.example.senior_on.ui.parent.medication.viewmodel.ParentMedicationContent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.senior_on.R
import com.example.senior_on.domain.model.parent.ParentMedication
import com.example.senior_on.ui.parent.component.ParentDetailTopBar
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.LocalTime

@Composable
fun ParentMedicationScreen(
    uiState: ParentMedicationUiState,
    onBackClick: () -> Unit,
    onTakenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .safeDrawingPadding()
    ) {
        ParentDetailTopBar(
            title = "복약",
            onBackClick = onBackClick
        )

        when (uiState.content) {
            ParentMedicationContent.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SeniorOnColors.Primary600)
            }

            ParentMedicationContent.Due -> MedicationDueContent(
                isSubmitting = uiState.isSubmitting,
                onTakenClick = onTakenClick
            )

            ParentMedicationContent.Completed -> MedicationResultContent(
                iconResId = R.drawable.ic_big_checkfilled,
                title = "잘 하셨어요!",
                description = "자녀에게 알림을 보냈어요",
                titleColor = SeniorOnColors.Primary700,
                onBackClick = onBackClick
            )

            ParentMedicationContent.Empty -> MedicationResultContent(
                iconResId = R.drawable.ic_illust_scheduled,
                title = "등록된 약이 없어요",
                description = "자녀에게 약 등록을\n요청해 보세요",
                titleColor = SeniorOnColors.Gray800,
                onBackClick = onBackClick,
                highlightDescription = true
            )
        }
    }
}

@Composable
private fun MedicationDueContent(
    isSubmitting: Boolean,
    onTakenClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(22.dp))
        MedicationIcon(
            iconResId = R.drawable.ic_illust_medication
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "약 드실 시간이에요!",
            style = SeniorOnTextStyles.HeadingM,
            color = SeniorOnColors.Gray800,
            textAlign = TextAlign.Center
        )

        Text(
            text = buildAnnotatedString {
                append("드신 후 ")
                withStyle(SpanStyle(color = SeniorOnColors.Primary600)) {
                    append("아래 버튼")
                }
                append("을\n눌러주세요")
            },
            modifier = Modifier.padding(top = 10.dp),
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Gray400,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(30.dp))

        Box(
            modifier = Modifier
                .size(232.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = SeniorOnColors.Primary600,
                    spotColor = SeniorOnColors.Primary600
                )
                .clip(CircleShape)
                .background(
                    if (isSubmitting) {
                        SeniorOnColors.Primary400
                    } else {
                        SeniorOnColors.Primary700
                    }
                )
                .clickable(
                    enabled = !isSubmitting,
                    onClick = onTakenClick
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    color = SeniorOnColors.White,
                    strokeWidth = 4.dp
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                        tint = SeniorOnColors.White
                    )
                    Text(
                        text = "약 먹었어요",
                        modifier = Modifier.padding(top = 8.dp),
                        style = SeniorOnTextStyles.HeadingS,
                        color = SeniorOnColors.White
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicationResultContent(
    iconResId: Int,
    title: String,
    description: String,
    titleColor: Color,
    onBackClick: () -> Unit,
    highlightDescription: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(22.dp))
        MedicationIcon(iconResId = iconResId)

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = title,
            style = SeniorOnTextStyles.HeadingM,
            color = titleColor,
            textAlign = TextAlign.Center
        )

        if (highlightDescription) {
            Text(
                text = buildAnnotatedString {
                    append("자녀에게 ")
                    withStyle(SpanStyle(color = SeniorOnColors.Primary600)) {
                        append("약 등록")
                    }
                    append("을\n요청해 보세요")
                },
                modifier = Modifier.padding(top = 10.dp),
                style = SeniorOnTextStyles.BodyLBold,
                color = SeniorOnColors.Gray400,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = description,
                modifier = Modifier.padding(top = 10.dp),
                style = SeniorOnTextStyles.BodyLBold,
                color = SeniorOnColors.Gray400,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Box(
            modifier = Modifier
                .size(232.dp)
                .shadow(14.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFFAAAAAA))
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                    tint = SeniorOnColors.White
                )
                Text(
                    text = "돌아가기",
                    modifier = Modifier.padding(top = 8.dp),
                    style = SeniorOnTextStyles.HeadingS,
                    color = SeniorOnColors.White
                )
            }
        }
    }
}

@Composable
private fun MedicationIcon(
    iconResId: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(96.dp)
            .shadow(5.dp, CircleShape)
            .clip(CircleShape)
            .background(SeniorOnColors.White),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = null,
            modifier = Modifier.size(58.dp),
            tint = Color.Unspecified
        )
    }
}

@Composable
fun ParentMedicationReminderOverlay(
    medication: ParentMedication,
    onConfirmClick: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .size(width = 270.dp, height = 336.dp)
                    .clip(RoundedCornerShape(SeniorOnRadius.XLarge))
                    .background(SeniorOnColors.White)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MedicationIcon(
                    iconResId = R.drawable.ic_illust_medication,
                    modifier = Modifier.size(62.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "약 드실\n시간이에요",
                    style = SeniorOnTextStyles.HeadingL,
                    color = SeniorOnColors.Gray800,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = SeniorOnColors.Primary700)) {
                            append(medication.scheduledTime.toMedicationTime())
                            append(" ${medication.name}")
                        }
                        append("을\n복용해 주세요.")
                    },
                    modifier = Modifier.padding(top = 14.dp),
                    style = SeniorOnTextStyles.BodyMRegular,
                    color = SeniorOnColors.Gray500,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(SeniorOnRadius.Medium))
                        .background(SeniorOnColors.Primary600)
                        .clickable(onClick = onConfirmClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "복약 확인하기",
                        style = SeniorOnTextStyles.ButtonL,
                        color = SeniorOnColors.White
                    )
                }
            }
        }
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
private fun ParentMedicationDuePreview() {
    SENIOR_ONTheme {
        ParentMedicationScreen(
            uiState = ParentMedicationUiState(
                content = ParentMedicationContent.Due,
                medication = ParentMedication(
                    id = "preview",
                    name = "혈압약",
                    scheduledTime = LocalTime.of(14, 0)
                )
            ),
            onBackClick = {},
            onTakenClick = {}
        )
    }
}
