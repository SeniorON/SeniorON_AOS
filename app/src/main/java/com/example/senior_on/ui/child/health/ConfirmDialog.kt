package com.example.senior_on.ui.child.health

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.senior_on.R
import com.example.senior_on.ui.common.component.SeniorOnActionButton
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
internal fun SeniorOnConfirmDialog(
    @DrawableRes iconResId: Int,
    title: String,
    description: String? = null,
    cancelLabel: String,
    confirmLabel: String,
    confirmColor: Color,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        SeniorOnConfirmDialogContent(
            iconResId = iconResId,
            title = title,
            description = description,
            cancelLabel = cancelLabel,
            confirmLabel = confirmLabel,
            confirmColor = confirmColor,
            onCancel = onCancel,
            onConfirm = onConfirm
        )
    }
}

@Composable
internal fun SeniorOnDeleteConfirmDialog(
    title: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    isConfirmLoading: Boolean = false,
) {
    Dialog(
        onDismissRequest = {
            if (!isConfirmLoading) onCancel()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        SeniorOnDeleteConfirmDialogContent(
            title = title,
            onCancel = onCancel,
            onConfirm = onConfirm,
            isConfirmLoading = isConfirmLoading,
        )
    }
}

@Composable
private fun SeniorOnConfirmDialogContent(
    @DrawableRes iconResId: Int,
    title: String,
    description: String? = null,
    cancelLabel: String,
    confirmLabel: String,
    confirmColor: Color,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 56.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(SeniorOnColors.SupportWhite100)
            .padding(top = 32.dp, bottom = 24.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(38.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Gray800,
            textAlign = TextAlign.Center
        )
        description?.let {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = it,
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray500,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(26.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ConfirmDialogButton(
                label = cancelLabel,
                color = SeniorOnColors.Gray200,
                contentColor = SeniorOnColors.Gray500,
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            )
            ConfirmDialogButton(
                label = confirmLabel,
                color = confirmColor,
                contentColor = SeniorOnColors.SupportWhite100,
                onClick = onConfirm,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SeniorOnDeleteConfirmDialogContent(
    title: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    isConfirmLoading: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 61.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(SeniorOnColors.SupportWhite100)
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_modal_trash),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(38.dp)
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = title,
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Gray800,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(26.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ConfirmDialogButton(
                label = "취소",
                color = SeniorOnColors.Gray200,
                contentColor = SeniorOnColors.Gray500,
                onClick = onCancel,
                enabled = !isConfirmLoading,
                modifier = Modifier.weight(1f)
            )
            ConfirmDialogButton(
                label = "삭제하기",
                color = SeniorOnColors.Red400,
                contentColor = SeniorOnColors.SupportWhite100,
                onClick = onConfirm,
                enabled = !isConfirmLoading,
                isLoading = isConfirmLoading,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ConfirmDialogButton(
    label: String,
    color: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    SeniorOnActionButton(
        text = label,
        onClick = onClick,
        modifier = modifier.height(34.dp),
        enabled = enabled,
        isLoading = isLoading,
        containerColor = color,
        contentColor = contentColor,
        disabledContainerColor = color,
        disabledContentColor = contentColor,
        minHeight = 34.dp,
        textStyle = SeniorOnTextStyles.ButtonS,
        loadingIndicatorSize = 18.dp,
    )
}

@Composable
private fun ConfirmDialogPreviewHost(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Preview(
    name = "삭제 모달",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Composable
private fun SeniorOnConfirmDeleteDialogPreview() {
    SENIOR_ONTheme {
        ConfirmDialogPreviewHost {
            SeniorOnDeleteConfirmDialogContent(
                title = "'혈압약'을 삭제할까요?",
                onCancel = {},
                onConfirm = {}
            )
        }
    }
}

@Preview(
    name = "수정 중 나가기 모달",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Composable
private fun SeniorOnConfirmUnsavedExitDialogPreview() {
    SENIOR_ONTheme {
        ConfirmDialogPreviewHost {
            SeniorOnConfirmDialogContent(
                iconResId = R.drawable.ic_modal_unsaved,
                title = "저장하지 않고\n나가시겠어요?",
                description = "지금 나가면 수정한 내용이\n저장되지 않습니다.",
                cancelLabel = "나가기",
                confirmLabel = "계속 입력",
                confirmColor = SeniorOnColors.Primary600,
                onCancel = {},
                onConfirm = {}
            )
        }
    }
}

@Preview(
    name = "등록 중 나가기 모달",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Composable
private fun SeniorOnConfirmUnsavedAddExitDialogPreview() {
    SENIOR_ONTheme {
        ConfirmDialogPreviewHost {
            SeniorOnConfirmDialogContent(
                iconResId = R.drawable.ic_modal_unsaved,
                title = "등록하지 않고\n나가시겠어요?",
                description = "지금 나가면 입력한 내용이\n저장되지 않습니다.",
                cancelLabel = "나가기",
                confirmLabel = "계속 입력",
                confirmColor = SeniorOnColors.Primary600,
                onCancel = {},
                onConfirm = {}
            )
        }
    }
}
