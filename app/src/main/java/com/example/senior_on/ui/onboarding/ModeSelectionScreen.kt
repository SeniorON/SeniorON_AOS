package com.example.senior_on.ui.onboarding

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

private enum class ModeType {
    Child,
    Senior
}

@Composable
fun ModeSelectionScreen(
    onChildClick: () -> Unit,
    onSeniorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMode by rememberSaveable { mutableStateOf<ModeType?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_splash),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(127.dp))

            Icon(
                painter = painterResource(id = R.drawable.ic_phone_setting),
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = SeniorOnColors.Primary600
            )

            Text(
                text = "누구로 시작할까요?",
                modifier = Modifier.padding(top = 20.dp),
                style = SeniorOnTextStyles.HeadingM,
                color = SeniorOnColors.Gray800,
                textAlign = TextAlign.Center
            )

            Text(
                text = "역할에 맞게 선택해주세요",
                modifier = Modifier.padding(top = 14.dp),
                style = SeniorOnTextStyles.BodyMRegular,
                color = SeniorOnColors.Gray600,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(61.5.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModeCard(
                    label = "자녀",
                    iconResId = R.drawable.ic_big_dependent,
                    selected = selectedMode == ModeType.Child,
                    dimmed = selectedMode != null && selectedMode != ModeType.Child,
                    onClick = {
                        selectedMode = ModeType.Child
                    },
                    modifier = Modifier.weight(1f)
                )
                ModeCard(
                    label = "시니어",
                    iconResId = R.drawable.ic_senior,
                    selected = selectedMode == ModeType.Senior,
                    dimmed = selectedMode != null && selectedMode != ModeType.Senior,
                    onClick = {
                        selectedMode = ModeType.Senior
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            val selected = selectedMode
            if (selected != null) {
                ModeSelectionNextButton(
                    onClick = {
                        when (selected) {
                            ModeType.Child -> onChildClick()
                            ModeType.Senior -> onSeniorClick()
                        }
                    }
                )
            } else {
                Spacer(modifier = Modifier.height(50.dp))
            }

            Spacer(modifier = Modifier.height(70.5.dp))
        }
    }
}

@Composable
private fun ModeSelectionNextButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(SeniorOnColors.Primary600)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "다음",
            style = SeniorOnTextStyles.ButtonM,
            color = SeniorOnColors.SupportWhite100
        )
    }
}

@Composable
private fun ModeCard(
    label: String,
    iconResId: Int,
    selected: Boolean,
    dimmed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Medium)
    val borderWidth = if (selected) 2.dp else 1.dp
    val borderColor = if (selected) SeniorOnColors.Primary400 else SeniorOnColors.Primary600
    val backgroundColor = if (selected) SeniorOnColors.Primary100 else SeniorOnColors.SupportWhite100
    val contentAlpha = if (dimmed) 0.5f else 1f

    Column(
        modifier = modifier
            .height(215.dp)
            .alpha(contentAlpha)
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(62.dp),
            tint = SeniorOnColors.Primary600
        )

        Text(
            text = label,
            modifier = Modifier.padding(top = 6.dp),
            style = SeniorOnTextStyles.BodyLSemiBold,
            color = SeniorOnColors.Gray800
        )

        Box(
            modifier = Modifier
                .padding(top = 2.15.dp)
                .size(width = 20.dp, height = 1.85.dp)
                .background(SeniorOnColors.Primary600)
        )

        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(30.dp)
                .background(SeniorOnColors.Primary200, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_vector_right),
                contentDescription = null,
                modifier = Modifier.size(width = 15.dp, height = 12.dp),
                tint = SeniorOnColors.Primary600
            )
        }
    }
}

@Preview(
    name = "Mode Selection",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun ModeSelectionScreenPreview() {
    SENIOR_ONTheme {
        ModeSelectionScreen(
            onChildClick = {},
            onSeniorClick = {}
        )
    }
}
