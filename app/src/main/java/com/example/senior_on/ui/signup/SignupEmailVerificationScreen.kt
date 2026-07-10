package com.example.senior_on.ui.signup

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.senior_on.data.auth.MockSignupAuthRepository
import com.example.senior_on.data.auth.SignupAuthRepository
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val VerificationTimeoutSeconds = 5 * 60

private enum class EmailVerificationResult {
    None,
    Invalid,
    Verified
}

@Composable
fun SignupEmailVerificationScreen(
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
    authRepository: SignupAuthRepository = MockSignupAuthRepository()
) {
    val coroutineScope = rememberCoroutineScope()
    var email by rememberSaveable { mutableStateOf("") }
    var verificationCode by rememberSaveable { mutableStateOf("") }
    var hasRequestedCode by rememberSaveable { mutableStateOf(false) }
    var verificationResult by rememberSaveable { mutableStateOf(EmailVerificationResult.None) }
    var remainingSeconds by rememberSaveable { mutableIntStateOf(VerificationTimeoutSeconds) }
    var requestCount by rememberSaveable { mutableIntStateOf(0) }
    var isRequestingCode by rememberSaveable { mutableStateOf(false) }
    var isVerifyingCode by rememberSaveable { mutableStateOf(false) }

    val canRequestVerification = email.isValidEmail() && !isRequestingCode
    val isVerified = verificationResult == EmailVerificationResult.Verified
    val shouldShowVerificationCodeInput = hasRequestedCode && !isRequestingCode
    val canVerifyCode = hasRequestedCode &&
        verificationCode.length == 6 &&
        !isVerified &&
        !isVerifyingCode

    LaunchedEffect(hasRequestedCode, requestCount, isVerified) {
        if (!hasRequestedCode || isVerified) return@LaunchedEffect

        remainingSeconds = VerificationTimeoutSeconds
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds -= 1
        }
    }

    SignupStepScaffold(
        progress = 2 / 4f,
        onBackClick = onBackClick,
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "이메일을 인증해 주세요",
            style = SeniorOnTextStyles.OnboardingHeading,
            color = SeniorOnColors.Gray800
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column {
            Row(verticalAlignment = Alignment.Bottom) {
                SignupUnderlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        hasRequestedCode = false
                        verificationCode = ""
                        verificationResult = EmailVerificationResult.None
                    },
                    placeholder = "이메일 입력",
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(7.dp))

                SignupEmailRequestButton(
                    text = if (shouldShowVerificationCodeInput) "재전송" else "인증 요청",
                    width = if (shouldShowVerificationCodeInput) 53.dp else 68.dp,
                    enabled = canRequestVerification,
                    onClick = {
                        coroutineScope.launch {
                            isRequestingCode = true
                            hasRequestedCode = false
                            verificationCode = ""
                            verificationResult = EmailVerificationResult.None
                            val isSent = authRepository.requestEmailVerification(email)
                            isRequestingCode = false

                            if (isSent) {
                                hasRequestedCode = true
                                requestCount += 1
                            }
                        }
                    }
                )
            }

            if (shouldShowVerificationCodeInput) {
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "인증번호를 전송했어요.",
                    style = SeniorOnTextStyles.CaptionRegular,
                    color = SeniorOnColors.Primary600
                )
            }
        }

        if (shouldShowVerificationCodeInput) {
            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    SignupVerificationCodeField(
                        value = verificationCode,
                        onValueChange = {
                            verificationCode = it.filter(Char::isDigit).take(6)
                            verificationResult = EmailVerificationResult.None
                        },
                        placeholder = "인증번호 6자리 입력",
                        timerText = if (isVerified) null else remainingSeconds.toTimerText(),
                        errorMessage = if (verificationResult == EmailVerificationResult.Invalid) {
                            "인증번호가 일치하지 않아요."
                        } else {
                            null
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    SignupCodeVerificationButton(
                        verified = isVerified,
                        enabled = canVerifyCode,
                        onClick = {
                            coroutineScope.launch {
                                isVerifyingCode = true
                                val verified = authRepository.verifyEmailCode(
                                    email = email,
                                    code = verificationCode
                                )
                                isVerifyingCode = false
                                verificationResult = if (verified) {
                                    EmailVerificationResult.Verified
                                } else {
                                    EmailVerificationResult.Invalid
                                }
                            }
                        }
                    )
                }

                if (isVerified) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "인증 완료됐어요.",
                        style = SeniorOnTextStyles.CaptionRegular,
                        color = SeniorOnColors.Primary600
                    )
                } else if (verificationResult == EmailVerificationResult.Invalid) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "인증번호가 일치하지 않아요.",
                        style = SeniorOnTextStyles.CaptionRegular,
                        color = SeniorOnColors.Red300
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        SignupNextButton(
            enabled = isVerified,
            onClick = onNextClick
        )

        Spacer(modifier = Modifier.height(22.5.dp))
    }
}

@Composable
private fun SignupVerificationCodeField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    timerText: String?,
    modifier: Modifier = Modifier,
    errorMessage: String? = null
) {
    var isFocused by rememberSaveable { mutableStateOf(false) }
    val underlineColor = when {
        errorMessage != null -> SeniorOnColors.Red300
        isFocused -> SeniorOnColors.Primary600
        else -> SeniorOnColors.Gray200
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(43.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(43.dp)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                textStyle = SeniorOnTextStyles.BodyMMedium.copy(color = SeniorOnColors.Gray800),
                cursorBrush = SolidColor(SeniorOnColors.Primary600),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (value.isEmpty() && !isFocused) {
                            Text(
                                text = placeholder,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(start = 6.dp, bottom = 9.5.dp),
                                style = SeniorOnTextStyles.BodyMMedium,
                                color = SeniorOnColors.Gray300
                            )
                        }

                        Box(
                            propagateMinConstraints = true,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .padding(
                                    start = 6.dp,
                                    end = if (timerText != null) 76.dp else 6.dp,
                                    bottom = 9.5.dp
                                )
                        ) {
                            innerTextField()
                        }
                    }
                }
            )

            if (timerText != null) {
                Text(
                    text = timerText,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 6.dp, bottom = 9.5.dp),
                    style = SeniorOnTextStyles.BodyMMedium,
                    color = SeniorOnColors.Primary600
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(underlineColor)
        )
    }
}

@Composable
private fun SignupEmailRequestButton(
    text: String,
    width: Dp,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(36.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .background(
                color = SeniorOnColors.Primary600,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.ButtonS,
            color = SeniorOnColors.SupportWhite100
        )
    }
}

@Composable
private fun SignupCodeVerificationButton(
    verified: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    val borderColor = if (verified) SeniorOnColors.Gray200 else SeniorOnColors.Primary600
    val contentColor = if (verified) SeniorOnColors.Gray300 else SeniorOnColors.Primary600

    Box(
        modifier = Modifier
            .width(65.dp)
            .height(36.dp)
            .alpha(if (enabled || verified) 1f else 0.5f)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (verified) "인증완료" else "인증하기",
            style = SeniorOnTextStyles.ButtonS,
            color = contentColor
        )
    }
}

private fun String.isValidEmail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

private fun Int.toTimerText(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Preview(
    name = "Signup Email Verification",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun SignupEmailVerificationScreenPreview() {
    SENIOR_ONTheme {
        SignupEmailVerificationScreen(
            onBackClick = {},
            onNextClick = {}
        )
    }
}
