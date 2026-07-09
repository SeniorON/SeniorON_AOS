package com.example.senior_on.ui.signup

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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

@OptIn(ExperimentalMaterial3Api::class)
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
    var selectedTerms by rememberSaveable { mutableStateOf<TermsItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isRequiredAgreed = isServiceTermsAgreed && isPrivacyAgreed && isAgeAgreed
    val isAllAgreed = isRequiredAgreed && isMarketingAgreed

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
            onClick = { selectedTerms = TermsItem.Service }
        )

        Spacer(modifier = Modifier.height(12.dp))

        SignupAgreementRow(
            checked = isServiceTermsAgreed,
            text = "(필수) 서비스 이용약관 동의",
            onCheckClick = { isServiceTermsAgreed = !isServiceTermsAgreed },
            onDetailClick = { selectedTerms = TermsItem.Service }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SignupAgreementRow(
            checked = isPrivacyAgreed,
            text = "(필수) 개인정보 수집 및 이용 동의",
            onCheckClick = { isPrivacyAgreed = !isPrivacyAgreed },
            onDetailClick = { selectedTerms = TermsItem.Privacy }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SignupAgreementRow(
            checked = isAgeAgreed,
            text = "(필수) 만 14세 이용확인 동의",
            onCheckClick = { isAgeAgreed = !isAgeAgreed },
            onDetailClick = { selectedTerms = TermsItem.Age }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SignupAgreementRow(
            checked = isMarketingAgreed,
            text = "(선택) 마케팅 활용 및 광고 수신 동의",
            onCheckClick = { isMarketingAgreed = !isMarketingAgreed },
            onDetailClick = { selectedTerms = TermsItem.Marketing }
        )

        Spacer(modifier = Modifier.weight(1f))

        SignupNextButton(
            enabled = isRequiredAgreed,
            onClick = onCompleteClick
        )

        Spacer(modifier = Modifier.height(22.5.dp))
    }

    selectedTerms?.let { currentTerms ->
        SignupTermsBottomSheet(
            termsItem = currentTerms,
            sheetState = sheetState,
            isChecked = when (currentTerms) {
                TermsItem.Service -> isServiceTermsAgreed
                TermsItem.Privacy -> isPrivacyAgreed
                TermsItem.Age -> isAgeAgreed
                TermsItem.Marketing -> isMarketingAgreed
            },
            onCheckedChange = { checked ->
                when (currentTerms) {
                    TermsItem.Service -> isServiceTermsAgreed = checked
                    TermsItem.Privacy -> isPrivacyAgreed = checked
                    TermsItem.Age -> isAgeAgreed = checked
                    TermsItem.Marketing -> isMarketingAgreed = checked
                }
            },
            onDismiss = { selectedTerms = null }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignupTermsBottomSheet(
    termsItem: TermsItem,
    sheetState: SheetState,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SeniorOnColors.SupportWhite100,
        scrimColor = SeniorOnColors.Black.copy(alpha = 0.16f),
        dragHandle = null,
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )
    ) {
        SignupTermsBottomSheetContent(
            termsItem = termsItem,
            isChecked = isChecked,
            onCheckedChange = onCheckedChange,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun SignupTermsBottomSheetContent(
    termsItem: TermsItem,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = termsItem.sheetTitle,
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start=12.dp)
                .clickable { onCheckedChange(!isChecked) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            SignupAgreementCheckBox(checked = isChecked)

            Text(
                text = termsItem.rowTitle,
                modifier = Modifier.padding(start = 8.dp),
                style = SeniorOnTextStyles.BodyMMedium,
                color = SeniorOnColors.Gray800
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(281.dp)
                .clip(RoundedCornerShape(SeniorOnRadius.Medium))
                .border(
                    width = 1.dp,
                    color = SeniorOnColors.Gray200,
                    shape = RoundedCornerShape(SeniorOnRadius.Medium)
                )
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
        ) {
            LazyColumn(

            ) {
                item {
                    Text(
                        text = termsItem.content.trimIndent().trimStart(),
                        style = SeniorOnTextStyles.CaptionMedium,
                        color = SeniorOnColors.Gray500
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SignupNextButton(
            enabled = isChecked,
            onClick = onDismiss,
            text = "확인"
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private enum class TermsItem(
    val sheetTitle: String,
    val rowTitle: String,
    val content: String
) {
    Service(
        sheetTitle = "약관 동의",
        rowTitle = "(필수) 서비스 이용약관 동의",
        content = ServiceTermsContent
    ),
    Privacy(
        sheetTitle = "개인정보 동의",
        rowTitle = "(필수) 개인정보 수집 및 이용 동의",
        content = "개인정보 수집 및 이용 동의 내용은 추후 개인정보처리방침에 맞춰 연결합니다."
    ),
    Age(
        sheetTitle = "이용확인 동의",
        rowTitle = "(필수) 만 14세 이용확인 동의",
        content = "본인은 만 14세 이상이며, 서비스 이용을 위한 회원가입 절차를 진행하는 것에 동의합니다."
    ),
    Marketing(
        sheetTitle = "마케팅 동의",
        rowTitle = "(선택) 마케팅 활용 및 광고 수신 동의",
        content = "이벤트, 혜택, 서비스 안내 등 마케팅 정보 수신에 동의합니다. 선택 항목은 동의하지 않아도 서비스를 이용할 수 있습니다."
    )
}

private const val ServiceTermsContent = """
제1조 (목적)

본 약관은 시니어ON(이하 "회사")이 제공하는 건강 관리 및 보호자 연동 서비스(이하 "서비스")의 이용과 관련하여 회사와 회원 간의 권리, 의무 및 책임사항을 규정함을 목적으로 합니다.

제2조 (정의)

본 약관에서 사용하는 용어의 정의는 다음과 같습니다.

1. "서비스"란 회사가 제공하는 건강 기록 관리, 복약 알림, 일정 관리, 보호자 연동, 병원 정보 관리 및 이와 관련한 제반 서비스를 의미합니다.
2. "회원"이란 본 약관에 동의하고 회사와 이용계약을 체결하여 서비스를 이용하는 자를 말합니다.
3. "보호자"란 회원의 요청 또는 동의에 따라 회원의 건강 정보 및 서비스 이용 현황 등을 열람·관리할 수 있는 권한을 부여받은 자를 말합니다.
4. "연동"이란 회원과 보호자가 각자의 계정을 상호 연결하여 건강 정보 등을 공유하는 기능을 말합니다.
5. "콘텐츠"란 서비스 내에서 회사 또는 회원이 게시·등록·저장한 부호, 문자, 이미지, 데이터 등 일체의 정보를 말합니다.

제3조 (약관의 효력 및 변경)

1. 본 약관은 서비스 화면에 게시하거나 기타의 방법으로 회원에게 공지함으로써 효력이 발생합니다.
2. 회사는 관련 법령을 위반하지 않는 범위에서 본 약관을 변경할 수 있으며, 변경 시 적용일자 및 변경사유를 명시하여 적용일 최소 7일 전(회원에게 불리한 변경의 경우 30일 전)부터 공지합니다.
3. 회원이 변경된 약관에 동의하지 않는 경우, 이용계약을 해지할 수 있습니다. 공지 후에도 회원이 명시적으로 거부 의사를 표시하지 않고 서비스를 계속 이용하는 경우 변경된 약관에 동의한 것으로 봅니다.

제4조 (서비스의 내용)

1. 회사는 회원에게 다음 각 호의 서비스를 제공합니다.
- 건강 기록 관리
- 복약 알림
- 일정 관리
- 보호자 연동
- 병원 정보 관리
- 기타 회사가 추가 개발하거나 제휴를 통해 제공하는 부가 서비스
2. 회사는 서비스의 품질 향상, 운영상·기술상의 필요에 따라 서비스의 전부 또는 일부 내용을 변경하거나 추가·중단할 수 있으며, 이 경우 사전에 공지합니다. 다만 긴급한 경우 사후 공지할 수 있습니다.

제5조 (이용계약의 성립)

1. 이용계약은 이용자가 본 약관에 동의하고 회사가 정한 절차에 따라 이용신청을 하며, 회사가 이를 승낙함으로써 성립합니다.
2. 회사는 다음 각 호에 해당하는 경우 이용신청을 승낙하지 않거나 사후에 이용계약을 해지할 수 있습니다.
- 타인의 명의를 도용한 경우
- 허위 정보를 기재한 경우
- 만 14세 미만 아동이 법정대리인의 동의 없이 신청한 경우
- 기타 회원으로 등록하는 것이 서비스 운영상 현저히 지장이 있다고 판단되는 경우

제6조 (보호자 연동 서비스)

1. 회원은 본인의 선택에 따라 배우자, 자녀 등 특정인을 "보호자"로 지정하여 건강 정보 및 서비스 이용 현황을 공유할 수 있습니다.
2. 보호자 연동은 회원 본인의 명시적 동의 및 인증 절차(연동 코드 입력, 초대 승인 등)를 통해서만 이루어집니다.
3. 회원은 언제든지 보호자 연동을 해제할 수 있으며, 연동 해제 시 보호자는 더 이상 해당 회원의 정보를 열람할 수 없습니다.
4. 보호자가 열람할 수 있는 정보의 범위는 회원이 직접 설정할 수 있으며, 회사는 회원이 설정한 범위를 초과하여 정보를 제공하지 않습니다.
5. 회원이 스스로 개인정보 처리에 대한 판단이 어려운 경우(고령, 인지저하 등) 법정대리인 또는 실질적 부양자가 회원을 대리하여 본 조의 동의 절차를 진행할 수 있으며, 이 경우 관련 법령이 정한 절차를 따릅니다.

제7조 (개인정보의 보호)

1. 회사는 관련 법령이 정하는 바에 따라 회원의 개인정보를 보호하기 위해 노력하며, 개인정보의 보호 및 이용에 관하여는 관련 법령 및 회사의 개인정보처리방침이 적용됩니다.
2. 건강 정보 등 민감정보는 관련 법령에 따라 별도의 동의를 받은 경우에 한하여 수집·이용·제공되며, 회사의 개인정보처리방침에서 정한 목적 범위 내에서만 처리됩니다.
3. 회사는 회원의 개인정보를 본인의 동의 없이 제3자에게 제공하지 않습니다. 다만 법령에 특별한 규정이 있는 경우는 예외로 합니다.

제8조 (회사의 의무)

1. 회사는 관련 법령과 본 약관이 금지하거나 미풍양속에 반하는 행위를 하지 않으며, 지속적이고 안정적으로 서비스를 제공하기 위해 노력합니다.
2. 회사는 회원의 개인정보(건강정보 포함) 보호를 위한 보안 시스템을 구축하며, 개인정보처리방침을 공시하고 준수합니다.
3. 회사는 서비스 이용과 관련하여 회원으로부터 제기된 의견이나 불만이 정당하다고 인정할 경우 이를 처리하기 위한 절차를 마련·운영합니다.

제9조 (회원의 의무)

1. 회원은 본 약관 및 관계 법령을 준수하여야 하며, 다음 각 호의 행위를 하여서는 안 됩니다.
- 타인의 개인정보(건강정보 포함)를 도용하거나 무단으로 수집·이용하는 행위
- 회사의 서비스 운영을 방해하거나 시스템에 부정하게 접근하는 행위
- 서비스를 이용하여 법령 또는 공서양속에 반하는 정보를 유포하는 행위
- 회사의 지적재산권 및 제3자의 권리를 침해하는 행위
2. 회원은 계정 정보(아이디, 비밀번호, 연동 코드 등)를 안전하게 관리할 책임이 있으며, 제3자에게 자신의 계정을 이용하게 해서는 안 됩니다.
3. 회원은 보호자를 지정함에 있어 진실한 관계에 기반하여야 하며, 부정한 목적으로 타인을 보호자로 등록해서는 안 됩니다.

제10조 (서비스 이용 제한 및 계약 해지)

1. 회원이 관련 법령 또는 본 약관을 위반하는 경우, 회사는 사전 통지 후 서비스 이용을 제한하거나 이용계약을 해지할 수 있습니다. 다만 긴급히 조치할 필요가 있다고 인정되는 경우 사후에 통지할 수 있습니다.
2. 회원은 언제든지 서비스 내 설정 메뉴 또는 고객센터를 통해 이용계약 해지(회원 탈퇴)를 신청할 수 있으며, 회사는 관련 법령이 정한 바에 따라 이를 즉시 처리합니다.
3. 이용계약이 종료되는 경우, 회원과 연동된 보호자와의 정보 공유도 함께 종료되며, 회사는 관련 법령 및 개인정보처리방침에 따라 회원의 정보를 파기하거나 보관합니다.

제11조 (서비스 제공의 중단)

1. 회사는 다음 각 호에 해당하는 경우 서비스 제공을 일시적으로 중단할 수 있습니다.
- 서비스 설비의 보수, 점검, 교체 또는 고장 등 부득이한 경우
- 국가비상사태, 정전, 서비스 설비 장애 또는 이용 폭주 등으로 정상적인 서비스 제공이 어려운 경우
- 기타 회사의 제반 사정으로 서비스를 제공할 수 없는 경우
2. 회사는 제1항의 사유로 서비스 제공이 중단되는 경우, 그 사실을 사전 또는 사후에 지체 없이 회원에게 공지합니다.

제12조 (면책조항)

1. 회사는 천재지변, 통신설비의 장애 등 불가항력으로 인하여 서비스를 제공할 수 없는 경우 책임이 면제됩니다.
2. 회사는 회원의 귀책사유로 인한 서비스 이용 장애에 대하여 책임을 지지 않습니다.
3. 회사가 제공하는 건강 관리 정보 및 알림은 참고용이며 의학적 진단·처방을 대체하지 않습니다. 회원의 건강상 판단 및 의료적 결정은 반드시 의료 전문가와 상담하여야 하며, 회사는 이를 근거로 한 회원의 결정에 대해 책임을 지지 않습니다.
4. 회사는 회원 간 또는 회원과 보호자 간에 발생한 분쟁에 대하여 개입할 의무가 없으며, 이로 인한 손해에 대해 책임을 지지 않습니다.

제13조 (손해배상)

회사 또는 회원은 본 약관을 위반하여 상대방에게 손해를 끼친 경우, 관련 법령에 따라 그 손해를 배상할 책임이 있습니다.

제14조 (분쟁 해결 및 관할법원)

1. 회사와 회원 간에 발생한 분쟁에 대하여는 소송 제기 전에 상호 협의하여 원만히 해결하도록 노력합니다.
2. 본 약관과 관련하여 분쟁이 발생하여 소송이 제기되는 경우, 민사소송법상의 관할법원에 제소합니다.
3. 본 약관은 대한민국 법령에 따라 규정되고 이행됩니다.

부칙

본 약관은 2026년 8월 19일부터 시행합니다.
"""

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
                .background(SeniorOnColors.Black.copy(alpha = 0.16f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 28.dp,
                            topEnd = 28.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        )
                    )
                    .background(SeniorOnColors.SupportWhite100)
            ) {
                SignupTermsBottomSheetContent(
                    termsItem = TermsItem.Service,
                    isChecked = true,
                    onCheckedChange = {},
                    onDismiss = {}
                )
            }
        }
    }
}
