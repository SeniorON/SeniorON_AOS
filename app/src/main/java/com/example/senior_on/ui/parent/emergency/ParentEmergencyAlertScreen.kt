package com.example.senior_on.ui.parent.emergency

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.parent.component.ParentDetailTopBar
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun ParentEmergencyAlertScreen(
    uiState: ParentEmergencyAlertUiState,
    onBackClick: () -> Unit,
    onSendClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSending = uiState.status == ParentEmergencyAlertStatus.Sending
    val isButtonEnabled = !isSending &&
        uiState.status != ParentEmergencyAlertStatus.Sent

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF4148),
                        Color(0xFFFF7377),
                        Color(0xFFFFA9AB)
                    )
                )
            )
            .safeDrawingPadding()
    ) {
        ParentDetailTopBar(
            title = "긴급알림",
            onBackClick = onBackClick,
            backgroundColor = Color.Transparent,
            contentColor = SeniorOnColors.White,
            showShadow = false
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .border(
                        width = 1.dp,
                        color = SeniorOnColors.White,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_big_alert_filled),
                    contentDescription = null,
                    modifier = Modifier.size(38.dp),
                    tint = SeniorOnColors.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "긴급알림을\n보낼까요?",
                style = SeniorOnTextStyles.HeadingXL,
                color = SeniorOnColors.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = if (isSending) "긴급알림을 보내고 있어요" else "5초 후 자동 전송",
                style = SeniorOnTextStyles.BodyMRegular,
                color = SeniorOnColors.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = SeniorOnColors.White,
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    text = uiState.remainingSeconds.toString(),
                    style = SeniorOnTextStyles.HeadingXL,
                    color = SeniorOnColors.White
                )
            }

            uiState.errorMessage?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(top = 12.dp),
                    style = SeniorOnTextStyles.BodyMRegular,
                    color = SeniorOnColors.White,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            EmergencyActionButton(
                text = if (uiState.status == ParentEmergencyAlertStatus.Failed) {
                    "다시 보낼게요"
                } else {
                    "네, 보낼게요"
                },
                enabled = isButtonEnabled,
                containerColor = SeniorOnColors.White,
                contentColor = SeniorOnColors.DangerSOS,
                onClick = onSendClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            EmergencyActionButton(
                text = "취소",
                enabled = isButtonEnabled,
                containerColor = Color.Transparent,
                contentColor = SeniorOnColors.White,
                borderColor = SeniorOnColors.White,
                onClick = onCancelClick
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun EmergencyActionButton(
    text: String,
    enabled: Boolean,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color? = null
) {
    val shape = RoundedCornerShape(16.dp)
    val borderModifier = if (borderColor == null) {
        Modifier
    } else {
        Modifier.border(1.dp, borderColor, shape)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .then(borderModifier)
            .clip(shape)
            .background(containerColor)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.HeadingM,
            color = contentColor.copy(alpha = if (enabled) 1f else 0.65f)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ParentEmergencyAlertScreenPreview() {
    SENIOR_ONTheme {
        ParentEmergencyAlertScreen(
            uiState = ParentEmergencyAlertUiState(
                remainingSeconds = 5,
                status = ParentEmergencyAlertStatus.CountingDown
            ),
            onBackClick = {},
            onSendClick = {},
            onCancelClick = {}
        )
    }
}
