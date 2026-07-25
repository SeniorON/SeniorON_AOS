package com.example.senior_on.ui.display

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.senior_on.R
import com.example.senior_on.data.repository.MockDisplayFixtures
import com.example.senior_on.data.repository.MockDisplayScenario
import com.example.senior_on.data.repository.MockParentInfoFixtures
import com.example.senior_on.domain.model.DisplayDevice
import com.example.senior_on.domain.model.DisplayDeviceConnectionStatus
import com.example.senior_on.domain.model.ParentInfo
import com.example.senior_on.domain.model.SeniorScreenConfiguration
import com.example.senior_on.ui.child.ChildBottomNavigation
import com.example.senior_on.ui.child.ChildMainTab
import com.example.senior_on.ui.notification.ParentPhoneInternetRequiredDialog
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnBrushes
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun DisplayTabScreen(
    uiState: DisplayTabUiState,
    modifier: Modifier = Modifier,
    canEditScreen: Boolean = true,
    onDeviceClick: () -> Unit = {},
    onParentInfoClick: () -> Unit = {},
    onLargePreviewClick: () -> Unit = {},
    onFontEditClick: () -> Unit = {},
    onButtonEditClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
    ) {
        DisplayTopBar()

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                DisplaySummarySection(
                    parentInfo = uiState.parentInfo,
                    device = uiState.device,
                    onDeviceClick = onDeviceClick,
                    onParentInfoClick = onParentInfoClick,
                )
            }

            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(SeniorOnColors.Background1)
                )
            }

            item {
                ScreenEditSection(
                    parentInfo = uiState.parentInfo,
                    configuration = uiState.screenConfiguration,
                    canEditScreen = canEditScreen,
                    onLargePreviewClick = onLargePreviewClick,
                    onFontEditClick = onFontEditClick,
                    onButtonEditClick = onButtonEditClick,
                )
            }
        }
    }
}

@Composable
private fun DisplayTopBar() {
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
                    offset = DpOffset(x = 0.dp, y = 4.dp),
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
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = "화면 지원",
                style = SeniorOnTextStyles.HeadingM,
                color = SeniorOnColors.Gray800,
            )
        }
    }
}

@Composable
private fun DisplaySummarySection(
    parentInfo: ParentInfo?,
    device: DisplayDevice?,
    onDeviceClick: () -> Unit,
    onParentInfoClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        DeviceConnectionBanner(
            device = device,
            relationshipLabel = parentInfo?.relationshipLabel,
            onClick = onDeviceClick,
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (parentInfo == null) {
            EmptyParentInformationCard(onClick = onParentInfoClick)
        } else {
            ParentInformationCard(
                parentInfo = parentInfo,
                onClick = onParentInfoClick,
            )
        }
    }
}

@Composable
private fun DeviceConnectionBanner(
    device: DisplayDevice?,
    relationshipLabel: String?,
    onClick: () -> Unit,
) {
    val isNotConnected = device == null
    val isOnline = device?.connectionStatus == DisplayDeviceConnectionStatus.Online
    val leadingContentColor = when {
        isNotConnected -> SeniorOnColors.Red300
        isOnline -> SeniorOnColors.Primary600
        else -> SeniorOnColors.Gray700
    }
    val backgroundBrush = when {
        isNotConnected -> SeniorOnBrushes.DisplayDeviceNotConnected
        isOnline -> SeniorOnBrushes.DisplayDeviceConnected
        else -> SolidColor(SeniorOnColors.Gray100)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(backgroundBrush)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_sm_link),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = leadingContentColor,
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = device?.let {
                listOfNotNull(it.name, relationshipLabel)
                    .filter(String::isNotBlank)
                    .joinToString(" · ")
            } ?: "연결된 기기가 없습니다",
            modifier = Modifier.weight(1f),
            style = SeniorOnTextStyles.BodySSemiBold,
            color = if (isNotConnected) SeniorOnColors.Red300 else SeniorOnColors.Gray700,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (isOnline) {
            Icon(
                painter = painterResource(id = R.drawable.ic_sm_battery),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.Primary600,
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "${device?.batteryLevelPercent ?: 0}%",
                style = SeniorOnTextStyles.BodySSemiBold,
                color = SeniorOnColors.Primary600,
            )

            Spacer(modifier = Modifier.width(4.dp))
        } else if (device != null) {
            Text(
                text = "오프라인",
                style = SeniorOnTextStyles.BodySSemiBold,
                color = SeniorOnColors.Gray500,
            )

            Spacer(modifier = Modifier.width(6.dp))
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_sm_arrow_right),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isNotConnected) SeniorOnColors.Red300 else SeniorOnColors.Gray700,
        )
    }
}

@Composable
private fun ParentInformationCard(
    parentInfo: ParentInfo,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Large)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp)
            .dropShadow(
                shape = shape,
                shadow = Shadow(
                    radius = 14.dp,
                    spread = 0.dp,
                    color = Color(0xFFB5C69C).copy(alpha = 0.9f),
                    offset = DpOffset(x = 0.dp, y = 4.dp),
                )
            )
            .clip(shape)
            .background(SeniorOnColors.Primary600)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = parentInfo.name,
                style = SeniorOnTextStyles.HeadingS,
                color = SeniorOnColors.White,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(17.dp))
                    .background(SeniorOnColors.White.copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = parentInfo.relationshipLabel,
                    style = SeniorOnTextStyles.CaptionMedium,
                    color = SeniorOnColors.White,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                painter = painterResource(id = R.drawable.ic_sm_arrow_right),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.White,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = parentInfo.birthDescription(),
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.White,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.width(154.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = SeniorOnColors.Primary200,
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = parentInfo.fullAddress,
                    modifier = Modifier.weight(1f),
                    style = SeniorOnTextStyles.CaptionMedium,
                    color = SeniorOnColors.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(SeniorOnColors.White.copy(alpha = 0.2f))
            )

            Spacer(modifier = Modifier.width(26.dp))

            Icon(
                painter = painterResource(id = R.drawable.ic_call),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.Primary200,
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = parentInfo.phoneNumber,
                style = SeniorOnTextStyles.CaptionMedium,
                color = SeniorOnColors.White,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun EmptyParentInformationCard(onClick: () -> Unit) {
    val shape = RoundedCornerShape(SeniorOnRadius.Large)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp)
            .clip(shape)
            .background(SeniorOnColors.Primary600)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "부모님 정보가 없습니다",
            style = SeniorOnTextStyles.HeadingS,
            color = SeniorOnColors.White,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "부모님 정보를 입력하면 이곳에서 확인할 수 있어요",
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.White,
        )
    }
}

@Composable
private fun ScreenEditSection(
    parentInfo: ParentInfo?,
    configuration: SeniorScreenConfiguration,
    canEditScreen: Boolean,
    onLargePreviewClick: () -> Unit,
    onFontEditClick: () -> Unit,
    onButtonEditClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_phone1),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.Gray800,
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "화면 편집",
                style = SeniorOnTextStyles.HeadingS,
                color = SeniorOnColors.Gray800,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SeniorScreenPreviewCard(
            phoneLabel = "${parentInfo?.relationshipLabel ?: "부모님"} 폰",
            configuration = configuration,
            onLargePreviewClick = onLargePreviewClick,
        )

        if (canEditScreen) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DisplayEditButton(
                    text = "글씨 편집",
                    iconResId = R.drawable.ic_pencil,
                    outlined = true,
                    onClick = onFontEditClick,
                    modifier = Modifier.weight(1f),
                )

                DisplayEditButton(
                    text = "버튼 편집",
                    iconResId = R.drawable.ic_pencil_box,
                    outlined = false,
                    onClick = onButtonEditClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SeniorScreenPreviewCard(
    phoneLabel: String,
    configuration: SeniorScreenConfiguration,
    onLargePreviewClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(328f / 336f)
            .clip(RoundedCornerShape(SeniorOnRadius.Large))
            .background(SeniorOnColors.Background1),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 22.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = phoneLabel,
                modifier = Modifier.weight(1f),
                style = SeniorOnTextStyles.BodyMBold,
                color = SeniorOnColors.Gray700,
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(31.dp))
                    .border(
                        width = 1.dp,
                        color = SeniorOnColors.Primary600,
                        shape = RoundedCornerShape(31.dp),
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onLargePreviewClick,
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sm_preview),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = SeniorOnColors.Primary600,
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "크게 보기",
                    style = SeniorOnTextStyles.CaptionMedium,
                    color = SeniorOnColors.Primary600,
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
        ) {
            SeniorPhonePreview(configuration = configuration)
        }
    }
}

@Composable
private fun DisplayEditButton(
    text: String,
    @DrawableRes iconResId: Int,
    outlined: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)
    val backgroundColor = if (outlined) SeniorOnColors.White else SeniorOnColors.Primary600
    val contentColor = if (outlined) SeniorOnColors.Primary600 else SeniorOnColors.White

    Row(
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .background(backgroundColor)
            .then(
                if (outlined) {
                    Modifier.border(1.dp, SeniorOnColors.Primary600, shape)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = contentColor,
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = text,
            style = SeniorOnTextStyles.ButtonM,
            color = contentColor,
        )
    }
}

private fun ParentInfo.birthDescription(): String =
    "${birthDate.year}년 ${birthDate.monthValue}월 ${birthDate.dayOfMonth}일생 · 만 ${age()}세"

@Preview(
    name = "Display Connected",
    showBackground = true,
    widthDp = 360,
    heightDp = 888,
)
@Composable
private fun DisplayTabConnectedPreview() {
    val overview = MockDisplayFixtures.overview(MockDisplayScenario.Connected)

    SENIOR_ONTheme {
        DisplayTabPreviewFrame(
            uiState = DisplayTabUiState(
                parentInfo = MockParentInfoFixtures.mother,
                device = overview.device,
                screenConfiguration = overview.screenConfiguration,
            )
        )
    }
}

@Preview(
    name = "Display Not Connected",
    showBackground = true,
    widthDp = 360,
    heightDp = 888,
)
@Composable
private fun DisplayTabNotConnectedPreview() {
    val overview = MockDisplayFixtures.overview(MockDisplayScenario.NotConnected)

    SENIOR_ONTheme {
        DisplayTabPreviewFrame(
            uiState = DisplayTabUiState(
                parentInfo = MockParentInfoFixtures.mother,
                device = overview.device,
                screenConfiguration = overview.screenConfiguration,
            )
        )
    }
}

@Preview(
    name = "Display Offline",
    showBackground = true,
    widthDp = 360,
    heightDp = 888,
)
@Composable
private fun DisplayTabOfflinePreview() {
    val overview = MockDisplayFixtures.overview(MockDisplayScenario.Offline)

    SENIOR_ONTheme {
        DisplayTabPreviewFrame(
            uiState = DisplayTabUiState(
                parentInfo = MockParentInfoFixtures.mother,
                device = overview.device,
                screenConfiguration = overview.screenConfiguration,
            )
        )
    }
}

@Preview(
    name = "Display Offline Dialog",
    showBackground = true,
    widthDp = 360,
    heightDp = 888,
)
@Composable
private fun DisplayTabOfflineDialogPreview() {
    val overview = MockDisplayFixtures.overview(MockDisplayScenario.Offline)

    SENIOR_ONTheme {
        DisplayTabPreviewFrame(
            uiState = DisplayTabUiState(
                parentInfo = MockParentInfoFixtures.mother,
                device = overview.device,
                screenConfiguration = overview.screenConfiguration,
            ),
            showInternetRequiredDialog = true,
        )
    }
}

@Composable
private fun DisplayTabPreviewFrame(
    uiState: DisplayTabUiState,
    showInternetRequiredDialog: Boolean = false,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                DisplayTabScreen(uiState = uiState)
            }

            ChildBottomNavigation(
                selectedTab = ChildMainTab.Screen,
                onTabClick = {},
            )
        }

        if (showInternetRequiredDialog) {
            ParentPhoneInternetRequiredDialog(
                onConfirmClick = {},
                obscureBackgroundContent = true,
            )
        }
    }
}
