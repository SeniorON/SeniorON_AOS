package com.example.senior_on.ui.parent.photo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

private val ParentPhotoSheetShape = RoundedCornerShape(
    topStart = 28.dp,
    topEnd = 28.dp
)

private fun Modifier.parentPhotoSheetShadow(): Modifier =
    dropShadow(
        shape = ParentPhotoSheetShape,
        shadow = Shadow(
            radius = 8.dp,
            spread = 3.dp,
            color = Color.Black.copy(alpha = 0x26 / 255f),
            offset = DpOffset(x = 0.dp, y = 4.dp)
        )
    ).dropShadow(
        shape = ParentPhotoSheetShape,
        shadow = Shadow(
            radius = 3.dp,
            spread = 0.dp,
            color = Color.Black.copy(alpha = 0x4D / 255f),
            offset = DpOffset(x = 0.dp, y = 1.dp)
        )
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentPhotoSourceBottomSheet(
    onDismiss: () -> Unit,
    onFamilyPhotosClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        scrimColor = SeniorOnColors.Black.copy(alpha = 0.50f),
        tonalElevation = 0.dp,
        shape = RectangleShape,
        dragHandle = null
    ) {
        ParentPhotoSheetSurface(
            onFamilyPhotosClick = onFamilyPhotosClick,
            onGalleryClick = onGalleryClick
        )
    }
}

@Composable
private fun ParentPhotoSheetSurface(
    onFamilyPhotosClick: () -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            // 그림자가 시트 영역 밖에서 잘리지 않도록 투명 영역을 확보한다.
            .padding(top = 12.dp)
            .parentPhotoSheetShadow()
            .background(
                color = SeniorOnColors.White,
                shape = ParentPhotoSheetShape
            )
    ) {
        ParentPhotoSheetDragHandle()
        ParentPhotoSourceContent(
            onFamilyPhotosClick = onFamilyPhotosClick,
            onGalleryClick = onGalleryClick
        )
    }
}

@Composable
private fun ParentPhotoSheetDragHandle(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(4.dp)
                .background(
                    color = SeniorOnColors.Gray800,
                    shape = RoundedCornerShape(100.dp)
                )
        )
    }
}

@Composable
private fun ParentPhotoSourceContent(
    onFamilyPhotosClick: () -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 24.dp)
    ) {
        ParentPhotoSourceOption(
            title = "가족이 보낸 사진",
            description = "가족이 올린 사진보기",
            backgroundColor = SeniorOnColors.Primary200,
            onClick = onFamilyPhotosClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        ParentPhotoSourceOption(
            title = "내 갤러리",
            description = "기존 사진 앱 열기",
            backgroundColor = SeniorOnColors.Gray100,
            onClick = onGalleryClick
        )
    }
}

@Composable
private fun ParentPhotoSourceOption(
    title: String,
    description: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(113.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(start = 18.dp, end = 8.dp, top = 20.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = SeniorOnTextStyles.HeadingXXL,
                color = SeniorOnColors.Gray800
            )
            Text(
                text = description,
                modifier = Modifier.padding(top = 2.dp),
                style = SeniorOnTextStyles.HeadingXS,
                color = SeniorOnColors.Gray500
            )
        }

        Icon(
            painter = painterResource(R.drawable.ic_arrow_next),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = SeniorOnColors.Gray500
        )
    }
}

@Preview(
    name = "Photo Source Bottom Sheet",
    showBackground = true,
    widthDp = 360,
    heightDp = 420
)
@Composable
private fun ParentPhotoSourceBottomSheetPreview() {
    SENIOR_ONTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Black.copy(alpha = 0.50f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            ParentPhotoSheetSurface(
                onFamilyPhotosClick = {},
                onGalleryClick = {}
            )
        }
    }
}
