package com.example.senior_on.ui.settings

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
        question = "가족 코드를 잃어버렸어요",
        answer = "가족코드는 앱의 [설정] > [내 계정]에서 언제든 다시 확인할 수 있어요. " +
            "가족코드를 잃어버렸다면 해당 메뉴에서 코드를 확인한 후 가족에게 다시 공유해 주세요."
    ),
    HelpFaqItem(
        id = "install",
        question = "부모님 앱 설치 방법",
        answer = "답변 추가 예정"
    ),
    HelpFaqItem(
        id = "button",
        question = "버튼이 부모님 폰에 안 떠요",
        answer = "답변 추가 예정"
    ),
    HelpFaqItem(
        id = "notification",
        question = "알림이 오지 않아요",
        answer = "답변 추가 예정"
    )
)

private const val InstallGuideAnswer =
    "답변 추가 예정"

@Composable
fun HelpInquiryScreen(
    onBackClick: () -> Unit,
    onInquiryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedFaqId by rememberSaveable { mutableStateOf<String?>(null) }
    var isInstallGuideExpanded by rememberSaveable { mutableStateOf(false) }

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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                style = SeniorOnTextStyles.HeadingM,
                color = SeniorOnColors.Gray800
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(SeniorOnRadius.Small))
                    .background(SeniorOnColors.Background1)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "자주 묻는 질문",
                    style = SeniorOnTextStyles.BodyMBold,
                    color = SeniorOnColors.Gray800
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            HelpFaqItems.forEach { item ->
                HelpFaqRow(
                    question = item.question,
                    answer = item.answer,
                    expanded = expandedFaqId == item.id,
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
                answer = InstallGuideAnswer,
                expanded = isInstallGuideExpanded,
                onClick = { isInstallGuideExpanded = !isInstallGuideExpanded }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "다른 질문이 있으신가요?",
                modifier = Modifier.fillMaxWidth(),
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Primary600,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsPrimaryButton(
                text = "1:1 문의하기",
                enabled = true,
                onClick = onInquiryClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HelpFaqRow(
    question: String,
    answer: String,
    expanded: Boolean,
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
            .padding(horizontal = 16.dp, vertical = 14.dp)
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
                style = SeniorOnTextStyles.BodyMMedium,
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
                modifier = Modifier.size(20.dp),
                tint = SeniorOnColors.Gray300
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
    answer: String,
    expanded: Boolean,
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

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = SeniorOnTextStyles.BodyMMedium,
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
                modifier = Modifier.size(20.dp),
                tint = SeniorOnColors.Gray300
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, start = 34.dp)
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
