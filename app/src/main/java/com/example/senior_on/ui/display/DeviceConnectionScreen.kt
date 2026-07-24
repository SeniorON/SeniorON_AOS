package com.example.senior_on.ui.display

import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.data.repository.MockDisplayFixtures
import com.example.senior_on.data.repository.MockDisplayScenario
import com.example.senior_on.domain.model.DisplayDevice
import com.example.senior_on.domain.model.DisplayDeviceConnectionStatus
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnBrushes
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun DeviceConnectionScreen(
    device: DisplayDevice?,
    relationshipLabel: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onRefreshClick: () -> Unit = {},
    onDisconnectClick: () -> Unit = {},
    onInstallGuideClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.Background1)
    ) {
        ConnectionStatusTopBar(
            onBackClick = onBackClick,
            onRefreshClick = onRefreshClick,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            DeviceStatusCard(
                device = device,
                relationshipLabel = relationshipLabel,
            )

            Spacer(modifier = Modifier.height(12.dp))

            DeviceInformationCard(
                device = device,
                onDisconnectClick = onDisconnectClick,
                onInstallGuideClick = onInstallGuideClick,
            )
        }
    }
}

@Composable
private fun ConnectionStatusTopBar(
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SeniorOnColors.White)
            .statusBarsPadding()
            .height(54.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        ConnectionTopBarIconButton(
            iconResId = R.drawable.ic_arrow_back,
            contentDescription = "뒤로가기",
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart),
        )

        Text(
            text = "연결 상태",
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Gray800,
        )

        ConnectionTopBarIconButton(
            iconResId = R.drawable.ic_refresh,
            contentDescription = "연결 상태 새로고침",
            onClick = onRefreshClick,
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}

@Composable
private fun ConnectionTopBarIconButton(
    @DrawableRes iconResId: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(26.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = contentDescription,
            modifier = Modifier.size(26.dp),
            tint = Color.Unspecified,
        )
    }
}

@Composable
private fun DeviceStatusCard(
    device: DisplayDevice?,
    relationshipLabel: String,
) {
    val isOnline = device?.connectionStatus == DisplayDeviceConnectionStatus.Online
    val backgroundBrush = when {
        device == null -> SeniorOnBrushes.DisplayDeviceNotConnected
        isOnline -> SolidColor(SeniorOnColors.Primary600)
        else -> SolidColor(SeniorOnColors.Gray300)
    }
    val contentColor = when {
        device == null -> SeniorOnColors.Red400
        else -> SeniorOnColors.White
    }
    val statusDescription = when {
        device == null -> "가족 코드를 연결해주세요"
        isOnline -> "연결됨"
        else -> listOfNotNull("오프라인", device.lastConnectedAtLabel)
            .joinToString(" · ")
    }

    val shape = RoundedCornerShape(SeniorOnRadius.Large)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(89.dp)
            .border(1.dp, SeniorOnColors.Background4, shape)
            .clip(shape)
            .background(backgroundBrush)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device?.name ?: "연결된 기기 없음",
                style = SeniorOnTextStyles.HeadingS,
                color = contentColor,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = statusDescription,
                style = SeniorOnTextStyles.CaptionMedium,
                color = contentColor,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .height(25.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(
                    if (device == null) {
                        SeniorOnColors.White
                    } else {
                        SeniorOnColors.White.copy(alpha = 0.2f)
                    }
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = relationshipLabel,
                style = SeniorOnTextStyles.CaptionMedium,
                color = if (device == null) {
                    SeniorOnColors.Gray600
                } else {
                    SeniorOnColors.White
                },
            )
        }
    }
}

@Composable
private fun DeviceInformationCard(
    device: DisplayDevice?,
    onDisconnectClick: () -> Unit,
    onInstallGuideClick: () -> Unit,
) {
    val isOnline = device?.connectionStatus == DisplayDeviceConnectionStatus.Online
    val unavailableText = "연결 후 확인 가능"
    val valueColor = if (device == null) SeniorOnColors.Gray300 else SeniorOnColors.Gray800
    val shape = RoundedCornerShape(SeniorOnRadius.Medium)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(238.dp)
            .border(1.dp, SeniorOnColors.Background4, shape)
            .clip(shape)
            .background(SeniorOnColors.White)
            .padding(14.dp)
    ) {
        Text(
            text = "기기 정보",
            style = SeniorOnTextStyles.BodyMBold,
            color = SeniorOnColors.Gray800,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SeniorOnColors.Background4)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DeviceInformationRow(
                iconResId = R.drawable.ic_battery,
                label = "배터리",
                value = device?.batteryLevelPercent?.let { "$it%" } ?: unavailableText,
                valueColor = valueColor,
            )
            DeviceInformationRow(
                iconResId = R.drawable.ic_wifi,
                label = "네트워크",
                value = when {
                    device == null -> unavailableText
                    isOnline -> "연결됨"
                    else -> "미연결"
                },
                valueColor = valueColor,
            )
            DeviceInformationRow(
                iconResId = R.drawable.ic_share_location,
                label = "마지막 위치 업데이트",
                value = device?.lastLocationUpdatedAtLabel ?: unavailableText,
                valueColor = valueColor,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        DeviceConnectionActionButton(
            isConnected = device != null,
            onClick = if (device == null) onInstallGuideClick else onDisconnectClick,
        )
    }
}

@Composable
private fun DeviceInformationRow(
    @DrawableRes iconResId: Int,
    label: String,
    value: String,
    valueColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = SeniorOnColors.Gray500,
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray600,
        )

        Text(
            text = value,
            style = SeniorOnTextStyles.BodySMedium,
            color = valueColor,
        )
    }
}

@Composable
private fun DeviceConnectionActionButton(
    isConnected: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (isConnected) SeniorOnColors.Red300 else SeniorOnColors.Gray700
    val shape = RoundedCornerShape(SeniorOnRadius.Small)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .border(
                width = 1.dp,
                color = if (isConnected) SeniorOnColors.Red300 else SeniorOnColors.Gray200,
                shape = shape,
            )
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        if (!isConnected) {
            Icon(
                painter = painterResource(R.drawable.ic_download),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = contentColor,
            )

            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = if (isConnected) "연결 해제" else "부모님 앱 설치 방법",
            style = SeniorOnTextStyles.ButtonM,
            color = contentColor,
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(name = "Not connected", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun DeviceConnectionNotConnectedPreview() {
    SENIOR_ONTheme {
        DeviceConnectionScreen(
            device = null,
            relationshipLabel = "아버지",
        )
    }
}

@Preview(name = "Offline", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun DeviceConnectionOfflinePreview() {
    SENIOR_ONTheme {
        DeviceConnectionScreen(
            device = MockDisplayFixtures.overview(MockDisplayScenario.Offline).device,
            relationshipLabel = "어머니",
        )
    }
}

@Preview(name = "Connected", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun DeviceConnectionConnectedPreview() {
    SENIOR_ONTheme {
        DeviceConnectionScreen(
            device = MockDisplayFixtures.overview(MockDisplayScenario.Connected).device,
            relationshipLabel = "어머니",
        )
    }
}
