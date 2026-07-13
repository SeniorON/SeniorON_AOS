package com.example.senior_on.ui.findaccount

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun FindIdResultScreen(
    isSuccess: Boolean,
    name: String,
    userId: String,
    joinDate: String,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onFindPasswordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FindAccountScaffold(
        modifier = modifier.systemBarsPadding(),
        onBackClick = onBackClick,
        showTabs = false,
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                FindAccountPrimaryButton(
                    text = "로그인",
                    enabled = true,
                    onClick = onLoginClick
                )

                Spacer(modifier = Modifier.height(8.dp))

                FindAccountSecondaryButton(
                    text = "비밀번호 찾기",
                    onClick = onFindPasswordClick
                )
            }
        }
    ) {
        if (isSuccess) {
            FindIdSuccessContent(
                name = name,
                userId = userId,
                joinDate = joinDate
            )
        } else {
            FindIdFailureContent()
        }
    }
}

@Composable
private fun FindIdSuccessContent(
    name: String,
    userId: String,
    joinDate: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 34.dp)
    ) {
        Text(
            text = "${name}님의 아이디",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = SeniorOnTextStyles.OnboardingHeading,
            color = SeniorOnColors.Gray800
        )

        Text(
            text = "가입일: $joinDate",
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp),
            style = SeniorOnTextStyles.BodyMMedium,
            color = SeniorOnColors.Gray500
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(68.dp)
                .background(SeniorOnColors.Gray100),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userId,
                style = SeniorOnTextStyles.HeadingXS,
                color = SeniorOnColors.Gray800
            )
        }
    }
}

@Composable
private fun FindIdFailureContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 34.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = SeniorOnColors.Primary600)) {
                    append("일치하는 아이디")
                }
                withStyle(SpanStyle(color = SeniorOnColors.Gray800)) {
                    append("를\n")
                }
                withStyle(SpanStyle(color = SeniorOnColors.Gray800)) {
                    append("찾지 못했어요")
                }
            },
            style = SeniorOnTextStyles.OnboardingHeading
        )

        Text(
            text = "이름과 이메일을 다시 확인해 주세요.",
            modifier = Modifier.padding(top = 12.dp),
            style = SeniorOnTextStyles.BodyMMedium,
            color = SeniorOnColors.Gray500
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun FindIdResultSuccessPreview() {
    SENIOR_ONTheme {
        FindIdResultScreen(
            isSuccess = true,
            name = "홍길동",
            userId = "User_Id",
            joinDate = "2026.01.05",
            onBackClick = {},
            onLoginClick = {},
            onFindPasswordClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun FindIdResultFailurePreview() {
    SENIOR_ONTheme {
        FindIdResultScreen(
            isSuccess = false,
            name = "",
            userId = "",
            joinDate = "",
            onBackClick = {},
            onLoginClick = {},
            onFindPasswordClick = {}
        )
    }
}
