package com.example.senior_on.ui.child.family

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.senior_on.R
import com.example.senior_on.domain.model.family.FamilyPhotoReportReason
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

enum class FamilyPhotoReportTarget {
    Post,
    Author,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FamilyPhotoReportTargetBottomSheet(
    authorName: String,
    onDismiss: () -> Unit,
    onTargetSelected: (FamilyPhotoReportTarget) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SeniorOnColors.White,
        scrimColor = SeniorOnColors.Black.copy(alpha = 0.72f),
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(
            topStart = SeniorOnRadius.XLarge,
            topEnd = SeniorOnRadius.XLarge,
        ),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 24.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(32.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(SeniorOnRadius.XLarge))
                    .background(SeniorOnColors.Gray800),
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "무엇을 신고할까요?",
                style = SeniorOnTextStyles.HeadingXS,
                color = SeniorOnColors.Gray800,
            )
            Spacer(modifier = Modifier.height(12.dp))
            FamilyPhotoReportTargetRow(
                iconResId = R.drawable.ic_family_report_post,
                title = "게시물 신고",
                description = "부적절한 게시물을 신고합니다",
                onClick = { onTargetSelected(FamilyPhotoReportTarget.Post) },
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SeniorOnColors.Gray100),
            )
            FamilyPhotoReportTargetRow(
                iconResId = R.drawable.ic_family_report_author,
                title = "게시자 신고",
                description = "${authorName}님을 신고합니다",
                onClick = { onTargetSelected(FamilyPhotoReportTarget.Author) },
            )
        }
    }
}

@Composable
private fun FamilyPhotoReportTargetRow(
    iconResId: Int,
    title: String,
    description: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .alpha(if (enabled) 1f else 0.45f)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified,
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = SeniorOnTextStyles.BodyLSemiBold,
                color = SeniorOnColors.Gray800,
            )
            Text(
                text = description,
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray500,
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_sm_arrow_right),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (enabled) SeniorOnColors.Gray800 else SeniorOnColors.Gray300,
        )
    }
}

@Composable
internal fun FamilyPhotoReportReasonDialog(
    target: FamilyPhotoReportTarget,
    selectedReason: FamilyPhotoReportReason?,
    detail: String,
    hide: Boolean,
    isSubmitting: Boolean,
    errorMessage: String?,
    onReasonSelected: (FamilyPhotoReportReason) -> Unit,
    onDetailChanged: (String) -> Unit,
    onHideChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
) {
    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Black.copy(alpha = 0.72f))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 286.dp)
                    .fillMaxWidth()
                    .heightIn(min = 446.dp)
                    .clip(RoundedCornerShape(SeniorOnRadius.XLarge))
                    .background(SeniorOnColors.White)
                    .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (target == FamilyPhotoReportTarget.Post) {
                            "게시물 신고 사유"
                        } else {
                            "게시자 신고 사유"
                        },
                        modifier = Modifier.weight(1f),
                        style = SeniorOnTextStyles.BodyMSemiBold,
                        color = SeniorOnColors.Gray800,
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "신고 닫기",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                enabled = !isSubmitting,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDismiss,
                            ),
                        tint = SeniorOnColors.Gray800,
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                ReportReasonGrid(
                    selectedReason = selectedReason,
                    onReasonSelected = onReasonSelected,
                )
                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(
                    value = detail,
                    onValueChange = onDetailChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp)
                        .clip(RoundedCornerShape(SeniorOnRadius.Small))
                        .background(SeniorOnColors.White)
                        .border(
                            width = 1.dp,
                            color = SeniorOnColors.Gray200,
                            shape = RoundedCornerShape(SeniorOnRadius.Small),
                        )
                        .padding(10.dp),
                    textStyle = SeniorOnTextStyles.CaptionRegular.copy(
                        color = SeniorOnColors.Gray800,
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SeniorOnColors.White)
                                .padding(0.dp),
                        ) {
                            innerTextField()
                        }
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "허위 신고 또는 반복적인 악의적 신고는 제한될 수\n있습니다.",
                    style = SeniorOnTextStyles.CaptionRegular,
                    color = SeniorOnColors.Gray500,
                )
                Spacer(modifier = Modifier.height(16.dp))
                FamilyPhotoReportHideOption(
                    checked = hide,
                    enabled = !isSubmitting,
                    label = if (target == FamilyPhotoReportTarget.Author) {
                        "이 게시자의 게시물 보지 않기"
                    } else {
                        "이 사진 다시 보지 않기"
                    },
                    onCheckedChange = onHideChanged,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "신고는 반대 의견을 표시하는 기능이 아닙니다.",
                    style = SeniorOnTextStyles.CaptionRegular,
                    color = SeniorOnColors.Gray500,
                )
                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = SeniorOnTextStyles.CaptionRegular,
                        color = SeniorOnColors.Red400,
                    )
                }
                Spacer(modifier = Modifier.height(23.dp))
                FamilyTextActionButton(
                    text = "신고하기",
                    backgroundColor = SeniorOnColors.Red300,
                    contentColor = SeniorOnColors.White,
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedReason != null && !isSubmitting,
                    isLoading = isSubmitting,
                    buttonHeight = 36.dp,
                )
            }
        }
    }
}

@Composable
private fun FamilyPhotoReportHideOption(
    checked: Boolean,
    enabled: Boolean,
    label: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (checked) SeniorOnColors.Red300 else SeniorOnColors.Gray200),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = SeniorOnColors.White,
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray800,
        )
    }
}

@Composable
private fun ReportReasonGrid(
    selectedReason: FamilyPhotoReportReason?,
    onReasonSelected: (FamilyPhotoReportReason) -> Unit,
) {
    val rows = listOf(
        FamilyPhotoReportReason.Advertisement to FamilyPhotoReportReason.Copyright,
        FamilyPhotoReportReason.Sexual to FamilyPhotoReportReason.Abuse,
        FamilyPhotoReportReason.Privacy to FamilyPhotoReportReason.Duplicate,
        FamilyPhotoReportReason.Etc to null,
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { (left, right) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(44.dp),
            ) {
                FamilyPhotoReportReasonOption(
                    reason = left,
                    selected = selectedReason == left,
                    onClick = { onReasonSelected(left) },
                    modifier = Modifier.weight(1f),
                )
                if (right == null) {
                    Spacer(modifier = Modifier.weight(1f))
                } else {
                    FamilyPhotoReportReasonOption(
                        reason = right,
                        selected = selectedReason == right,
                        onClick = { onReasonSelected(right) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun FamilyPhotoReportReasonOption(
    reason: FamilyPhotoReportReason,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .heightIn(min = 18.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(
                id = if (selected) {
                    R.drawable.ic_radio_button_1
                } else {
                    R.drawable.ic_radio_button_2
                },
            ),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (selected) SeniorOnColors.Red300 else SeniorOnColors.Gray200,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = reason.toDisplayLabel(),
            style = SeniorOnTextStyles.CaptionRegular,
            color = SeniorOnColors.Gray800,
            maxLines = 1,
        )
    }
}

private fun FamilyPhotoReportReason.toDisplayLabel(): String = when (this) {
    FamilyPhotoReportReason.Advertisement -> "영리/홍보성"
    FamilyPhotoReportReason.Copyright -> "저작권 침해"
    FamilyPhotoReportReason.Sexual -> "음란/선정성"
    FamilyPhotoReportReason.Abuse -> "욕설/인신공격"
    FamilyPhotoReportReason.Privacy -> "개인정보 노출"
    FamilyPhotoReportReason.Duplicate -> "같은 사진 반복"
    FamilyPhotoReportReason.Etc -> "기타"
}

@Composable
internal fun FamilyPhotoReportSuccessDialog(
    onConfirmClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Black.copy(alpha = 0.72f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .width(249.dp)
                    .clip(RoundedCornerShape(SeniorOnRadius.XLarge))
                    .background(SeniorOnColors.White)
                    .padding(start = 18.dp, top = 30.dp, end = 18.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_modal_check),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.Unspecified,
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "신고가 접수됐어요",
                    style = SeniorOnTextStyles.BodyLBold,
                    color = SeniorOnColors.Gray800,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "신고해 주신 내용은 검토 후\n운영 정책에 따라 조치하겠습니다.",
                    style = SeniorOnTextStyles.CaptionRegular,
                    color = SeniorOnColors.Gray500,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(24.dp))
                FamilyTextActionButton(
                    text = "확인",
                    backgroundColor = SeniorOnColors.Primary600,
                    contentColor = SeniorOnColors.White,
                    onClick = onConfirmClick,
                    modifier = Modifier.fillMaxWidth(),
                    buttonHeight = 36.dp,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun FamilyPhotoReportReasonDialogPreview() {
    SENIOR_ONTheme {
        FamilyPhotoReportReasonDialog(
            target = FamilyPhotoReportTarget.Author,
            selectedReason = null,
            detail = "",
            hide = false,
            isSubmitting = false,
            errorMessage = null,
            onReasonSelected = {},
            onDetailChanged = {},
            onHideChanged = {},
            onDismiss = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun FamilyPhotoReportSuccessDialogPreview() {
    SENIOR_ONTheme {
        FamilyPhotoReportSuccessDialog(onConfirmClick = {})
    }
}
