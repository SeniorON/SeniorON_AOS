package com.example.senior_on.ui.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
internal fun SignupStepScaffold(
    progress: Float,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.SupportWhite100)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SignupStepTopBar(onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(4.dp))
        SignupStepProgressBar(progress = progress)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            content = content
        )
    }
}

@Composable
private fun SignupStepTopBar(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
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
            text = "회원가입",
            modifier = Modifier.padding(start = 16.dp),
            style = SeniorOnTextStyles.HeadingS,
            color = SeniorOnColors.Gray800
        )
    }
}

@Composable
private fun SignupStepProgressBar(
    progress: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(4.dp)
            .background(SeniorOnColors.Gray100)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(4.dp)
                .background(SeniorOnColors.Primary600)
        )
    }
}

@Composable
internal fun SignupUnderlinedTextField(
    label: String? = null,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    supportMessage: String? = null,
    supportColor: Color = SeniorOnColors.Gray400,
    underlineColor: Color? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    textStyle: TextStyle = SeniorOnTextStyles.BodyMSemiBold,
    trailingContent: (@Composable () -> Unit)? = null,
    showClearButton: Boolean = true,
    inputStartPadding: Dp = 6.dp,
    inputEndPadding: Dp? = null
) {
    var isFocused by rememberSaveable { mutableStateOf(false) }
    val shouldShowClearButton = showClearButton && trailingContent == null && value.isNotEmpty() && !isFocused
    val hasTrailingIcon = shouldShowClearButton || trailingContent != null
    val trailingIconReservedPadding = 42.dp
    val requestedInputEndPadding = inputEndPadding
        ?: if (hasTrailingIcon) trailingIconReservedPadding else 6.dp
    val resolvedInputEndPadding = if (
        hasTrailingIcon &&
        requestedInputEndPadding < trailingIconReservedPadding
    ) {
        trailingIconReservedPadding
    } else {
        requestedInputEndPadding
    }
    val currentUnderlineColor = when {
        errorMessage != null -> SeniorOnColors.Red300
        underlineColor != null -> underlineColor
        isFocused -> SeniorOnColors.Primary600
        else -> SeniorOnColors.Gray200
    }
    val helperMessage = errorMessage ?: supportMessage
    val helperColor = if (errorMessage != null) SeniorOnColors.Red300 else supportColor

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.Gray800
            )

            Spacer(modifier = Modifier.height(4.dp))
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(43.dp)
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                },
            textStyle = textStyle.copy(color = SeniorOnColors.Gray800),
            cursorBrush = SolidColor(SeniorOnColors.Primary600),
            singleLine = true,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (value.isEmpty() && !isFocused) {
                        Text(
                            text = placeholder,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = inputStartPadding),
                            style = SeniorOnTextStyles.BodyMMedium,
                            color = SeniorOnColors.Gray300
                        )
                    }

                    Box(
                        propagateMinConstraints = true,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxWidth()
                            .padding(
                                start = inputStartPadding,
                                end = resolvedInputEndPadding
                            )
                    ) {
                        innerTextField()
                    }

                    if (shouldShowClearButton) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 12.dp)
                                .size(24.dp)
                                .clickable { onValueChange("") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close_filled),
                                contentDescription = "입력 내용 지우기",
                                modifier = Modifier.size(18.dp),
                                tint = Color.Unspecified
                            )
                        }
                    }

                    if (trailingContent != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 12.dp)
                                .size(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            trailingContent()
                        }
                    }
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(currentUnderlineColor)
        )

        if (helperMessage != null) {
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = helperMessage,
                style = SeniorOnTextStyles.CaptionRegular,
                color = helperColor
            )
        }
    }
}

@Composable
internal fun SignupNextButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "다음"
) {
    val backgroundColor = if (enabled) {
        SeniorOnColors.Primary600
    } else {
        SeniorOnColors.Primary600.copy(alpha = 0.5f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.ButtonM,
            color = SeniorOnColors.SupportWhite100
        )
    }
}

@Preview(
    name = "Signup Text Field States",
    showBackground = true,
    widthDp = 360,
    heightDp = 360
)
@Composable
private fun SignupTextFieldStatesPreview() {
    SENIOR_ONTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SeniorOnColors.SupportWhite100)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            SignupUnderlinedTextField(
                label = "이름",
                value = "",
                onValueChange = {},
                placeholder = "이름 입력(20자 이내)"
            )

            SignupUnderlinedTextField(
                label = "이름",
                value = "ghdrlfbdd",
                onValueChange = {},
                placeholder = "이름 입력(20자 이내)"
            )

            SignupUnderlinedTextField(
                label = "이메일",
                value = "senior-on",
                onValueChange = {},
                placeholder = "이메일 입력",
                errorMessage = "이메일 형식이 올바르지 않아요."
            )
        }
    }
}

@Preview(
    name = "Signup Buttons",
    showBackground = true,
    widthDp = 360,
    heightDp = 160
)
@Composable
private fun SignupButtonsPreview() {
    SENIOR_ONTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SeniorOnColors.SupportWhite100)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SignupNextButton(
                enabled = false,
                onClick = {}
            )

            SignupNextButton(
                enabled = true,
                onClick = {}
            )
        }
    }
}
