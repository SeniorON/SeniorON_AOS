package com.example.senior_on.ui.family

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.senior_on.R
import com.example.senior_on.data.model.MockFamilyFixtures
import com.example.senior_on.ui.child.ChildBottomNavigation
import com.example.senior_on.ui.child.ChildMainTab
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnBrushes
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

private val RegularFamilyAvatarSize = 90.dp
private val PrimaryFamilyAvatarSize = 120.dp
private val RegularFamilyMemberMinHeight = 141.dp
private val PrimaryFamilyMemberMinHeight = 174.dp
private val FamilyBackgroundTopSpacing = 44.dp
private const val FamilyBackgroundAspectRatio = 1170f / 690f
private const val FamilySharePictureAspectRatio = 984f / 600f

@Composable
fun FamilyTabScreen(
    modifier: Modifier = Modifier,
    uiState: FamilyTabUiState = FamilyTabUiState(),
    onMemberSettingsClick: () -> Unit = {},
    onAddFamilyClick: () -> Unit = {},
    onInviteFamilyClick: () -> Unit = {},
    onMorePhotosClick: () -> Unit = {},
    onUploadPhotoClick: () -> Unit = {},
    onPhotoClick: (String) -> Unit = {},
    onRetryClick: () -> Unit = {},
    memberImage: @Composable BoxScope.(FamilyMemberUiModel) -> Unit = {
        FamilyMemberImage(it)
    },
    sharedPhotoImage: @Composable BoxScope.(SharedFamilyPhotoUiModel) -> Unit = {
        SharedFamilyPhotoImage(it)
    }
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
    ) {
        FamilyTopBar()

        if (uiState.isLoading && uiState.members.isEmpty()) {
            FamilyLoadingContent(modifier = Modifier.weight(1f))
        } else if (uiState.errorMessage != null && uiState.members.isEmpty()) {
            FamilyErrorContent(
                message = uiState.errorMessage,
                onRetryClick = onRetryClick,
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
        ) {
            item {
                FamilyOverviewSection(
                    members = uiState.visibleMembers,
                    invitationSlotCount = uiState.invitationSlotCount,
                    onInviteClick = onInviteFamilyClick,
                    memberImage = memberImage
                )
            }

            item {
                Spacer(
                    modifier = Modifier.height(
                        if (uiState.canManageMembers) 30.dp else 16.dp
                    )
                )
                FamilyManagementButtons(
                    canManageMembers = uiState.canManageMembers,
                    onMemberSettingsClick = onMemberSettingsClick,
                    onAddFamilyClick = onAddFamilyClick
                )
            }

            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 34.dp, vertical = 24.dp),
                    thickness = 2.dp,
                    color = SeniorOnColors.Background4
                )
            }

            item {
                SharedPhotoHeader(
                    members = uiState.visibleMembers,
                    memberImage = memberImage
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                if (uiState.visibleSharedPhotos.isEmpty()) {
                    EmptySharedPhotoCard()
                } else {
                    SharedPhotoGrid(
                        photos = uiState.visibleSharedPhotos,
                        sharedPhotoImage = sharedPhotoImage,
                        onPhotoClick = onPhotoClick
                    )

                    if (uiState.shouldShowMorePhotos) {
                        Spacer(modifier = Modifier.height(2.dp))
                        MorePhotosButton(onClick = onMorePhotosClick)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                FamilyActionButton(
                    text = "사진 올리기",
                    iconResId = R.drawable.ic_plus,
                    onClick = onUploadPhotoClick,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    buttonHeight = 48.dp
                )
            }
            }
        }
    }
}

@Composable
private fun FamilyTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f)
            .dropShadow(
                shape = RectangleShape,
                shadow = Shadow(
                    radius = 12.dp,
                    spread = 0.dp,
                    color = SeniorOnColors.Black.copy(alpha = 0.06f),
                    offset = DpOffset(x = 0.dp, y = 4.dp)
                )
            )
            .background(SeniorOnColors.White)
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "가족",
                style = SeniorOnTextStyles.HeadingM,
                color = SeniorOnColors.Gray800
            )
        }
    }
}

@Composable
private fun FamilyOverviewSection(
    members: List<FamilyMemberUiModel>,
    invitationSlotCount: Int,
    onInviteClick: () -> Unit,
    memberImage: @Composable BoxScope.(FamilyMemberUiModel) -> Unit
) {
    val backgroundPainter = painterResource(id = R.drawable.bg_family)
    var memberRowTop by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val backgroundHeight = size.width / FamilyBackgroundAspectRatio

                translate(top = memberRowTop - FamilyBackgroundTopSpacing.toPx()) {
                    with(backgroundPainter) {
                        draw(
                            size = Size(
                                width = size.width,
                                height = backgroundHeight
                            )
                        )
                    }
                }
            }
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        FamilySectionIntroduction()

        Spacer(modifier = Modifier.height(34.dp))

        FamilyMemberRow(
            members = members,
            invitationSlotCount = invitationSlotCount,
            onInviteClick = onInviteClick,
            memberImage = memberImage,
            modifier = Modifier.onGloballyPositioned { coordinates ->
                memberRowTop = coordinates.positionInParent().y
            }
        )
    }
}

@Composable
private fun FamilySectionIntroduction() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_big_family),
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                tint = SeniorOnColors.Primary600
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "함께 하는 가족",
                style = SeniorOnTextStyles.HeadingS,
                color = SeniorOnColors.Gray800
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = "함께 도와드릴 가족을 연결할 수 있어요",
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray500
        )
    }
}

@Composable
private fun FamilyMemberRow(
    members: List<FamilyMemberUiModel>,
    invitationSlotCount: Int,
    onInviteClick: () -> Unit,
    memberImage: @Composable BoxScope.(FamilyMemberUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val horizontalPadding = 16.dp
        val initialSlotWidths = buildList {
            members.take(3).forEach { member ->
                add(
                    if (member.role == FamilyCaregiverRole.Primary) {
                        PrimaryFamilyAvatarSize
                    } else {
                        RegularFamilyAvatarSize
                    }
                )
            }
            repeat(invitationSlotCount) {
                add(RegularFamilyAvatarSize)
            }
        }
        val initialProfilesWidth = initialSlotWidths.fold(0.dp) { total, width ->
            total + width
        }
        val memberSpacing = if (initialSlotWidths.size > 1) {
            (
                (maxWidth - horizontalPadding * 2 - initialProfilesWidth) /
                    (initialSlotWidths.size - 1).toFloat()
            ).coerceAtLeast(0.dp)
        } else {
            0.dp
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(memberSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(
                count = members.size,
                key = { index -> members[index].id }
            ) { index ->
                FamilyMemberItem(
                    member = members[index],
                    memberImage = memberImage
                )
            }

            items(
                count = invitationSlotCount,
                key = { index -> "family-invitation-$index" }
            ) {
                FamilyInvitationItem(onClick = onInviteClick)
            }
        }
    }
}

@Composable
private fun FamilyMemberItem(
    member: FamilyMemberUiModel,
    memberImage: @Composable BoxScope.(FamilyMemberUiModel) -> Unit
) {
    val isPrimary = member.role == FamilyCaregiverRole.Primary
    val avatarSize = if (isPrimary) PrimaryFamilyAvatarSize else RegularFamilyAvatarSize
    val memberMinHeight = if (isPrimary) {
        PrimaryFamilyMemberMinHeight
    } else {
        RegularFamilyMemberMinHeight
    }

    Column(
        modifier = Modifier
            .width(avatarSize)
            .heightIn(min = memberMinHeight),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(avatarSize),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .shadow(
                        elevation = 14.dp,
                        shape = CircleShape,
                        clip = false,
                        ambientColor = SeniorOnColors.Black.copy(alpha = 0.11f),
                        spotColor = SeniorOnColors.Black.copy(alpha = 0.11f)
                    )
                    .clip(CircleShape)
                    .background(SeniorOnColors.Background4)
                    .then(
                        if (isPrimary) {
                            Modifier.border(
                                width = 4.dp,
                                brush = SeniorOnBrushes.FamilyPrimaryBorder,
                                shape = CircleShape
                            )
                        } else {
                            Modifier.border(
                                width = 2.dp,
                                color = SeniorOnColors.White,
                                shape = CircleShape
                            )
                        }
                    )
            ) {
                memberImage(member)
            }

            if (member.isCurrentUser) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .clip(RoundedCornerShape(39.dp))
                        .background(SeniorOnColors.Primary600)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "나",
                        style = SeniorOnTextStyles.CaptionMedium,
                        color = SeniorOnColors.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = member.name,
            modifier = Modifier.fillMaxWidth(),
            style = SeniorOnTextStyles.BodyMSemiBold,
            color = SeniorOnColors.Gray800,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = member.role.label,
            modifier = Modifier.fillMaxWidth(),
            style = SeniorOnTextStyles.CaptionMedium,
            color = if (isPrimary) SeniorOnColors.Primary600 else SeniorOnColors.Gray500,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FamilyInvitationItem(
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .width(RegularFamilyAvatarSize)
            .heightIn(min = RegularFamilyMemberMinHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(RegularFamilyAvatarSize),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(RegularFamilyAvatarSize)
                    .drawBehind {
                        drawCircle(
                            color = SeniorOnColors.Gray300,
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(
                                    intervals = floatArrayOf(5.dp.toPx(), 5.dp.toPx())
                                )
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_plus),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = SeniorOnColors.Gray300
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "초대",
            style = SeniorOnTextStyles.BodySRegular,
            color = SeniorOnColors.Gray400
        )
    }
}

@Composable
private fun FamilyManagementButtons(
    canManageMembers: Boolean,
    onMemberSettingsClick: () -> Unit,
    onAddFamilyClick: () -> Unit
) {
    if (canManageMembers) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FamilyActionButton(
                text = "구성원 설정",
                iconResId = R.drawable.ic_setting,
                onClick = onMemberSettingsClick,
                modifier = Modifier.weight(1f)
            )

            FamilyActionButton(
                text = "가족 추가",
                iconResId = R.drawable.ic_plus,
                onClick = onAddFamilyClick,
                modifier = Modifier.weight(1f),
                outlined = true
            )
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            FamilyActionButton(
                text = "가족 추가",
                iconResId = R.drawable.ic_plus,
                onClick = onAddFamilyClick,
                outlined = true,
                buttonHeight = 36.dp,
                expandWidth = false,
                cornerRadius = 22.dp,
            )
        }
    }
}

@Composable
private fun SharedPhotoHeader(
    members: List<FamilyMemberUiModel>,
    memberImage: @Composable BoxScope.(FamilyMemberUiModel) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "가족 사진",
                    style = SeniorOnTextStyles.HeadingS,
                    color = SeniorOnColors.Primary700
                )

                Text(
                    text = " 공유",
                    style = SeniorOnTextStyles.HeadingS,
                    color = SeniorOnColors.Gray800
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "공유한 사진이 부모님 화면에 표시돼요",
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray500
            )
        }

        if (members.isNotEmpty()) {
            FamilyMemberPreviewStack(
                members = members,
                memberImage = memberImage
            )
        }
    }
}

@Composable
private fun EmptySharedPhotoCard() {
    Image(
        painter = painterResource(id = R.drawable.img_family_share_picture),
        contentDescription = null,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .aspectRatio(FamilySharePictureAspectRatio)
            .clip(RoundedCornerShape(SeniorOnRadius.Large)),
        contentScale = ContentScale.FillBounds
    )
}

@Composable
private fun SharedPhotoGrid(
    photos: List<SharedFamilyPhotoUiModel>,
    sharedPhotoImage: @Composable BoxScope.(SharedFamilyPhotoUiModel) -> Unit,
    onPhotoClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        photos.chunked(2).forEach { rowPhotos ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowPhotos.forEach { photo ->
                    SharedPhotoCard(
                        photo = photo,
                        sharedPhotoImage = sharedPhotoImage,
                        modifier = Modifier.weight(1f),
                        onClick = { onPhotoClick(photo.id) }
                    )
                }

                if (rowPhotos.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MorePhotosButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "사진 더 보기",
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray700
        )

        Spacer(modifier = Modifier.width(5.dp))

        Icon(
            painter = painterResource(id = R.drawable.ic_sm_arrow_right),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = SeniorOnColors.Gray500
        )
    }
}

@Preview(
    name = "Family Tab Primary Mock",
    showBackground = true,
    widthDp = 360,
    heightDp = 888
)
@Composable
private fun FamilyTabScreenPreview() {
    val uiState = MockFamilyFixtures.primaryCaregiverOverview.toFamilyTabUiState()

    SENIOR_ONTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Background2)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                FamilyTabScreen(uiState = uiState)
            }

            ChildBottomNavigation(
                selectedTab = ChildMainTab.Family,
                onTabClick = {}
            )
        }
    }
}
