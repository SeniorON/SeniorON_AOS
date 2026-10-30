package com.example.senior_on.ui.child.family

import com.example.senior_on.ui.theme.SeniorOnDimensions
import com.example.senior_on.ui.child.family.viewmodel.toFamilyPhotoUiModel

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.data.source.mock.fixtures.MockFamilyFixtures
import com.example.senior_on.ui.common.component.SeniorOnLoadingIndicator
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun FamilyPhotoDetailScreen(
    uiState: FamilyPhotoDetailUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    photos: List<SharedFamilyPhotoUiModel> = listOfNotNull(uiState.photo),
    initialPhotoId: String? = uiState.photo?.id,
    onDisplayedPhotoChange: (SharedFamilyPhotoUiModel) -> Unit = {},
    onLoadMore: () -> Unit = {},
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
    val availablePhotos = remember(photos, uiState.photo) {
        (photos + listOfNotNull(uiState.photo))
            .distinctBy(SharedFamilyPhotoUiModel::id)
    }
    val initialPage = remember(initialPhotoId, availablePhotos) {
        availablePhotos.indexOfFirst { it.id == initialPhotoId }
            .coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { availablePhotos.size },
    )
    var initialPhotoPositioned by remember(initialPhotoId) { mutableStateOf(false) }
    val photo = availablePhotos.getOrNull(pagerState.currentPage) ?: uiState.photo
    var pagerScrollEnabled by remember(photo?.id) { mutableStateOf(true) }

    LaunchedEffect(initialPhotoId, availablePhotos) {
        if (!initialPhotoPositioned) {
            val page = availablePhotos.indexOfFirst { it.id == initialPhotoId }
            if (page >= 0) {
                pagerState.scrollToPage(page)
                initialPhotoPositioned = true
            }
        }
    }

    LaunchedEffect(photo?.id) {
        photo?.let(onDisplayedPhotoChange)
        if (photo != null && pagerState.currentPage >= availablePhotos.lastIndex - 1) {
            onLoadMore()
        }
    }

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
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = pagerScrollEnabled,
                    key = { page -> availablePhotos[page].id },
                ) { page ->
                    val pagePhoto = availablePhotos[page]
                    ZoomableFamilyPhoto(
                        photo = pagePhoto,
                        onZoomChanged = { isZoomed ->
                            if (page == pagerState.currentPage) {
                                pagerScrollEnabled = !isZoomed
                            }
                        },
                        sharedPhotoImage = sharedPhotoImage,
                    )
                }

                if (availablePhotos.size > 1) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${availablePhotos.size}",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 14.dp)
                            .background(
                                color = SeniorOnColors.Black.copy(alpha = 0.55f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                            )
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        style = SeniorOnTextStyles.CaptionMedium,
                        color = SeniorOnColors.White,
                    )
                }

                if (isSaveSuccessVisible) {
                    Image(
                        painter = painterResource(
                            id = R.drawable.ic_family_photo_save_success,
                        ),
                        contentDescription = "사진 저장 완료",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(82.dp),
                    )
                }
            }

            FamilyPhotoDetailActions(
                showDelete = photo.canDelete,
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
private fun ZoomableFamilyPhoto(
    photo: SharedFamilyPhotoUiModel,
    onZoomChanged: (Boolean) -> Unit,
    sharedPhotoImage: @Composable BoxScope.(SharedFamilyPhotoUiModel) -> Unit,
) {
    var scale by remember(photo.id) { mutableFloatStateOf(1f) }
    var translation by remember(photo.id) { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = translation.x
                    translationY = translation.y
                }
                .pointerInput(photo.id) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        var pointersPressed: Boolean
                        do {
                            val event = awaitPointerEvent()
                            val isPinching = event.changes.size >= 2
                            if (isPinching || scale > 1f) {
                                val newScale = if (isPinching) {
                                    (scale * event.calculateZoom()).coerceIn(1f, 5f)
                                } else {
                                    scale
                                }
                                val maxTranslationX = size.width * (newScale - 1f) / 2f
                                val maxTranslationY = size.height * (newScale - 1f) / 2f
                                val pan = event.calculatePan()
                                scale = newScale
                                translation = if (newScale == 1f) {
                                    Offset.Zero
                                } else {
                                    Offset(
                                        x = (translation.x + pan.x).coerceIn(
                                            -maxTranslationX,
                                            maxTranslationX,
                                        ),
                                        y = (translation.y + pan.y).coerceIn(
                                            -maxTranslationY,
                                            maxTranslationY,
                                        ),
                                    )
                                }
                                onZoomChanged(newScale > 1f)
                                event.changes.forEach { it.consume() }
                            }
                            pointersPressed = event.changes.any { it.pressed }
                        } while (pointersPressed)
                    }
                },
        ) {
            sharedPhotoImage(photo)
        }
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
            .height(SeniorOnDimensions.TopBarHeight)
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
            isLoading = isSaving,
            onClick = onDownloadClick
        )

        if (showDelete) {
            FamilyPhotoDetailActionIcon(
                iconResId = R.drawable.ic_big_trash2,
                contentDescription = "사진 삭제",
                enabled = !isDeleting,
                isLoading = isDeleting,
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
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .alpha(if (enabled || isLoading) 1f else 0.5f)
            .clickable(
                enabled = enabled && !isLoading,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            SeniorOnLoadingIndicator(
                color = SeniorOnColors.Gray800,
                size = 22.dp,
            )
        } else {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = contentDescription,
                modifier = Modifier.size(30.dp),
                tint = SeniorOnColors.Gray800
            )
        }
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
