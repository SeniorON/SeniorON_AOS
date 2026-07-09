package com.example.senior_on.ui.senior_info

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalMaterial3Api::class)
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
                            customRelationship = if (selectedRelationship == SeniorRelationship.Custom) {
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
            keyboardType = KeyboardType.Text
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
        SeniorInfoTextField(
            value = birthDate,
            onValueChange = {},
            placeholder = "YYYY.MM.DD",
            readOnly = true,
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
                SearchIcon(
                    modifier = Modifier.size(24.dp),
                    color = SeniorOnColors.Gray400
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

@Composable
private fun SeniorInfoTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
                elevation = 8.dp,
                spotColor = SeniorOnColors.Gray200.copy(alpha = 0.45f)
            )
            .background(SeniorOnColors.White)
            .padding(start = 10.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBackClick
                ),
            contentAlignment = Alignment.Center
        ) {
            BackArrowIcon(
                modifier = Modifier.size(24.dp),
                color = SeniorOnColors.Gray800
            )
        }

        Text(
            text = "정보 입력",
            modifier = Modifier.padding(start = 2.dp),
            style = SeniorOnTextStyles.HeadingS,
            color = SeniorOnColors.Gray800
        )
    }
}

@Composable
private fun SeniorInfoTitle() {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = SeniorOnColors.Primary600)) {
                append("시니어")
            }
            append(" 정보를 입력해 주세요")
        },
        style = SeniorOnTextStyles.OnboardingHeading,
        color = SeniorOnColors.Gray800
    )
    Spacer(modifier = Modifier.height(9.dp))
    Text(
        text = "나중에 설정에서 변경할 수 있어요.",
        style = SeniorOnTextStyles.BodySMedium,
        color = SeniorOnColors.Gray500
    )
}

@Composable
private fun InputLabel(
    text: String,
    modifier: Modifier = Modifier,
    optionalText: String? = null
) {
    Text(
        text = buildAnnotatedString {
            append(text)
            optionalText?.let {
                withStyle(SpanStyle(color = SeniorOnColors.Gray500)) {
                    append(it)
                }
            }
        },
        modifier = modifier,
        style = SeniorOnTextStyles.BodyMSemiBold,
        color = SeniorOnColors.Gray800
    )
}

@Composable
private fun SeniorInfoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor = if (isFocused) SeniorOnColors.Primary600 else SeniorOnColors.Gray200

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(43.dp)
            .clip(shape)
            .background(SeniorOnColors.White)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            )
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxSize(),
            interactionSource = interactionSource,
            readOnly = readOnly,
            singleLine = true,
            textStyle = SeniorOnTextStyles.BodyMRegular.copy(color = SeniorOnColors.Gray800),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 14.dp, end = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = SeniorOnTextStyles.BodyMRegular,
                                color = SeniorOnColors.Gray300
                            )
                        }
                        innerTextField()
                    }

                    trailingContent?.let {
                        Spacer(modifier = Modifier.width(10.dp))
                        it()
                    }
                }
            }
        )

        onClick?.let {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(onClick = it)
            )
        }
    }
}

@Composable
private fun SeniorInfoPhoneTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor = if (isFocused) SeniorOnColors.Primary600 else SeniorOnColors.Gray200

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(43.dp)
            .clip(shape)
            .background(SeniorOnColors.White)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            ),
        interactionSource = interactionSource,
        singleLine = true,
        textStyle = SeniorOnTextStyles.BodyMRegular.copy(color = SeniorOnColors.Gray800),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        ),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 14.dp, end = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.text.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = SeniorOnTextStyles.BodyMRegular,
                            color = SeniorOnColors.Gray300
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Composable
private fun RelationshipSelector(
    selectedRelationship: SeniorRelationship?,
    customRelationshipLabel: String,
    onRelationshipClick: (SeniorRelationship) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(SeniorOnColors.Background3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SeniorRelationship.entries.forEach { relationship ->
            val isSelected = relationship == selectedRelationship
            val itemShape = RoundedCornerShape(SeniorOnRadius.Small)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(itemShape)
                    .background(if (isSelected) SeniorOnColors.Primary100 else Color.Transparent)
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                width = 1.dp,
                                color = SeniorOnColors.Primary400,
                                shape = itemShape
                            )
                        } else {
                            Modifier
                        }
                    )
                    .clickable { onRelationshipClick(relationship) },
                contentAlignment = Alignment.Center
            ) {
                val displayLabel = if (
                    relationship == SeniorRelationship.Custom &&
                    customRelationshipLabel.isNotBlank()
                ) {
                    customRelationshipLabel
                } else {
                    relationship.label
                }

                Text(
                    text = displayLabel,
                    style = SeniorOnTextStyles.BodySSemiBold,
                    color = if (isSelected) SeniorOnColors.Primary600 else SeniorOnColors.Gray600,
                    textDecoration = if (relationship == SeniorRelationship.Custom) {
                        TextDecoration.Underline
                    } else {
                        null
                    }
                )
            }
        }
    }
}

@Composable
private fun SeniorInfoBottomActions(
    onSkipClick: () -> Unit,
    onSaveClick: () -> Unit,
    isSaveEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SeniorOnColors.White)
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
    ) {
        SeniorInfoWarning()
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SeniorInfoActionButton(
                text = "다음에 하기",
                onClick = onSkipClick,
                modifier = Modifier.weight(1f),
                style = SeniorInfoButtonStyle.Outlined
            )
            SeniorInfoActionButton(
                text = "저장 후 시작",
                onClick = onSaveClick,
                modifier = Modifier.weight(1f),
                style = SeniorInfoButtonStyle.Filled,
                enabled = isSaveEnabled
            )
        }
    }
}

@Composable
private fun SeniorInfoWarning(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(SeniorOnColors.Gray200, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "!",
                style = SeniorOnTextStyles.BodySSemiBold,
                color = SeniorOnColors.White
            )
        }

        Spacer(modifier = Modifier.width(9.dp))
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
}

private enum class SeniorInfoButtonStyle {
    Outlined,
    Filled
}

@Composable
private fun SeniorInfoActionButton(
    text: String,
    onClick: () -> Unit,
    style: SeniorInfoButtonStyle,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)
    val backgroundColor = when (style) {
        SeniorInfoButtonStyle.Outlined -> SeniorOnColors.White
        SeniorInfoButtonStyle.Filled -> if (enabled) SeniorOnColors.Primary600 else SeniorOnColors.Primary300
    }
    val contentColor = when (style) {
        SeniorInfoButtonStyle.Outlined -> SeniorOnColors.Primary600
        SeniorInfoButtonStyle.Filled -> SeniorOnColors.White
    }
    val borderColor = when (style) {
        SeniorInfoButtonStyle.Outlined -> SeniorOnColors.Primary600
        SeniorInfoButtonStyle.Filled -> Color.Transparent
    }

    Box(
        modifier = modifier
            .height(60.dp)
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            )
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.ButtonM,
            color = contentColor
        )
    }
}

@Composable
private fun SeniorInfoSkipNoticeDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .requiredSize(width = 238.dp, height = 241.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(SeniorOnColors.White)
                .padding(start = 25.dp, end = 25.dp, top = 31.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(SeniorOnColors.Background3, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_clock_1),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(SeniorOnColors.Primary500)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "다음에 하기",
                style = SeniorOnTextStyles.BodyLBold,
                color = SeniorOnColors.Gray800
            )

            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "부모님 정보를 입력하지 않으면\n일부 기능이 제한돼요.",
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray500,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(23.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SeniorInfoSkipNoticeButton(
                    text = "취소",
                    backgroundColor = SeniorOnColors.Gray200,
                    contentColor = SeniorOnColors.Gray500,
                    onClick = onDismiss
                )
                SeniorInfoSkipNoticeButton(
                    text = "확인",
                    backgroundColor = SeniorOnColors.Primary600,
                    contentColor = SeniorOnColors.White,
                    onClick = onConfirm
                )
            }
        }
    }
}

@Composable
private fun SeniorInfoSkipNoticeButton(
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .requiredSize(width = 84.dp, height = 34.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.ButtonS,
            color = contentColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthDateBottomSheet(
    initialBirthDate: String,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val today = remember { LocalDate.now() }
    val initialDate = remember(initialBirthDate, today) {
        parseBirthDate(initialBirthDate)
            ?: LocalDate.of(
                (today.year - DefaultBirthAge).coerceAtLeast(MinBirthYear),
                1,
                1
            )
    }
    var selectedYear by rememberSaveable { mutableStateOf(initialDate.year) }
    var selectedMonth by rememberSaveable { mutableStateOf(initialDate.monthValue) }
    var selectedDay by rememberSaveable { mutableStateOf(initialDate.dayOfMonth) }

    val years = remember(today.year) {
        (MinBirthYear..today.year).toList()
    }
    val months = remember(selectedYear, today) {
        val lastMonth = if (selectedYear == today.year) today.monthValue else 12
        (1..lastMonth).toList()
    }
    val adjustedMonth = selectedMonth.coerceIn(months.first(), months.last())

    LaunchedEffect(adjustedMonth) {
        if (selectedMonth != adjustedMonth) {
            selectedMonth = adjustedMonth
        }
    }

    val days = remember(selectedYear, adjustedMonth, today) {
        val lastDayOfMonth = YearMonth.of(selectedYear, adjustedMonth).lengthOfMonth()
        val lastDay = if (
            selectedYear == today.year &&
            adjustedMonth == today.monthValue
        ) {
            today.dayOfMonth
        } else {
            lastDayOfMonth
        }
        (1..lastDay).toList()
    }
    val adjustedDay = selectedDay.coerceIn(days.first(), days.last())

    LaunchedEffect(adjustedDay) {
        if (selectedDay != adjustedDay) {
            selectedDay = adjustedDay
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SeniorOnColors.White,
        scrimColor = SeniorOnColors.Black.copy(alpha = 0.45f),
        dragHandle = null,
        shape = RoundedCornerShape(
            topStart = SeniorOnRadius.XLarge,
            topEnd = SeniorOnRadius.XLarge
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Text(
                    text = "생년월일",
                    modifier = Modifier.align(Alignment.Center),
                    style = SeniorOnTextStyles.BodyLBold,
                    color = SeniorOnColors.Gray800
                )
                Text(
                    text = "닫기",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clip(RoundedCornerShape(SeniorOnRadius.Small))
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    style = SeniorOnTextStyles.BodyMSemiBold,
                    color = SeniorOnColors.Primary600
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateWheelPicker(
                    values = years,
                    selectedValue = selectedYear,
                    valueLabel = Int::toString,
                    onValueSelected = { selectedYear = it },
                    modifier = Modifier.weight(1f)
                )
                DateWheelPicker(
                    values = months,
                    selectedValue = adjustedMonth,
                    valueLabel = { "${it}월" },
                    onValueSelected = { selectedMonth = it },
                    modifier = Modifier.weight(1f)
                )
                DateWheelPicker(
                    values = days,
                    selectedValue = adjustedDay,
                    valueLabel = Int::toString,
                    onValueSelected = { selectedDay = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            SeniorInfoActionButton(
                text = "확인",
                onClick = {
                    onConfirm(
                        LocalDate.of(
                            selectedYear,
                            adjustedMonth,
                            adjustedDay
                        )
                    )
                },
                style = SeniorInfoButtonStyle.Filled,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DateWheelPicker(
    values: List<Int>,
    selectedValue: Int,
    valueLabel: (Int) -> String,
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = values.indexOf(selectedValue).coerceAtLeast(0)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedIndex
    )
    val coroutineScope = rememberCoroutineScope()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(listState, values) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (
                layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset
                ) / 2

            layoutInfo.visibleItemsInfo.minByOrNull { item ->
                abs(item.offset + item.size / 2 - viewportCenter)
            }?.index
        }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { index ->
                values.getOrNull(index)?.let(onValueSelected)
            }
    }

    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress) {
            listState.scrollToItem(selectedIndex)
        }
    }

    Box(
        modifier = modifier.height(DateWheelHeight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(DateWheelItemHeight)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(SeniorOnRadius.Small))
                .background(SeniorOnColors.Background3)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(vertical = DateWheelItemHeight),
            flingBehavior = flingBehavior
        ) {
            itemsIndexed(
                items = values,
                key = { _, value -> value }
            ) { index, value ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DateWheelItemHeight)
                        .clickable {
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = valueLabel(value),
                        style = if (value == selectedValue) {
                            SeniorOnTextStyles.BodyLBold
                        } else {
                            SeniorOnTextStyles.BodyLMedium
                        },
                        color = if (value == selectedValue) {
                            SeniorOnColors.Gray800
                        } else {
                            SeniorOnColors.Gray400
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomRelationshipBottomSheet(
    value: String,
    onValueChange: (String) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onCancel,
        containerColor = SeniorOnColors.White,
        scrimColor = SeniorOnColors.Black.copy(alpha = 0.2f),
        dragHandle = null,
        shape = RoundedCornerShape(
            topStart = SeniorOnRadius.XLarge,
            topEnd = SeniorOnRadius.XLarge
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "취소",
                        modifier = Modifier
                            .clip(RoundedCornerShape(SeniorOnRadius.Small))
                            .clickable(onClick = onCancel)
                            .padding(vertical = 8.dp),
                        style = SeniorOnTextStyles.BodyMSemiBold,
                        color = SeniorOnColors.Primary600
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "직접 작성",
                        style = SeniorOnTextStyles.BodyMSemiBold,
                        color = SeniorOnColors.Gray800
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "확인",
                        modifier = Modifier
                            .clip(RoundedCornerShape(SeniorOnRadius.Small))
                            .clickable(onClick = onConfirm)
                            .padding(vertical = 8.dp),
                        style = SeniorOnTextStyles.BodyMSemiBold,
                        color = SeniorOnColors.Primary600
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            CustomRelationshipInputField(
                value = value,
                onValueChange = onValueChange,
                placeholder = "관계(기타)",
                modifier = Modifier.focusRequester(focusRequester)
            )
        }
    }
}

@Composable
private fun CustomRelationshipInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        singleLine = true,
        textStyle = SeniorOnTextStyles.BodyMRegular.copy(color = SeniorOnColors.Gray800),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        decorationBox = { innerTextField ->
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = SeniorOnTextStyles.BodyMRegular,
                            color = SeniorOnColors.Gray300
                        )
                    }
                    innerTextField()
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(SeniorOnColors.Gray200)
                )
            }
        }
    )
}

private const val CustomRelationshipMaxLength = 6
private const val MinBirthYear = 1900
private const val DefaultBirthAge = 70
private const val RequiredPhoneNumberLength = 11
private val DateWheelItemHeight = 44.dp
private val DateWheelHeight = 132.dp

private fun parseBirthDate(value: String): LocalDate? {
    val dateParts = value.split(".")
    if (dateParts.size != 3) {
        return null
    }

    return runCatching {
        LocalDate.of(
            dateParts[0].toInt(),
            dateParts[1].toInt(),
            dateParts[2].toInt()
        )
    }.getOrNull()?.takeUnless { it.isAfter(LocalDate.now()) }
}

private fun LocalDate.toBirthDateString(): String {
    return "${year.toString().padStart(4, '0')}." +
        "${monthValue.toString().padStart(2, '0')}." +
        dayOfMonth.toString().padStart(2, '0')
}

private fun formatPhoneFieldValue(value: TextFieldValue): TextFieldValue {
    val formattedPhoneNumber = formatPhoneNumber(value.text)

    return TextFieldValue(
        text = formattedPhoneNumber,
        selection = TextRange(formattedPhoneNumber.length)
    )
}

private fun formatPhoneNumber(value: String): String {
    val digits = value.filter(Char::isDigit).take(11)

    return when {
        digits.length <= 3 -> digits
        digits.length <= 7 -> "${digits.substring(0, 3)}-${digits.substring(3)}"
        else -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
    }
}

@Composable
private fun BackArrowIcon(
    modifier: Modifier = Modifier,
    color: Color = SeniorOnColors.Gray800
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.4.dp.toPx()
        val centerY = size.height / 2f
        drawLine(
            color = color,
            start = Offset(size.width * 0.26f, centerY),
            end = Offset(size.width * 0.78f, centerY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.28f, centerY),
            end = Offset(size.width * 0.52f, size.height * 0.27f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.28f, centerY),
            end = Offset(size.width * 0.52f, size.height * 0.73f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun SearchIcon(
    modifier: Modifier = Modifier,
    color: Color = SeniorOnColors.Gray400
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(
            color = color,
            radius = size.minDimension * 0.29f,
            center = Offset(size.width * 0.44f, size.height * 0.42f),
            style = stroke
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.64f, size.height * 0.64f),
            end = Offset(size.width * 0.82f, size.height * 0.82f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
    }
}

@Preview(
    name = "Senior Info Input",
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
