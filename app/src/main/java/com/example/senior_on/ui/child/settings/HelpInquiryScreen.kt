package com.example.senior_on.ui.child.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

private data class HelpFaqItem(
    val id: String,
    val question: String,
    val answer: String
)

private val HelpFaqItems = listOf(
    HelpFaqItem(
        id = "family_code",
        question = "가족공유코드를 잃어버렸어요.",
        answer = "가족공유코드는 앱의 [가족탭]>[가족추가 버튼]을 누르시면 확인할 수 있습니다. " +
            "해당 화면에서 코드를 확인하신 후 가족에게 다시 공유해 주세요."
    ),
    HelpFaqItem(
        id = "home_app",
        question = "시니어 홈 화면에 추가하고 싶은 앱이 없어요.",
        answer = "버튼 목록은 보호자 휴대폰에 설치된 앱을 기준으로 표시됩니다. " +
            "시니어On은 개인정보 보호를 위해 시니어 휴대폰에 설치된 앱 목록을 수집하지 않습니다.\n\n" +
            "원하는 앱이 목록에 없다면 보호자 휴대폰에 해당 앱을 먼저 설치해 주세요. " +
            "또한 버튼이 정상적으로 작동하려면 시니어 휴대폰에도 동일한 앱이 설치되어 있어야 합니다."
    ),
    HelpFaqItem(
        id = "button",
        question = "버튼이 시니어의 화면에 반영이 안 돼요.",
        answer = "먼저 시니어 휴대폰이 Wi-Fi 또는 모바일 데이터에 연결되어 있는지 확인해 주세요. " +
            "인터넷에 연결되지 않은 경우 화면 변경 사항이 즉시 반영되지 않을 수 있습니다."
    ),
    HelpFaqItem(
        id = "notification",
        question = "알림이 오지 않아요.",
        answer = "- 시니어 휴대폰의 알림 권한이 허용되어 있는지 확인해 주세요. (설정 > 앱 > 시니어On > 알림)\n" +
            "- 시니어 휴대폰의 배터리 절약 모드가 켜져 있으면 알림이 지연되거나 수신되지 않을 수 있습니다. " +
            "시니어On을 배터리 최적화 제외 앱으로 설정해 주세요.\n" +
            "- [알림탭]에서 원하는 알림(SOS/무활동감지 등)이 켜져 있는지 확인해 주세요.\n" +
            "- 시니어 휴대폰이 방해금지 모드로 설정되어 있지 않은지 확인해 주세요."
    )
)

@Composable
fun HelpInquiryScreen(
    onBackClick: () -> Unit,
    onInquiryClick: () -> Unit,
    modifier: Modifier = Modifier,
    onInstallGuideClick: () -> Unit = {},
) {
    var expandedFaqId by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .statusBarsPadding()
    ) {
        SettingsBackTopAppBar(
            title = "도움말 · 문의",
            onBackClick = onBackClick
        )

        HorizontalDivider(
            thickness = 0.5.dp,
            color = SeniorOnColors.Gray200
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "무엇을\n도와드릴까요?",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp),
                style = SeniorOnTextStyles.HeadingM,
                color = SeniorOnColors.Gray800
            )

            Spacer(modifier = Modifier.height(34.dp))

            Text(
                text = "자주 묻는 질문",
                modifier = Modifier.padding(start = 26.dp),
                style = SeniorOnTextStyles.BodyLBold,
                color = SeniorOnColors.Gray800
            )

            Spacer(modifier = Modifier.height(26.dp))

            HelpFaqItems.forEachIndexed { index, item ->
                HelpFaqRow(
                    question = item.question,
                    answer = item.answer,
                    expanded = expandedFaqId == item.id,
                    topPadding = if (index == 0) 0.dp else 14.dp,
                    onClick = {
                        expandedFaqId = if (expandedFaqId == item.id) null else item.id
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(SeniorOnColors.Gray50)
            )

            HelpGuideRow(
                title = "부모님 앱 설치 가이드",
                onClick = onInstallGuideClick,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = SeniorOnColors.Primary600,
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append("다른 질문이 ")
                    }
                    withStyle(SpanStyle(color = SeniorOnColors.Gray600)) {
                        append("있으신가요?")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                style = SeniorOnTextStyles.BodySRegular,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsPrimaryButton(
                text = "1:1 문의하기",
                enabled = true,
                onClick = onInquiryClick,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 168.dp, height = 42.dp)
            )

            Spacer(modifier = Modifier.height(82.dp))
        }
    }
}

@Composable
private fun HelpFaqRow(
    question: String,
    answer: String,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    topPadding: Dp = 14.dp,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp)
            .padding(top = topPadding, bottom = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Q",
                style = SeniorOnTextStyles.BodyMBold,
                color = SeniorOnColors.Primary600
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = question,
                modifier = Modifier.weight(1f),
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray800
            )

            Icon(
                painter = painterResource(
                    id = if (expanded) {
                        R.drawable.ic_sm_fold
                    } else {
                        R.drawable.ic_sm_chevron_down_2
                    }
                ),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.Gray500
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, start = 22.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(SeniorOnRadius.Small))
                    .background(SeniorOnColors.Gray50)
                    .padding(12.dp)
            ) {
                Text(
                    text = answer,
                    style = SeniorOnTextStyles.BodySRegular,
                    color = SeniorOnColors.Gray600
                )
            }
        }
    }
}

@Composable
private fun HelpGuideRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_download),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.Primary600
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = SeniorOnTextStyles.BodyMMedium,
                color = SeniorOnColors.Gray700
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(180f),
                tint = SeniorOnColors.Gray500
            )
        }

    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HelpInquiryScreenPreview() {
    SENIOR_ONTheme {
        HelpInquiryScreen(
            onBackClick = {},
            onInquiryClick = {}
        )
    }
}
