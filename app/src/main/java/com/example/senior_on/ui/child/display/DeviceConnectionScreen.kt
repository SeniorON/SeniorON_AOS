package com.example.senior_on.ui.child.display

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.data.source.display.MockDisplayScenario
import com.example.senior_on.data.source.mock.fixtures.MockDisplayFixtures
import com.example.senior_on.domain.model.display.DisplayDevice
import com.example.senior_on.domain.model.display.DisplayDeviceConnectionStatus
import com.example.senior_on.ui.common.component.SeniorOnActionButton
import com.example.senior_on.ui.common.component.SeniorOnLoadingIndicator
import com.example.senior_on.ui.common.time.toRelativeTimeLabel
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
    isRefreshing: Boolean = false,
    isDisconnecting: Boolean = false,
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
            isRefreshing = isRefreshing,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DeviceStatusCard(
                device = device,
                relationshipLabel = relationshipLabel,
            )
            DeviceInformationCard(device = device)
            DeviceSettingsCard(device = device)
            DeviceConnectionActionButton(
                isConnected = device != null,
                isLoading = device != null && isDisconnecting,
                onClick = if (device == null) onInstallGuideClick else onDisconnectClick,
            )
        }
    }
}

@Composable
private fun ConnectionStatusTopBar(
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    isRefreshing: Boolean,
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
            isLoading = isRefreshing,
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
    isLoading: Boolean = false,
) {
    Box(
        modifier = modifier
            .size(26.dp)
            .clickable(
                enabled = !isLoading,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            SeniorOnLoadingIndicator(
                color = SeniorOnColors.Gray800,
                size = 20.dp,
            )
        } else {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = contentDescription,
                modifier = Modifier.size(26.dp),
                tint = Color.Unspecified,
            )
        }
    }
}

@Composable
private fun DeviceStatusCard(
    device: DisplayDevice?,
    relationshipLabel: String,
) {
    val status = device?.connectionStatus
    val isLoginExpired = status == DisplayDeviceConnectionStatus.LoginExpired
    val backgroundBrush = when (status) {
        DisplayDeviceConnectionStatus.Online -> SolidColor(SeniorOnColors.Primary600)
        DisplayDeviceConnectionStatus.Offline -> SolidColor(SeniorOnColors.Gray300)
        DisplayDeviceConnectionStatus.LoginExpired -> SolidColor(SeniorOnColors.Beige100)
        null -> SeniorOnBrushes.DisplayDeviceNotConnected
    }
    val titleColor = when (status) {
        DisplayDeviceConnectionStatus.LoginExpired -> SeniorOnColors.Gray800
        null -> SeniorOnColors.Red400
        else -> SeniorOnColors.White
    }
    val statusColor = when (status) {
        DisplayDeviceConnectionStatus.LoginExpired,
        null -> SeniorOnColors.Red400
        else -> SeniorOnColors.White
    }
    val statusDescription = when (status) {
        DisplayDeviceConnectionStatus.Online -> "연결됨"
        DisplayDeviceConnectionStatus.Offline -> listOfNotNull(
            "오프라인",
            device.lastConnectedAtLabel.toRelativeTimeLabel(),
        ).joinToString(" · ")
        DisplayDeviceConnectionStatus.LoginExpired -> "로그인 만료"
        null -> "가족 코드를 연결해주세요"
    }
    val shape = RoundedCornerShape(SeniorOnRadius.Medium)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(89.dp)
            .border(
                width = 1.dp,
                color = if (isLoginExpired) {
                    SeniorOnColors.Beige200
                } else {
                    SeniorOnColors.Background4
                },
                shape = shape,
            )
            .clip(shape)
            .background(backgroundBrush)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device?.name ?: "연결된 기기 없음",
                style = SeniorOnTextStyles.HeadingXS,
                color = titleColor,
            )
            Spacer(modifier = Modifier.height(2.dp))

            if (isLoginExpired) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_sm_alertfilled),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Unspecified,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = statusDescription,
                        style = SeniorOnTextStyles.CaptionMedium,
                        color = statusColor,
                    )
                }
            } else {
                Text(
                    text = statusDescription,
                    style = SeniorOnTextStyles.CaptionMedium,
                    color = statusColor,
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))
        RelationshipTag(
            text = relationshipLabel,
            status = status,
        )
    }
}

@Composable
private fun RelationshipTag(
    text: String,
    status: DisplayDeviceConnectionStatus?,
) {
    val isLoginExpiredOrMissing =
        status == DisplayDeviceConnectionStatus.LoginExpired || status == null
    Box(
        modifier = Modifier
            .height(25.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(
                if (isLoginExpiredOrMissing) {
                    SeniorOnColors.White
                } else {
                    SeniorOnColors.SupportWhite20
                }
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.CaptionMedium,
            color = if (isLoginExpiredOrMissing) {
                SeniorOnColors.Gray800
            } else {
                SeniorOnColors.White
            },
        )
    }
}

@Composable
private fun DeviceInformationCard(device: DisplayDevice?) {
    val unavailableText = device.unavailableStatusText()
    val valuesHidden = device?.connectionStatus == DisplayDeviceConnectionStatus.LoginExpired
    val lastConnected = if (valuesHidden) null else device?.lastConnectedAtLabel.toRelativeTimeLabel()
    val batteryLevel = if (valuesHidden) null else device?.batteryLevelPercent
    val networkConnected = if (valuesHidden) null else device?.networkConnected
    val defaultHomeEnabled = if (valuesHidden) null else device?.defaultHomeEnabled
    val sharingEnabled = if (valuesHidden) null else device?.deviceStatusSharingEnabled

    DeviceSectionCard(
        title = "기기 정보",
        height = 230.dp,
    ) {
        DeviceInformationRow(
            iconResId = R.drawable.ic_clock_2,
            label = "마지막 접속",
            value = lastConnected ?: unavailableText,
            valueAvailable = lastConnected != null,
        )
        DeviceInformationRow(
            iconResId = R.drawable.ic_battery,
            label = "배터리",
            value = batteryLevel?.let { "$it%" } ?: unavailableText,
            valueAvailable = batteryLevel != null,
            showChargingIcon = batteryLevel != null && device?.charging == true,
        )
        DeviceInformationRow(
            iconResId = R.drawable.ic_wifi,
            label = "인터넷 연결",
            value = networkConnected.toDisplayText(
                enabledText = "연결됨",
                disabledText = "미연결",
                unavailableText = unavailableText,
            ),
            valueAvailable = networkConnected != null,
        )
        DeviceInformationRow(
            iconResId = R.drawable.ic_device_status_home,
            label = "기본 홈 설정",
            value = defaultHomeEnabled.toDisplayText(
                enabledText = "시니어On",
                disabledText = "설정 안 됨",
                unavailableText = unavailableText,
            ),
            valueAvailable = defaultHomeEnabled != null,
        )
        DeviceInformationRow(
            iconResId = R.drawable.ic_device_status_share,
            label = "보호자에게 기기 상태 공유",
            value = sharingEnabled.toDisplayText(
                enabledText = "켜짐",
                disabledText = "꺼짐",
                unavailableText = unavailableText,
            ),
            valueAvailable = sharingEnabled != null,
        )
    }
}

@Composable
private fun DeviceSettingsCard(device: DisplayDevice?) {
    val valuesHidden = device?.connectionStatus == DisplayDeviceConnectionStatus.LoginExpired
    val unavailableText = device.unavailableStatusText()
    val rowHeight = if (valuesHidden) 24.dp else 25.dp

    DeviceSectionCard(
        title = "설정 상태",
        height = if (valuesHidden) 198.dp else 202.dp,
    ) {
        DeviceSettingRow(
            iconResId = R.drawable.ic_device_status_location,
            label = "위치 권한",
            enabled = device?.locationPermissionGranted.takeUnless { valuesHidden },
            unavailableText = unavailableText,
            rowHeight = rowHeight,
        )
        DeviceSettingRow(
            iconResId = R.drawable.ic_device_status_gps,
            label = "GPS",
            enabled = device?.gpsEnabled.takeUnless { valuesHidden },
            unavailableText = unavailableText,
            rowHeight = rowHeight,
        )
        DeviceSettingRow(
            iconResId = R.drawable.ic_device_status_notification,
            label = "알림 권한",
            enabled = device?.notificationPermissionGranted.takeUnless { valuesHidden },
            unavailableText = unavailableText,
            rowHeight = rowHeight,
        )
        DeviceSettingRow(
            iconResId = R.drawable.ic_device_status_app_setting,
            label = "앱 실행 유지 설정",
            enabled = device?.appExecutionMaintained.takeUnless { valuesHidden },
            unavailableText = unavailableText,
            rowHeight = rowHeight,
        )
    }
}

@Composable
private fun DeviceSectionCard(
    title: String,
    height: Dp,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Medium)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .border(1.dp, SeniorOnColors.Background4, shape)
            .clip(shape)
            .background(SeniorOnColors.White)
            .padding(horizontal = 14.dp, vertical = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
        ) {
            Text(
                text = title,
                style = SeniorOnTextStyles.BodyMBold,
                color = SeniorOnColors.Gray800,
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SeniorOnColors.Background4)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            content()
        }
    }
}

@Composable
private fun DeviceInformationRow(
    @DrawableRes iconResId: Int,
    label: String,
    value: String,
    valueAvailable: Boolean,
    showChargingIcon: Boolean = false,
) {
    DeviceStatusRow(
        iconResId = iconResId,
        label = label,
        rowHeight = 24.dp,
    ) {
        Text(
            text = value,
            style = SeniorOnTextStyles.BodySMedium,
            color = if (valueAvailable) SeniorOnColors.Gray600 else SeniorOnColors.Gray300,
        )
        if (showChargingIcon) {
            Icon(
                painter = painterResource(R.drawable.ic_device_charging_bolt),
                contentDescription = "충전 중",
                modifier = Modifier.size(16.dp),
                tint = Color.Unspecified,
            )
        }
    }
}

@Composable
private fun DeviceSettingRow(
    @DrawableRes iconResId: Int,
    label: String,
    enabled: Boolean?,
    unavailableText: String,
    rowHeight: Dp,
) {
    DeviceStatusRow(
        iconResId = iconResId,
        label = label,
        rowHeight = rowHeight,
    ) {
        if (enabled == null) {
            Text(
                text = unavailableText,
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray300,
            )
        } else {
            DeviceSettingStatusTag(enabled = enabled)
        }
    }
}

@Composable
private fun DeviceStatusRow(
    @DrawableRes iconResId: Int,
    label: String,
    rowHeight: Dp,
    valueContent: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight),
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            valueContent()
        }
    }
}

@Composable
private fun DeviceSettingStatusTag(enabled: Boolean) {
    Box(
        modifier = Modifier
            .height(25.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(if (enabled) SeniorOnColors.Primary200 else SeniorOnColors.Gray100)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (enabled) "허용됨" else "꺼짐",
            style = SeniorOnTextStyles.CaptionMedium,
            color = if (enabled) SeniorOnColors.Primary700 else SeniorOnColors.Gray500,
        )
    }
}

@Composable
private fun DeviceConnectionActionButton(
    isConnected: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (isConnected) SeniorOnColors.Red300 else SeniorOnColors.Gray700

    SeniorOnActionButton(
        text = if (isConnected) "연결 해제" else "부모님 앱 설치 방법",
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        isLoading = isLoading,
        containerColor = SeniorOnColors.White,
        contentColor = contentColor,
        border = BorderStroke(
            width = 1.dp,
            color = if (isConnected) SeniorOnColors.Red300 else SeniorOnColors.Gray200,
        ),
        shape = RoundedCornerShape(SeniorOnRadius.Small),
        minHeight = 48.dp,
        leadingContent = if (isConnected) {
            null
        } else {
            {
                Icon(
                    painter = painterResource(R.drawable.ic_download),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = androidx.compose.material3.LocalContentColor.current,
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
        },
    )
}

private fun DisplayDevice?.unavailableStatusText(): String = when {
    this?.connectionStatus == DisplayDeviceConnectionStatus.LoginExpired -> "로그인 후 확인 가능"
    this == null -> "연결 후 확인 가능"
    else -> "확인 불가"
}

private fun Boolean?.toDisplayText(
    enabledText: String,
    disabledText: String,
    unavailableText: String,
): String = when (this) {
    true -> enabledText
    false -> disabledText
    null -> unavailableText
}

@Preview(name = "Not connected", showBackground = true, widthDp = 360, heightDp = 862)
@Composable
private fun DeviceConnectionNotConnectedPreview() {
    SENIOR_ONTheme {
        DeviceConnectionScreen(
            device = null,
            relationshipLabel = "아버지",
        )
    }
}

@Preview(name = "Login expired", showBackground = true, widthDp = 360, heightDp = 862)
@Composable
private fun DeviceConnectionLoginExpiredPreview() {
    SENIOR_ONTheme {
        DeviceConnectionScreen(
            device = MockDisplayFixtures.overview(MockDisplayScenario.LoginExpired).device,
            relationshipLabel = "어머니",
        )
    }
}

@Preview(name = "Offline", showBackground = true, widthDp = 360, heightDp = 862)
@Composable
private fun DeviceConnectionOfflinePreview() {
    SENIOR_ONTheme {
        DeviceConnectionScreen(
            device = MockDisplayFixtures.overview(MockDisplayScenario.Offline).device,
            relationshipLabel = "어머니",
        )
    }
}

@Preview(name = "Connected", showBackground = true, widthDp = 360, heightDp = 862)
@Composable
private fun DeviceConnectionConnectedPreview() {
    SENIOR_ONTheme {
        DeviceConnectionScreen(
            device = MockDisplayFixtures.overview(MockDisplayScenario.Connected).device,
            relationshipLabel = "어머니",
        )
    }
}
