package com.example.senior_on.ui.parent.link

import com.example.senior_on.ui.parent.link.viewmodel.ParentLinkDetectionUiState

import com.example.senior_on.ui.parent.link.viewmodel.ParentLinkDetectionStatus

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun ParentLinkDetectionScreen(
    uiState: ParentLinkDetectionUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.SupportWhite100)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFC9F290).copy(alpha = 0.2f),
                        Color.Transparent
                    )
                )
            )
            .safeDrawingPadding()
            .padding(horizontal = 31.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        LinkInspectionIndicator()

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = when (uiState.status) {
                ParentLinkDetectionStatus.Dangerous -> "위험한 링크예요"
                ParentLinkDetectionStatus.Failed -> "링크를 확인하지 못했어요"
                else -> "잠시만 기다려 주세요"
            },
            style = SeniorOnTextStyles.Display,
            color = SeniorOnColors.Black,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = buildAnnotatedString {
                when (uiState.status) {
                    ParentLinkDetectionStatus.Dangerous -> {
                        withStyle(SpanStyle(color = SeniorOnColors.Red300)) {
                            append("안전을 위해 ")
                        }
                        append("페이지를 열지 않았어요.")
                    }
                    ParentLinkDetectionStatus.Failed -> {
                        append(uiState.errorMessage.orEmpty())
                    }
                    else -> {
                        withStyle(SpanStyle(color = SeniorOnColors.Primary700)) {
                            append("안전한 링크인지 ")
                        }
                        append("확인하는 중입니다.")
                    }
                }
            },
            style = SeniorOnTextStyles.HeadingXS,
            color = SeniorOnColors.Gray500,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(2f))
    }
}

@Composable
private fun LinkInspectionIndicator() {
    val transition = rememberInfiniteTransition(label = "link-inspection")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_250, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "link-inspection-rotation"
    )

    Box(
        modifier = Modifier.size(130.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(
                width = 6.dp.toPx(),
                cap = StrokeCap.Butt
            )

            drawCircle(
                color = SeniorOnColors.Gray200,
                style = stroke
            )
            drawArc(
                color = SeniorOnColors.Primary600,
                startAngle = rotation - 90f,
                sweepAngle = 360f * 0.32f,
                useCenter = false,
                style = stroke
            )
        }

        MagnifyingLinkIcon()
    }
}

@Composable
private fun MagnifyingLinkIcon(
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(R.drawable.ic_link_search),
        contentDescription = null,
        modifier = modifier.size(90.dp),
        tint = Color.Unspecified
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ParentLinkDetectionScreenPreview() {
    SENIOR_ONTheme {
        ParentLinkDetectionScreen(
            uiState = ParentLinkDetectionUiState(
                url = "https://www.naver.com",
                status = ParentLinkDetectionStatus.Checking
            )
        )
    }
}
