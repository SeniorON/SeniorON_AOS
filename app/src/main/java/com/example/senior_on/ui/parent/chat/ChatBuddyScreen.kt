package com.example.senior_on.ui.parent.chat

import com.example.senior_on.ui.parent.chat.viewmodel.ChatBuddyUiState

import com.example.senior_on.ui.parent.chat.viewmodel.ChatBuddyPhase

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.domain.model.parent.ChatBuddySafetyType
import com.example.senior_on.ui.parent.component.ParentDetailTopBar
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun ChatBuddyScreen(
    uiState: ChatBuddyUiState,
    onBackClick: () -> Unit,
    onVoiceButtonClick: () -> Unit,
    onErrorConsumed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onErrorConsumed()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(R.drawable.bg_parent_chat),
                contentScale = ContentScale.Crop
            )
            .safeDrawingPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ParentDetailTopBar(
                title = "말벗",
                onBackClick = onBackClick,
                backgroundColor = Color.Transparent,
                showShadow = false
            )

            Spacer(modifier = Modifier.height(59.dp))

            ChatBuddyMessage(
                uiState = uiState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            if (uiState.phase == ChatBuddyPhase.Listening) {
                Row(
                    modifier = Modifier.padding(start = 16.dp, top = 17.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "듣고 있어요....",
                        style = SeniorOnTextStyles.HeadingM,
                        color = SeniorOnColors.Primary700
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        painter = painterResource(R.drawable.ic_ear),
                        contentDescription = "듣는 중",
                        modifier = Modifier.size(28.dp),
                        tint = Color.Unspecified
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            ChatBuddyVoiceButton(
                phase = uiState.phase,
                enabled = uiState.conversationId != null && !uiState.isStartingConversation,
                onClick = onVoiceButtonClick,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Text(
                text = if (uiState.isStartingConversation) {
                    "말벗을 준비하고 있어요"
                } else {
                    uiState.phase.instruction
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, start = 26.dp, end = 26.dp),
                style = SeniorOnTextStyles.HeadingS,
                color = SeniorOnColors.Gray600,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(63.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 20.dp),
        ) { data ->
            Snackbar(containerColor = SeniorOnColors.Gray700) {
                Text(
                    text = data.visuals.message,
                    style = SeniorOnTextStyles.BodyMMedium,
                    color = SeniorOnColors.SupportWhite100,
                )
            }
        }
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
            style = SeniorOnTextStyles.HeadingXXXL,
            color = SeniorOnColors.Gray800
        )

        val supportingText = when (uiState.phase) {
            ChatBuddyPhase.Listening -> ""
            ChatBuddyPhase.Thinking -> uiState.recognizedText

            ChatBuddyPhase.Responding -> uiState.replyText
            ChatBuddyPhase.Idle -> uiState.replyText
        }

        if (supportingText.isNotBlank()) {
            Text(
                text = supportingText,
                modifier = Modifier.padding(top = 17.dp),
                style = SeniorOnTextStyles.HeadingM,
                color = if (uiState.safetyType == ChatBuddySafetyType.EMERGENCY) {
                    SeniorOnColors.Red300
                } else {
                    SeniorOnColors.Primary700
                }
            )
        }
    }
}

@Composable
private fun ChatBuddyVoiceButton(
    phase: ChatBuddyPhase,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "chat buddy voice")
    val listeningProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "listening wave"
    )
    val thinkingStep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2_100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "thinking dots sequence"
    )
    val isInteractive = enabled && (
        phase == ChatBuddyPhase.Idle || phase == ChatBuddyPhase.Listening
    )
    val isResponding = phase == ChatBuddyPhase.Responding
    val outerGradient = if (isResponding) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF7ACE24).copy(alpha = 0.5f),
                Color(0xFFCCEFA7).copy(alpha = 0.5f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFF7ACE24), Color(0xFFCCEFA7))
        )
    }
    val buttonGradient = if (isResponding) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF578E1E).copy(alpha = 0.5f),
                Color(0xFF8EC951).copy(alpha = 0.5f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFF64B215), Color(0xFF8EC951))
        )
    }

    Box(
        modifier = modifier
            .size(177.dp)
            .clip(CircleShape)
            .background(brush = outerGradient)
            .clickable(
                enabled = isInteractive,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(161.dp)
                .clip(CircleShape)
                .background(Color(0xFFF2F9E8))
        )

        Box(
            modifier = Modifier
                .size(132.dp)
                .dropShadow(
                    shape = CircleShape,
                    shadow = Shadow(
                        radius = if (isResponding) 25.dp else 28.2.dp,
                        spread = 0.dp,
                        color = Color(0xFF91BC53).copy(
                            alpha = if (isResponding) 0.5f else 1f
                        ),
                        offset = DpOffset.Zero
                    )
                )
                .clip(CircleShape)
                .background(buttonGradient),
            contentAlignment = Alignment.Center
        ) {
            when (phase) {
                ChatBuddyPhase.Idle -> MicrophoneGraphic()
                ChatBuddyPhase.Listening -> VoiceWaveGraphic(progress = listeningProgress)
                ChatBuddyPhase.Thinking -> ThinkingDotsGraphic(progress = thinkingStep)
                ChatBuddyPhase.Responding -> Unit
            }
        }
    }
}

@Composable
private fun MicrophoneGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(60.dp)) {
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
    Canvas(modifier = modifier.size(66.dp)) {
        val barCount = 5
        val gap = size.width / (barCount + 1)
        val baseHeightRatios = floatArrayOf(0.48f, 0.70f, 0.92f, 0.70f, 0.48f)
        repeat(barCount) { index ->
            val phaseOffset = index * 0.72
            val wave = (
                (sin(progress * 2 * PI - phaseOffset) + 1.0) / 2.0
                ).toFloat()
            val heightScale = 0.78f + wave * 0.22f
            val barHeight = size.height * baseHeightRatios[index] * heightScale
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

@Composable
private fun ThinkingDotsGraphic(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(width = 48.dp, height = 16.dp)) {
        val gap = size.width / 4f
        repeat(3) { index ->
            val localProgress = (progress - index).coerceIn(0f, 1f)
            val wave = if (progress in index.toFloat()..(index + 1f)) {
                sin(localProgress * PI).toFloat().coerceAtLeast(0f)
            } else {
                0f
            }
            val radius = (3.2.dp + 1.2.dp * wave).toPx()
            val verticalOffset = 4.dp.toPx() * wave
            val alpha = 0.55f + 0.45f * wave
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = radius,
                center = androidx.compose.ui.geometry.Offset(
                    x = gap * (index + 1),
                    y = size.height / 2f - verticalOffset + 2.dp.toPx()
                )
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
    ChatBuddyPhase.Listening -> "지금\n말씀해 주세요"
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

@Preview(
    name = "Voice Button - Listening (Interactive)",
    showBackground = true,
    backgroundColor = 0xFFF8FCF3,
    widthDp = 240,
    heightDp = 240
)
@Composable
private fun ChatBuddyListeningButtonPreview() {
    SENIOR_ONTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ChatBuddyVoiceButton(
                phase = ChatBuddyPhase.Listening,
                onClick = {}
            )
        }
    }
}

@Preview(
    name = "Voice Button - Thinking (Interactive)",
    showBackground = true,
    backgroundColor = 0xFFF8FCF3,
    widthDp = 240,
    heightDp = 240
)
@Composable
private fun ChatBuddyThinkingButtonPreview() {
    SENIOR_ONTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ChatBuddyVoiceButton(
                phase = ChatBuddyPhase.Thinking,
                onClick = {}
            )
        }
    }
}
