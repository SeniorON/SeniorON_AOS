package com.example.senior_on.ui.family_code

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

const val MockFamilyShareCode = "43TS-6GTE"

@Composable
fun FamilyShareCodeCreatedScreen(
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
    familyShareCode: String = MockFamilyShareCode
) {
    var isCopied by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

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
            Spacer(modifier = Modifier.height(108.dp))
            FamilyShareCodeCreatedTitle()
            Spacer(modifier = Modifier.height(52.dp))
            FamilyShareCodeBox(
                code = familyShareCode,
                onCopyClick = {
                    copyFamilyShareCode(
                        context = context,
                        familyShareCode = familyShareCode
                    )
                    isCopied = true
                }
            )

            if (isCopied) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "코드가 복사됐습니다.",
                    style = SeniorOnTextStyles.CaptionRegular,
                    color = SeniorOnColors.Primary600
                )
            }
        }

        FamilyShareCodeBottomButton(
            text = "다음",
            enabled = true,
            onClick = onNextClick,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun FamilyShareCodeCreatedTitle(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = SeniorOnColors.Primary600)) {
                    append("가족공유코드")
                }
                append("가 생성되었어요")
            },
            modifier = Modifier.fillMaxWidth(),
            style = SeniorOnTextStyles.HeadingM,
            color = SeniorOnColors.Gray800,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "가족 구성원에게 아래 코드를 공유해 주세요.",
            modifier = Modifier.fillMaxWidth(),
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray400,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FamilyShareCodeBox(
    code: String,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(71.dp)
            .border(
                width = 1.dp,
                color = SeniorOnColors.Gray200,
                shape = RoundedCornerShape(SeniorOnRadius.Medium)
            )
            .padding(start = 12.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = code,
            modifier = Modifier.weight(1f),
            style = SeniorOnTextStyles.OnboardingHeading,
            color = SeniorOnColors.Gray800
        )

        Box(
            modifier = Modifier
                .size(26.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCopyClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_family_code_copy),
                contentDescription = "가족공유코드 복사",
                modifier = Modifier.size(26.dp),
                tint = SeniorOnColors.Gray300
            )
        }
    }
}

private fun copyFamilyShareCode(
    context: Context,
    familyShareCode: String
) {
    val clipboardManager = context.getSystemService(
        Context.CLIPBOARD_SERVICE
    ) as ClipboardManager
    val clipData = ClipData.newPlainText("가족공유코드", familyShareCode)

    clipboardManager.setPrimaryClip(clipData)
}

@Preview(
    name = "Family Share Code Created",
    showBackground = true,
    widthDp = 360,
    heightDp = 707
)
@Composable
private fun FamilyShareCodeCreatedScreenPreview() {
    SENIOR_ONTheme {
        FamilyShareCodeCreatedScreen(
            onBackClick = {},
            onNextClick = {}
        )
    }
}
