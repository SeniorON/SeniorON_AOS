package com.example.senior_on.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.senior_on.R
import com.example.senior_on.ui.senior_info.CustomRelationshipBottomSheet
import com.example.senior_on.ui.senior_info.CustomRelationshipMaxLength
import com.example.senior_on.ui.senior_info.InputLabel
import com.example.senior_on.ui.senior_info.RelationshipSelector
import com.example.senior_on.ui.senior_info.SeniorInfoActionButton
import com.example.senior_on.ui.senior_info.SeniorInfoButtonStyle
import com.example.senior_on.ui.senior_info.SeniorInfoPhoneTextField
import com.example.senior_on.ui.senior_info.SeniorInfoTextField
import com.example.senior_on.ui.senior_info.SeniorRelationship
import com.example.senior_on.ui.senior_info.formatPhoneFieldValue
import com.example.senior_on.ui.senior_info.parseBirthDate
import com.example.senior_on.ui.signup.SignupBirthDateModalBottomSheet
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.LocalDate
import java.time.Period

data class ConnectedSeniorDeviceUiState(
    val deviceName: String = "Galaxy S24",
    val name: String = "김순자",
    val relationship: SeniorRelationship = SeniorRelationship.Mother,
    val customRelationship: String = "",
    val birthDate: String = "1958.04.12",
    val phoneNumber: String = "010-1234-5678",
    val address: String = "경기도 하남시 창우동",
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
                style = SeniorOnTextStyles.BodyMBold,
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
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.White
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(SeniorOnRadius.Small))
                    .background(SeniorOnColors.Primary200)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDeviceInfoClick
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "기기 정보",
                    style = SeniorOnTextStyles.CaptionMedium,
                    color = SeniorOnColors.Primary700
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider(
            thickness = 1.dp,
            color = SeniorOnColors.White.copy(alpha = 0.25f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = device.name,
                style = SeniorOnTextStyles.BodyLBold,
                color = SeniorOnColors.White
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(17.dp))
                    .background(SeniorOnColors.White.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = device.relationshipLabel,
                    style = SeniorOnTextStyles.CaptionMedium,
                    color = SeniorOnColors.White
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                painter = painterResource(id = R.drawable.ic_sm_pencil),
                contentDescription = "정보 수정",
                modifier = Modifier
                    .size(20.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onEditInfoClick
                    ),
                tint = SeniorOnColors.White
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = device.birthDateWithAgeLabel,
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.White.copy(alpha = 0.9f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = SeniorOnColors.White
            )
            Spacer(modifier = Modifier.width(4.dp))
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
                modifier = Modifier.size(16.dp),
                tint = SeniorOnColors.White
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = device.phoneNumber.ifBlank { "연락처 없음" },
                style = SeniorOnTextStyles.CaptionMedium,
                color = SeniorOnColors.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
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
                ),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditConnectedDeviceInfoScreen(
    device: ConnectedSeniorDeviceUiState,
    onBackClick: () -> Unit,
    onSaveClick: (ConnectedSeniorDeviceUiState) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable { mutableStateOf(device.name) }
    var relationship by rememberSaveable { mutableStateOf(device.relationship) }
    var customRelationship by rememberSaveable { mutableStateOf(device.customRelationship) }
    var customRelationshipDraft by rememberSaveable { mutableStateOf(device.customRelationship) }
    var birthDate by rememberSaveable { mutableStateOf(device.birthDate) }
    var phoneNumber by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(device.phoneNumber))
    }
    var address by rememberSaveable { mutableStateOf(device.address) }
    var addressDetail by rememberSaveable { mutableStateOf(device.addressDetail) }
    var showCustomRelationshipSheet by rememberSaveable { mutableStateOf(false) }
    var showBirthDateSheet by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val isRelationshipValid = when (relationship) {
        SeniorRelationship.Custom -> customRelationship.isNotBlank()
        else -> true
    }
    val canSave = name.isNotBlank() &&
        isRelationshipValid &&
        parseBirthDate(birthDate) != null

    if (showCustomRelationshipSheet) {
        CustomRelationshipBottomSheet(
            value = customRelationshipDraft,
            onValueChange = { customRelationshipDraft = it.take(CustomRelationshipMaxLength) },
            onCancel = { showCustomRelationshipSheet = false },
            onConfirm = {
                val trimmed = customRelationshipDraft.trim()
                if (trimmed.isNotEmpty()) {
                    customRelationship = trimmed.take(CustomRelationshipMaxLength)
                    relationship = SeniorRelationship.Custom
                    showCustomRelationshipSheet = false
                }
            }
        )
    }

    if (showBirthDateSheet) {
        SignupBirthDateModalBottomSheet(
            initialBirthDate = birthDate,
            onDismiss = { showBirthDateSheet = false },
            onConfirm = { selectedBirthDate ->
                birthDate = selectedBirthDate
                showBirthDateSheet = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .statusBarsPadding()
    ) {
        SettingsBackTopAppBar(
            title = "정보 수정",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 16.dp)
        ) {
            InputLabel(text = "이름")
            Spacer(modifier = Modifier.height(6.dp))
            SeniorInfoTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "이름 입력",
                keyboardType = KeyboardType.Text,
                onClearClick = { name = "" }
            )

            Spacer(modifier = Modifier.height(16.dp))
            InputLabel(text = "관계")
            Spacer(modifier = Modifier.height(6.dp))
            RelationshipSelector(
                selectedRelationship = if (showCustomRelationshipSheet) {
                    SeniorRelationship.Custom
                } else {
                    relationship
                },
                customRelationshipLabel = customRelationship,
                onRelationshipClick = { selected ->
                    if (selected == SeniorRelationship.Custom) {
                        customRelationshipDraft = customRelationship
                        showCustomRelationshipSheet = true
                    } else {
                        customRelationship = ""
                        relationship = selected
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            InputLabel(text = "생년월일", optionalText = " (필수)")
            Spacer(modifier = Modifier.height(6.dp))
            SeniorInfoTextField(
                value = birthDate,
                onValueChange = {},
                placeholder = "생년월일 선택",
                readOnly = true,
                onClick = { showBirthDateSheet = true },
                trailingContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sm_chevron_down_1),
                        contentDescription = "생년월일 선택",
                        modifier = Modifier.size(24.dp),
                        tint = SeniorOnColors.Gray500
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            InputLabel(text = "전화번호", optionalText = "(선택)")
            Spacer(modifier = Modifier.height(6.dp))
            SeniorInfoPhoneTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = formatPhoneFieldValue(it) },
                placeholder = "010-0000-0000",
                onClearClick = { phoneNumber = TextFieldValue("") }
            )

            Spacer(modifier = Modifier.height(16.dp))
            InputLabel(text = "자택 주소", optionalText = "(선택)")
            Spacer(modifier = Modifier.height(4.dp))
            SeniorInfoTextField(
                value = address,
                onValueChange = { address = it },
                placeholder = "주소 검색",
                keyboardType = KeyboardType.Text,
                trailingContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "주소 검색",
                        modifier = Modifier.size(24.dp),
                        tint = SeniorOnColors.Gray500
                    )
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            SeniorInfoTextField(
                value = addressDetail,
                onValueChange = { addressDetail = it },
                placeholder = "상세 주소 입력",
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
                onClearClick = { addressDetail = "" }
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "외출·귀가 알림 기준 위치로 사용돼요.",
                style = SeniorOnTextStyles.CaptionMedium,
                color = SeniorOnColors.Gray400
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_alert_filled),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = SeniorOnColors.Gray200
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buildAnnotatedString {
                        append("다음에 하기 선택시 ")
                        withStyle(SpanStyle(color = SeniorOnColors.Red300)) {
                            append("일부 기능이 제한")
                        }
                        append("될 수 있어요")
                    },
                    style = SeniorOnTextStyles.BodySRegular,
                    color = SeniorOnColors.Gray400
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SeniorInfoActionButton(
                text = "저장",
                onClick = {
                    onSaveClick(
                        device.copy(
                            name = name.trim(),
                            relationship = relationship,
                            customRelationship = if (relationship == SeniorRelationship.Custom) {
                                customRelationship
                            } else {
                                ""
                            },
                            birthDate = birthDate,
                            phoneNumber = phoneNumber.text,
                            address = address.trim(),
                            addressDetail = addressDetail.trim()
                        )
                    )
                },
                style = SeniorInfoButtonStyle.Filled,
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ConnectedDevicesScreenPreview() {
    SENIOR_ONTheme {
        ConnectedDevicesScreen(
            device = ConnectedSeniorDeviceUiState(),
            onBackClick = {},
            onEditInfoClick = {},
            onDisconnectConfirm = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun EditConnectedDeviceInfoScreenPreview() {
    SENIOR_ONTheme {
        EditConnectedDeviceInfoScreen(
            device = ConnectedSeniorDeviceUiState(
                birthDate = "1949.04.01",
                phoneNumber = "",
                address = "서울특별시 성북구 길음로 33",
                addressDetail = "203동 2403호"
            ),
            onBackClick = {},
            onSaveClick = {}
        )
    }
}
