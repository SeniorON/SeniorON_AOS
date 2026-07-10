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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
            .background(SeniorOnColors.White)
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        startY = size.height,
                        endY = size.height + 12.dp.toPx()
                    ),
                    topLeft = Offset(0f, size.height),
                    size = Size(size.width, 12.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
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
            modifier = Modifier.align(Alignment.Center),
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Gray800,
            textAlign = TextAlign.Center
        )
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
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.BodyLMedium,
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
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val interactionSource = remember { MutableInteractionSource() }
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = SeniorOnColors.Primary600,
        unfocusedBorderColor = SeniorOnColors.Gray200,
        focusedContainerColor = SeniorOnColors.SupportWhite100,
        unfocusedContainerColor = SeniorOnColors.SupportWhite100,
        cursorColor = SeniorOnColors.Primary600
    )
    val fieldShape = RoundedCornerShape(SeniorOnRadius.Small)
    val fieldTextStyle = SeniorOnTextStyles.BodyMMedium.copy(color = SeniorOnColors.Gray800)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = SeniorOnTextStyles.BodyMMedium,
            color = SeniorOnColors.Gray800
        )

        Spacer(modifier = Modifier.height(6.dp))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(43.dp),
            textStyle = fieldTextStyle,
            singleLine = true,
            keyboardOptions = keyboardOptions,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = value,
                    visualTransformation = VisualTransformation.None,
                    innerTextField = innerTextField,
                    placeholder = {
                        Text(
                            text = placeholder,
                            style = SeniorOnTextStyles.BodyMMedium,
                            color = SeniorOnColors.Gray300
                        )
                    },
                    trailingIcon = if (value.isNotEmpty()) {
                        {
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
                    } else {
                        null
                    },
                    singleLine = true,
                    enabled = true,
                    isError = false,
                    interactionSource = interactionSource,
                    colors = textFieldColors,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 12.dp,
                        vertical = 10.5.dp
                    ),
                    container = {
                        OutlinedTextFieldDefaults.ContainerBox(
                            enabled = true,
                            isError = false,
                            interactionSource = interactionSource,
                            colors = textFieldColors,
                            shape = fieldShape
                        )
                    }
                )
            }
        )
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
            .height(50.dp)
            .background(backgroundColor, RoundedCornerShape(SeniorOnRadius.Small))
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

@Composable
internal fun FindAccountSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(1.dp, SeniorOnColors.Gray200, RoundedCornerShape(SeniorOnRadius.Small))
            .background(SeniorOnColors.White, RoundedCornerShape(SeniorOnRadius.Small))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.ButtonM,
            color = SeniorOnColors.Gray500
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
                .padding(bottom = 23.5.dp)
        ) {
            bottomBar()
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "Find Account - Input")
@Composable
private fun FindAccountComponentsInputPreview() {
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

@Preview(showBackground = true, widthDp = 360, heightDp = 200, name = "Find Account - Tabs")
@Composable
private fun FindAccountTabRowPreview() {
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
