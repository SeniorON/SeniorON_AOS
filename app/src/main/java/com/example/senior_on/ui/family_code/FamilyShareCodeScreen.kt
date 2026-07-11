package com.example.senior_on.ui.family_code

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

enum class FamilyShareCodeOption {
    HasCode,
    NoCode
}

@Composable
fun FamilyShareCodeScreen(
    onBackClick: () -> Unit,
    onNextClick: (FamilyShareCodeOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOption by rememberSaveable {
        mutableStateOf<FamilyShareCodeOption?>(null)
    }

    FamilyShareCodeScreenContent(
        selectedOption = selectedOption,
        onOptionSelected = { selectedOption = it },
        onBackClick = onBackClick,
        onNextClick = {
            selectedOption?.let(onNextClick)
        },
        modifier = modifier
    )
}

@Composable
private fun FamilyShareCodeScreenContent(
    selectedOption: FamilyShareCodeOption?,
    onOptionSelected: (FamilyShareCodeOption) -> Unit,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .statusBarsPadding()
    ) {
        FamilyShareCodeTopBar(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(107.dp))
            FamilyShareCodeTitle()

            Spacer(modifier = Modifier.height(78.dp))
            FamilyShareCodeOptionButton(
                text = "네 있어요",
                selected = selectedOption == FamilyShareCodeOption.HasCode,
                onClick = {
                    onOptionSelected(FamilyShareCodeOption.HasCode)
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            FamilyShareCodeOptionButton(
                text = "아직 없어요",
                selected = selectedOption == FamilyShareCodeOption.NoCode,
                onClick = {
                    onOptionSelected(FamilyShareCodeOption.NoCode)
                }
            )
        }

        FamilyShareCodeBottomButton(
            text = "다음",
            enabled = selectedOption != null,
            onClick = onNextClick,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun FamilyShareCodeTitle(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = SeniorOnColors.Primary600)) {
                    append("가족공유코드")
                }
                append("가 있으신가요?")
            },
            modifier = Modifier.fillMaxWidth(),
            style = SeniorOnTextStyles.HeadingM,
            color = SeniorOnColors.Gray800,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "가족이 전달한 코드를 입력하면 서비스를 이용할 수 있어요.",
            modifier = Modifier.fillMaxWidth(),
            style = SeniorOnTextStyles.CaptionMedium,
            color = SeniorOnColors.Gray500,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FamilyShareCodeOptionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(104.dp)
    val backgroundColor = if (selected) {
        SeniorOnColors.Primary600
    } else {
        SeniorOnColors.White
    }
    val contentColor = if (selected) {
        SeniorOnColors.White
    } else {
        SeniorOnColors.Gray800
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(69.dp)
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = SeniorOnColors.Primary600,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.BodyLSemiBold,
            color = contentColor
        )
    }
}

@Preview(
    name = "Family Share Code",
    showBackground = true,
    widthDp = 360,
    heightDp = 707
)
@Composable
private fun FamilyShareCodeScreenPreview() {
    SENIOR_ONTheme {
        FamilyShareCodeScreenContent(
            selectedOption = FamilyShareCodeOption.NoCode,
            onOptionSelected = {},
            onBackClick = {},
            onNextClick = {}
        )
    }
}
