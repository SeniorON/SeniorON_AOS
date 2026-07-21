package com.example.senior_on.ui.findaccount

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
internal fun FindAccountTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(SeniorOnColors.White),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
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
                    modifier = Modifier.size(24.dp),
                    tint = SeniorOnColors.Gray800
                )
            }

            Text(
                text = "아이디/비밀번호 찾기",
                modifier = Modifier.weight(1f),
                style = SeniorOnTextStyles.BodyLBold,
                color = SeniorOnColors.Gray800,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
internal fun FindAccountTabRow(
    selectedTab: FindAccountTab,
    onTabSelected: (FindAccountTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        FindAccountTabItem(
            text = "아이디 찾기",
            tab = FindAccountTab.Id,
            selected = selectedTab == FindAccountTab.Id,
            onClick = { onTabSelected(FindAccountTab.Id) },
            modifier = Modifier.weight(1f)
        )
        FindAccountTabItem(
            text = "비밀번호 찾기",
            tab = FindAccountTab.Password,
            selected = selectedTab == FindAccountTab.Password,
            onClick = { onTabSelected(FindAccountTab.Password) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FindAccountTabItem(
    text: String,
    tab: FindAccountTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = if (selected) SeniorOnColors.Primary600 else SeniorOnColors.Gray500
    val indicatorColor = if (selected) SeniorOnColors.Primary600 else SeniorOnColors.Gray200
    val indicatorPaddingStart = if (tab == FindAccountTab.Id) 16.dp else 0.dp
    val indicatorPaddingEnd = if (tab == FindAccountTab.Password) 16.dp else 0.dp

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.BodyLBold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = indicatorPaddingStart, end = indicatorPaddingEnd)
                .height(if (selected) 2.dp else 1.dp)
                .background(indicatorColor)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FindAccountTextField(
    label: String? = null,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    textStyle: androidx.compose.ui.text.TextStyle = SeniorOnTextStyles.BodyMMedium.copy(
        color = SeniorOnColors.Gray800
    ),
    isError: Boolean = false,
    errorMessage: String? = null,
    supportMessage: String? = null,
    showClearIcon: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = if (isError) SeniorOnColors.Red300 else SeniorOnColors.Primary600,
        unfocusedBorderColor = if (isError) SeniorOnColors.Red300 else SeniorOnColors.Gray200,
        errorBorderColor = SeniorOnColors.Red300,
        focusedContainerColor = SeniorOnColors.SupportWhite100,
        unfocusedContainerColor = SeniorOnColors.SupportWhite100,
        cursorColor = SeniorOnColors.Primary600
    )
    val fieldShape = RoundedCornerShape(SeniorOnRadius.Small)

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.Gray800
            )

            Spacer(modifier = Modifier.height(6.dp))
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(43.dp),
            textStyle = textStyle,
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
                    trailingIcon = if (trailingContent != null || (showClearIcon && value.isNotEmpty())) {
                        {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                trailingContent?.invoke()
                                if (showClearIcon && value.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = { onValueChange("") }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_close_filled),
                                            contentDescription = "입력값 지우기",
                                            modifier = Modifier.size(18.dp),
                                            tint = Color.Unspecified
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        null
                    },
                    singleLine = true,
                    enabled = true,
                    isError = isError,
                    interactionSource = interactionSource,
                    colors = textFieldColors,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 12.dp,
                        vertical = 10.5.dp
                    ),
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

        when {
            isError && errorMessage != null -> {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(top = 6.dp),
                    style = SeniorOnTextStyles.CaptionRegular,
                    color = SeniorOnColors.Red300
                )
            }
            supportMessage != null -> {
                Text(
                    text = supportMessage,
                    modifier = Modifier.padding(top = 6.dp),
                    style = SeniorOnTextStyles.CaptionRegular,
                    color = SeniorOnColors.Gray400
                )
            }
        }
    }
}

@Composable
internal fun FindAccountPasswordTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    supportMessage: String? = null
) {
    FindAccountTextField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password),
        visualTransformation = if (isVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation(mask = '●')
        },
        textStyle = if (isVisible) {
            SeniorOnTextStyles.BodyMMedium.copy(color = SeniorOnColors.Gray800)
        } else {
            SeniorOnTextStyles.PasswordDot.copy(color = SeniorOnColors.Gray800)
        },
        isError = isError,
        errorMessage = errorMessage,
        supportMessage = supportMessage,
        showClearIcon = false,
        trailingContent = {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onVisibilityToggle
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
                    ),
                    contentDescription = if (isVisible) "비밀번호 숨기기" else "비밀번호 보기",
                    modifier = Modifier.size(24.dp),
                    tint = SeniorOnColors.Gray400
                )
            }
        }
    )
}

@Composable
internal fun FindAccountInfoBanner(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_information),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = SeniorOnColors.Gray400
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray400
        )
    }
}

@Composable
internal fun FindAccountSmallPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 53.dp,
    enabled: Boolean = true
) {
    val backgroundColor = if (enabled) {
        SeniorOnColors.Primary600
    } else {
        SeniorOnColors.Primary600.copy(alpha = 0.5f)
    }

    Box(
        modifier = modifier
            .width(width)
            .height(43.dp)
            .background(backgroundColor, RoundedCornerShape(SeniorOnRadius.Small))
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
internal fun FindPasswordSuccessDialogContent(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(249.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Large))
            .background(SeniorOnColors.White)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.ic_modal_check),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = Color.Unspecified
            )

            Text(
                text = "비밀번호 변경 완료!",
                modifier = Modifier.padding(top = 16.dp),
                style = SeniorOnTextStyles.BodyLBold,
                color = SeniorOnColors.Gray800,
                textAlign = TextAlign.Center
            )

            Text(
                text = "새로운 비밀번호로 변경되었습니다.",
                modifier = Modifier.padding(top = 18.dp),
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray500,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(26.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(SeniorOnColors.Primary600, RoundedCornerShape(SeniorOnRadius.Medium))
                    .clickable(onClick = onLoginClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "로그인 화면으로",
                    style = SeniorOnTextStyles.ButtonS,
                    color = SeniorOnColors.SupportWhite100
                )
            }
        }
    }
}

@Composable
internal fun FindPasswordSuccessDialogOverlay(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        FindPasswordSuccessDialogContent(onLoginClick = onLoginClick)
    }
}

@Composable
internal fun FindAccountPrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (enabled) {
        SeniorOnColors.Primary600
    } else {
        SeniorOnColors.Primary600.copy(alpha = 0.5f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(SeniorOnRadius.Small))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 14.dp),
            style = SeniorOnTextStyles.ButtonM,
            color = SeniorOnColors.SupportWhite100,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun FindAccountSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, SeniorOnColors.Gray200, RoundedCornerShape(SeniorOnRadius.Small))
            .background(SeniorOnColors.White, RoundedCornerShape(SeniorOnRadius.Small))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 14.dp),
            style = SeniorOnTextStyles.ButtonM,
            color = SeniorOnColors.Gray400,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun FindAccountScaffold(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    showTabs: Boolean = true,
    selectedTab: FindAccountTab = FindAccountTab.Id,
    onTabSelected: (FindAccountTab) -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
    ) {
        FindAccountTopBar(onBackClick = onBackClick)

        if (showTabs) {
            FindAccountTabRow(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            content()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            bottomBar()
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
    name = "FindAccountTab - Id Input",
    group = "FindAccountTab"
)
@Composable
internal fun FindAccountComponentsInputPreview() {
    SENIOR_ONTheme {
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }

        FindAccountScaffold(
            onBackClick = {},
            selectedTab = FindAccountTab.Id,
            onTabSelected = {},
            bottomBar = {
                FindAccountPrimaryButton(
                    text = "다음",
                    enabled = name.isNotBlank() && email.isNotBlank(),
                    onClick = {}
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 32.dp)
            ) {
                FindAccountTextField(
                    label = "이름",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "이름 입력"
                )
                Spacer(modifier = Modifier.height(24.dp))
                FindAccountTextField(
                    label = "이메일",
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "이메일 입력"
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 140,
    name = "FindAccountTab - Tab Row (Id)",
    group = "FindAccountTab"
)
@Composable
internal fun FindAccountTabRowPreview() {
    SENIOR_ONTheme {
        Column(modifier = Modifier.background(SeniorOnColors.White)) {
            FindAccountTopBar(onBackClick = {})
            FindAccountTabRow(
                selectedTab = FindAccountTab.Id,
                onTabSelected = {}
            )
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 140,
    name = "FindAccountTab - Tab Row (Password)",
    group = "FindAccountTab"
)
@Composable
internal fun FindAccountTabRowPasswordPreview() {
    SENIOR_ONTheme {
        Column(modifier = Modifier.background(SeniorOnColors.White)) {
            FindAccountTopBar(onBackClick = {})
            FindAccountTabRow(
                selectedTab = FindAccountTab.Password,
                onTabSelected = {}
            )
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
    name = "FindAccountTab - Password Input",
    group = "FindAccountTab"
)
@Composable
internal fun FindAccountPasswordInputPreview() {
    SENIOR_ONTheme {
        var name by remember { mutableStateOf("홍길동") }
        var userId by remember { mutableStateOf("User_Id") }

        FindAccountScaffold(
            onBackClick = {},
            selectedTab = FindAccountTab.Password,
            onTabSelected = {},
            bottomBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FindAccountInfoBanner(
                        text = "계정에 등록된 이메일로 인증번호가 전송됩니다"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FindAccountPrimaryButton(
                        text = "다음",
                        enabled = name.isNotBlank() && userId.isNotBlank(),
                        onClick = {}
                    )
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp)
            ) {
                FindAccountTextField(
                    label = "이름",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "이름 입력"
                )
                Spacer(modifier = Modifier.height(24.dp))
                FindAccountTextField(
                    label = "아이디",
                    value = userId,
                    onValueChange = { userId = it },
                    placeholder = "아이디 입력"
                )
            }
        }
    }
}
