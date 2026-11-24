package com.example.senior_on.ui.onboarding.signup

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxHeight
import com.example.senior_on.ui.common.component.SeniorOnActionButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.onboarding.viewmodel.SignupAgreements
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

internal enum class ConsentTerm(val label: String, val resource: Int, val required: Boolean = true, val seniorOnly: Boolean = false) {
    Service("서비스 이용약관", R.raw.terms_service),
    Privacy("개인정보 수집 및 이용", R.raw.terms_privacy),
    Health("건강 관련 정보 수집 및 이용", R.raw.terms_health, false, true),
    Location("위치정보 수집·이용 및 제공", R.raw.terms_location, seniorOnly = true),
    Family("가족 구성원에 대한 개인정보 제공", R.raw.terms_family),
    Age("만 14세 이상 이용 확인", R.raw.terms_age),
    Marketing("마케팅 정보 활용 및 광고성 정보 수신", R.raw.terms_marketing, false);
    val bit get() = 1 shl ordinal
    companion object {
        fun forRole(senior: Boolean) = entries.filter { senior || !it.seniorOnly }
            .sortedByDescending { it.required }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConsentTermDialog(term: ConsentTerm, agree: (() -> Unit)? = null, close: () -> Unit) {
    val context = LocalContext.current
    val body = remember(term, context) {
        context.resources.openRawResource(term.resource).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
    ModalBottomSheet(onDismissRequest = close, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SeniorOnColors.SupportWhite100, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.85f).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(term.label, style = SeniorOnTextStyles.HeadingS, color = SeniorOnColors.Gray800)
            Text(body, Modifier.weight(1f).verticalScroll(rememberScrollState()), style = SeniorOnTextStyles.BodyMMedium,
                color = SeniorOnColors.Gray700)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SeniorOnActionButton("닫기", close, Modifier.weight(1f),
                    containerColor = if (agree == null) SeniorOnColors.Primary600 else SeniorOnColors.Background1,
                    contentColor = if (agree == null) SeniorOnColors.White else SeniorOnColors.Gray700)
                if (agree != null) SeniorOnActionButton("동의하기", agree, Modifier.weight(1f))
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
fun SignupTermsAgreementScreen(
    onBackClick: () -> Unit,
    onCompleteClick: (SignupAgreements, (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    isSenior: Boolean = false,
) {
    val terms = ConsentTerm.forRole(isSenior)
    var mask by rememberSaveable(isSenior) { mutableStateOf(0) }
    var detail by remember { mutableStateOf<ConsentTerm?>(null) }
    var submitting by remember { mutableStateOf(false) }
    fun checked(t: ConsentTerm) = mask and t.bit != 0
    val all = terms.all(::checked)
    SignupStepScaffold(progress = 1f, onBackClick = onBackClick, modifier = modifier) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(36.dp))
            Text("서비스 이용을 위해\n동의가 필요해요", style = SeniorOnTextStyles.OnboardingHeading, color = SeniorOnColors.Gray800)
            Spacer(Modifier.height(24.dp))
            SignupAllAgreementCard(all) { mask = if (all) 0 else terms.sumOf { it.bit } }
            terms.forEachIndexed { index, t ->
                Spacer(Modifier.height(if (index == 0) 12.dp else 16.dp))
                SignupAgreementRow(checked(t), (if (t.required) "(필수) " else "(선택) ") + t.label,
                    { mask = mask xor t.bit }, { detail = t })
            }
        }
        SignupNextButton(text = "회원가입 완료", enabled = !submitting && terms.filter { it.required }.all(::checked),
            isLoading = submitting, onClick = {
                submitting = true
                onCompleteClick(SignupAgreements(
                    serviceTerms = checked(ConsentTerm.Service), privacyPolicy = checked(ConsentTerm.Privacy),
                    ageOver14 = checked(ConsentTerm.Age), marketing = checked(ConsentTerm.Marketing),
                    agreeHealthInfo = isSenior && checked(ConsentTerm.Health),
                    agreeLocationInfo = isSenior && checked(ConsentTerm.Location),
                    agreeFamilyInfoProvision = checked(ConsentTerm.Family),
                )) { submitting = false }
            })
        Spacer(Modifier.height(22.5.dp))
    }
    detail?.let { ConsentTermDialog(it) { detail = null } }
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
            .heightIn(min = 24.dp)
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
