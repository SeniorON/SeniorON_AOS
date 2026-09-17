package com.example.senior_on.ui.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.senior_on.ui.theme.*

/** Defaults preserve child settings; callers can supply senior typography and spacing. */
internal data class SettingsBottomDialogStyle(
    val titleStyle: TextStyle = SeniorOnTextStyles.HeadingXS,
    val descriptionStyle: TextStyle = SeniorOnTextStyles.BodyMMedium,
    val buttonStyle: TextStyle = SeniorOnTextStyles.ButtonM,
    val horizontalPadding: Dp = 20.dp,
    val topPadding: Dp = 24.dp,
    val bottomPadding: Dp = 0.dp,
    val titleDescriptionGap: Dp? = null,
    val descriptionButtonGap: Dp = 34.dp,
    val buttonHeight: Dp = 48.dp,
    val cancelColor: Color = SeniorOnColors.Gray500,
    val wrapContentHeight: Boolean = false,
)

@Composable
internal fun SettingsLogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isConfirmEnabled: Boolean = true,
    isConfirmLoading: Boolean = false,
    style: SettingsBottomDialogStyle = SettingsBottomDialogStyle(),
) {
    SettingsConfirmBottomDialog(
        onDismiss = onDismiss,
        title = buildAnnotatedString {
            withStyle(SpanStyle(color = SeniorOnColors.Primary600)) {
                append("로그아웃")
            }
            append(" 할까요?")
        },
        description = "로그인 화면으로 이동해요",
        descriptionAnnotated = null,
        dialogHeight = 207.dp,
        titleToDescriptionSpacing = 24.dp,
        cancelText = "취소",
        confirmText = "로그아웃",
        confirmBackgroundColor = SeniorOnColors.Primary600,
        isConfirmEnabled = isConfirmEnabled,
        isConfirmLoading = isConfirmLoading,
        onConfirm = onConfirm,
        style = style,
    )
}

@Composable
internal fun SettingsWithdrawDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isConfirmEnabled: Boolean = true,
    isConfirmLoading: Boolean = false,
    style: SettingsBottomDialogStyle = SettingsBottomDialogStyle(),
) {
    SettingsConfirmBottomDialog(
        onDismiss = onDismiss,
        title = buildAnnotatedString {
            append("정말 ")
            withStyle(SpanStyle(color = SeniorOnColors.Red400)) {
                append("탈퇴")
            }
            append("하시겠어요?")
        },
        description = null,
        descriptionAnnotated = buildAnnotatedString {
            append("탈퇴 후에는 ")
            withStyle(SpanStyle(color = SeniorOnColors.Red300)) {
                append("모든 데이터가\n 복구되지 않아요")
            }
        },
        dialogHeight = 229.dp,
        titleToDescriptionSpacing = 24.dp,
        cancelText = "취소",
        confirmText = "탈퇴",
        confirmBackgroundColor = SeniorOnColors.Red400,
        onConfirm = onConfirm,
        isConfirmEnabled = isConfirmEnabled,
        isConfirmLoading = isConfirmLoading,
        style = style,
    )
}

@Composable
internal fun SettingsConfirmBottomDialog(
    onDismiss: () -> Unit,
    title: androidx.compose.ui.text.AnnotatedString,
    description: String?,
    descriptionAnnotated: androidx.compose.ui.text.AnnotatedString?,
    cancelText: String,
    confirmText: String,
    confirmBackgroundColor: Color,
    onConfirm: () -> Unit,
    dialogHeight: Dp,
    titleToDescriptionSpacing: Dp = 12.dp,
    isConfirmEnabled: Boolean = true,
    isConfirmLoading: Boolean = false,
    showCancelButton: Boolean = true,
    style: SettingsBottomDialogStyle = SettingsBottomDialogStyle(),
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (style.wrapContentHeight) Modifier else Modifier.height(dialogHeight))
                    .clip(
                        RoundedCornerShape(
                            topStart = SeniorOnRadius.XLarge,
                            topEnd = SeniorOnRadius.XLarge
                        )
                    )
                    .background(SeniorOnColors.White)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = style.horizontalPadding)
                    .padding(top = style.topPadding, bottom = style.bottomPadding)
            ) {
                Text(
                    text = title,
                    style = style.titleStyle,
                    color = SeniorOnColors.Gray800,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(style.titleDescriptionGap ?: titleToDescriptionSpacing))

                if (descriptionAnnotated != null) {
                    Text(
                        text = descriptionAnnotated,
                        style = style.descriptionStyle,
                        color = SeniorOnColors.Gray500,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (description != null) {
                    Text(
                        text = description,
                        style = style.descriptionStyle,
                        color = SeniorOnColors.Gray500,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(style.descriptionButtonGap))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showCancelButton) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(style.buttonHeight)
                                .clip(RoundedCornerShape(SeniorOnRadius.Small))
                                .border(
                                    width = 1.dp,
                                    color = SeniorOnColors.Gray200,
                                    shape = RoundedCornerShape(SeniorOnRadius.Small)
                                )
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cancelText,
                                style = style.buttonStyle,
                                color = style.cancelColor
                            )
                        }
                    }

                    SeniorOnActionButton(
                        text = confirmText,
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(style.buttonHeight),
                        enabled = isConfirmEnabled,
                        isLoading = isConfirmLoading,
                        containerColor = confirmBackgroundColor,
                        contentColor = SeniorOnColors.White,
                        minHeight = style.buttonHeight,
                        textStyle = style.buttonStyle,
                    )
                }
            }
        }
    }
}
