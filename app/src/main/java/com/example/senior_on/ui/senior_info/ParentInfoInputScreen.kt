package com.example.senior_on.ui.senior_info

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

enum class SeniorRelationship(val label: String) {
    Mother("어머니"),
    Father("아버지"),
    Grandparent("조부모"),
    Custom("직접 작성")
}

data class ParentInfoInputState(
    val name: String,
    val relationship: SeniorRelationship,
    val birthDate: String,
    val phoneNumber: String,
    val address: String,
    val addressDetail: String,
    val addressLatitude: Double?,
    val addressLongitude: Double?,
    val customRelationship: String = ""
)

@Composable
fun ParentInfoInputScreen(
    modifier: Modifier = Modifier,
    selectedAddress: String = "",
    selectedAddressLatitude: Double? = null,
    selectedAddressLongitude: Double? = null,
    onBackClick: () -> Unit = {},
    onSkipClick: () -> Unit = {},
    onSearchAddressClick: () -> Unit = {},
    onSaveClick: (ParentInfoInputState) -> Unit = {}
) {
    var name by rememberSaveable { mutableStateOf("") }
    var relationship by rememberSaveable { mutableStateOf<SeniorRelationship?>(null) }
    var customRelationship by rememberSaveable { mutableStateOf("") }
    var customRelationshipDraft by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var address by rememberSaveable { mutableStateOf(selectedAddress) }
    var addressLatitude by rememberSaveable { mutableStateOf(selectedAddressLatitude) }
    var addressLongitude by rememberSaveable { mutableStateOf(selectedAddressLongitude) }
    var addressDetail by rememberSaveable { mutableStateOf("") }
    var showCustomRelationshipSheet by rememberSaveable { mutableStateOf(false) }
    var showBirthDateSheet by rememberSaveable { mutableStateOf(false) }
    var showSkipNotice by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(
        selectedAddress,
        selectedAddressLatitude,
        selectedAddressLongitude
    ) {
        if (selectedAddress.isNotBlank() && selectedAddress != address) {
            address = selectedAddress
        }
        addressLatitude = selectedAddressLatitude
        addressLongitude = selectedAddressLongitude
    }

    val isRelationshipValid = when (relationship) {
        SeniorRelationship.Custom -> customRelationship.isNotBlank()
        null -> false
        else -> true
    }
    val isSaveEnabled = name.isNotBlank() &&
        isRelationshipValid &&
        parseBirthDate(birthDate) != null &&
        phoneNumber.text.count(Char::isDigit) == RequiredPhoneNumberLength

    if (showCustomRelationshipSheet) {
        CustomRelationshipBottomSheet(
            value = customRelationshipDraft,
            onValueChange = { customRelationshipDraft = it.take(CustomRelationshipMaxLength) },
            onCancel = { showCustomRelationshipSheet = false },
            onConfirm = {
                val trimmedRelationship = customRelationshipDraft.trim()

                if (trimmedRelationship.isNotEmpty()) {
                    customRelationship = trimmedRelationship.take(CustomRelationshipMaxLength)
                    relationship = SeniorRelationship.Custom
                    showCustomRelationshipSheet = false
                }
            }
        )
    }

    if (showBirthDateSheet) {
        BirthDateBottomSheet(
            initialBirthDate = birthDate,
            onDismiss = { showBirthDateSheet = false },
            onConfirm = { selectedDate ->
                birthDate = selectedDate.toBirthDateString()
                showBirthDateSheet = false
            }
        )
    }

    if (showSkipNotice) {
        SeniorInfoSkipNoticeDialog(
            onDismiss = { showSkipNotice = false },
            onConfirm = {
                showSkipNotice = false
                onSkipClick()
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .statusBarsPadding()
    ) {
        SeniorInfoTopBar(onBackClick = onBackClick)

        SeniorInfoFormContent(
            name = name,
            onNameChange = { name = it },
            selectedRelationship = if (showCustomRelationshipSheet) {
                SeniorRelationship.Custom
            } else {
                relationship
            },
            customRelationshipLabel = customRelationship,
            onRelationshipClick = { selectedRelationship ->
                if (selectedRelationship == SeniorRelationship.Custom) {
                    customRelationshipDraft = customRelationship
                    showCustomRelationshipSheet = true
                } else {
                    customRelationship = ""
                    relationship = selectedRelationship
                }
            },
            birthDate = birthDate,
            onBirthDateClick = { showBirthDateSheet = true },
            phoneNumber = phoneNumber,
            onPhoneNumberChange = { phoneNumber = formatPhoneFieldValue(it) },
            address = address,
            onAddressChange = { address = it },
            addressDetail = addressDetail,
            onAddressDetailChange = { addressDetail = it },
            onSearchAddressClick = onSearchAddressClick,
            modifier = Modifier.weight(1f)
        )

        SeniorInfoBottomActions(
            onSkipClick = { showSkipNotice = true },
            onSaveClick = {
                val selectedRelationship = relationship

                if (isSaveEnabled && selectedRelationship != null) {
                    onSaveClick(
                        ParentInfoInputState(
                            name = name,
                            relationship = selectedRelationship,
                            customRelationship = if (
                                selectedRelationship == SeniorRelationship.Custom
                            ) {
                                customRelationship
                            } else {
                                ""
                            },
                            birthDate = birthDate,
                            phoneNumber = phoneNumber.text,
                            address = address,
                            addressDetail = addressDetail,
                            addressLatitude = addressLatitude,
                            addressLongitude = addressLongitude
                        )
                    )
                }
            },
            isSaveEnabled = isSaveEnabled
        )
    }
}

@Composable
private fun SeniorInfoFormContent(
    name: String,
    onNameChange: (String) -> Unit,
    selectedRelationship: SeniorRelationship?,
    customRelationshipLabel: String,
    onRelationshipClick: (SeniorRelationship) -> Unit,
    birthDate: String,
    onBirthDateClick: () -> Unit,
    phoneNumber: TextFieldValue,
    onPhoneNumberChange: (TextFieldValue) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    addressDetail: String,
    onAddressDetailChange: (String) -> Unit,
    onSearchAddressClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        SeniorInfoTitle()

        Spacer(modifier = Modifier.height(24.dp))
        InputLabel(text = "이름")
        Spacer(modifier = Modifier.height(6.dp))
        SeniorInfoTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = "이름 입력",
            keyboardType = KeyboardType.Text,
            trailingContent = if (name.isNotEmpty()) {
                {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sm_close),
                        contentDescription = "이름 지우기",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onNameChange("") },
                        tint = SeniorOnColors.Gray400
                    )
                }
            } else {
                null
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        InputLabel(text = "관계")
        Spacer(modifier = Modifier.height(6.dp))
        RelationshipSelector(
            selectedRelationship = selectedRelationship,
            customRelationshipLabel = customRelationshipLabel,
            onRelationshipClick = onRelationshipClick
        )

        Spacer(modifier = Modifier.height(16.dp))
        InputLabel(text = "생년월일", optionalText = " (필수)")
        Spacer(modifier = Modifier.height(6.dp))
        BirthDateSelector(
            value = birthDate,
            onClick = onBirthDateClick
        )

        Spacer(modifier = Modifier.height(16.dp))
        InputLabel(text = "전화번호", optionalText = " (필수)")
        Spacer(modifier = Modifier.height(6.dp))
        SeniorInfoPhoneTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            placeholder = "010-0000-0000"
        )

        Spacer(modifier = Modifier.height(16.dp))
        InputLabel(text = "자택 주소", optionalText = " (선택)")
        Spacer(modifier = Modifier.height(4.dp))
        SeniorInfoTextField(
            value = address,
            onValueChange = onAddressChange,
            placeholder = "주소 검색",
            readOnly = true,
            onClick = onSearchAddressClick,
            trailingContent = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = "주소 검색",
                    modifier = Modifier.size(24.dp),
                    tint = SeniorOnColors.Gray500
                )
            }
        )
        Spacer(modifier = Modifier.height(6.dp))
        SeniorInfoTextField(
            value = addressDetail,
            onValueChange = onAddressDetailChange,
            placeholder = "상세 주소 입력",
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "외출·귀가 알림 기준 위치로 사용돼요.",
            style = SeniorOnTextStyles.CaptionMedium,
            color = SeniorOnColors.Gray400
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Preview(
    name = "00. 전체 정보 입력 화면",
    group = "Senior Info",
    showBackground = true,
    widthDp = 360,
    heightDp = 707
)
@Composable
private fun ParentInfoInputScreenPreview() {
    SENIOR_ONTheme {
        ParentInfoInputScreen()
    }
}

@Preview(
    name = "01. 관계 직접 작성",
    group = "Senior Info",
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
private fun CustomRelationshipBottomSheetPreview() {
    SENIOR_ONTheme {
        SeniorInfoBottomSheetPreviewFrame(scrimAlpha = 0.2f) {
            CustomRelationshipSheetContent(
                value = "배우자",
                onValueChange = {},
                onCancel = {},
                onConfirm = {}
            )
        }
    }
}

@Preview(
    name = "02. 생년월일 선택",
    group = "Senior Info",
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
private fun BirthDateBottomSheetPreview() {
    SENIOR_ONTheme {
        SeniorInfoBottomSheetPreviewFrame(scrimAlpha = 0.45f) {
            BirthDateSheetContent(
                initialBirthDate = "1956.01.01",
                onDismiss = {},
                onConfirm = {}
            )
        }
    }
}

@Preview(
    name = "03. 다음에 하기 팝업",
    group = "Senior Info",
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
private fun SeniorInfoSkipNoticeDialogPreview() {
    SENIOR_ONTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            SeniorInfoSkipNoticeContent(
                onDismiss = {},
                onConfirm = {}
            )
        }
    }
}

@Composable
private fun SeniorInfoBottomSheetPreviewFrame(
    scrimAlpha: Float,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SeniorOnColors.Black.copy(alpha = scrimAlpha)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = SeniorOnRadius.XLarge,
                        topEnd = SeniorOnRadius.XLarge
                    )
                )
                .background(SeniorOnColors.White)
        ) {
            content()
        }
    }
}
