package com.example.senior_on.ui.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun SignupScreen(
    onBackClick: () -> Unit,
    onKakaoClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onEmailClick: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.SupportWhite100)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SignupTopBar(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(79.dp))

            SignupLogo()

            Text(
                text = buildAnnotatedString {
                    append("부모님과 자녀를 잇는 ")
                    withStyle(SpanStyle(color = SeniorOnColors.Primary600)) {
                        append("디지털 케어 서비스")
                    }
                },
                modifier = Modifier.padding(top = 12.dp),
                style = SeniorOnTextStyles.CaptionMedium,
                color = SeniorOnColors.Gray800
            )

            Spacer(modifier = Modifier.height(52.dp))

            SignupAuthButton(
                text = "카카오로 시작하기",
                iconResId = R.drawable.ic_kakao,
                containerColor = SeniorOnColors.Yellow,
                contentColor = SeniorOnColors.Gray800,
                onClick = onKakaoClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            SignupAuthButton(
                text = "구글로 시작하기",
                iconResId = R.drawable.ic_google,
                containerColor = SeniorOnColors.SupportWhite100,
                contentColor = SeniorOnColors.Gray800,
                borderColor = SeniorOnColors.Gray200,
                contentWidth = 143.dp,
                useOriginalIconColor = true,
                onClick = onGoogleClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            SignupAuthButton(
                text = "이메일로 시작하기",
                iconResId = R.drawable.ic_mail,
                containerColor = SeniorOnColors.Background3,
                contentColor = SeniorOnColors.Gray800,
                onClick = onEmailClick
            )

            Spacer(modifier = Modifier.height(54.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "이미 계정이 있으신가요? ",
                    style = SeniorOnTextStyles.BodySMedium,
                    color = SeniorOnColors.Gray600
                )
                Text(
                    text = "로그인",
                    modifier = Modifier.clickable(onClick = onLoginClick),
                    style = SeniorOnTextStyles.BodySMedium,
                    color = SeniorOnColors.Primary600
                )
            }
        }
    }
}

@Composable
private fun SignupTopBar(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = "뒤로가기",
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.Gray800
            )
        }

        Text(
            text = "회원가입",
            modifier = Modifier.padding(start = 16.dp),
            style = SeniorOnTextStyles.HeadingS,
            color = SeniorOnColors.Gray800
        )
    }
}

@Composable
private fun SignupLogo() {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_splash_logo_text),
            contentDescription = null,
            modifier = Modifier.size(width = 110.dp, height = 34.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.ic_splash_on),
            contentDescription = null,
            modifier = Modifier.size(width = 60.dp, height = 44.dp)
        )
    }
}

@Composable
private fun SignupAuthButton(
    text: String,
    iconResId: Int,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    contentWidth: Dp? = null,
    useOriginalIconColor: Boolean = false
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Medium)
    val borderModifier = if (borderColor != null) {
        Modifier.border(
            width = 1.dp,
            color = borderColor,
            shape = shape
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(shape)
            .background(containerColor)
            .then(borderModifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = contentWidth?.let { Modifier.width(it) } ?: Modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (useOriginalIconColor) Color.Unspecified else contentColor
            )

            Text(
                text = text,
                modifier = Modifier.padding(start = 4.dp),
                style = SeniorOnTextStyles.ButtonM,
                color = contentColor
            )
        }
    }
}

@Preview(
    name = "Signup",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun SignupScreenPreview() {
    SENIOR_ONTheme {
        SignupScreen(
            onBackClick = {},
            onKakaoClick = {},
            onGoogleClick = {},
            onEmailClick = {},
            onLoginClick = {}
        )
    }
}
