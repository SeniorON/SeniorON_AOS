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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun SignupModeGuideScreen(
    onBackClick: () -> Unit,
    onReselectClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
    showGuideImage: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.SupportWhite100)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SignupModeGuideTopBar(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            SignupModeGuideBadge()

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = SeniorOnColors.Primary700)) {
                        append("회원가입 모드")
                    }
                    append("를\n꼭 확인해주세요.")
                },
                modifier = Modifier.padding(top = 24.dp),
                style = SeniorOnTextStyles.OnboardingHeading,
                color = SeniorOnColors.Gray800
            )

            Text(
                text = buildAnnotatedString {
                    append("처음 선택한 모드는 계정에 저장되며,\n앞으로는 ")
                    withStyle(
                        SeniorOnTextStyles.BodySSemiBold.toSpanStyle().copy(
                            color = SeniorOnColors.Primary700,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("가입한 모드로만 로그인")
                    }
                    append("할 수 있으니,\n회원가입 전에 선택한 모드를 다시 한번 확인해주세요.")
                },
                modifier = Modifier.padding(top = 12.dp),
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray500
            )

            SignupModeGuidePreview(
                modifier = Modifier.padding(top = 34.dp),
                showImage = showGuideImage
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SignupModeGuideActionButton(
                    text = "다시 선택",
                    onClick = onReselectClick,
                    containerColor = SeniorOnColors.SupportWhite100,
                    contentColor = SeniorOnColors.Primary600,
                    borderColor = SeniorOnColors.Primary600,
                    modifier = Modifier.weight(1f)
                )

                SignupModeGuideActionButton(
                    text = "계속 진행",
                    onClick = onContinueClick,
                    containerColor = SeniorOnColors.Primary600,
                    contentColor = SeniorOnColors.SupportWhite100,
                    borderColor = null,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SignupModeGuideTopBar(
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
private fun SignupModeGuideBadge() {
    Box(
        modifier = Modifier
            .width(73.dp)
            .height(25.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(SeniorOnColors.Primary500),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "가입 안내",
            style = SeniorOnTextStyles.CaptionMedium,
            color = SeniorOnColors.SupportWhite100
        )
    }
}

@Composable
private fun SignupModeGuidePreview(
    modifier: Modifier = Modifier,
    showImage: Boolean = true
) {
    val isInPreview = LocalInspectionMode.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(243.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .border(
                width = 1.dp,
                color = SeniorOnColors.Background4,
                shape = RoundedCornerShape(SeniorOnRadius.Medium)
            )
    ) {
        if (showImage && !isInPreview) {
            Image(
                painter = painterResource(id = R.drawable.img_login_essential_read),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(width = 296.dp, height = 228.dp),
                contentScale = ContentScale.Fit,
                alignment = Alignment.BottomCenter
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SeniorOnColors.SupportWhite100)
            )
        }
    }
}

@Composable
private fun SignupModeGuideActionButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color?,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)
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
            .height(50.dp)
            .clip(shape)
            .background(containerColor)
            .then(borderModifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.ButtonM,
            color = contentColor
        )
    }
}

@Preview(
    name = "Signup Mode Guide",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun SignupModeGuideScreenPreview() {
    SENIOR_ONTheme {
        SignupModeGuideScreen(
            onBackClick = {},
            onReselectClick = {},
            onContinueClick = {},
            showGuideImage = false
        )
    }
}
