package com.example.senior_on.ui.login

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
import androidx.compose.foundation.layout.width
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
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

enum class LoginWrongModeDialogType {
    SeniorAccount,
    ChildAccount
}

@Composable
fun LoginWrongModeDialogContent(
    type: LoginWrongModeDialogType,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(249.dp)
            .height(288.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Large))
            .background(SeniorOnColors.SupportWhite100)
            .padding(horizontal = 16.dp, vertical = 32.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(38.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_modal_alert_filled),
                    contentDescription = null,
                    modifier = Modifier.size(20.4.dp),
                    tint = Color.Unspecified
                )
            }

            Text(
                text = when (type) {
                    LoginWrongModeDialogType.SeniorAccount ->
                        "시니어 계정으로\n가입된 회원입니다"
                    LoginWrongModeDialogType.ChildAccount ->
                        "자녀 계정으로\n가입된 회원입니다"
                },
                modifier = Modifier.padding(top = 16.dp),
                style = SeniorOnTextStyles.BodyLBold,
                color = SeniorOnColors.Gray800,
                textAlign = TextAlign.Center
            )

            Text(
                text = when (type) {
                    LoginWrongModeDialogType.SeniorAccount ->
                        "시니어 모드로 선택한 후\n다시 진행해주세요."
                    LoginWrongModeDialogType.ChildAccount ->
                        "자녀 모드로 선택한 후\n다시 진행해주세요."
                },
                modifier = Modifier.padding(top = 18.dp),
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray500,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(26.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(SeniorOnRadius.Medium))
                    .background(SeniorOnColors.Primary600)
                    .clickable(onClick = onConfirmClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "처음으로",
                    style = SeniorOnTextStyles.ButtonM,
                    color = SeniorOnColors.SupportWhite100
                )
            }
        }
    }
}

@Composable
fun LoginWrongModeDialogOverlay(
    type: LoginWrongModeDialogType,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.Black.copy(alpha = 0.4f))
            .padding(
                start = 56.dp,
                top = 255.5.dp,
                end = 55.dp,
                bottom = 256.5.dp
            ),
        contentAlignment = Alignment.TopStart
    ) {
        LoginWrongModeDialogContent(
            type = type,
            onConfirmClick = onConfirmClick
        )
    }
}

@Preview(name = "Wrong Mode - Senior Account", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginWrongModeSeniorAccountPreview() {
    SENIOR_ONTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.White)
        ) {
            LoginWrongModeDialogOverlay(
                type = LoginWrongModeDialogType.SeniorAccount,
                onConfirmClick = {}
            )
        }
    }
}

@Preview(name = "Wrong Mode - Child Account", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginWrongModeChildAccountPreview() {
    SENIOR_ONTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.White)
        ) {
            LoginWrongModeDialogOverlay(
                type = LoginWrongModeDialogType.ChildAccount,
                onConfirmClick = {}
            )
        }
    }
}
