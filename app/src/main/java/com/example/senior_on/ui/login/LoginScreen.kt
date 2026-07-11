package com.example.senior_on.ui.login

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.data.auth.MockLoginAuthRepository
import com.example.senior_on.ui.app.AppUserMode
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

private enum class LoginFieldError {
    None,
    EmptyPassword,
    InvalidCredentials,
    PasswordMismatch
}

@Composable
fun LoginScreen(
    selectedMode: AppUserMode,
    onLoginClick: () -> Unit = {},
    onGoToModeSelection: () -> Unit = {},
    onFindIdClick: () -> Unit = {},
    onFindPasswordClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onKakaoClick: () -> Unit = {},
    onGoogleClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var userId by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var keepLoggedIn by rememberSaveable { mutableStateOf(false) }
    var loginError by rememberSaveable { mutableStateOf(LoginFieldError.None) }
    var wrongModeDialogType by rememberSaveable { mutableStateOf<LoginWrongModeDialogType?>(null) }

    val userIdError = loginError == LoginFieldError.InvalidCredentials
    val passwordError = loginError != LoginFieldError.None
    val passwordErrorMessage = when (loginError) {
        LoginFieldError.EmptyPassword -> "비밀번호를 입력해주세요"
        LoginFieldError.InvalidCredentials -> "아이디 또는 비밀번호를 다시 확인해 주세요."
        LoginFieldError.PasswordMismatch -> "비밀번호가 일치하지 않아요"
        LoginFieldError.None -> null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
        Spacer(modifier = Modifier.height(133.dp)) //맨위와 간격

        Image(
            painter = painterResource(id = R.drawable.ic_splash_on),
            contentDescription = null,
            modifier = Modifier.size(width = 60.dp, height = 60.dp) //아이콘 사이즈
        )

        Text(
            text = "안녕하세요!\n로그인 정보를 입력해 주세요",
            modifier = Modifier.padding(top = 8.dp),
            style = SeniorOnTextStyles.OnboardingHeading,
            color = SeniorOnColors.Gray800
        )

        Spacer(modifier = Modifier.height(42.dp))

        LoginTextField(
            value = userId,
            onValueChange = {
                userId = it
                loginError = LoginFieldError.None
            },
            placeholder = "아이디 입력",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = userIdError,
            showClearButton = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        LoginTextField(
            value = password,
            onValueChange = {
                password = it
                loginError = LoginFieldError.None
            },
            placeholder = "비밀번호 입력",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            isError = passwordError,
            errorMessage = passwordErrorMessage,
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible) {
                                R.drawable.ic_visibility
                            } else {
                                R.drawable.ic_visibility_off
                            }
                        ),
                        contentDescription = if (passwordVisible) "비밀번호 숨기기" else "비밀번호 보기",
                        tint = SeniorOnColors.Gray400,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(26.dp))// 비밀번호 입력 - 로그인 상태 유지 간격

        LoginStayLoggedInRow(
            checked = keepLoggedIn,
            onCheckedChange = { keepLoggedIn = it }
        )

        Spacer(modifier = Modifier.height(16.dp))// 로그인 상태 유지 - 로그인 박스 간격

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp) //높이 50px
                .clip(RoundedCornerShape(SeniorOnRadius.Medium))
                .background(SeniorOnColors.Primary600)
                .clickable {
                    loginError = when {
                        password.isBlank() -> LoginFieldError.EmptyPassword
                        userId.isBlank() -> LoginFieldError.InvalidCredentials
                        else -> LoginFieldError.None
                    }
                    if (loginError != LoginFieldError.None) return@clickable

                    val accountMode = MockLoginAuthRepository.accountModeFor(userId)
                    if (accountMode == null) {
                        loginError = LoginFieldError.InvalidCredentials
                        return@clickable
                    }
                    if (accountMode != selectedMode) {
                        wrongModeDialogType = when (accountMode) {
                            AppUserMode.Senior -> LoginWrongModeDialogType.SeniorAccount
                            AppUserMode.Child -> LoginWrongModeDialogType.ChildAccount
                        }
                        return@clickable
                    }
                    onLoginClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "로그인",
                style = SeniorOnTextStyles.ButtonL,
                color = SeniorOnColors.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LoginLinkRow(
            onFindIdClick = onFindIdClick,
            onFindPasswordClick = onFindPasswordClick,
            onSignUpClick = onSignUpClick
        )

        Spacer(modifier = Modifier.height(34.dp))//로그인 박스 - 아이디 찾기~ 사이 간격 34px

        SnsLoginDivider()

        Spacer(modifier = Modifier.height(24.dp)) //sns~ - 아이콘 사이 간격

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 69.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SocialLoginButton(
                backgroundColor = SeniorOnColors.Yellow,
                borderColor = null,
                onClick = onKakaoClick
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_kakao),
                    contentDescription = "카카오 로그인",
                    modifier = Modifier.size(23.dp),//원 안 아이콘 크기 23px
                    tint = SeniorOnColors.Black
                )
            }

            Spacer(modifier = Modifier.size(16.dp))

            SocialLoginButton(
                backgroundColor = SeniorOnColors.White,
                borderColor = SeniorOnColors.Gray200,
                onClick = onGoogleClick
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "구글 로그인",
                    modifier = Modifier.size(27.dp), //원 안 아이콘 크기 27px
                    tint = Color.Unspecified
                )
            }
        }
        }

        wrongModeDialogType?.let { dialogType ->
            LoginWrongModeDialogOverlay(
                type = dialogType,
                onConfirmClick = {
                    wrongModeDialogType = null
                    onGoToModeSelection()
                }
            )
        }
    }
}

@Composable
private fun LoginStayLoggedInRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (checked) SeniorOnColors.Primary600 else SeniorOnColors.White
                )
                .border(
                    width = 1.dp,
                    color = if (checked) SeniorOnColors.Primary600 else SeniorOnColors.Gray200,
                    shape = RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = SeniorOnColors.White
                )
            }
        }

        Text(
            text = "로그인 상태 유지",
            modifier = Modifier.padding(start = 8.dp),
            style = SeniorOnTextStyles.BodySRegular,
            color = SeniorOnColors.Gray800
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    errorMessage: String? = null,
    showClearButton: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = SeniorOnColors.Primary600,
        unfocusedBorderColor = SeniorOnColors.Gray200,
        errorBorderColor = SeniorOnColors.Red500,
        focusedContainerColor = SeniorOnColors.SupportWhite100,
        unfocusedContainerColor = SeniorOnColors.SupportWhite100,
        errorContainerColor = SeniorOnColors.SupportWhite100,
        cursorColor = SeniorOnColors.Primary600,
        errorCursorColor = SeniorOnColors.Red500
    )
    val fieldShape = RoundedCornerShape(SeniorOnRadius.Small)
    val fieldTextStyle = SeniorOnTextStyles.BodyMRegular.copy(color = SeniorOnColors.Gray800)

    Column(modifier = modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(43.dp),//입력창 높이
            textStyle = fieldTextStyle,
            singleLine = true,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = value,
                    visualTransformation = visualTransformation,
                    innerTextField = innerTextField,
                    placeholder = {
                        Text(
                            text = placeholder,
                            style = SeniorOnTextStyles.BodyMMedium,
                            color = SeniorOnColors.Gray300
                        )
                    },
                    trailingIcon = {
                        when {
                            trailingIcon != null -> trailingIcon()
                            showClearButton && value.isNotEmpty() -> {
                                IconButton(onClick = { onValueChange("") }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_close),
                                        contentDescription = "입력값 지우기",
                                        tint = SeniorOnColors.Gray400,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    },
                    singleLine = true,
                    enabled = true,
                    isError = isError,
                    interactionSource = interactionSource,
                    colors = textFieldColors,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp), //// 텍스트가 잘리지 않도록 테두리 안쪽 여백 조정
                    container = {
                        OutlinedTextFieldDefaults.ContainerBox(
                            enabled = true,
                            isError = isError,
                            interactionSource = interactionSource,
                            colors = textFieldColors,
                            shape = fieldShape
                        )
                    }
                )
            }
        )

        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                modifier = Modifier.padding(top = 6.dp), //에러메세지 - 비밀번호 입력 박스 사이 간격
                style = SeniorOnTextStyles.CaptionRegular,
                color = SeniorOnColors.Red300 //에러 테두리 색깔
            )
        }
    }
}

@Composable
private fun LoginLinkRow(
    onFindIdClick: () -> Unit,
    onFindPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LoginLink(text = "아이디 찾기", onClick = onFindIdClick)
        LoginLinkDivider()
        LoginLink(text = "비밀번호 찾기", onClick = onFindPasswordClick)
        LoginLinkDivider()
        LoginLink(
            text = "회원가입",
            onClick = onSignUpClick,
            color = SeniorOnColors.Primary600
        )
    }
}

@Composable
private fun LoginLink(
    text: String,
    onClick: () -> Unit,
    color: Color = SeniorOnColors.Gray500
) {
    Text(
        text = text,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 24.dp), //로그인 박스와의 거리 20px, 글자 사이 간격 24px
        style = SeniorOnTextStyles.BodySMedium, //글씨체
        color = color
    )
}

@Composable
private fun LoginLinkDivider() {
    Box(
        modifier = Modifier
            .size(width = 1.dp, height = 12.dp)
            .background(SeniorOnColors.Gray300) //세로선 색
    )
}

@Composable
private fun SnsLoginDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(SeniorOnColors.Gray200)
        )
        Text(
            text = "SNS 계정으로 로그인",
            modifier = Modifier.padding(horizontal = 21.dp), //가로선 , sns- 사이 간격
            style = SeniorOnTextStyles.BodySRegular,
            color = SeniorOnColors.Gray500,
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(SeniorOnColors.Gray200)
        )
    }
}

@Composable
private fun SocialLoginButton(
    backgroundColor: Color,
    borderColor: Color?,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(54.dp) //아이콘 들어갈 원 크기
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (borderColor != null) {
                    Modifier.border(1.dp, borderColor, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Preview(
    name = "Login - Default",
    showBackground = true,
    showSystemUi = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun LoginScreenDefaultPreview() {
    SENIOR_ONTheme {
        LoginScreen(selectedMode = AppUserMode.Child)
    }
}

@Preview(
    name = "Login - Compact",
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
private fun LoginScreenCompactPreview() {
    SENIOR_ONTheme {
        LoginScreen(selectedMode = AppUserMode.Child)
    }
}

@Preview(
    name = "Login - Empty Password Error",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun LoginTextFieldEmptyPasswordErrorPreview() {
    SENIOR_ONTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SeniorOnColors.White)
                .padding(24.dp)
        ) {
            LoginTextField(
                value = "text",
                onValueChange = {},
                placeholder = "아이디 입력",
                showClearButton = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            LoginTextField(
                value = "",
                onValueChange = {},
                placeholder = "비밀번호 입력",
                visualTransformation = PasswordVisualTransformation(),
                isError = true,
                errorMessage = "비밀번호를 입력해주세요",
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_visibility_off),
                        contentDescription = null,
                        tint = SeniorOnColors.Gray400,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
        }
    }
}

@Preview(
    name = "Login - Invalid Credentials Error",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun LoginTextFieldInvalidCredentialsPreview() {
    SENIOR_ONTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SeniorOnColors.White)
                .padding(24.dp)
        ) {
            LoginTextField(
                value = "text",
                onValueChange = {},
                placeholder = "아이디 입력",
                isError = true,
                showClearButton = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            LoginTextField(
                value = "password",
                onValueChange = {},
                placeholder = "비밀번호 입력",
                visualTransformation = PasswordVisualTransformation(),
                isError = true,
                errorMessage = "아이디 또는 비밀번호를 다시 확인해 주세요.",
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_visibility_off),
                        contentDescription = null,
                        tint = SeniorOnColors.Gray400,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
        }
    }
}

@Preview(
    name = "Login - Password Mismatch Error",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun LoginTextFieldPasswordMismatchPreview() {
    SENIOR_ONTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SeniorOnColors.White)
                .padding(24.dp)
        ) {
            LoginTextField(
                value = "text",
                onValueChange = {},
                placeholder = "아이디 입력",
                showClearButton = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            LoginTextField(
                value = "password",
                onValueChange = {},
                placeholder = "비밀번호 입력",
                visualTransformation = PasswordVisualTransformation(),
                isError = true,
                errorMessage = "비밀번호가 일치하지 않아요",
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_visibility_off),
                        contentDescription = null,
                        tint = SeniorOnColors.Gray400,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
        }
    }
}
