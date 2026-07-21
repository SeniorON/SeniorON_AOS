package com.example.senior_on.ui.family

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.example.senior_on.R
import com.example.senior_on.domain.model.FamilyImageSource
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
internal fun FamilyBackTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    centerTitle: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(SeniorOnColors.White),
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
                    onClick = onBackClick,
                ),
            tint = SeniorOnColors.Gray800,
        )

        Text(
            text = title,
            modifier = if (centerTitle) {
                Modifier.align(Alignment.Center)
            } else {
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 58.dp)
            },
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Gray800,
        )
    }
}

@Composable
internal fun FamilyActionButton(
    text: String,
    iconResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    outlined: Boolean = false,
    buttonHeight: Dp = 44.dp,
    expandWidth: Boolean = true,
    iconSpacing: Dp = 4.dp,
    cornerRadius: Dp = SeniorOnRadius.Small,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val backgroundColor = if (outlined) SeniorOnColors.White else SeniorOnColors.Primary600
    val contentColor = if (outlined) SeniorOnColors.Primary600 else SeniorOnColors.White

    Row(
        modifier = modifier
            .then(if (expandWidth) Modifier.fillMaxWidth() else Modifier)
            .height(buttonHeight)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(shape)
            .background(backgroundColor)
            .then(
                if (outlined) {
                    Modifier.border(1.dp, SeniorOnColors.Primary600, shape)
                } else {
                    Modifier
                },
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = contentColor,
        )
        Spacer(modifier = Modifier.width(iconSpacing))
        Text(
            text = text,
            style = SeniorOnTextStyles.ButtonM,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun FamilyTextActionButton(
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    enabled: Boolean = true,
    buttonHeight: Dp = 48.dp,
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)
    Box(
        modifier = modifier
            .height(buttonHeight)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(shape)
            .background(backgroundColor)
            .then(
                if (borderColor == null) {
                    Modifier
                } else {
                    Modifier.border(1.dp, borderColor, shape)
                },
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.ButtonS,
            color = contentColor,
        )
    }
}

@Composable
internal fun FamilyConfirmDialog(
    iconResId: Int,
    title: String,
    confirmText: String,
    confirmBackgroundColor: Color,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    dialogHeight: Dp,
    verticalPadding: Dp,
    description: String? = null,
    dialogWidth: Dp = 268.dp,
    actionsTopSpacing: Dp = if (description == null) 18.dp else 24.dp,
    showAsDialog: Boolean = true,
) {
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .width(dialogWidth)
                .height(dialogHeight)
                .clip(RoundedCornerShape(SeniorOnRadius.XLarge))
                .background(SeniorOnColors.White)
                .padding(horizontal = 28.dp, vertical = verticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                modifier = Modifier.size(38.dp),
                tint = Color.Unspecified,
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = title,
                style = SeniorOnTextStyles.BodyLBold,
                color = SeniorOnColors.Gray800,
                textAlign = TextAlign.Center,
            )
            description?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    style = SeniorOnTextStyles.CaptionRegular,
                    color = SeniorOnColors.Gray500,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(actionsTopSpacing))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FamilyTextActionButton(
                    text = "취소",
                    backgroundColor = SeniorOnColors.Gray200,
                    contentColor = SeniorOnColors.Gray500,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    buttonHeight = 34.dp,
                )
                FamilyTextActionButton(
                    text = confirmText,
                    backgroundColor = confirmBackgroundColor,
                    contentColor = SeniorOnColors.White,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    buttonHeight = 34.dp,
                )
            }
        }
    }

    if (showAsDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            content = content,
        )
    } else {
        content()
    }
}

@Composable
internal fun FamilyPhotoShareSuccessDialog(
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
                .background(SeniorOnColors.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .width(249.dp)
                    .height(264.dp)
                    .clip(RoundedCornerShape(SeniorOnRadius.XLarge))
                    .background(SeniorOnColors.White)
                    .padding(start = 18.dp, top = 34.dp, end = 18.dp, bottom = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_modal_check),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Color.Unspecified,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "공유 완료!",
                    style = SeniorOnTextStyles.HeadingS,
                    color = SeniorOnColors.Gray800,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "부모님 앱에 사진이 전달됐어요 🌿",
                    style = SeniorOnTextStyles.BodySMedium,
                    color = SeniorOnColors.Gray500,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(26.dp))

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FamilyPhotoSourceBottomSheet(
    onDismiss: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SeniorOnColors.White,
        scrimColor = SeniorOnColors.Black.copy(alpha = 0.2f),
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
                .height(160.dp)
                .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(26.dp))
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(32.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(SeniorOnRadius.XLarge))
                    .background(SeniorOnColors.Gray800),
            )
            Spacer(modifier = Modifier.height(20.dp))
            FamilyPhotoSourceOption(
                text = "내 갤러리에서 선택",
                iconResId = R.drawable.ic_photo_line,
                onClick = onGalleryClick,
            )
            Spacer(modifier = Modifier.height(16.dp))
            FamilyPhotoSourceOption(
                text = "사진 찍기",
                iconResId = R.drawable.ic_camera_line,
                onClick = onCameraClick,
            )
        }
    }
}

@Composable
private fun FamilyPhotoSourceOption(
    text: String,
    iconResId: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = SeniorOnColors.Gray800,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = SeniorOnTextStyles.BodyMMedium,
            color = SeniorOnColors.Gray800,
        )
    }
}

@Composable
internal fun FamilyLoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = SeniorOnColors.Primary600)
    }
}

@Composable
internal fun FamilyErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = SeniorOnTextStyles.BodyMMedium,
            color = SeniorOnColors.Gray500,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "다시 시도",
            modifier = Modifier.clickable(onClick = onRetryClick),
            style = SeniorOnTextStyles.BodyMSemiBold,
            color = SeniorOnColors.Primary600,
        )
    }
}

@Composable
internal fun FamilyMemberPreviewStack(
    members: List<FamilyMemberUiModel>,
    memberImage: @Composable BoxScope.(FamilyMemberUiModel) -> Unit,
) {
    val primaryMember = members.firstOrNull { member ->
        member.role == FamilyCaregiverRole.Primary
    }
    val previewMembers = if (primaryMember == null) {
        members.take(3)
    } else {
        members.filterNot { member -> member.id == primaryMember.id }.take(2) + primaryMember
    }
    val avatarSize = 45.dp
    val overlapSize = 28.dp
    val itemOffset = avatarSize - overlapSize

    Box(
        modifier = Modifier
            .width(avatarSize + itemOffset * (previewMembers.size - 1))
            .height(avatarSize),
    ) {
        previewMembers.forEachIndexed { index, member ->
            Box(
                modifier = Modifier
                    .offset(x = itemOffset * index)
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(SeniorOnColors.Background4)
                    .border(2.dp, SeniorOnColors.White, CircleShape),
            ) {
                memberImage(member)
            }
        }
    }
}

@Composable
internal fun SharedPhotoCard(
    photo: SharedFamilyPhotoUiModel,
    sharedPhotoImage: @Composable BoxScope.(SharedFamilyPhotoUiModel) -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .height(94.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .background(SeniorOnColors.Background4)
            .then(
                if (onClick == null) Modifier else Modifier.clickable(onClick = onClick),
            ),
    ) {
        sharedPhotoImage(photo)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(57.dp)
                .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SeniorOnColors.Black.copy(alpha = 0f),
                            SeniorOnColors.Black,
                        ),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 9.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = photo.authorName,
                style = SeniorOnTextStyles.BodySSemiBold,
                color = SeniorOnColors.White,
                maxLines = 1,
            )
            Text(
                text = photo.relativeTime,
                style = SeniorOnTextStyles.CaptionRegular,
                color = SeniorOnColors.White,
                maxLines = 1,
            )
        }
    }
}

@Composable
internal fun BoxScope.FamilyMemberImage(member: FamilyMemberUiModel) {
    when (val imageSource = member.imageSource) {
        is FamilyImageSource.Local -> Image(
            painter = painterResource(id = imageSource.drawableResId),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )
        is FamilyImageSource.Remote -> AsyncImage(
            model = imageSource.url,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_dependent),
        )
        is FamilyImageSource.Uri -> AsyncImage(
            model = imageSource.value,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_dependent),
        )
        null -> FamilyMemberImagePlaceholder()
    }
}

@Composable
internal fun BoxScope.SharedFamilyPhotoImage(photo: SharedFamilyPhotoUiModel) {
    when (val imageSource = photo.imageSource) {
        is FamilyImageSource.Local -> Image(
            painter = painterResource(id = imageSource.drawableResId),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )
        is FamilyImageSource.Remote -> AsyncImage(
            model = imageSource.url,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_photo),
        )
        is FamilyImageSource.Uri -> AsyncImage(
            model = imageSource.value,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_photo),
        )
        null -> SharedPhotoImagePlaceholder()
    }
}

@Composable
private fun BoxScope.FamilyMemberImagePlaceholder() {
    Icon(
        painter = painterResource(id = R.drawable.ic_dependent),
        contentDescription = null,
        modifier = Modifier
            .align(Alignment.Center)
            .size(38.dp),
        tint = SeniorOnColors.Gray400,
    )
}

@Composable
private fun BoxScope.SharedPhotoImagePlaceholder() {
    Icon(
        painter = painterResource(id = R.drawable.ic_photo),
        contentDescription = null,
        modifier = Modifier
            .align(Alignment.Center)
            .size(34.dp),
        tint = SeniorOnColors.Gray400,
    )
}
