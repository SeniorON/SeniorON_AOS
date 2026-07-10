package com.example.senior_on.ui.findaccount

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun FindPasswordVerifyScreen(
    maskedEmail: String,
    onBackClick: () -> Unit,
    onVerifySuccess: () -> Unit,
    onVerifyCode: (String) -> Boolean,
    onResendCode: () -> Unit,
    onTabSelected: (FindAccountTab) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var verificationCode by rememberSaveable { mutableStateOf("") }
    var isVerificationError by rememberSaveable { mutableStateOf(false) }

    val isVerifyEnabled = verificationCode.length == 6

    FindAccountScaffold(
        modifier = modifier.systemBarsPadding(),
        onBackClick = onBackClick,
        selectedTab = FindAccountTab.Password,
        onTabSelected = onTabSelected,
        bottomBar = {
            Column {
                FindAccountInfoBanner(
                    text = "계정에 등록된 이메일로 인증번호가 전송됩니다"
                )

                Spacer(modifier = Modifier.height(12.dp))

                FindAccountPrimaryButton(
                    text = "인증하기",
                    enabled = isVerifyEnabled,
                    onClick = {
                        val verified = onVerifyCode(verificationCode)
                        if (verified) {
                            isVerificationError = false
                            onVerifySuccess()
                        } else {
                            isVerificationError = true
                        }
                    }
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp)
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
                        append("으로\n")
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    FindAccountTextField(
                        label = null,
                        value = verificationCode,
                        onValueChange = {
                            verificationCode = it.filter(Char::isDigit).take(6)
                            isVerificationError = false
                        },
                        placeholder = "인증번호 6자리 입력",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = isVerificationError,
                        errorMessage = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FindAccountSmallPrimaryButton(
                        text = "재전송",
                        onClick = {
                            verificationCode = ""
                            isVerificationError = false
                            onResendCode()
                        }
                    )
                }

                if (isVerificationError) {
                    Text(
                        text = "인증번호가 일치하지 않아요.",
                        modifier = Modifier.padding(top = 6.dp),
                        style = SeniorOnTextStyles.CaptionRegular,
                        color = SeniorOnColors.Red300
                    )
                }
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
            onVerifyCode = { it == "123456" },
            onResendCode = {}
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
            onVerifyCode = { false },
            onResendCode = {}
        )
    }
}
