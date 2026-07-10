package com.example.senior_on.ui.signup

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.data.auth.MockSignupAuthRepository
import com.example.senior_on.data.auth.SignupAuthRepository
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import kotlinx.coroutines.launch

@Composable
fun SignupAccountInfoScreen(
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
    authRepository: SignupAuthRepository = MockSignupAuthRepository()
) {
    val coroutineScope = rememberCoroutineScope()
    var userId by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordConfirm by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isPasswordConfirmVisible by rememberSaveable { mutableStateOf(false) }
    var idCheckState by rememberSaveable { mutableStateOf(IdCheckState.Idle) }

    val isPasswordValid = password.isEmpty() || isValidPassword(password)
    val isPasswordConfirmValid = passwordConfirm.isEmpty() || password == passwordConfirm
    val canGoNext = idCheckState == IdCheckState.Available &&
        isValidPassword(password) &&
        passwordConfirm.isNotEmpty() &&
        password == passwordConfirm

    SignupStepScaffold(
        progress = 3 / 4f,
        onBackClick = onBackClick,
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "회원가입을 위해\n필요한 정보를 입력해 주세요",
            style = SeniorOnTextStyles.OnboardingHeading,
            color = SeniorOnColors.Gray800
        )

        Spacer(modifier = Modifier.height(24.dp))

        SignupAccountFieldRow {
            SignupUnderlinedTextField(
                label = "아이디",
                value = userId,
                onValueChange = {
                    userId = it.take(20)
                    idCheckState = IdCheckState.Idle
                },
                placeholder = "",
                underlineColor = when (idCheckState) {
                    IdCheckState.Available -> SeniorOnColors.Primary600
                    IdCheckState.Duplicated -> SeniorOnColors.Red300
                    else -> null
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            SignupDuplicateCheckButton(
                enabled = userId.isNotBlank(),
                onClick = {
                    coroutineScope.launch {
                        idCheckState = if (authRepository.isUserIdAvailable(userId)) {
                            IdCheckState.Available
                        } else {
                            IdCheckState.Duplicated
                        }
                    }
                }
            )
        }

        SignupIdCheckMessage(idCheckState = idCheckState)

        Spacer(modifier = Modifier.height(7.dp))

        SignupUnderlinedTextField(
            label = "비밀번호",
            value = password,
            onValueChange = { password = it.take(30) },
            placeholder = "",
            visualTransformation = if (isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation(mask = '●')
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textStyle = if (isPasswordVisible) {
                SeniorOnTextStyles.BodyMSemiBold
            } else {
                SeniorOnTextStyles.PasswordDot
            },
            trailingContent = {
                SignupPasswordVisibilityIcon(
                    isVisible = isPasswordVisible,
                    onClick = { isPasswordVisible = !isPasswordVisible }
                )
            },
            supportMessage = if (isPasswordValid) {
                if (password.isEmpty()) "영문, 숫자 포함 8자 이상" else null
            } else {
                null
            },
            errorMessage = if (isPasswordValid) {
                null
            } else {
                "영문과 숫자를 포함해 8자 이상 입력해 주세요."
            },
            inputStartPadding = if (isPasswordVisible) 6.dp else 12.dp
        )

        Spacer(modifier = Modifier.height(7.dp))

        SignupUnderlinedTextField(
            label = "비밀번호 확인",
            value = passwordConfirm,
            onValueChange = { passwordConfirm = it.take(30) },
            placeholder = "",
            visualTransformation = if (isPasswordConfirmVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation(mask = '●')
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textStyle = if (isPasswordConfirmVisible) {
                SeniorOnTextStyles.BodyMSemiBold
            } else {
                SeniorOnTextStyles.PasswordDot
            },
            trailingContent = {
                SignupPasswordVisibilityIcon(
                    isVisible = isPasswordConfirmVisible,
                    onClick = { isPasswordConfirmVisible = !isPasswordConfirmVisible }
                )
            },
            errorMessage = if (isPasswordConfirmValid) {
                null
            } else {
                "비밀번호가 일치하지 않아요."
            },
            inputStartPadding = if (isPasswordConfirmVisible) 6.dp else 12.dp
        )

        Spacer(modifier = Modifier.weight(1f))

        SignupNextButton(
            enabled = canGoNext,
            onClick = onNextClick
        )

        Spacer(modifier = Modifier.height(22.5.dp))
    }
}

@Composable
private fun SignupAccountFieldRow(
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        content()
    }
}

@Composable
private fun SignupIdCheckMessage(
    idCheckState: IdCheckState
) {
    val message = when (idCheckState) {
        IdCheckState.Available -> "사용 가능한 아이디예요."
        IdCheckState.Duplicated -> "이미 사용 중인 아이디입니다."
        IdCheckState.Idle -> ""
    }
    val messageColor = when (idCheckState) {
        IdCheckState.Available -> SeniorOnColors.Primary600
        IdCheckState.Duplicated -> SeniorOnColors.Red300
        IdCheckState.Idle -> Color.Transparent
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = message,
        modifier = Modifier.height(17.dp),
        style = SeniorOnTextStyles.CaptionRegular,
        color = messageColor
    )
}

@Composable
private fun SignupDuplicateCheckButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(68.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .border(
                width = 1.dp,
                color = SeniorOnColors.Primary600,
                shape = RoundedCornerShape(SeniorOnRadius.Small)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "중복 확인",
            style = SeniorOnTextStyles.ButtonS,
            color = SeniorOnColors.Primary600
        )
    }
}

@Composable
private fun SignupPasswordVisibilityIcon(
    isVisible: Boolean,
    onClick: () -> Unit
) {
    Icon(
        painter = painterResource(
            id = if (isVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
        ),
        contentDescription = if (isVisible) "비밀번호 숨기기" else "비밀번호 보기",
        modifier = Modifier
            .size(24.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        tint = SeniorOnColors.Gray300
    )
}

private enum class IdCheckState {
    Idle,
    Available,
    Duplicated
}

private fun isValidPassword(password: String): Boolean {
    val hasLetter = password.any { it in 'a'..'z' || it in 'A'..'Z' }
    val hasDigit = password.any { it.isDigit() }
    return password.length >= 8 && hasLetter && hasDigit
}

@Preview(
    name = "Signup Account Info",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun SignupAccountInfoScreenPreview() {
    SENIOR_ONTheme {
        SignupAccountInfoScreen(
            onBackClick = {},
            onNextClick = {}
        )
    }
}
