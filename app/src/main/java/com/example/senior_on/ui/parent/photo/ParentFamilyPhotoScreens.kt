package com.example.senior_on.ui.parent.photo

import com.example.senior_on.ui.parent.photo.viewmodel.ParentFamilyPhotoUiState

import com.example.senior_on.ui.parent.photo.viewmodel.ParentPhotoMemberUiModel

import com.example.senior_on.ui.parent.photo.viewmodel.ParentFamilyPhotoUiModel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.senior_on.R
import com.example.senior_on.data.source.mock.fixtures.MockFamilyFixtures
import com.example.senior_on.data.source.mock.fixtures.MockUserFixtures
import com.example.senior_on.domain.model.family.FamilyImageSource
import com.example.senior_on.ui.parent.component.ParentDetailTopBar
import com.example.senior_on.ui.parent.component.parentCardShadow
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun ParentFamilyMembersPhotoScreen(
    uiState: ParentFamilyPhotoUiState,
    onBackClick: () -> Unit,
    onMemberClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .safeDrawingPadding()
    ) {
        ParentDetailTopBar(
            title = "가족 사진",
            onBackClick = onBackClick
        )

        when {
            uiState.isLoading -> ParentPhotoLoading()
            uiState.errorMessage != null -> ParentPhotoError(
                message = uiState.errorMessage,
                onRetryClick = onRetryClick
            )
            uiState.members.isEmpty() -> ParentPhotoEmpty(
                message = "가족이 보낸 사진이 아직 없어요"
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 30.dp,
                    vertical = 36.dp
                ),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(
                    items = uiState.members,
                    key = ParentPhotoMemberUiModel::memberId
                ) { member ->
                    ParentPhotoMemberCard(
                        member = member,
                        onClick = { onMemberClick(member.memberId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ParentPhotoMemberCard(
    member: ParentPhotoMemberUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latestPhoto = member.latestPhoto ?: return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(300f / 294f)
            .parentCardShadow(
                shape = RoundedCornerShape(SeniorOnRadius.Large),
                radius = 14,
                offsetY = 14,
                color = Color.Black.copy(alpha = 0x38 / 255f)
            )
            .clip(RoundedCornerShape(SeniorOnRadius.Large))
            .background(SeniorOnColors.Background4)
            .clickable(onClick = onClick)
    ) {
        ParentPhotoImage(
            imageSource = latestPhoto.imageSource,
            contentDescription = "${member.memberName}님의 최근 사진",
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.45f to Color.Transparent,
                            1f to SeniorOnColors.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 23.dp, bottom = 21.dp)
        ) {
            Text(
                text = member.memberName,
                style = SeniorOnTextStyles.HeadingXXXL,
                color = SeniorOnColors.SupportWhite100
            )
            Text(
                text = "사진 ${member.photos.size}장",
                style = SeniorOnTextStyles.HeadingXS,
                color = SeniorOnColors.SupportWhite100
            )
        }

        if (latestPhoto.isNew) {
            Text(
                text = "새로운 사진",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 21.dp)
                    .background(
                        color = SeniorOnColors.SupportWhite20,
                        shape = RoundedCornerShape(37.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                style = SeniorOnTextStyles.HeadingXS,
                color = SeniorOnColors.SupportWhite100
            )
        }
    }
}

@Composable
fun ParentMemberPhotoGridScreen(
    member: ParentPhotoMemberUiModel,
    onBackClick: () -> Unit,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .safeDrawingPadding()
    ) {
        ParentDetailTopBar(
            title = member.memberName,
            onBackClick = onBackClick
        )

        Text(
            text = "전체 사진",
            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp),
            style = SeniorOnTextStyles.HeadingL,
            color = SeniorOnColors.Gray800
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 20.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = member.photos,
                key = ParentFamilyPhotoUiModel::id
            ) { photo ->
                ParentPhotoGridItem(
                    photo = photo,
                    onClick = { onPhotoClick(photo.id) }
                )
            }
        }
    }
}

@Composable
private fun ParentPhotoGridItem(
    photo: ParentFamilyPhotoUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .clickable(onClick = onClick)
    ) {
        ParentPhotoImage(
            imageSource = photo.imageSource,
            contentDescription = "${photo.memberName}님이 올린 사진",
            modifier = Modifier.fillMaxSize()
        )

        if (photo.isNew) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(93.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                SeniorOnColors.Black
                            )
                        )
                    )
            )
            Text(
                text = "새로운 사진",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp),
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.White
            )
        }
    }
}

@Composable
fun ParentPhotoViewerScreen(
    member: ParentPhotoMemberUiModel,
    initialPhotoId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initialPage = remember(initialPhotoId, member.photos) {
        member.photos.indexOfFirst { it.id == initialPhotoId }
            .coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { member.photos.size }
    )
    val currentPhoto = member.photos.getOrNull(pagerState.currentPage)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.Background2)
            .safeDrawingPadding()
    ) {
        ParentDetailTopBar(
            title = "",
            onBackClick = onBackClick
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            val photoWidth = maxWidth - 64.dp
            val photoHeight = photoWidth * (380f / 295f)
            val photoShape = RoundedCornerShape(20.dp)

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(photoHeight),
                pageSpacing = 32.dp,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 32.dp
                )
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .parentCardShadow(
                            shape = photoShape,
                            radius = 20,
                            offsetY = 14,
                            color = Color.Black.copy(alpha = 0x38 / 255f)
                        )
                        .clip(photoShape)
                        .border(
                            width = 3.dp,
                            color = SeniorOnColors.SupportWhite100,
                            shape = photoShape
                        )
                ) {
                    ParentPhotoImage(
                        imageSource = member.photos[page].imageSource,
                        contentDescription = "${member.memberName}님의 사진 ${page + 1}",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .width(90.dp)
                    .height(12.dp)
                    .clipToBounds()
            ) {
                val pagePosition =
                    pagerState.currentPage + pagerState.currentPageOffsetFraction
                val slotWidth = 20.dp.toPx()
                val dotRadius = 6.dp.toPx()
                val centerX = size.width / 2f
                val centerY = size.height / 2f

                member.photos.indices.forEach { pageIndex ->
                    val relativePosition = pageIndex - pagePosition
                    val distanceFromCenter = kotlin.math.abs(relativePosition)
                    val alpha = when {
                        distanceFromCenter <= 1f -> 1f
                        distanceFromCenter >= 2f -> 0.45f
                        else -> 1f - ((distanceFromCenter - 1f) * 0.55f)
                    }
                    val selectedFraction =
                        (1f - distanceFromCenter).coerceIn(0f, 1f)
                    val dotColor = androidx.compose.ui.graphics.lerp(
                        SeniorOnColors.Gray200.copy(alpha = alpha),
                        SeniorOnColors.Primary600,
                        selectedFraction
                    )

                    drawCircle(
                        color = dotColor,
                        radius = dotRadius,
                        center = androidx.compose.ui.geometry.Offset(
                            x = centerX + (relativePosition * slotWidth),
                            y = centerY
                        )
                    )
                }
            }
        }

        currentPhoto?.let { photo ->
            if (photo.message.isBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, top = 24.dp, end = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = photo.memberName,
                        modifier = Modifier.fillMaxWidth(),
                        style = SeniorOnTextStyles.HeadingXXL,
                        color = SeniorOnColors.Gray800,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = photo.uploadedAt.toParentPhotoDisplayTime(),
                        modifier = Modifier.fillMaxWidth(),
                        style = SeniorOnTextStyles.HeadingXS,
                        color = SeniorOnColors.Gray600,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                ) {
                    Text(
                        text = photo.memberName,
                        style = SeniorOnTextStyles.HeadingL,
                        color = SeniorOnColors.Gray600
                    )
                    Text(
                        text = photo.uploadedAt.toParentPhotoDisplayTime(),
                        style = SeniorOnTextStyles.HeadingXS,
                        color = SeniorOnColors.Gray600
                    )
                    Text(
                        text = photo.message,
                        modifier = Modifier.padding(top = 12.dp),
                        style = SeniorOnTextStyles.HeadingXXXL,
                        color = SeniorOnColors.Gray800
                    )
                }
            }
        }
    }
}

@Composable
private fun ParentPhotoImage(
    imageSource: FamilyImageSource,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    when (imageSource) {
        is FamilyImageSource.Local -> Image(
            painter = painterResource(imageSource.drawableResId),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
        is FamilyImageSource.Remote -> AsyncImage(
            model = imageSource.url,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.ic_photo)
        )
        is FamilyImageSource.Uri -> AsyncImage(
            model = imageSource.value,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.ic_photo)
        )
    }
}

@Composable
private fun ParentPhotoLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = SeniorOnColors.Primary600)
    }
}

@Composable
private fun ParentPhotoError(
    message: String,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = SeniorOnTextStyles.BodyLMedium,
            color = SeniorOnColors.Gray500
        )
        Text(
            text = "다시 시도",
            modifier = Modifier
                .padding(top = 12.dp)
                .clickable(onClick = onRetryClick),
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Primary600
        )
    }
}

@Composable
private fun ParentPhotoEmpty(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = SeniorOnTextStyles.BodyLMedium,
            color = SeniorOnColors.Gray400
        )
    }
}

private fun Instant.toParentPhotoDisplayTime(): String {
    val dateTime = atZone(ZoneId.systemDefault())
    val datePrefix = if (dateTime.toLocalDate() == LocalDate.now()) {
        "오늘"
    } else {
        "${dateTime.monthValue}월 ${dateTime.dayOfMonth}일"
    }
    val period = if (dateTime.hour < 12) "오전" else "오후"
    val hour = (dateTime.hour % 12).let { if (it == 0) 12 else it }
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "$datePrefix $period $hour:$minute"
}

private fun parentPhotoPreviewMember(
    memberId: String = MockFamilyFixtures.SECONDARY_ASSISTANT_MEMBER_ID,
    memberName: String = MockUserFixtures.secondaryCaregiver.name,
    usePrimaryImageFirst: Boolean = true
): ParentPhotoMemberUiModel {
    val now = Instant.now()
    val primaryImage = FamilyImageSource.Local(R.drawable.img_mock_family_primary)
    val assistantImage = FamilyImageSource.Local(R.drawable.img_mock_family_assistant)

    return ParentPhotoMemberUiModel(
        memberId = memberId,
        memberName = memberName,
        photos = List(10) { index ->
            val photoNumber = index + 1
            ParentFamilyPhotoUiModel(
                id = "$memberId-photo-$photoNumber",
                memberId = memberId,
                memberName = memberName,
                uploadedAt = now.minusSeconds(index * 12L * 60L * 60L),
                imageSource = if ((index % 2 == 0) == usePrimaryImageFirst) {
                    primaryImage
                } else {
                    assistantImage
                },
                message = when (index) {
                    0 -> "오늘도 행복한 하루 보내고 맛있는 거 많이 먹고 쉬세요"
                    1 -> "나 보드게임 카페야"
                    4 -> "주말에 다녀온 곳이에요"
                    else -> ""
                },
                isNew = index < 2
            )
        }
    )
}

@Preview(
    name = "Family Photos - Content",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Composable
private fun ParentFamilyMembersPhotoContentPreview() {
    SENIOR_ONTheme {
        ParentFamilyMembersPhotoScreen(
            uiState = ParentFamilyPhotoUiState(
                members = listOf(
                    parentPhotoPreviewMember(),
                    parentPhotoPreviewMember(
                        memberId = MockFamilyFixtures.PRIMARY_MEMBER_ID,
                        memberName = MockUserFixtures.primaryCaregiver.name,
                        usePrimaryImageFirst = false
                    )
                ),
                isLoading = false
            ),
            onBackClick = {},
            onMemberClick = {},
            onRetryClick = {}
        )
    }
}

@Preview(
    name = "Family Photos - Loading",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Composable
private fun ParentFamilyMembersPhotoLoadingPreview() {
    SENIOR_ONTheme {
        ParentFamilyMembersPhotoScreen(
            uiState = ParentFamilyPhotoUiState(isLoading = true),
            onBackClick = {},
            onMemberClick = {},
            onRetryClick = {}
        )
    }
}

@Preview(
    name = "Family Photos - Empty",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Composable
private fun ParentFamilyMembersPhotoEmptyPreview() {
    SENIOR_ONTheme {
        ParentFamilyMembersPhotoScreen(
            uiState = ParentFamilyPhotoUiState(isLoading = false),
            onBackClick = {},
            onMemberClick = {},
            onRetryClick = {}
        )
    }
}

@Preview(
    name = "Family Photos - Error",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Composable
private fun ParentFamilyMembersPhotoErrorPreview() {
    SENIOR_ONTheme {
        ParentFamilyMembersPhotoScreen(
            uiState = ParentFamilyPhotoUiState(
                isLoading = false,
                errorMessage = "가족사진을 불러오지 못했어요."
            ),
            onBackClick = {},
            onMemberClick = {},
            onRetryClick = {}
        )
    }
}

@Preview(
    name = "Member Photo Grid",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Composable
private fun ParentMemberPhotoGridPreview() {
    SENIOR_ONTheme {
        ParentMemberPhotoGridScreen(
            member = parentPhotoPreviewMember(),
            onBackClick = {},
            onPhotoClick = {}
        )
    }
}

@Preview(
    name = "Photo Viewer",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Composable
private fun ParentPhotoViewerPreview() {
    val member = parentPhotoPreviewMember()
    SENIOR_ONTheme {
        ParentPhotoViewerScreen(
            member = member,
            initialPhotoId = member.photos.first().id,
            onBackClick = {}
        )
    }
}
