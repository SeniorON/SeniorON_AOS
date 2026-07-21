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
import androidx.compose.ui.unit.Dp
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
                .padding(horizontal = 16.dp)
        ) {
        LoginTopBar(onBackClick = onGoToModeSelection)

        Spacer(modifier = Modifier.height(30.dp))

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

        Spacer(modifier = Modifier.height(34.dp))

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
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(26.dp))// 비밀번호 입력 - 로그인 상태 유지 간격

        LoginStayLoggedInRow(
            checked = keepLoggedIn,
            onCheckedChange = { keepLoggedIn = it }
        )

        Spacer(modifier = Modifier.height(18.dp)) // 로그인 상태 유지 - 로그인 버튼 간격

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp) //높이 50px
                .clip(RoundedCornerShape(SeniorOnRadius.Small))
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
                    if (MockLoginAuthRepository.login(userId, password) == null) {
                        loginError = LoginFieldError.InvalidCredentials
                        return@clickable
                    }
                    onLoginClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "로그인",
                style = SeniorOnTextStyles.ButtonM,
                color = SeniorOnColors.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp)) // 로그인 버튼 - 아이디 찾기 row 간격

        LoginLinkRow(
            onFindIdClick = onFindIdClick,
            onFindPasswordClick = onFindPasswordClick,
            onSignUpClick = onSignUpClick
        )

        Spacer(modifier = Modifier.height(34.dp)) // 아이디 찾기 row - SNS 계정으로 로그인 간격

        SnsLoginDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_sociallogin_kakao),
                contentDescription = "카카오 로그인",
                modifier = Modifier
                    .size(width = 55.dp, height = 54.dp)
                    .clickable(onClick = onKakaoClick)
            )

            Spacer(modifier = Modifier.size(10.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_sociallogin_google),
                contentDescription = "구글 로그인",
                modifier = Modifier
                    .size(54.dp)
                    .clickable(onClick = onGoogleClick)
            )
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
private fun LoginTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = "뒤로가기",
                modifier = Modifier.size(16.dp),
                tint = SeniorOnColors.Gray800
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
        if (checked) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(SeniorOnColors.Primary600)
                    .border(
                        width = 1.dp,
                        color = SeniorOnColors.Primary600,
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = SeniorOnColors.White
                )
            }
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_check_unfilled),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.Unspecified
            )
        }

        Text(
            text = "로그인 상태 유지",
            modifier = Modifier.padding(start = 4.dp),
            style = SeniorOnTextStyles.BodySMedium,
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
        errorBorderColor = SeniorOnColors.Red200,
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
        LoginLink(
            text = "아이디 찾기",
            onClick = onFindIdClick,
            paddingEnd = 19.dp
        )
        LoginLinkDivider()
        LoginLink(
            text = "비밀번호 찾기",
            onClick = onFindPasswordClick,
            paddingEnd = 19.dp
        )
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
    color: Color = SeniorOnColors.Gray700,
    paddingStart: Dp = 20.dp,
    paddingEnd: Dp = 20.dp
) {
    Text(
        text = text,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(start = paddingStart, end = paddingEnd),
        style = SeniorOnTextStyles.BodySMedium,
        color = color
    )
}

@Composable
private fun LoginLinkDivider() {
    Box(
        modifier = Modifier
            .size(width = 1.dp, height = 20.dp)
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
            modifier = Modifier.padding(horizontal = 10.5.dp), // 좌우 각 10.5dp (합 21)
            style = SeniorOnTextStyles.BodySMedium,
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
                        modifier = Modifier.size(16.dp)
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
                        modifier = Modifier.size(16.dp)
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
