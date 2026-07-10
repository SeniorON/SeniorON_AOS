package com.example.senior_on.ui.signup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun SignupTermsAgreementScreen(
    onBackClick: () -> Unit,
    onCompleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isServiceTermsAgreed by rememberSaveable { mutableStateOf(false) }
    var isPrivacyAgreed by rememberSaveable { mutableStateOf(false) }
    var isAgeAgreed by rememberSaveable { mutableStateOf(false) }
    var isMarketingAgreed by rememberSaveable { mutableStateOf(false) }
    var isTermsSheetVisible by rememberSaveable { mutableStateOf(false) }
    var hasOpenedTermsSheet by rememberSaveable { mutableStateOf(false) }
    var termsSheetInitialIndex by rememberSaveable { mutableStateOf(0) }
    val isRequiredAgreed = isServiceTermsAgreed && isPrivacyAgreed && isAgeAgreed
    val isAllAgreed = isRequiredAgreed && isMarketingAgreed

    fun openTermsSheet(initialTermsItem: TermsItem = TermsItem.Service) {
        hasOpenedTermsSheet = true
        termsSheetInitialIndex = initialTermsItem.ordinal
        isTermsSheetVisible = true
    }

    fun handleAgreementClick(
        initialTermsItem: TermsItem,
        onToggle: () -> Unit
    ) {
        if (hasOpenedTermsSheet) {
            onToggle()
        } else {
            openTermsSheet(initialTermsItem)
        }
    }

    SignupStepScaffold(
        progress = 4 / 4f,
        onBackClick = onBackClick,
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "서비스 이용을 위해\n동의가 필요해요",
            style = SeniorOnTextStyles.OnboardingHeading,
            color = SeniorOnColors.Gray800
        )

        Spacer(modifier = Modifier.height(24.dp))

        SignupAllAgreementCard(
            checked = isAllAgreed,
            onClick = {
                handleAgreementClick(TermsItem.Service) {
                    val nextChecked = !isAllAgreed
                    isServiceTermsAgreed = nextChecked
                    isPrivacyAgreed = nextChecked
                    isAgeAgreed = nextChecked
                    isMarketingAgreed = nextChecked
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        SignupAgreementRow(
            checked = isServiceTermsAgreed,
            text = TermsItem.Service.rowTitle,
            onCheckClick = {
                handleAgreementClick(TermsItem.Service) {
                    isServiceTermsAgreed = !isServiceTermsAgreed
                }
            },
            onDetailClick = { openTermsSheet(TermsItem.Service) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SignupAgreementRow(
            checked = isPrivacyAgreed,
            text = TermsItem.Privacy.rowTitle,
            onCheckClick = {
                handleAgreementClick(TermsItem.Privacy) {
                    isPrivacyAgreed = !isPrivacyAgreed
                }
            },
            onDetailClick = { openTermsSheet(TermsItem.Privacy) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SignupAgreementRow(
            checked = isAgeAgreed,
            text = TermsItem.Age.rowTitle,
            onCheckClick = {
                handleAgreementClick(TermsItem.Age) {
                    isAgeAgreed = !isAgeAgreed
                }
            },
            onDetailClick = { openTermsSheet(TermsItem.Age) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SignupAgreementRow(
            checked = isMarketingAgreed,
            text = TermsItem.Marketing.rowTitle,
            onCheckClick = {
                handleAgreementClick(TermsItem.Marketing) {
                    isMarketingAgreed = !isMarketingAgreed
                }
            },
            onDetailClick = { openTermsSheet(TermsItem.Marketing) }
        )

        Spacer(modifier = Modifier.weight(1f))

        SignupNextButton(
            enabled = isRequiredAgreed,
            onClick = onCompleteClick,
            text = "회원가입 완료"
        )

        Spacer(modifier = Modifier.height(22.5.dp))
    }

    if (isTermsSheetVisible) {
        SignupTermsBottomSheet(
            initialTermsItem = TermsItem.entries[termsSheetInitialIndex],
            isServiceTermsAgreed = isServiceTermsAgreed,
            isPrivacyAgreed = isPrivacyAgreed,
            isAgeAgreed = isAgeAgreed,
            isMarketingAgreed = isMarketingAgreed,
            onAllCheckedChange = { checked ->
                isServiceTermsAgreed = checked
                isPrivacyAgreed = checked
                isAgeAgreed = checked
                isMarketingAgreed = checked
            },
            onServiceCheckedChange = { isServiceTermsAgreed = it },
            onPrivacyCheckedChange = { isPrivacyAgreed = it },
            onAgeCheckedChange = { isAgeAgreed = it },
            onMarketingCheckedChange = { isMarketingAgreed = it },
            onDismiss = { isTermsSheetVisible = false }
        )
    }
}

@Composable
private fun SignupAllAgreementCard(
    checked: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .border(
                width = 1.dp,
                color = SeniorOnColors.Gray200,
                shape = RoundedCornerShape(SeniorOnRadius.Medium)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SignupAgreementCheckBox(
            checked = checked,
            isLarge = true
        )

        Text(
            text = "전체 동의",
            modifier = Modifier.padding(start = 8.dp),
            style = SeniorOnTextStyles.BodyLSemiBold,
            color = SeniorOnColors.Gray800
        )
    }
}

@Composable
private fun SignupAgreementRow(
    checked: Boolean,
    text: String,
    onCheckClick: () -> Unit,
    onDetailClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onCheckClick),
            contentAlignment = Alignment.Center
        ) {
            SignupAgreementCheckBox(checked = checked)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onCheckClick),
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray800
        )

        Box(
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onDetailClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_next),
                contentDescription = "약관 상세 보기",
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.Gray200
            )
        }
    }
}

@Composable
private fun SignupAgreementCheckBox(
    checked: Boolean,
    isLarge: Boolean = false
) {
    val iconSize = if (isLarge) 30.dp else 24.dp
    val checkedBoxSize = if (isLarge) 18.dp else 16.dp
    val checkSize = if (isLarge) 14.dp else 12.dp

    if (checked) {
        Box(
            modifier = Modifier.size(iconSize),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(checkedBoxSize)
                    .clip(RoundedCornerShape(if (isLarge) 4.dp else 3.dp))
                    .background(SeniorOnColors.Primary700),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(checkSize),
                    tint = SeniorOnColors.SupportWhite100
                )
            }
        }

        return
    }

    val iconResId = if (isLarge) {
        R.drawable.ic_big_checkcircle
    } else {
        R.drawable.ic_check_circle
    }

    Icon(
        painter = painterResource(id = iconResId),
        contentDescription = null,
        modifier = Modifier.size(iconSize),
        tint = SeniorOnColors.Gray300
    )
}

@Composable
private fun SignupTermsBottomSheet(
    initialTermsItem: TermsItem,
    isServiceTermsAgreed: Boolean,
    isPrivacyAgreed: Boolean,
    isAgeAgreed: Boolean,
    isMarketingAgreed: Boolean,
    onAllCheckedChange: (Boolean) -> Unit,
    onServiceCheckedChange: (Boolean) -> Unit,
    onPrivacyCheckedChange: (Boolean) -> Unit,
    onAgeCheckedChange: (Boolean) -> Unit,
    onMarketingCheckedChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val visibleState = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }

    fun closeWithAnimation() {
        visibleState.targetState = false
    }

    LaunchedEffect(visibleState.isIdle, visibleState.currentState) {
        if (visibleState.isIdle && !visibleState.currentState) {
            onDismiss()
        }
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = ::closeWithAnimation
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visibleState = visibleState,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(
                                topStart = 24.dp,
                                topEnd = 24.dp,
                                bottomStart = 0.dp,
                                bottomEnd = 0.dp
                            )
                        )
                        .background(SeniorOnColors.SupportWhite100)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                ) {
                    SignupTermsBottomSheetContent(
                        initialTermsItem = initialTermsItem,
                        isServiceTermsAgreed = isServiceTermsAgreed,
                        isPrivacyAgreed = isPrivacyAgreed,
                        isAgeAgreed = isAgeAgreed,
                        isMarketingAgreed = isMarketingAgreed,
                        onAllCheckedChange = onAllCheckedChange,
                        onServiceCheckedChange = onServiceCheckedChange,
                        onPrivacyCheckedChange = onPrivacyCheckedChange,
                        onAgeCheckedChange = onAgeCheckedChange,
                        onMarketingCheckedChange = onMarketingCheckedChange,
                        onDismiss = ::closeWithAnimation
                    )
                }
            }
        }
    }
}

@Composable
private fun SignupTermsBottomSheetContent(
    initialTermsItem: TermsItem,
    isServiceTermsAgreed: Boolean,
    isPrivacyAgreed: Boolean,
    isAgeAgreed: Boolean,
    isMarketingAgreed: Boolean,
    onAllCheckedChange: (Boolean) -> Unit,
    onServiceCheckedChange: (Boolean) -> Unit,
    onPrivacyCheckedChange: (Boolean) -> Unit,
    onAgeCheckedChange: (Boolean) -> Unit,
    onMarketingCheckedChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAllChecked = isServiceTermsAgreed &&
        isPrivacyAgreed &&
        isAgeAgreed &&
        isMarketingAgreed
    val isRequiredChecked = isServiceTermsAgreed && isPrivacyAgreed && isAgeAgreed
    val termsListState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialTermsItem.ordinal
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "약관 동의",
                style = SeniorOnTextStyles.BodyLSemiBold,
                color = SeniorOnColors.Gray800
            )

            Text(
                text = "닫기",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable(onClick = onDismiss),
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.Primary600
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        SignupTermsSheetAgreementRow(
            checked = isAllChecked,
            text = "전체 동의",
            onClick = { onAllCheckedChange(!isAllChecked) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(369.dp),
            state = termsListState,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SignupTermsSheetSection(
                    termsItem = TermsItem.Service,
                    checked = isServiceTermsAgreed,
                    onCheckedChange = onServiceCheckedChange
                )
            }
            item {
                SignupTermsSheetSection(
                    termsItem = TermsItem.Privacy,
                    checked = isPrivacyAgreed,
                    onCheckedChange = onPrivacyCheckedChange
                )
            }
            item {
                SignupTermsSheetSection(
                    termsItem = TermsItem.Age,
                    checked = isAgeAgreed,
                    onCheckedChange = onAgeCheckedChange
                )
            }
            item {
                SignupTermsSheetSection(
                    termsItem = TermsItem.Marketing,
                    checked = isMarketingAgreed,
                    onCheckedChange = onMarketingCheckedChange
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SignupNextButton(
            enabled = isRequiredChecked,
            onClick = onDismiss,
            text = "확인"
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SignupTermsSheetSection(
    termsItem: TermsItem,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SignupTermsSheetAgreementRow(
            checked = checked,
            text = termsItem.rowTitle,
            onClick = { onCheckedChange(!checked) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(182.dp)
                .clip(RoundedCornerShape(SeniorOnRadius.Medium))
                .border(
                    width = 1.dp,
                    color = SeniorOnColors.Gray200,
                    shape = RoundedCornerShape(SeniorOnRadius.Medium)
                )
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
        ) {
            LazyColumn {
                item {
                    Text(
                        text = termsItem.content.trimIndent().trim(),
                        style = SeniorOnTextStyles.CaptionMedium,
                        color = SeniorOnColors.Gray500
                    )
                }
            }
        }
    }
}

@Composable
private fun SignupTermsSheetAgreementRow(
    checked: Boolean,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SignupAgreementCheckBox(checked = checked)

        Text(
            text = text,
            modifier = Modifier.padding(start = 8.dp),
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray800
        )
    }
}

private enum class TermsItem(
    val rowTitle: String,
    val content: String
) {
    Service(
        rowTitle = "(필수) 서비스 이용약관 동의",
        content = """
            제1조 (목적)
            본 약관은 시니어ON이 제공하는 건강 관리 및 보호자 연동 서비스의 이용과 관련하여 회사와 회원 간의 권리, 의무 및 책임사항을 규정함을 목적으로 합니다.

            제2조 (서비스 이용)
            회사는 회원에게 건강 기록 관리, 복약 알림, 일정 관리, 보호자 연동, 병원 정보 관리 및 기타 부가 서비스를 제공합니다.
        """
    ),
    Privacy(
        rowTitle = "(필수) 개인정보 수집 및 이용 동의",
        content = """
            개인정보 수집 및 이용 동의
            회사는 회원가입, 본인 확인, 가족공유코드 기반 연동, 서비스 제공을 위해 필요한 최소한의 개인정보를 수집합니다.

            수집 항목은 이름, 생년월일, 이메일, 아이디, 비밀번호 등이며 수집 목적 달성 후 관련 법령에 따라 보관 또는 파기합니다.
        """
    ),
    Age(
        rowTitle = "(필수) 만 14세 이용확인 동의",
        content = """
            만 14세 이용확인 동의
            회원은 본인이 만 14세 이상임을 확인하며, 서비스 이용을 위한 회원가입 절차를 진행하는 것에 동의합니다.

            만 14세 미만인 경우 법정대리인의 동의가 필요할 수 있습니다.
        """
    ),
    Marketing(
        rowTitle = "(선택) 마케팅 활용 및 광고 수신 동의",
        content = """
            마케팅 활용 및 광고 수신 동의
            이벤트, 혜택, 서비스 안내 등 마케팅 정보를 수신하는 것에 동의합니다.

            선택 항목은 동의하지 않아도 서비스 이용이 가능하며, 언제든지 설정에서 철회할 수 있습니다.
        """
    )
}

@Preview(
    name = "Signup Terms Agreement",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun SignupTermsAgreementScreenPreview() {
    SENIOR_ONTheme {
        SignupTermsAgreementScreen(
            onBackClick = {},
            onCompleteClick = {}
        )
    }
}

@Preview(
    name = "Signup Terms Bottom Sheet",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun SignupTermsBottomSheetPreview() {
    SENIOR_ONTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 24.dp,
                            topEnd = 24.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        )
                    )
                    .background(SeniorOnColors.SupportWhite100)
            ) {
                SignupTermsBottomSheetContent(
                    initialTermsItem = TermsItem.Service,
                    isServiceTermsAgreed = true,
                    isPrivacyAgreed = true,
                    isAgeAgreed = false,
                    isMarketingAgreed = false,
                    onAllCheckedChange = {},
                    onServiceCheckedChange = {},
                    onPrivacyCheckedChange = {},
                    onAgeCheckedChange = {},
                    onMarketingCheckedChange = {},
                    onDismiss = {}
                )
            }
        }
    }
}
