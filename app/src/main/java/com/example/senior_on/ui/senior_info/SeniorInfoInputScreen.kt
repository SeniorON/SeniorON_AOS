package com.example.senior_on.ui.senior_info

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

enum class SeniorRelationship(val label: String) {
    Mother("어머니"),
    Father("아버지"),
    Grandparent("조부모"),
    Custom("직접 작성")
}

data class SeniorInfoInputState(
    val name: String,
    val relationship: SeniorRelationship,
    val phoneNumber: String,
    val address: String,
    val addressDetail: String
)

@Composable
fun SeniorInfoInputScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSkipClick: () -> Unit = {},
    onSearchAddressClick: () -> Unit = {},
    onSaveClick: (SeniorInfoInputState) -> Unit = {}
) {
    var name by rememberSaveable { mutableStateOf("") }
    var relationship by rememberSaveable { mutableStateOf(SeniorRelationship.Custom) }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var addressDetail by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .statusBarsPadding()
    ) {
        SeniorInfoTopBar(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(31.dp))
            SeniorInfoTitle()

            Spacer(modifier = Modifier.height(24.dp))
            InputLabel(text = "이름")
            Spacer(modifier = Modifier.height(6.dp))
            SeniorInfoTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "이름 입력",
                keyboardType = KeyboardType.Text
            )

            Spacer(modifier = Modifier.height(24.dp))
            InputLabel(text = "관계")
            Spacer(modifier = Modifier.height(6.dp))
            RelationshipSelector(
                selectedRelationship = relationship,
                onRelationshipChange = { relationship = it }
            )

            Spacer(modifier = Modifier.height(24.dp))
            InputLabel(text = "전화번호", optionalText = "(선택)")
            Spacer(modifier = Modifier.height(6.dp))
            SeniorInfoTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                placeholder = "010-0000-0000",
                keyboardType = KeyboardType.Phone
            )

            Spacer(modifier = Modifier.height(24.dp))
            InputLabel(text = "자택 주소", optionalText = "(선택)")
            Spacer(modifier = Modifier.height(4.dp))
            SeniorInfoTextField(
                value = address,
                onValueChange = { address = it },
                placeholder = "주소 검색",
                readOnly = true,
                onClick = onSearchAddressClick,
                trailingContent = {
                    SearchIcon(
                        modifier = Modifier.size(24.dp),
                        color = SeniorOnColors.Gray400
                    )
                }
            )
            Spacer(modifier = Modifier.height(6.dp))
            SeniorInfoTextField(
                value = addressDetail,
                onValueChange = { addressDetail = it },
                placeholder = "상세 주소 입력",
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "외출·귀가 알림 기준 위치로 사용돼요.",
                style = SeniorOnTextStyles.CaptionRegular,
                color = SeniorOnColors.Gray400
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        SeniorInfoBottomActions(
            onSkipClick = onSkipClick,
            onSaveClick = {
                onSaveClick(
                    SeniorInfoInputState(
                        name = name,
                        relationship = relationship,
                        phoneNumber = phoneNumber,
                        address = address,
                        addressDetail = addressDetail
                    )
                )
            }
        )
    }
}

@Composable
private fun SeniorInfoTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = 8.dp,
                spotColor = SeniorOnColors.Gray200.copy(alpha = 0.45f)
            )
            .background(SeniorOnColors.White)
            .padding(start = 10.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBackClick
                ),
            contentAlignment = Alignment.Center
        ) {
            BackArrowIcon(
                modifier = Modifier.size(24.dp),
                color = SeniorOnColors.Gray800
            )
        }

        Text(
            text = "정보 입력",
            modifier = Modifier.padding(start = 2.dp),
            style = SeniorOnTextStyles.HeadingXS,
            color = SeniorOnColors.Gray800
        )
    }
}

@Composable
private fun SeniorInfoTitle() {
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
    Spacer(modifier = Modifier.height(9.dp))
    Text(
        text = "나중에 설정에서 변경할 수 있어요.",
        style = SeniorOnTextStyles.BodyMRegular,
        color = SeniorOnColors.Gray500
    )
}

@Composable
private fun InputLabel(
    text: String,
    modifier: Modifier = Modifier,
    optionalText: String? = null
) {
    Text(
        text = buildAnnotatedString {
            append(text)
            optionalText?.let {
                withStyle(SpanStyle(color = SeniorOnColors.Gray800)) {
                    append(it)
                }
            }
        },
        modifier = modifier,
        style = SeniorOnTextStyles.BodyMBold,
        color = SeniorOnColors.Gray800
    )
}

@Composable
private fun SeniorInfoTextField(
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
    val clickableModifier = if (onClick == null) {
        Modifier
    } else {
        Modifier.clickable(onClick = onClick)
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .requiredSize(width = 328.dp, height = 43.dp)
            .clip(shape)
            .background(SeniorOnColors.White)
            .border(
                width = 1.dp,
                color = SeniorOnColors.Gray200,
                shape = shape
            )
            .then(clickableModifier),
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
}

@Composable
private fun RelationshipSelector(
    selectedRelationship: SeniorRelationship,
    onRelationshipChange: (SeniorRelationship) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .requiredSize(width = 328.dp, height = 52.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(SeniorOnColors.Background3)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SeniorRelationship.entries.forEach { relationship ->
            val isSelected = relationship == selectedRelationship

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onRelationshipChange(relationship) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = relationship.label,
                    style = SeniorOnTextStyles.BodyMSemiBold,
                    color = if (isSelected) SeniorOnColors.Gray800 else SeniorOnColors.Gray600,
                    textDecoration = if (isSelected && relationship == SeniorRelationship.Custom) {
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
private fun SeniorInfoBottomActions(
    onSkipClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SeniorOnColors.White)
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
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
                style = SeniorInfoButtonStyle.Filled
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
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(SeniorOnColors.Gray200, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "!",
                style = SeniorOnTextStyles.BodySSemiBold,
                color = SeniorOnColors.White
            )
        }

        Spacer(modifier = Modifier.width(9.dp))
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

private enum class SeniorInfoButtonStyle {
    Outlined,
    Filled
}

@Composable
private fun SeniorInfoActionButton(
    text: String,
    onClick: () -> Unit,
    style: SeniorInfoButtonStyle,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)
    val backgroundColor = when (style) {
        SeniorInfoButtonStyle.Outlined -> SeniorOnColors.White
        SeniorInfoButtonStyle.Filled -> SeniorOnColors.Primary400
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
            .clickable(onClick = onClick),
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
private fun BackArrowIcon(
    modifier: Modifier = Modifier,
    color: Color = SeniorOnColors.Gray800
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.4.dp.toPx()
        val centerY = size.height / 2f
        drawLine(
            color = color,
            start = Offset(size.width * 0.26f, centerY),
            end = Offset(size.width * 0.78f, centerY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.28f, centerY),
            end = Offset(size.width * 0.52f, size.height * 0.27f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.28f, centerY),
            end = Offset(size.width * 0.52f, size.height * 0.73f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun SearchIcon(
    modifier: Modifier = Modifier,
    color: Color = SeniorOnColors.Gray400
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(
            color = color,
            radius = size.minDimension * 0.29f,
            center = Offset(size.width * 0.44f, size.height * 0.42f),
            style = stroke
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.64f, size.height * 0.64f),
            end = Offset(size.width * 0.82f, size.height * 0.82f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
    }
}

@Preview(
    name = "Senior Info Input",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun SeniorInfoInputScreenPreview() {
    SENIOR_ONTheme {
        SeniorInfoInputScreen()
    }
}
