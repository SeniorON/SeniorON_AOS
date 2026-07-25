package com.example.senior_on.ui.parent.chat

import com.example.senior_on.ui.parent.chat.viewmodel.ChatBuddyUiState

import com.example.senior_on.ui.parent.chat.viewmodel.ChatBuddyPhase

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.parent.component.ParentDetailTopBar
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun ChatBuddyScreen(
    uiState: ChatBuddyUiState,
    onBackClick: () -> Unit,
    onVoiceButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SeniorOnColors.Primary100,
                        SeniorOnColors.White,
                        Color(0xFFFBFFF4)
                    )
                )
            )
            .safeDrawingPadding()
    ) {
        ParentDetailTopBar(
            title = "말벗",
            onBackClick = onBackClick,
            backgroundColor = Color.Transparent
        )

        ChatBuddyMessage(
            uiState = uiState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 28.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        ChatBuddyVoiceButton(
            phase = uiState.phase,
            onClick = onVoiceButtonClick,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = uiState.phase.instruction,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            style = SeniorOnTextStyles.BodyMSemiBold,
            color = SeniorOnColors.Gray600,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun ChatBuddyMessage(
    uiState: ChatBuddyUiState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = uiState.phase.headline(uiState.completedTurns),
            style = SeniorOnTextStyles.HeadingL,
            color = SeniorOnColors.Gray800
        )

        val supportingText = when (uiState.phase) {
            ChatBuddyPhase.Listening,
            ChatBuddyPhase.Thinking -> uiState.recognizedText

            ChatBuddyPhase.Responding -> uiState.replyText
            ChatBuddyPhase.Idle -> uiState.replyText
        }

        if (supportingText.isNotBlank()) {
            Text(
                text = supportingText,
                modifier = Modifier.padding(top = 12.dp),
                style = SeniorOnTextStyles.BodyLBold,
                color = SeniorOnColors.Primary700
            )
        }
    }
}

@Composable
private fun ChatBuddyVoiceButton(
    phase: ChatBuddyPhase,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "chat buddy voice")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "voice pulse"
    )
    val isInteractive = phase == ChatBuddyPhase.Idle ||
        phase == ChatBuddyPhase.Listening
    val buttonColor = when (phase) {
        ChatBuddyPhase.Responding -> SeniorOnColors.Primary400.copy(alpha = 0.55f)
        else -> Color(0xFF6FC52D)
    }

    Box(
        modifier = modifier.size(170.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .graphicsLayer {
                    val animatedScale = if (
                        phase == ChatBuddyPhase.Listening ||
                        phase == ChatBuddyPhase.Thinking
                    ) {
                        1f + pulse * 0.08f
                    } else {
                        1f
                    }
                    scaleX = animatedScale
                    scaleY = animatedScale
                    alpha = if (phase == ChatBuddyPhase.Responding) 0.45f else 0.7f
                }
                .border(
                    width = 7.dp,
                    color = SeniorOnColors.Primary400.copy(alpha = 0.58f),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .background(buttonColor)
                .clickable(
                    enabled = isInteractive,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            when (phase) {
                ChatBuddyPhase.Idle -> MicrophoneGraphic()
                ChatBuddyPhase.Listening -> VoiceWaveGraphic(progress = pulse)
                ChatBuddyPhase.Thinking -> Text(
                    text = "•••",
                    style = SeniorOnTextStyles.HeadingXL,
                    color = SeniorOnColors.White
                )
                ChatBuddyPhase.Responding -> Unit
            }
        }
    }
}

@Composable
private fun MicrophoneGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(42.dp)) {
        val centerX = size.width / 2
        val micWidth = size.width * 0.27f
        val micHeight = size.height * 0.5f
        val left = centerX - micWidth / 2
        val top = size.height * 0.12f

        drawRoundRect(
            color = Color.White,
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(micWidth, micHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(micWidth / 2)
        )
        drawArc(
            color = Color.White,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(
                centerX - size.width * 0.28f,
                top + micHeight * 0.35f
            ),
            size = androidx.compose.ui.geometry.Size(
                size.width * 0.56f,
                size.height * 0.42f
            ),
            style = Stroke(width = size.width * 0.07f, cap = StrokeCap.Round)
        )
        drawLine(
            color = Color.White,
            start = androidx.compose.ui.geometry.Offset(centerX, size.height * 0.72f),
            end = androidx.compose.ui.geometry.Offset(centerX, size.height * 0.88f),
            strokeWidth = size.width * 0.07f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White,
            start = androidx.compose.ui.geometry.Offset(
                centerX - size.width * 0.16f,
                size.height * 0.88f
            ),
            end = androidx.compose.ui.geometry.Offset(
                centerX + size.width * 0.16f,
                size.height * 0.88f
            ),
            strokeWidth = size.width * 0.07f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun VoiceWaveGraphic(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(width = 58.dp, height = 46.dp)) {
        val barCount = 5
        val gap = size.width / (barCount + 1)
        repeat(barCount) { index ->
            val phaseOffset = index * 0.55
            val wave = abs(sin(progress * 2 * PI + phaseOffset)).toFloat()
            val barHeight = size.height * (0.34f + wave * 0.58f)
            val x = gap * (index + 1)
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(
                    x,
                    size.height / 2 - barHeight / 2
                ),
                end = androidx.compose.ui.geometry.Offset(
                    x,
                    size.height / 2 + barHeight / 2
                ),
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

private val ChatBuddyPhase.instruction: String
    get() = when (this) {
        ChatBuddyPhase.Idle -> "눌러서 말하기"
        ChatBuddyPhase.Listening -> "말씀이 끝나면 버튼을 눌러주세요"
        ChatBuddyPhase.Thinking -> "답변을 준비하고 있어요"
        ChatBuddyPhase.Responding -> "답변이 끝나면 말씀하실 수 있어요"
    }

private fun ChatBuddyPhase.headline(completedTurns: Int): String = when (this) {
    ChatBuddyPhase.Idle -> if (completedTurns == 0) {
        "안녕하세요!\n말씀해 주세요"
    } else {
        "더 이야기해 주세요"
    }
    ChatBuddyPhase.Listening -> "지금\n말해주세요"
    ChatBuddyPhase.Thinking -> "생각 중...\n잠시만 기다려 주세요."
    ChatBuddyPhase.Responding -> "답변하고\n있어요...."
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ChatBuddyIdlePreview() {
    SENIOR_ONTheme {
        ChatBuddyScreen(
            uiState = ChatBuddyUiState(),
            onBackClick = {},
            onVoiceButtonClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ChatBuddyListeningPreview() {
    SENIOR_ONTheme {
        ChatBuddyScreen(
            uiState = ChatBuddyUiState(
                phase = ChatBuddyPhase.Listening,
                recognizedText = "오늘 날씨가 참 좋네요."
            ),
            onBackClick = {},
            onVoiceButtonClick = {}
        )
    }
}
