package com.example.senior_on.ui.family

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.data.model.MockFamilyFixtures
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun FamilyPhotoDetailScreen(
    uiState: FamilyPhotoDetailUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSaving: Boolean = false,
    isSaveSuccessVisible: Boolean = false,
    isDeleteDialogVisible: Boolean = false,
    onDownloadClick: () -> Unit = {},
    onDeleteRequest: () -> Unit = {},
    onDeleteDismiss: () -> Unit = {},
    onDeleteConfirm: () -> Unit = {},
    onRetryClick: () -> Unit = {},
    sharedPhotoImage: @Composable BoxScope.(SharedFamilyPhotoUiModel) -> Unit = {
        SharedFamilyPhotoImage(it)
    }
) {
    val photo = uiState.photo

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .statusBarsPadding()
    ) {
        FamilyPhotoDetailTopBar(
            photo = photo,
            onBackClick = onBackClick
        )

        if (photo == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(SeniorOnColors.Background1),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = SeniorOnColors.Primary600)
                } else if (uiState.errorMessage != null) {
                    FamilyErrorContent(
                        message = uiState.errorMessage,
                        onRetryClick = onRetryClick,
                    )
                } else {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        style = SeniorOnTextStyles.BodyMMedium,
                        color = SeniorOnColors.Gray500,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(SeniorOnColors.Background4)
            ) {
                sharedPhotoImage(photo)

                if (isSaveSuccessVisible) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(82.dp)
                            .clip(CircleShape)
                            .background(SeniorOnColors.SupportWhite80),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = "사진 저장 완료",
                            modifier = Modifier.size(38.dp),
                            tint = SeniorOnColors.Media
                        )
                    }
                }
            }

            FamilyPhotoDetailActions(
                showDelete = photo.isOwnedByCurrentUser,
                isSaving = isSaving,
                isDeleting = uiState.isDeleting,
                onDownloadClick = onDownloadClick,
                onDeleteClick = onDeleteRequest
            )
        }
    }

    if (isDeleteDialogVisible) {
        FamilyPhotoDeleteDialog(
            onDismiss = onDeleteDismiss,
            onConfirm = onDeleteConfirm
        )
    }
}

@Composable
private fun FamilyPhotoDetailTopBar(
    photo: SharedFamilyPhotoUiModel?,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(SeniorOnColors.White)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_back),
            contentDescription = "뒤로가기",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .size(26.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBackClick
                ),
            tint = SeniorOnColors.Gray800
        )

        if (photo != null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = photo.authorName,
                    style = SeniorOnTextStyles.BodyMBold,
                    color = SeniorOnColors.Gray800
                )
                Text(
                    text = photo.uploadedDate,
                    style = SeniorOnTextStyles.BodySMedium,
                    color = SeniorOnColors.Gray800
                )
            }
        }
    }
}

@Composable
private fun FamilyPhotoDetailActions(
    showDelete: Boolean,
    isSaving: Boolean,
    isDeleting: Boolean,
    onDownloadClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SeniorOnColors.White)
            .navigationBarsPadding()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = Alignment.End
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FamilyPhotoDetailActionIcon(
            iconResId = R.drawable.ic_big_download,
            contentDescription = "사진 다운로드",
            enabled = !isSaving,
            onClick = onDownloadClick
        )

        if (showDelete) {
            FamilyPhotoDetailActionIcon(
                iconResId = R.drawable.ic_big_trash2,
                contentDescription = "사진 삭제",
                enabled = !isDeleting,
                onClick = onDeleteClick
            )
        }
    }
}

@Composable
private fun FamilyPhotoDetailActionIcon(
    iconResId: Int,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            modifier = Modifier.size(30.dp),
            tint = SeniorOnColors.Gray800
        )
    }
}

@Composable
private fun FamilyPhotoDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    FamilyConfirmDialog(
        iconResId = R.drawable.ic_modal_trash,
        title = "선택한 사진을\n삭제할까요?",
        confirmText = "삭제하기",
        confirmBackgroundColor = SeniorOnColors.Red400,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        dialogWidth = 238.dp,
        dialogHeight = 214.dp,
        verticalPadding = 24.dp,
        actionsTopSpacing = 26.dp,
    )
}

@Preview(
    name = "Family Photo Detail",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun FamilyPhotoDetailScreenPreview() {
    val photo = MockFamilyFixtures.primaryCaregiverOverview.sharedPhotos
        .first()
        .toFamilyPhotoUiModel()

    SENIOR_ONTheme {
        FamilyPhotoDetailScreen(
            uiState = FamilyPhotoDetailUiState(photo = photo),
            onBackClick = {}
        )
    }
}
