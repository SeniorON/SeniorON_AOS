package com.example.senior_on.ui.family

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.data.model.MockFamilyFixtures
import com.example.senior_on.ui.child.ChildBottomNavigation
import com.example.senior_on.ui.child.ChildMainTab
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

private val PhotoCardHeight = 94.dp

@Composable
fun FamilyPhotoGalleryScreen(
    uiState: FamilyTabUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onGalleryClick: () -> Unit = {},
    onCameraClick: () -> Unit = {},
    onPhotoClick: (String) -> Unit = {},
    onRetryClick: () -> Unit = {},
    memberImage: @Composable BoxScope.(FamilyMemberUiModel) -> Unit = {
        FamilyMemberImage(it)
    },
    sharedPhotoImage: @Composable BoxScope.(SharedFamilyPhotoUiModel) -> Unit = {
        SharedFamilyPhotoImage(it)
    }
) {
    var isPhotoSourceSheetVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.Background1)
            .statusBarsPadding()
    ) {
        FamilyBackTopAppBar(
            title = "사진 더보기",
            onBackClick = onBackClick,
        )

        if (uiState.isLoading && uiState.members.isEmpty()) {
            FamilyLoadingContent(modifier = Modifier.weight(1f))
        } else if (uiState.errorMessage != null && uiState.members.isEmpty()) {
            FamilyErrorContent(
                message = uiState.errorMessage,
                onRetryClick = onRetryClick,
                modifier = Modifier.weight(1f),
            )
        } else {
            FamilyPhotoGalleryHeader(
                members = uiState.visibleMembers,
                memberImage = memberImage
            )

            LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 12.dp,
                end = 16.dp,
                bottom = 24.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            item(key = "add-family-photo") {
                AddFamilyPhotoCard(
                    onClick = { isPhotoSourceSheetVisible = true }
                )
            }

            items(
                items = uiState.sharedPhotos,
                key = SharedFamilyPhotoUiModel::id
            ) { photo ->
                SharedPhotoCard(
                    photo = photo,
                    sharedPhotoImage = sharedPhotoImage,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onPhotoClick(photo.id) }
                )
            }
            }
        }
    }

    if (isPhotoSourceSheetVisible) {
        FamilyPhotoSourceBottomSheet(
            onDismiss = { isPhotoSourceSheetVisible = false },
            onGalleryClick = {
                isPhotoSourceSheetVisible = false
                onGalleryClick()
            },
            onCameraClick = {
                isPhotoSourceSheetVisible = false
                onCameraClick()
            }
        )
    }
}

@Composable
private fun FamilyPhotoGalleryHeader(
    members: List<FamilyMemberUiModel>,
    memberImage: @Composable BoxScope.(FamilyMemberUiModel) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 24.dp, end = 16.dp),
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
private fun AddFamilyPhotoCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(PhotoCardHeight)
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .background(SeniorOnColors.Background4)
            .clickable(onClick = onClick)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_big_camera),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(48.dp),
            tint = SeniorOnColors.Gray400
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 7.dp, bottom = 7.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(SeniorOnColors.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_plus),
                contentDescription = "사진 추가",
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.Gray500
            )
        }
    }
}

@Preview(
    name = "Family Photo Gallery",
    showBackground = true,
    widthDp = 360,
    heightDp = 888
)
@Composable
private fun FamilyPhotoGalleryScreenPreview() {
    val uiState = MockFamilyFixtures.primaryCaregiverOverview.toFamilyTabUiState()

    SENIOR_ONTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Background2)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                FamilyPhotoGalleryScreen(
                    uiState = uiState,
                    onBackClick = {}
                )
            }

            ChildBottomNavigation(
                selectedTab = ChildMainTab.Family,
                onTabClick = {}
            )
        }
    }
}
