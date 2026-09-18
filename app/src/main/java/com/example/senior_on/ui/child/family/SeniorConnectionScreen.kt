package com.example.senior_on.ui.child.family

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.senior_on.R
import com.example.senior_on.domain.model.server.ServerConnectedSenior
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.util.Locale

@Composable
internal fun SeniorConnectionScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCodeEntryClick: () -> Unit = {},
) {
    SeniorConnectionScaffold("시니어 연결", onBackClick, modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(70.dp))
            Image(
                painter = painterResource(id = R.drawable.img_senior_connection),
                contentDescription = null,
                modifier = Modifier.size(116.dp),
            )
            Spacer(modifier = Modifier.height(17.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = SeniorOnColors.Primary700)) { append("시니어 코드") }
                    withStyle(SpanStyle(color = SeniorOnColors.Gray800)) {
                        append("를 입력하면\n가족사진을 함께 볼 수 있어요")
                    }
                },
                style = SeniorOnTextStyles.HeadingXS,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "연결할 시니어의 코드를 입력해주세요.\n" +
                    "연결하면 시니어와 보호자가 같은 가족 사진을 함께 볼 수 있어요.",
                style = SeniorOnTextStyles.CaptionMedium,
                color = SeniorOnColors.Gray600,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(63.dp))
            FamilyTextActionButton(
                text = "코드 입력하기",
                backgroundColor = SeniorOnColors.Primary600,
                contentColor = SeniorOnColors.White,
                onClick = onCodeEntryClick,
                modifier = Modifier.fillMaxWidth(),
                buttonHeight = 58.dp,
            )
        }
    }
}

@Composable
internal fun SeniorCodeInputScreen(
    onBackClick: () -> Unit,
    onConnectClick: (String) -> Unit,
    isConnecting: Boolean,
    errorMessage: String?,
    onCodeChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var code by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val isValid = code.length == SENIOR_CODE_LENGTH

    SeniorConnectionScaffold("시니어 연결", onBackClick, modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(107.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = SeniorOnColors.Primary700)) { append("시니어 코드") }
                    append("를 입력해 주세요")
                },
                style = SeniorOnTextStyles.HeadingXS,
                color = SeniorOnColors.Gray800,
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "연결할 시니어의 코드를 확인해 주세요.",
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray500,
            )
            Spacer(modifier = Modifier.height(45.dp))
            SeniorCodeTextField(
                value = code,
                onValueChange = {
                    code = normalizeSeniorConnectionCode(it)
                    onCodeChange()
                },
                errorMessage = errorMessage,
                focusManager = focusManager,
                onDone = { if (isValid && !isConnecting) onConnectClick(code) },
            )
            Spacer(modifier = Modifier.height(49.dp))
            FamilyTextActionButton(
                text = if (isConnecting) "연결 중..." else "연결하기",
                backgroundColor = SeniorOnColors.Primary600,
                contentColor = SeniorOnColors.White,
                enabled = isValid && !isConnecting,
                onClick = {
                    focusManager.clearFocus()
                    onConnectClick(code)
                },
                modifier = Modifier.fillMaxWidth(),
                buttonHeight = 52.dp,
            )
        }
    }
}

@Composable
private fun SeniorCodeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    errorMessage: String?,
    focusManager: FocusManager,
    onDone: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        BasicTextField(
            value = formatSeniorConnectionCode(value),
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            textStyle = SeniorOnTextStyles.BodyLBold.copy(
                color = SeniorOnColors.Gray800,
                textAlign = TextAlign.Center,
            ),
            singleLine = true,
            cursorBrush = SolidColor(SeniorOnColors.Primary600),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                onDone()
            }),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(SeniorOnRadius.Medium))
                        .border(
                            1.dp,
                            if (errorMessage == null) SeniorOnColors.Gray200 else SeniorOnColors.Red200,
                            RoundedCornerShape(SeniorOnRadius.Medium),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = "43TS-6GTE",
                            style = SeniorOnTextStyles.BodyLBold,
                            color = SeniorOnColors.Gray300,
                        )
                    }
                    innerTextField()
                }
            },
        )
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = errorMessage,
                style = SeniorOnTextStyles.CaptionMedium,
                color = SeniorOnColors.Red200,
            )
        }
    }
}

@Composable
internal fun ConnectedSeniorListScreen(
    seniors: List<ServerConnectedSenior>,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onDisconnectClick: (ServerConnectedSenior) -> Unit,
    modifier: Modifier = Modifier,
) {
    SeniorConnectionScaffold("연결된 시니어", onBackClick, modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(SeniorOnColors.Background2)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Spacer(modifier = Modifier.height(10.dp)) }
            items(seniors, key = ServerConnectedSenior::photoGroupId) { senior ->
                ConnectedSeniorCard(senior, onDisconnectClick = { onDisconnectClick(senior) })
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                        .clip(RoundedCornerShape(SeniorOnRadius.Medium))
                        .border(1.dp, SeniorOnColors.Primary400, RoundedCornerShape(SeniorOnRadius.Medium))
                        .clickable(onClick = onAddClick),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("＋", style = SeniorOnTextStyles.HeadingXS, color = SeniorOnColors.Primary600)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("시니어 추가하기", style = SeniorOnTextStyles.BodyMSemiBold, color = SeniorOnColors.Primary600)
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ConnectedSeniorCard(
    senior: ServerConnectedSenior,
    onDisconnectClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .background(SeniorOnColors.White).padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape)
                    .background(SeniorOnColors.AccountAvatarBackground)
                    .border(1.dp, SeniorOnColors.Green, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_senior_account_avatar),
                    contentDescription = null,
                    modifier = Modifier.size(31.dp),
                    tint = SeniorOnColors.AccountAvatarForeground,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(senior.name, style = SeniorOnTextStyles.BodyLBold, color = SeniorOnColors.Gray800)
                Text(
                    text = formatConnectedAt(senior.connectedAt),
                    style = SeniorOnTextStyles.CaptionMedium,
                    color = SeniorOnColors.Gray400,
                )
            }
            Text(
                text = senior.relationshipLabel,
                modifier = Modifier.clip(RoundedCornerShape(SeniorOnRadius.XLarge))
                    .background(SeniorOnColors.Primary200)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                style = SeniorOnTextStyles.CaptionMedium,
                color = SeniorOnColors.Primary700,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(42.dp)
                .clip(RoundedCornerShape(SeniorOnRadius.Small))
                .background(SeniorOnColors.Gray50)
                .clickable(onClick = onDisconnectClick),
            contentAlignment = Alignment.Center,
        ) {
            Text("연결 해제", style = SeniorOnTextStyles.BodySMedium, color = SeniorOnColors.Gray600)
        }
    }
}

@Composable
internal fun SeniorDisconnectConfirmationDialog(
    senior: ServerConnectedSenior,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isDisconnecting: Boolean = false,
) {
    val label = senior.relationshipLabel.trim()
        .takeIf { it.isNotEmpty() && it != "가족" }
        ?: senior.name.trim().ifBlank { "시니어" }
    val particle = if (label.hasFinalConsonant()) "과" else "와"

    Dialog(
        onDismissRequest = {
            if (!isDisconnecting) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = !isDisconnecting,
            dismissOnClickOutside = !isDisconnecting,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .width(249.dp)
                    .height(278.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SeniorOnColors.White),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(104.dp)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SeniorOnColors.Red50),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sm_link),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = SeniorOnColors.Red300,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "${label}${particle}의 연결을\n해제할까요?",
                        style = SeniorOnTextStyles.BodyLBold,
                        color = SeniorOnColors.Gray800,
                        textAlign = TextAlign.Center,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "해제 후 ${label}${particle} 연결된 보호자는\n" +
                            "더 이상 가족 사진을 함께 볼 수 없어요.",
                        style = SeniorOnTextStyles.BodySMedium,
                        color = SeniorOnColors.Gray500,
                        textAlign = TextAlign.Center,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(66.dp)
                        .padding(top = 8.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 6.dp,
                        alignment = Alignment.CenterHorizontally,
                    ),
                ) {
                    FamilyTextActionButton(
                        text = "취소",
                        backgroundColor = SeniorOnColors.Gray100,
                        contentColor = SeniorOnColors.Gray500,
                        onClick = onDismiss,
                        modifier = Modifier.width(84.dp),
                        enabled = !isDisconnecting,
                        buttonHeight = 34.dp,
                    )
                    FamilyTextActionButton(
                        text = "연결 해제",
                        backgroundColor = SeniorOnColors.Red400,
                        contentColor = SeniorOnColors.White,
                        onClick = onConfirm,
                        modifier = Modifier.width(84.dp),
                        enabled = !isDisconnecting,
                        isLoading = isDisconnecting,
                        buttonHeight = 34.dp,
                    )
                }
            }
        }
    }
}

@Composable
internal fun SeniorConnectionLoadingScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SeniorConnectionScaffold("시니어 연결", onBackClick, modifier) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SeniorOnColors.Primary600)
        }
    }
}

@Composable
internal fun SeniorConnectionErrorScreen(
    message: String,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SeniorConnectionScaffold("시니어 연결", onBackClick, modifier) {
        FamilyErrorContent(message = message, onRetryClick = onRetryClick)
    }
}

@Composable
private fun SeniorConnectionScaffold(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().background(SeniorOnColors.White).statusBarsPadding(),
    ) {
        FamilyBackTopAppBar(title = title, onBackClick = onBackClick)
        content()
    }
}

internal fun normalizeSeniorConnectionCode(value: String): String = value
    .uppercase(Locale.ROOT).filter { it in 'A'..'Z' || it in '0'..'9' }
    .take(SENIOR_CODE_LENGTH)

internal fun formatSeniorConnectionCode(value: String): String =
    if (value.length <= 4) value else value.take(4) + "-" + value.drop(4)

private fun formatConnectedAt(value: String): String {
    val date = value.take(10)
    return if (date.length == 10 && date[4] == '-' && date[7] == '-') {
        "${date.replace('-', '.')} 연결됨"
    } else {
        "연결됨"
    }
}

private fun String.hasFinalConsonant(): Boolean {
    val lastCharacter = lastOrNull() ?: return false
    if (lastCharacter !in '\uAC00'..'\uD7A3') return false
    return (lastCharacter.code - 0xAC00) % 28 != 0
}

private const val SENIOR_CODE_LENGTH = 8

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SeniorConnectionScreenPreview() {
    SENIOR_ONTheme { SeniorConnectionScreen(onBackClick = {}) }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SeniorDisconnectConfirmationDialogPreview() {
    SENIOR_ONTheme {
        SeniorDisconnectConfirmationDialog(
            senior = ServerConnectedSenior(
                photoGroupId = 31L,
                seniorId = 7L,
                name = "김순자",
                relationshipLabel = "어머니",
                connectedAt = "2026-09-18T12:00:00",
            ),
            onDismiss = {},
            onConfirm = {},
        )
    }
}
