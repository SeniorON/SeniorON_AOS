package com.example.senior_on.ui.health

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpOffset
import com.example.senior_on.R
import com.example.senior_on.data.repository.HospitalSpecialtyRepository
import com.example.senior_on.data.repository.MockHospitalSpecialtyRepository
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class HospitalEditorMode { Add, View, Edit }

enum class HospitalReminder(val label: String) {
    DayBefore("하루 전"),
    SameDay("당일"),
    None("받지 않음")
}

data class HospitalAppointmentDraft(
    val hospitalName: String,
    val specialty: String,
    val date: LocalDate,
    val time: LocalTime,
    val reminder: HospitalReminder
)

@Composable
fun HospitalAppointmentScreen(
    mode: HospitalEditorMode,
    initialDate: LocalDate,
    initialDraft: HospitalAppointmentDraft? = null,
    specialtyRepository: HospitalSpecialtyRepository = MockHospitalSpecialtyRepository,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onSaveClick: (HospitalAppointmentDraft) -> Unit,
    onDeleteClick: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    var hospitalName by rememberSaveable { mutableStateOf(initialDraft?.hospitalName.orEmpty()) }
    var specialty by rememberSaveable { mutableStateOf(initialDraft?.specialty.orEmpty()) }
    var specialtyOptions by remember { mutableStateOf(emptyList<String>()) }
    var isSpecialtyDirectInput by rememberSaveable { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(initialDraft?.date ?: initialDate) }
    var selectedTime by remember { mutableStateOf(initialDraft?.time) }
    var reminder by rememberSaveable {
        mutableStateOf(initialDraft?.reminder ?: HospitalReminder.DayBefore)
    }
    var showSpecialtySheet by rememberSaveable { mutableStateOf(false) }
    var showDateSheet by rememberSaveable { mutableStateOf(false) }
    var showTimeSheet by rememberSaveable { mutableStateOf(false) }
    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(specialtyRepository) {
        specialtyOptions = specialtyRepository.getSpecialties()
    }

    val isComplete = hospitalName.isNotBlank() && specialty.isNotBlank() && selectedTime != null
    val hasInput = hospitalName.isNotBlank() || specialty.isNotBlank() || selectedTime != null
    val hasChanges = initialDraft?.let { initial ->
        hospitalName != initial.hospitalName ||
            specialty != initial.specialty ||
            selectedDate != initial.date ||
            selectedTime != initial.time ||
            reminder != initial.reminder
    } ?: false
    val saveDraft: () -> Unit = {
        val time = selectedTime
        if (time != null) {
            onSaveClick(
                HospitalAppointmentDraft(
                    hospitalName = hospitalName,
                    specialty = specialty,
                    date = selectedDate,
                    time = time,
                    reminder = reminder
                )
            )
        }
    }
    val requestBack = {
        when {
            mode == HospitalEditorMode.Add && hasInput -> showExitDialog = true
            mode != HospitalEditorMode.Add && hasChanges -> showExitDialog = true
            else -> onBackClick()
        }
    }
    BackHandler(onBack = requestBack)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.SupportWhite100)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { focusManager.clearFocus() }
                )
            }
    ) {
        HospitalEditorTopBar(
            title = when (mode) {
                HospitalEditorMode.Add -> "진료 추가하기"
                HospitalEditorMode.View -> "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일 진료"
                HospitalEditorMode.Edit -> "진료 수정하기"
            },
            onBackClick = requestBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            HospitalFormSection(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp)) {
                HospitalFormLabel("병원 이름")
                Spacer(modifier = Modifier.height(12.dp))
                HospitalTextInput(
                    value = hospitalName,
                    placeholder = "병원이름",
                    onValueChange = { hospitalName = it },
                    filled = true,
                    enabled = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp).fillMaxWidth().background(SeniorOnColors.Background3))

            HospitalFormSection(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)) {
                HospitalFormLabel("진료 과목")
                Spacer(modifier = Modifier.height(12.dp))
                HospitalSpecialtySelectionField(
                    value = specialty,
                    placeholder = "진료과목 찾기",
                    isFocused = showSpecialtySheet || isSpecialtyDirectInput,
                    directInput = isSpecialtyDirectInput,
                    onValueChange = { specialty = it },
                    onDirectInputComplete = {
                        specialty = specialty.trim()
                        isSpecialtyDirectInput = false
                    },
                    onClick = {
                        focusManager.clearFocus()
                        showSpecialtySheet = true
                    },
                    onClear = {
                        specialty = ""
                        isSpecialtyDirectInput = false
                    },
                    enabled = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                HospitalFormLabel("진료일")
                Spacer(modifier = Modifier.height(10.dp))
                HospitalSelectionField(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("M월 d일")),
                    placeholder = "날짜 선택",
                    iconResId = R.drawable.ic_illust_hospital_schedule_2,
                    onClick = {
                        focusManager.clearFocus()
                        showDateSheet = true
                    },
                    isFocused = showDateSheet,
                    enabled = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                HospitalFormLabel("진료 시간")
                Spacer(modifier = Modifier.height(12.dp))
                HospitalSelectionField(
                    text = selectedTime?.toKoreanTime().orEmpty(),
                    placeholder = "시간 선택",
                    iconResId = R.drawable.ic_clock_1,
                    onClick = {
                        focusManager.clearFocus()
                        showTimeSheet = true
                    },
                    isFocused = showTimeSheet,
                    iconAtEnd = true,
                    enabled = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                HospitalFormLabel("알림 선택")
                Text(
                    text = "일정 알림을 받을 시간을 선택하세요.",
                    style = SeniorOnTextStyles.BodySMedium,
                    color = SeniorOnColors.Gray300
                )
                Spacer(modifier = Modifier.height(12.dp))
                HospitalReminderSelector(
                    selected = reminder,
                    onSelected = {
                        focusManager.clearFocus()
                        reminder = it
                    },
                    enabled = true
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (mode) {
                HospitalEditorMode.Add -> {
                    HospitalEditorSaveButton(
                        label = "진료 추가하기",
                        enabled = isComplete,
                        onClick = saveDraft,
                        iconResId = R.drawable.ic_plus,
                        modifier = Modifier.weight(1f)
                    )
                }
                HospitalEditorMode.View -> {
                    HospitalEditorDeleteButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                    HospitalEditorSaveButton(
                        label = "수정하기",
                        enabled = isComplete,
                        onClick = saveDraft,
                        iconResId = R.drawable.ic_pencil,
                        modifier = Modifier.weight(1f)
                    )
                }
                HospitalEditorMode.Edit -> {
                    HospitalEditorSaveButton(
                        label = "수정하기",
                        enabled = isComplete,
                        onClick = saveDraft,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (showSpecialtySheet) {
        HospitalSpecialtyBottomSheet(
            specialties = specialtyOptions,
            selected = specialty,
            onCancel = { showSpecialtySheet = false },
            onDirectInput = {
                specialty = ""
                showSpecialtySheet = false
                isSpecialtyDirectInput = true
            },
            onSave = {
                specialty = it
                isSpecialtyDirectInput = false
                showSpecialtySheet = false
            }
        )
    }
    if (showDateSheet) {
        ScheduleDatePickerBottomSheet(
            initialDate = selectedDate,
            onDismiss = { showDateSheet = false },
            onConfirm = {
                selectedDate = it
                showDateSheet = false
            }
        )
    }
    if (showTimeSheet) {
        ScheduleTimePickerBottomSheet(
            initialTime = selectedTime ?: LocalTime.of(13, 0),
            onDismiss = { showTimeSheet = false },
            onConfirm = {
                selectedTime = it
                showTimeSheet = false
            }
        )
    }
    if (showExitDialog) {
        SeniorOnConfirmDialog(
            iconResId = R.drawable.ic_modal_unsaved,
            title = if (mode == HospitalEditorMode.Add) {
                "등록하지 않고\n나가시겠어요?"
            } else {
                "저장하지 않고\n나가시겠어요?"
            },
            description = if (mode == HospitalEditorMode.Add) {
                "지금 나가면 입력한 내용이\n저장되지 않습니다."
            } else {
                "지금 나가면 수정한 내용이\n저장되지 않습니다."
            },
            cancelLabel = "나가기",
            confirmLabel = "계속 입력",
            confirmColor = SeniorOnColors.Primary600,
            onCancel = onBackClick,
            onConfirm = { showExitDialog = false }
        )
    }
    if (showDeleteDialog) {
        SeniorOnDeleteConfirmDialog(
            title = "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일 진료를\n삭제할까요?",
            onCancel = { showDeleteDialog = false },
            onConfirm = onDeleteClick
        )
    }
}

@Composable
private fun HospitalEditorTopBar(title: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .dropShadow(
                shape = RectangleShape,
                shadow = Shadow(
                    radius = 12.dp,
                    spread = 0.dp,
                    color = Color.Black.copy(alpha = 15f / 255f),
                    offset = DpOffset(x = 0.dp, y = 4.dp)
                )
            )
            .background(SeniorOnColors.SupportWhite100)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = "뒤로가기",
                tint = SeniorOnColors.Gray800,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = SeniorOnTextStyles.BodyLBold, color = SeniorOnColors.Gray800)
    }
}

@Composable
private fun HospitalFormSection(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = modifier.fillMaxWidth()) { content() }
}

@Composable
private fun HospitalFormLabel(text: String) {
    Text(text = text, style = SeniorOnTextStyles.BodyMSemiBold, color = SeniorOnColors.Gray800)
}

@Composable
private fun HospitalTextInput(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    filled: Boolean,
    enabled: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(SeniorOnRadius.Small)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        textStyle = SeniorOnTextStyles.BodyMMedium.copy(color = SeniorOnColors.Gray800),
        modifier = Modifier
            .fillMaxWidth()
            .height(49.dp)
            .onFocusChanged { isFocused = it.isFocused },
        singleLine = true,
        decorationBox = { inner ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(
                        if (filled) SeniorOnColors.Background3
                        else SeniorOnColors.SupportWhite100
                    )
                    .border(
                        width = 1.dp,
                        color = when {
                            isFocused && filled -> SeniorOnColors.Primary500
                            isFocused -> SeniorOnColors.Primary600
                            filled -> Color.Transparent
                            else -> SeniorOnColors.Gray200
                        },
                        shape = shape
                    )
                    .padding(start = 12.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = SeniorOnTextStyles.BodyMMedium,
                            color = SeniorOnColors.Gray300
                        )
                    }
                    inner()
                }
                if (enabled && value.isNotEmpty() && !isFocused) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "지우기",
                        tint = SeniorOnColors.Gray400,
                        modifier = Modifier.size(24.dp).clickable { onValueChange("") }
                    )
                }
            }
        }
    )
}

@Composable
private fun HospitalSpecialtySelectionField(
    value: String,
    placeholder: String,
    isFocused: Boolean,
    directInput: Boolean,
    onValueChange: (String) -> Unit,
    onDirectInputComplete: () -> Unit,
    onClick: () -> Unit,
    onClear: () -> Unit,
    enabled: Boolean
) {
    if (directInput) {
        HospitalSpecialtyDirectInputField(
            value = value,
            placeholder = placeholder,
            onValueChange = onValueChange,
            onComplete = onDirectInputComplete
        )
        return
    }

    val shape = RoundedCornerShape(SeniorOnRadius.Small)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(43.dp)
            .clip(shape)
            .background(SeniorOnColors.SupportWhite100)
            .border(
                width = 1.dp,
                color = if (isFocused) SeniorOnColors.Primary600 else SeniorOnColors.Gray200,
                shape = shape
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = null,
            tint = SeniorOnColors.Gray400,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = SeniorOnTextStyles.BodyMMedium,
                    color = SeniorOnColors.Gray300
                )
            } else {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(SeniorOnColors.Primary600)
                        .padding(start = 12.dp, top = 4.dp, end = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = value,
                        style = SeniorOnTextStyles.BodySMedium,
                        color = SeniorOnColors.SupportWhite100
                    )
                    if (enabled) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "진료 과목 지우기",
                            tint = SeniorOnColors.SupportWhite100,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable(onClick = onClear)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HospitalSpecialtyDirectInputField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    onComplete: () -> Unit
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var hasReceivedFocus by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = SeniorOnTextStyles.BodyMMedium.copy(color = SeniorOnColors.Gray800),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
                onComplete()
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(43.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    hasReceivedFocus = true
                } else if (hasReceivedFocus) {
                    hasReceivedFocus = false
                    onComplete()
                }
            },
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(SeniorOnColors.SupportWhite100)
                    .border(1.dp, SeniorOnColors.Primary600, shape)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = null,
                    tint = SeniorOnColors.Gray400,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = SeniorOnTextStyles.BodyMMedium,
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
private fun HospitalSelectionField(
    text: String,
    placeholder: String,
    iconResId: Int,
    onClick: () -> Unit,
    isFocused: Boolean = false,
    iconAtEnd: Boolean = false,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(43.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(SeniorOnColors.SupportWhite100)
            .border(
                width = 1.dp,
                color = if (isFocused) SeniorOnColors.Primary600 else SeniorOnColors.Gray200,
                shape = RoundedCornerShape(SeniorOnRadius.Small)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!iconAtEnd) {
            Icon(painterResource(iconResId), null, Modifier.size(24.dp), Color.Unspecified)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            Text(
                text = text.ifEmpty { placeholder },
                style = SeniorOnTextStyles.BodyMMedium,
                color = if (text.isEmpty()) SeniorOnColors.Gray300 else SeniorOnColors.Gray800
            )
        }
        if (iconAtEnd) {
            Icon(painterResource(iconResId), null, Modifier.size(24.dp), SeniorOnColors.Gray400)
        }
    }
}

@Composable
private fun HospitalReminderSelector(
    selected: HospitalReminder,
    onSelected: (HospitalReminder) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(SeniorOnColors.Background3)
    ) {
        HospitalReminder.entries.forEach { item ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .then(
                        if (item == selected) {
                            Modifier
                                .clip(RoundedCornerShape(SeniorOnRadius.Small))
                                .background(SeniorOnColors.Primary100)
                                .border(
                                    1.dp,
                                    SeniorOnColors.Primary400,
                                    RoundedCornerShape(SeniorOnRadius.Small)
                                )
                        } else Modifier
                    )
                    .clickable(enabled = enabled) { onSelected(item) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.label,
                    style = SeniorOnTextStyles.BodySSemiBold,
                    color = if (item == selected) SeniorOnColors.Primary600 else SeniorOnColors.Gray600
                )
            }
        }
    }
}

@Composable
private fun HospitalEditorDeleteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .border(
                1.dp,
                SeniorOnColors.Red300,
                RoundedCornerShape(SeniorOnRadius.Small)
            )
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_trash),
            contentDescription = null,
            tint = SeniorOnColors.Red300,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "삭제하기",
            style = SeniorOnTextStyles.ButtonM,
            color = SeniorOnColors.Red300
        )
    }
}

@Composable
private fun HospitalEditorSaveButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    @androidx.annotation.DrawableRes iconResId: Int? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(46.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(SeniorOnColors.Primary600)
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        iconResId?.let {
            Icon(painterResource(it), null, Modifier.size(20.dp), SeniorOnColors.SupportWhite100)
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(label, style = SeniorOnTextStyles.ButtonM, color = SeniorOnColors.SupportWhite100)
    }
}

internal fun LocalTime.toKoreanTime(): String =
    DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN).format(this)

@Preview(name = "Hospital Appointment Add", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun HospitalAppointmentPreview() {
    SENIOR_ONTheme {
        HospitalAppointmentScreen(
            mode = HospitalEditorMode.Add,
            initialDate = LocalDate.of(2026, 6, 17),
            onBackClick = {},
            onSaveClick = {}
        )
    }
}

@Preview(name = "Hospital Appointment Edit", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun HospitalAppointmentEditPreview() {
    SENIOR_ONTheme {
        HospitalAppointmentScreen(
            mode = HospitalEditorMode.Edit,
            initialDate = LocalDate.of(2026, 6, 17),
            initialDraft = HospitalAppointmentDraft(
                hospitalName = "연세세브란스",
                specialty = "내과",
                date = LocalDate.of(2026, 6, 17),
                time = LocalTime.of(14, 0),
                reminder = HospitalReminder.DayBefore
            ),
            onBackClick = {},
            onSaveClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(name = "Hospital Appointment View", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun HospitalAppointmentViewPreview() {
    SENIOR_ONTheme {
        HospitalAppointmentScreen(
            mode = HospitalEditorMode.View,
            initialDate = LocalDate.of(2026, 6, 17),
            initialDraft = HospitalAppointmentDraft(
                hospitalName = "연세세브란스",
                specialty = "내과",
                date = LocalDate.of(2026, 6, 17),
                time = LocalTime.of(14, 0),
                reminder = HospitalReminder.DayBefore
            ),
            onBackClick = {},
            onSaveClick = {},
            onDeleteClick = {}
        )
    }
}
