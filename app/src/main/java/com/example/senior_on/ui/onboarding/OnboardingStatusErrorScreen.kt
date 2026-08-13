package com.example.senior_on.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.common.component.SeniorOnLoadingIndicator
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun OnboardingStatusErrorScreen(
    message: String,
    onRetryClick: () -> Unit,
    onLoginWithAnotherAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
    isRetrying: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.SupportWhite100)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        Text(
            text = "로그인 상태를 확인하지 못했어요",
            style = SeniorOnTextStyles.HeadingXS,
            color = SeniorOnColors.Gray800,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray500,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        OnboardingStatusActionButton(
            text = "다시 시도",
            containerColor = SeniorOnColors.Primary600,
            contentColor = SeniorOnColors.SupportWhite100,
            onClick = onRetryClick,
            isLoading = isRetrying,
        )

        Spacer(modifier = Modifier.height(10.dp))

        OnboardingStatusActionButton(
            text = "다른 계정으로 로그인",
            containerColor = SeniorOnColors.Gray100,
            contentColor = SeniorOnColors.Gray700,
            onClick = onLoginWithAnotherAccountClick,
            enabled = !isRetrying,
        )
    }
}

@Composable
private fun OnboardingStatusActionButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(containerColor)
            .clickable(enabled = enabled && !isLoading, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (isLoading) {
            SeniorOnLoadingIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
            )
        } else {
            Text(
                text = text,
                style = SeniorOnTextStyles.ButtonM,
                color = contentColor,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OnboardingStatusErrorScreenPreview() {
    SENIOR_ONTheme {
        OnboardingStatusErrorScreen(
            message = "네트워크 연결을 확인한 후 다시 시도해 주세요.",
            onRetryClick = {},
            onLoginWithAnotherAccountClick = {},
        )
    }
}
