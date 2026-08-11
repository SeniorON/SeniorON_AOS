package com.example.senior_on.ui.child.settings

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.senior_on.R
import com.example.senior_on.ui.common.seniorinfo.SeniorRelationship
import com.example.senior_on.ui.common.seniorinfo.parseBirthDate
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.LocalDate
import java.time.Period

data class ConnectedSeniorDeviceUiState(
    val deviceName: String,
    val name: String,
    val relationship: SeniorRelationship,
    val customRelationship: String = "",
    val birthDate: String,
    val phoneNumber: String,
    val address: String,
    val addressDetail: String = ""
) {
    val relationshipLabel: String
        get() = if (relationship == SeniorRelationship.Custom && customRelationship.isNotBlank()) {
            customRelationship
        } else {
            relationship.label
        }

    val birthDateWithAgeLabel: String
        get() {
            val date = parseBirthDate(birthDate) ?: return birthDate
            val age = Period.between(date, LocalDate.now()).years
            return "${date.year}년 ${date.monthValue}월 ${date.dayOfMonth}일생 · 만 ${age}세"
        }
}

@Composable
fun ConnectedDevicesScreen(
    device: ConnectedSeniorDeviceUiState?,
    onBackClick: () -> Unit,
    onEditInfoClick: () -> Unit,
    onDisconnectConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    onDeviceInfoClick: () -> Unit = {}
) {
    var showDisconnectDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.Background1)
            .statusBarsPadding()
    ) {
        SettingsBackTopAppBar(
            title = "연결된 기기",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 20.dp, bottom = 24.dp)
        ) {
            Text(
                text = "연결된 시니어 기기",
                style = SeniorOnTextStyles.HeadingXS,
                color = SeniorOnColors.Gray800
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (device != null) {
                ConnectedSeniorDeviceCard(
                    device = device,
                    onDeviceInfoClick = onDeviceInfoClick,
                    onEditInfoClick = onEditInfoClick,
                    onDisconnectClick = { showDisconnectDialog = true }
                )
            } else {
                Text(
                    text = "연결된 시니어 기기가 없어요",
                    style = SeniorOnTextStyles.BodyMMedium,
                    color = SeniorOnColors.Gray500
                )
            }
        }
    }

    if (showDisconnectDialog) {
        DisconnectDeviceDialog(
            onDismiss = { showDisconnectDialog = false },
            onConfirm = {
                showDisconnectDialog = false
                onDisconnectConfirm()
            }
        )
    }
}

@Composable
private fun ConnectedSeniorDeviceCard(
    device: ConnectedSeniorDeviceUiState,
    onDeviceInfoClick: () -> Unit,
    onEditInfoClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .background(SeniorOnColors.Primary600)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = device.deviceName,
                modifier = Modifier.weight(1f),
                style = SeniorOnTextStyles.BodyMMedium,
                color = SeniorOnColors.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .height(25.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(SeniorOnColors.Primary200)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDeviceInfoClick
                    )
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "기기 정보",
                    style = SeniorOnTextStyles.CaptionMedium,
                    color = SeniorOnColors.Primary600,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        HorizontalDivider(
            thickness = 1.dp,
            color = SeniorOnColors.White.copy(alpha = 0.25f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = device.name,
                        style = SeniorOnTextStyles.BodyLBold,
                        color = SeniorOnColors.White
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .size(width = 64.dp, height = 25.dp)
                            .clip(RoundedCornerShape(17.dp))
                            .background(SeniorOnColors.White.copy(alpha = 0.2f))
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = device.relationshipLabel,
                            style = SeniorOnTextStyles.CaptionMedium,
                            color = SeniorOnColors.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = device.birthDateWithAgeLabel,
                    style = SeniorOnTextStyles.BodySMedium,
                    color = SeniorOnColors.White.copy(alpha = 0.9f)
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_sm_pencil),
                contentDescription = "정보 수정",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onEditInfoClick
                    ),
                tint = SeniorOnColors.White
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = device.address.ifBlank { "주소 없음" },
                style = SeniorOnTextStyles.CaptionMedium,
                color = SeniorOnColors.White
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(12.dp)
                    .background(SeniorOnColors.White.copy(alpha = 0.4f))
            )

            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                painter = painterResource(id = R.drawable.ic_call),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = device.phoneNumber.ifBlank { "연락처 없음" },
                style = SeniorOnTextStyles.CaptionMedium,
                color = SeniorOnColors.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .width(304.dp)
                .height(42.dp)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(SeniorOnRadius.Small))
                .border(
                    width = 1.dp,
                    color = SeniorOnColors.White,
                    shape = RoundedCornerShape(SeniorOnRadius.Small)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDisconnectClick
                )
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "연결 해제",
                style = SeniorOnTextStyles.ButtonS,
                color = SeniorOnColors.White
            )
        }
    }
}

@Composable
private fun DisconnectDeviceDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Black.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = SeniorOnRadius.XLarge,
                            topEnd = SeniorOnRadius.XLarge
                        )
                    )
                    .background(SeniorOnColors.White)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "연결을 해제할까요?",
                    style = SeniorOnTextStyles.BodyLBold,
                    color = SeniorOnColors.Gray800,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = buildAnnotatedString {
                        append("연결 해제 시 부모님 폰의\n")
                        withStyle(SpanStyle(color = SeniorOnColors.Red300)) {
                            append("모든 설정이 초기화")
                        }
                        append("되고 알림 수신이 중단돼요")
                    },
                    style = SeniorOnTextStyles.BodySMedium,
                    color = SeniorOnColors.Gray500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(SeniorOnRadius.Small))
                            .border(
                                width = 1.dp,
                                color = SeniorOnColors.Gray200,
                                shape = RoundedCornerShape(SeniorOnRadius.Small)
                            )
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "취소",
                            style = SeniorOnTextStyles.ButtonM,
                            color = SeniorOnColors.Gray500
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(SeniorOnRadius.Small))
                            .background(SeniorOnColors.Red400)
                            .clickable(onClick = onConfirm),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "연결 해제",
                            style = SeniorOnTextStyles.ButtonM,
                            color = SeniorOnColors.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ConnectedDevicesScreenPreview() {
    SENIOR_ONTheme {
        ConnectedDevicesScreen(
            device = PreviewConnectedDevice,
            onBackClick = {},
            onEditInfoClick = {},
            onDisconnectConfirm = {}
        )
    }
}

private val PreviewConnectedDevice = ConnectedSeniorDeviceUiState(
    deviceName = "Galaxy A16",
    name = "김순자",
    relationship = SeniorRelationship.Mother,
    birthDate = "1949.04.01",
    phoneNumber = "010-1234-5678",
    address = "서울특별시 성북구 길음로 33",
    addressDetail = "203동 2403호",
)
