package com.example.senior_on.ui.senior_info

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
internal fun SeniorInfoTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
                elevation = 4.dp,
                ambientColor = Color.Black.copy(alpha = 0.06f),
                spotColor = Color.Black.copy(alpha = 0.06f)
            )
            .background(SeniorOnColors.White)
            .padding(start = 16.dp, end = 16.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBackClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = "뒤로가기",
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
        }

        Text(
            text = "정보 입력",
            modifier = Modifier.padding(start = 16.dp),
            style = SeniorOnTextStyles.HeadingS,
            color = SeniorOnColors.Gray800
        )
    }
}

@Composable
internal fun SeniorInfoTitle() {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = SeniorOnColors.Primary600)) {
                append("시니어")
            }
            append(" 정보를 입력해 주세요")
        },
        style = SeniorOnTextStyles.OnboardingHeading,
        color = SeniorOnColors.Gray800
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "나중에 설정에서 변경할 수 있어요.",
        style = SeniorOnTextStyles.BodySMedium,
        color = SeniorOnColors.Gray500
    )
}

@Composable
internal fun InputLabel(
    text: String,
    modifier: Modifier = Modifier,
    optionalText: String? = null
) {
    Text(
        text = buildAnnotatedString {
            append(text)
            optionalText?.let {
                withStyle(SpanStyle(color = SeniorOnColors.Gray500)) {
                    append(it)
                }
            }
        },
        modifier = modifier,
        style = SeniorOnTextStyles.BodyMSemiBold,
        color = SeniorOnColors.Gray800
    )
}

@Composable
internal fun SeniorInfoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor = if (isFocused) SeniorOnColors.Primary600 else SeniorOnColors.Gray200

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(43.dp)
            .clip(shape)
            .background(SeniorOnColors.White)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            )
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxSize(),
            interactionSource = interactionSource,
            readOnly = readOnly,
            singleLine = true,
            textStyle = SeniorOnTextStyles.BodyMRegular.copy(color = SeniorOnColors.Gray800),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 14.dp, end = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = SeniorOnTextStyles.BodyMRegular,
                                color = SeniorOnColors.Gray300
                            )
                        }
                        innerTextField()
                    }

                    trailingContent?.let {
                        Spacer(modifier = Modifier.width(10.dp))
                        it()
                    }
                }
            }
        )

        onClick?.let {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(onClick = it)
            )
        }
    }
}

@Composable
internal fun SeniorInfoPhoneTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor = if (isFocused) SeniorOnColors.Primary600 else SeniorOnColors.Gray200

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(43.dp)
            .clip(shape)
            .background(SeniorOnColors.White)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            ),
        interactionSource = interactionSource,
        singleLine = true,
        textStyle = SeniorOnTextStyles.BodyMRegular.copy(color = SeniorOnColors.Gray800),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        ),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 14.dp, end = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.text.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = SeniorOnTextStyles.BodyMRegular,
                            color = SeniorOnColors.Gray300
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Composable
internal fun BirthDateSelector(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value.ifBlank { "생년월일 선택" },
                modifier = Modifier.weight(1f),
                style = SeniorOnTextStyles.BodyMRegular,
                color = if (value.isBlank()) {
                    SeniorOnColors.Gray300
                } else {
                    SeniorOnColors.Gray800
                }
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_sm_chevron_down_2),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = SeniorOnColors.Gray500
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SeniorOnColors.Gray200)
        )
    }
}

@Composable
internal fun RelationshipSelector(
    selectedRelationship: SeniorRelationship?,
    customRelationshipLabel: String,
    onRelationshipClick: (SeniorRelationship) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(SeniorOnColors.Background3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SeniorRelationship.entries.forEach { relationship ->
            val isSelected = relationship == selectedRelationship
            val itemShape = RoundedCornerShape(SeniorOnRadius.Small)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(itemShape)
                    .background(if (isSelected) SeniorOnColors.Primary100 else Color.Transparent)
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                width = 1.dp,
                                color = SeniorOnColors.Primary400,
                                shape = itemShape
                            )
                        } else {
                            Modifier
                        }
                    )
                    .clickable { onRelationshipClick(relationship) },
                contentAlignment = Alignment.Center
            ) {
                val displayLabel = if (
                    relationship == SeniorRelationship.Custom &&
                    customRelationshipLabel.isNotBlank()
                ) {
                    customRelationshipLabel
                } else {
                    relationship.label
                }

                Text(
                    text = displayLabel,
                    style = SeniorOnTextStyles.BodySSemiBold,
                    color = if (isSelected) {
                        SeniorOnColors.Primary600
                    } else {
                        SeniorOnColors.Gray600
                    },
                    textDecoration = if (relationship == SeniorRelationship.Custom) {
                        TextDecoration.Underline
                    } else {
                        null
                    }
                )
            }
        }
    }
}

@Composable
internal fun SeniorInfoBottomActions(
    onSkipClick: () -> Unit,
    onSaveClick: () -> Unit,
    isSaveEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SeniorOnColors.White)
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
    ) {
        SeniorInfoWarning()
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SeniorInfoActionButton(
                text = "다음에 하기",
                onClick = onSkipClick,
                modifier = Modifier.weight(1f),
                style = SeniorInfoButtonStyle.Outlined
            )
            SeniorInfoActionButton(
                text = "저장 후 시작",
                onClick = onSaveClick,
                modifier = Modifier.weight(1f),
                style = SeniorInfoButtonStyle.Filled,
                enabled = isSaveEnabled
            )
        }
    }
}

@Composable
private fun SeniorInfoWarning(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_alert_filled),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = SeniorOnColors.Gray200
        )

        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = buildAnnotatedString {
                append("다음에 하기 선택시 ")
                withStyle(SpanStyle(color = SeniorOnColors.Red300)) {
                    append("일부 기능이 제한")
                }
                append("될 수 있어요")
            },
            style = SeniorOnTextStyles.BodySRegular,
            color = SeniorOnColors.Gray400
        )
    }
}

internal enum class SeniorInfoButtonStyle {
    Outlined,
    Filled
}

@Composable
internal fun SeniorInfoActionButton(
    text: String,
    onClick: () -> Unit,
    style: SeniorInfoButtonStyle,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)
    val backgroundColor = when (style) {
        SeniorInfoButtonStyle.Outlined -> SeniorOnColors.White
        SeniorInfoButtonStyle.Filled -> {
            if (enabled) SeniorOnColors.Primary600 else SeniorOnColors.Primary300
        }
    }
    val contentColor = when (style) {
        SeniorInfoButtonStyle.Outlined -> SeniorOnColors.Primary600
        SeniorInfoButtonStyle.Filled -> SeniorOnColors.White
    }
    val borderColor = when (style) {
        SeniorInfoButtonStyle.Outlined -> SeniorOnColors.Primary600
        SeniorInfoButtonStyle.Filled -> Color.Transparent
    }

    Box(
        modifier = modifier
            .height(60.dp)
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            )
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.ButtonM,
            color = contentColor
        )
    }
}

@Composable
internal fun SeniorInfoSkipNoticeDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        SeniorInfoSkipNoticeContent(
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }
}

@Composable
internal fun SeniorInfoSkipNoticeContent(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .requiredSize(width = 238.dp, height = 241.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(SeniorOnColors.White)
            .padding(start = 25.dp, end = 25.dp, top = 31.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(SeniorOnColors.Gray100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_big_clock),
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                tint = SeniorOnColors.Primary500
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "다음에 하기",
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Gray800
        )

        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "부모님 정보를 입력하지 않으면\n일부 기능이 제한돼요.",
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray500,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(26.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SeniorInfoSkipNoticeButton(
                text = "취소",
                backgroundColor = SeniorOnColors.Gray200,
                contentColor = SeniorOnColors.Gray500,
                onClick = onDismiss
            )
            SeniorInfoSkipNoticeButton(
                text = "확인",
                backgroundColor = SeniorOnColors.Primary600,
                contentColor = SeniorOnColors.White,
                onClick = onConfirm
            )
        }
    }
}

@Composable
private fun SeniorInfoSkipNoticeButton(
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .requiredSize(width = 84.dp, height = 34.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.ButtonS,
            color = contentColor
        )
    }
}
