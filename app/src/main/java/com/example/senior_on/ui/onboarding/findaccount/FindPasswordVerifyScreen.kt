package com.example.senior_on.ui.onboarding.findaccount

import com.example.senior_on.ui.common.account.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import com.example.senior_on.domain.model.auth.VerificationRequestGate
import com.example.senior_on.ui.onboarding.rememberVerificationRequestUi

@Composable
fun FindPasswordVerifyScreen(
    maskedEmail: String,
    onBackClick: () -> Unit,
    onVerifySuccess: () -> Unit,
    onVerifyCode: (
        verificationCode: String,
        onResult: (Boolean) -> Unit
    ) -> Unit,
    onResendCode: (onResult: (Boolean) -> Unit) -> Unit,
    onTabSelected: (FindAccountTab) -> Unit = {},
    modifier: Modifier = Modifier,
    requestLoginId: String = "",
    verificationId: Long? = null,
    isVerified: Boolean = false,
    requestErrorMessage: String? = null,
    onInputChange: () -> Unit = {},
) {
    var verificationCode by rememberSaveable(requestLoginId, verificationId) { mutableStateOf("") }
    var isVerificationError by rememberSaveable(requestLoginId, verificationId) { mutableStateOf(false) }
    var isVerifying by androidx.compose.runtime.remember { mutableStateOf(false) }
    var isResending by androidx.compose.runtime.remember { mutableStateOf(false) }
    var inputAreaHeight by androidx.compose.runtime.remember { mutableStateOf(0) }
    val density = LocalDensity.current

    val requestUi = rememberVerificationRequestUi(VerificationRequestGate.recoveryKey(requestLoginId))
    val isVerifyEnabled = !isVerified && verificationCode.length == 6 && !isVerifying && !isResending && requestUi.remainingCodeSeconds > 0
    val verificationError = if (isVerified) null else requestErrorMessage ?: when {
        isVerificationError -> "인증번호가 일치하지 않아요."
        verificationId != null && requestUi.remainingCodeSeconds <= 0 ->
            "인증 시간이 만료됐어요. 재전송해 주세요."
        else -> null
    }

    FindAccountScaffold(
        modifier = modifier.systemBarsPadding(),
        keyboardProtectedContentHeight = with(density) { inputAreaHeight.toDp() } + 24.dp,
        onBackClick = onBackClick,
        selectedTab = FindAccountTab.Password,
        onTabSelected = onTabSelected,
        bottomBar = {
            Column {
                FindAccountPrimaryButton(
                    text = "인증하기",
                    enabled = isVerifyEnabled || (isVerified && !isVerifying && !isResending),
                    isLoading = isVerifying,
                    onClick = {
                        if (isVerified) {
                            onVerifySuccess()
                        } else if (isVerifyEnabled) {
                            isVerifying = true
                            onVerifyCode(verificationCode) { verified ->
                                isVerifying = false
                                isVerificationError = !verified
                                if (verified) onVerifySuccess()
                            }
                        }
                    },
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .onSizeChanged { inputAreaHeight = it.height }
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = SeniorOnColors.Primary600,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(maskedEmail)
                    }
                    withStyle(
                        SpanStyle(
                            color = SeniorOnColors.Gray800,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(if (maskedEmail.contains('@')) "으로\n" else "로\n")
                    }
                    withStyle(
                        SpanStyle(
                            color = SeniorOnColors.Gray800,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("인증번호를 전송했어요")
                    }
                },
                style = SeniorOnTextStyles.OnboardingHeading
            )

            Spacer(modifier = Modifier.height(30.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                    FindAccountTextField(
                        label = null,
                        value = verificationCode,
                        onValueChange = {
                            if (isVerified || isVerifying || isResending) return@FindAccountTextField
                            verificationCode = it.filter(Char::isDigit).take(6)
                            isVerificationError = false
                            onInputChange()
                        },
                        placeholder = "인증번호 6자리 입력",
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = isVerified || isVerifying || isResending,
                        showClearIcon = !isVerified && !isVerifying && !isResending,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = verificationError != null,
                        errorMessage = verificationError,
                    )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val seconds = requestUi.remainingCodeSeconds
                    Text(
                        text = if (isVerified) "인증 완료됐어요." else
                            "남은 시간 ${(seconds / 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}",
                        modifier = Modifier.weight(1f),
                        style = SeniorOnTextStyles.CaptionRegular,
                        color = if (isVerified) SeniorOnColors.Primary600 else SeniorOnColors.Red300,
                    )
                    TextButton(
                        enabled = !isVerified && !isVerifying && !isResending && !requestUi.locked,
                        onClick = {
                            isVerificationError = false
                            onInputChange()
                            isResending = true
                            onResendCode { sent ->
                                isResending = false
                                if (sent) {
                                    verificationCode = ""
                                    isVerificationError = false
                                }
                            }
                        }
                    ) {
                        Text(
                            text = when {
                                isResending -> "전송 중…"
                                !isVerified && requestUi.remainingRequestSeconds > 0 ->
                                    "${requestUi.remainingRequestSeconds}초 후 재전송 가능"
                                else -> "인증번호 재전송"
                            },
                            style = SeniorOnTextStyles.CaptionRegular,
                            textDecoration = if (!isResending && !requestUi.locked)
                                TextDecoration.Underline else TextDecoration.None,
                            color = if (isVerified || isVerifying || isResending || requestUi.locked)
                                SeniorOnColors.Gray400 else SeniorOnColors.Gray800,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun FindPasswordVerifyScreenPreview() {
    SENIOR_ONTheme {
        FindPasswordVerifyScreen(
            maskedEmail = "ex*******@gmail.com",
            onBackClick = {},
            onVerifySuccess = {},
            onVerifyCode = { code, onResult -> onResult(code == "123456") },
            onResendCode = { onResult -> onResult(true) }
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun FindPasswordVerifyErrorPreview() {
    SENIOR_ONTheme {
        FindPasswordVerifyScreen(
            maskedEmail = "ex*******@gmail.com",
            onBackClick = {},
            onVerifySuccess = {},
            onVerifyCode = { _, onResult -> onResult(false) },
            onResendCode = { onResult -> onResult(false) }
        )
    }
}
