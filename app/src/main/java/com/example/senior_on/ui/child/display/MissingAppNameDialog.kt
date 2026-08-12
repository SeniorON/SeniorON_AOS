package com.example.senior_on.ui.child.display

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
internal fun MissingAppNameDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var appName by rememberSaveable { mutableStateOf("") }
    val normalizedAppName = appName.trim()
    val confirmEnabled = normalizedAppName.isNotEmpty()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val confirm: () -> Unit = {
        if (confirmEnabled) {
            keyboardController?.hide()
            onConfirm(normalizedAppName)
        }
    }
    val dismiss: () -> Unit = {
        keyboardController?.hide()
        onDismiss()
    }

    Dialog(
        onDismissRequest = dismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = dismiss,
                )
                .imePadding(),
            contentAlignment = Alignment.Center,
        ) {
            MissingAppNameDialogContent(
                appName = appName,
                confirmEnabled = confirmEnabled,
                onAppNameChange = { appName = it },
                onConfirm = confirm,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null,
                        onClick = {},
                    ),
                inputModifier = Modifier.focusRequester(focusRequester),
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}

@Composable
private fun MissingAppNameDialogContent(
    appName: String,
    confirmEnabled: Boolean,
    onAppNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    inputModifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .size(width = 272.dp, height = 278.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.XLarge))
            .background(SeniorOnColors.White)
            .padding(horizontal = 16.dp)
            .padding(top = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "앱 이름을 입력해주세요",
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Gray800,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "앱 이름을 불러오지 못했어요.\n직접 입력해주세요.",
            modifier = Modifier.fillMaxWidth(),
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray500,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(18.dp))

        BasicTextField(
            value = appName,
            onValueChange = onAppNameChange,
            modifier = inputModifier
                .fillMaxWidth()
                .height(47.dp),
            textStyle = SeniorOnTextStyles.BodySMedium.copy(
                color = SeniorOnColors.Gray800,
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onConfirm() }),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(SeniorOnRadius.Small))
                        .background(SeniorOnColors.Background3)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (appName.isEmpty()) {
                        Text(
                            text = "앱 이름 작성",
                            style = SeniorOnTextStyles.BodySMedium,
                            color = SeniorOnColors.Gray300,
                        )
                    }
                    innerTextField()
                }
            },
        )

        Spacer(modifier = Modifier.height(22.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(RoundedCornerShape(SeniorOnRadius.Small))
                .background(
                    SeniorOnColors.Primary600.copy(
                        alpha = if (confirmEnabled) 1f else 0.5f,
                    ),
                )
                .clickable(
                    enabled = confirmEnabled,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onConfirm,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "확인",
                style = SeniorOnTextStyles.ButtonS,
                color = SeniorOnColors.White,
            )
        }
    }
}

@Preview(
    name = "Missing app name dialog",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun MissingAppNameDialogContentPreview() {
    SENIOR_ONTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            MissingAppNameDialogContent(
                appName = "",
                confirmEnabled = false,
                onAppNameChange = {},
                onConfirm = {},
            )
        }
    }
}
