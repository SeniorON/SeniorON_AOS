package com.example.senior_on.ui.parent.medication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import com.example.senior_on.ui.common.component.SeniorOnActionButton
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import com.example.senior_on.R
import com.example.senior_on.ui.consent.SeniorConsentSummary
import com.example.senior_on.ui.consent.SeniorConsentTermsRow
import com.example.senior_on.ui.theme.SeniorOnRadius
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.parent.component.ParentDetailTopBar
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

/** Show only after the server has confirmed that the senior has not consented. */
@Composable
internal fun ParentMedicationConsentScreen(
    saving: Boolean,
    saveError: String?,
    onAgree: () -> Unit,
    onLater: () -> Unit,
) {
    val terms = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    if (terms.value) com.example.senior_on.ui.onboarding.signup.ConsentTermDialog(com.example.senior_on.ui.onboarding.signup.ConsentTerm.Health) { terms.value = false }
    Column(Modifier.fillMaxSize().background(SeniorOnColors.Background1).statusBarsPadding()) {
        ParentDetailTopBar(title = "복약", onBackClick = onLater)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SeniorConsentSummary("선택 동의", "건강정보 수집·이용 동의",
                "복약 기능을 이용하려면 동의가 필요해요. 동의하지 않아도 다른 기능은 이용할 수 있어요.", R.drawable.ic_illust_medication)
            SeniorConsentTermsRow { terms.value = true }
            Column(Modifier.fillMaxWidth().background(SeniorOnColors.White, RoundedCornerShape(SeniorOnRadius.Medium)).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ConsentDetail("어떤 정보를 사용하나요?", "약 이름과 복용 시간, 복용 기록, 병원 정보를 사용해요.")
                HorizontalDivider(color = SeniorOnColors.Gray200)
                ConsentDetail("어디에 사용하나요?", "복약 알림과 복용 기록·병원 정보 관리에 사용하고, 연결된 보호자에게 복약 여부를 알려드려요.")
                HorizontalDivider(color = SeniorOnColors.Gray200)
                ConsentDetail("언제까지 보관하나요?", "회원 탈퇴 시 또는 해당 정보를 삭제할 때까지 보관해요.")
            }
        }
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (saveError != null) Text(saveError, style = SeniorOnTextStyles.BodyMMedium, color = SeniorOnColors.Red400)
            SeniorOnActionButton("동의하고 계속", onAgree, Modifier.fillMaxWidth(), enabled = !saving, isLoading = saving,
                minHeight = 64.dp, textStyle = SeniorOnTextStyles.HeadingS)
            SeniorOnActionButton("나중에", onLater, Modifier.fillMaxWidth(), containerColor = SeniorOnColors.White,
                contentColor = SeniorOnColors.Gray700, border = BorderStroke(1.dp, SeniorOnColors.Gray200),
                minHeight = 64.dp, textStyle = SeniorOnTextStyles.HeadingS)
        }
    }
}

@Composable
private fun ConsentDetail(title: String, text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = SeniorOnTextStyles.HeadingS, color = SeniorOnColors.Gray800)
        Text(text, style = SeniorOnTextStyles.HeadingXXS, color = SeniorOnColors.Gray700)
    }
}

@Preview(showBackground = true)
@Composable
private fun ParentMedicationConsentPreview() {
    SENIOR_ONTheme { ParentMedicationConsentScreen(false, null, {}, {}) }
}
