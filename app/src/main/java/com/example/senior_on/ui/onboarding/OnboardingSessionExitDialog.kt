package com.example.senior_on.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun OnboardingSessionExitDialog(
    onDismiss: () -> Unit,
    onExitApp: () -> Unit,
    onLoginWithAnotherAccount: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SeniorOnColors.SupportWhite100)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "온보딩을 중단할까요?",
                style = SeniorOnTextStyles.BodyLBold,
                color = SeniorOnColors.Gray800,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "로그인 상태는 유지되며, 다음 실행 시 이어서 진행할 수 있어요.",
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray500,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OnboardingSessionExitButton(
                    text = "앱 종료",
                    backgroundColor = SeniorOnColors.Gray100,
                    contentColor = SeniorOnColors.Gray700,
                    onClick = onExitApp,
                    modifier = Modifier.weight(1f),
                )
                OnboardingSessionExitButton(
                    text = "다른 계정 로그인",
                    backgroundColor = SeniorOnColors.Primary600,
                    contentColor = SeniorOnColors.SupportWhite100,
                    onClick = onLoginWithAnotherAccount,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun OnboardingSessionExitButton(
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.ButtonS,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingSessionExitDialogPreview() {
    SENIOR_ONTheme {
        OnboardingSessionExitDialog(
            onDismiss = {},
            onExitApp = {},
            onLoginWithAnotherAccount = {},
        )
    }
}
