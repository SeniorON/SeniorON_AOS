package com.example.senior_on.ui.family_code

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.util.Locale

private const val FamilyShareCodeLength = 8
private const val InvalidFamilyShareCodeMessage = "가족 공유 코드를 다시 확인해 주세요."

@Composable
fun FamilyShareCodeInputScreen(
    onBackClick: () -> Unit,
    onLoginClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var familyShareCode by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val isLoginEnabled = familyShareCode.length == FamilyShareCodeLength
    val focusManager = LocalFocusManager.current

    fun submitFamilyShareCode() {
        if (isLoginEnabled) {
            errorMessage = InvalidFamilyShareCodeMessage
            onLoginClick(familyShareCode)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .statusBarsPadding()
    ) {
        FamilyShareCodeTopBar(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(151.dp))
            FamilyShareCodeInputTitle()
            Spacer(modifier = Modifier.height(45.dp))
            FamilyShareCodeTextField(
                value = familyShareCode,
                onValueChange = {
                    familyShareCode = normalizeFamilyShareCode(it)
                    errorMessage = null
                },
                errorMessage = errorMessage,
                onDone = {
                    focusManager.clearFocus()
                    submitFamilyShareCode()
                }
            )
        }

        FamilyShareCodeBottomButton(
            text = "로그인",
            enabled = isLoginEnabled,
            onClick = {
                focusManager.clearFocus()
                submitFamilyShareCode()
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun FamilyShareCodeInputTitle(
    modifier: Modifier = Modifier
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = SeniorOnColors.Primary600)) {
                append("가족공유코드")
            }
            append("를 입력해주세요")
        },
        modifier = modifier.fillMaxWidth(),
        style = SeniorOnTextStyles.HeadingM,
        color = SeniorOnColors.Gray800,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun FamilyShareCodeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    errorMessage: String?,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    focusManager: FocusManager = LocalFocusManager.current
) {
    val underlineColor = if (errorMessage != null) {
        SeniorOnColors.Red200
    } else {
        SeniorOnColors.Gray200
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        textStyle = SeniorOnTextStyles.BodySSemiBold.copy(
            color = SeniorOnColors.Gray800
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Ascii,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                onDone()
            }
        ),
        visualTransformation = FamilyShareCodeVisualTransformation,
        cursorBrush = SolidColor(SeniorOnColors.Primary600),
        decorationBox = { innerTextField ->
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text = "가족 공유 코드 입력",
                                style = SeniorOnTextStyles.BodyMMedium,
                                color = SeniorOnColors.Gray300
                            )
                        }
                        innerTextField()
                    }

                    if (value.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(
                                    interactionSource = remember {
                                        MutableInteractionSource()
                                    },
                                    indication = null,
                                    onClick = { onValueChange("") }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close_filled),
                                contentDescription = "입력 내용 지우기",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                        }
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
    )

    errorMessage?.let {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = it,
            style = SeniorOnTextStyles.CaptionRegular,
            color = SeniorOnColors.Red300
        )
    }
}

private fun normalizeFamilyShareCode(value: String): String {
    return value
        .uppercase(Locale.ROOT)
        .filter { character ->
            character in 'A'..'Z' || character in '0'..'9'
        }
        .take(FamilyShareCodeLength)
}

private object FamilyShareCodeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        if (text.length <= 4) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val transformedText = AnnotatedString(
            text.text.take(4) + "-" + text.text.drop(4)
        )
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return if (offset <= 4) offset else offset + 1
            }

            override fun transformedToOriginal(offset: Int): Int {
                return if (offset <= 4) offset else offset - 1
            }
        }

        return TransformedText(transformedText, offsetMapping)
    }
}

@Preview(
    name = "Family Share Code Input",
    showBackground = true,
    widthDp = 360,
    heightDp = 707
)
@Composable
private fun FamilyShareCodeInputScreenPreview() {
    SENIOR_ONTheme {
        FamilyShareCodeInputScreen(
            onBackClick = {},
            onLoginClick = {}
        )
    }
}
