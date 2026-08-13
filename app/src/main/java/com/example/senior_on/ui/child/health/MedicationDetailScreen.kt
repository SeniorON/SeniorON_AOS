package com.example.senior_on.ui.child.health

import com.example.senior_on.ui.theme.SeniorOnDimensions
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import com.example.senior_on.R
import com.example.senior_on.common.time.koreaToday
import com.example.senior_on.ui.common.clearFocusOnBackgroundTap
import com.example.senior_on.ui.common.component.SeniorOnActionButton
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val LocalDateNullableSaver = Saver<LocalDate?, String>(
    save = { it?.toString().orEmpty() },
    restore = { saved -> saved.takeIf(String::isNotBlank)?.let(LocalDate::parse) },
)

private val LocalTimeListSaver = Saver<List<LocalTime>, List<String>>(
    save = { times -> times.map(LocalTime::toString) },
    restore = { saved -> saved.map(LocalTime::parse) },
)

private val WeekdaySetSaver = Saver<Set<Int>, List<Int>>(
    save = { weekdays -> weekdays.sorted() },
    restore = { saved -> saved.toSet() },
)

private val MedicationRepeatSelectionSaver = Saver<MedicationRepeatSelection, List<Any>>(
    save = { selection ->
        listOf(
            selection.frequency.name,
            selection.cycleValue,
            ArrayList(selection.weekdays),
            selection.duration.name,
            selection.periodValue,
            selection.endDate?.toString().orEmpty(),
        )
    },
    restore = { saved ->
        MedicationRepeatSelection(
            frequency = runCatching {
                MedicationRepeatFrequency.valueOf(saved[0] as String)
            }.getOrDefault(MedicationRepeatFrequency.Daily),
            cycleValue = saved[1] as Int,
            weekdays = (saved[2] as List<*>).mapNotNull { it as? Int }.toSet(),
            duration = MedicationRepeatDuration.valueOf(saved[3] as String),
            periodValue = saved[4] as Int,
            endDate = (saved[5] as String).takeIf(String::isNotBlank)?.let(LocalDate::parse),
        )
    },
)

@Composable
fun MedicationDetailScreen(
    mode: MedicationEditorMode,
    initialDraft: MedicationDraft,
    modifier: Modifier = Modifier,
    isSaving: Boolean = false,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onSaveClick: (MedicationDraft) -> Unit,
    onDeleteClick: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    var category by rememberSaveable(initialDraft) { mutableStateOf(initialDraft.category) }
    var name by rememberSaveable(initialDraft) { mutableStateOf(initialDraft.name) }
    var times by rememberSaveable(initialDraft, stateSaver = LocalTimeListSaver) {
        mutableStateOf(initialDraft.times)
    }
    var weekdays by rememberSaveable(initialDraft, stateSaver = WeekdaySetSaver) {
        mutableStateOf(
            initialDraft.weekdays.ifEmpty { MedicationWeekdayLabels.indices.toSet() }
        )
    }
    var startDate by rememberSaveable(
        initialDraft,
        stateSaver = LocalDateNullableSaver,
    ) {
        mutableStateOf(initialDraft.startDate)
    }
    var repeatSelection by rememberSaveable(
        initialDraft,
        stateSaver = MedicationRepeatSelectionSaver,
    ) {
        mutableStateOf(
            initialDraft.repeat.copy(
                weekdays = initialDraft.weekdays.ifEmpty {
                    initialDraft.repeat.weekdays.ifEmpty { MedicationWeekdayLabels.indices.toSet() }
                },
            )
        )
    }
    var showTimeSheet by rememberSaveable { mutableStateOf(false) }
    var showDateSheet by rememberSaveable { mutableStateOf(false) }
    var showRepeatSheet by rememberSaveable { mutableStateOf(false) }
    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var snackbarMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val isEditable = mode != MedicationEditorMode.View
    val isComplete = category.isNotBlank() &&
        times.isNotEmpty() &&
        startDate != null &&
        weekdays.isNotEmpty()
    val hasInput =
        category.isNotBlank() || name.isNotBlank() || times.isNotEmpty() || startDate != null
    val hasChanges =
        category != initialDraft.category ||
            name != initialDraft.name ||
            times != initialDraft.times ||
            weekdays != initialDraft.weekdays ||
            startDate != initialDraft.startDate ||
            repeatSelection != initialDraft.repeat

    fun resolvedWeekdaysForSave(): Set<Int> =
        when (repeatSelection.frequency) {
            MedicationRepeatFrequency.Daily -> MedicationWeekdayLabels.indices.toSet()
            else -> weekdays.ifEmpty { MedicationWeekdayLabels.indices.toSet() }
        }

    fun isEndDateBeforeStartDate(
        selectedStartDate: LocalDate? = startDate,
        selectedRepeat: MedicationRepeatSelection = repeatSelection,
    ): Boolean =
        selectedRepeat.duration == MedicationRepeatDuration.Date &&
            selectedStartDate != null &&
            selectedRepeat.endDate?.isBefore(selectedStartDate) == true

    fun saveMedicationIfValid() {
        if (isEndDateBeforeStartDate()) {
            snackbarMessage = "복용 종료일은 시작일보다 빠를 수 없어요."
            return
        }
        onSaveClick(
            MedicationDraft(
                category = category.trim(),
                name = name.trim(),
                times = times,
                weekdays = resolvedWeekdaysForSave(),
                startDate = startDate,
                repeat = repeatSelection.copy(
                    weekdays = resolvedWeekdaysForSave(),
                ),
            )
        )
    }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            delay(2_000)
            snackbarMessage = null
        }
    }

    val requestBack = {
        when {
            mode == MedicationEditorMode.Add && hasInput -> showExitDialog = true
            mode == MedicationEditorMode.Edit && hasChanges -> showExitDialog = true
            else -> onBackClick()
        }
    }
    BackHandler(onBack = requestBack)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.SupportWhite100)
                .clearFocusOnBackgroundTap(focusManager)
        ) {
            HealthEditorTopBar(
                title = when (mode) {
                    MedicationEditorMode.Add -> "복약 추가하기"
                    MedicationEditorMode.View -> "복약 정보"
                    MedicationEditorMode.Edit -> "복약 수정하기"
                },
                onBackClick = requestBack
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                HealthFormSection(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp)
                ) {
                    HealthFormLabel("약 이름")
                    Spacer(modifier = Modifier.height(12.dp))
                    MedicationTextInput(
                        value = category,
                        placeholder = if (mode == MedicationEditorMode.Add) {
                            "약 이름을 입력해주세요"
                        } else {
                            "약 이름"
                        },
                        onValueChange = { category = it },
                        filled = true,
                        enabled = isEditable,
                        inputHeight = 49.dp
                    )
                }

                if (mode == MedicationEditorMode.Add) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(SeniorOnColors.Background3)
                    )
                }

                HealthFormSection(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HealthFormLabel("성분명")
                        Spacer(modifier = Modifier.width(8.dp))
                        MedicationSelectBadge()
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    MedicationTextInput(
                        value = name,
                        placeholder = if (mode == MedicationEditorMode.Add) {
                            "예시) 아암로디핀"
                        } else {
                            "성분명"
                        },
                        onValueChange = { name = it },
                        filled = false,
                        enabled = isEditable
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HealthFormLabel(
                            text = "복용 시간",
                            modifier = Modifier.weight(1f)
                        )
                        if (isEditable) {
                            val addTimeColor = if (times.isEmpty()) {
                                SeniorOnColors.Gray300
                            } else {
                                SeniorOnColors.Primary600
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    focusManager.clearFocus()
                                    showTimeSheet = true
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_sm_plus),
                                    contentDescription = null,
                                    tint = addTimeColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "복용 시간 추가",
                                    style = SeniorOnTextStyles.BodySMedium,
                                    color = addTimeColor
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    when {
                        times.isEmpty() && isEditable -> {
                            MedicationTimePickerField(
                                isFocused = showTimeSheet,
                                onClick = {
                                    focusManager.clearFocus()
                                    showTimeSheet = true
                                }
                            )
                        }
                        else -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                times.forEach { time ->
                                    if (isEditable) {
                                        MedicationEditableTimeChip(
                                            text = time.toMedicationTime(),
                                            onDeleteClick = {
                                                times = times.filterNot { it == time }
                                            }
                                        )
                                    } else {
                                        MedicationTimeChip(text = time.toMedicationTime())
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HealthFormLabel("복용 시작일")
                    Spacer(modifier = Modifier.height(12.dp))
                    MedicationStartDateField(
                        date = startDate,
                        isFocused = showDateSheet,
                        enabled = isEditable,
                        onClick = {
                            focusManager.clearFocus()
                            showDateSheet = true
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HealthFormLabel("반복")
                    Spacer(modifier = Modifier.height(12.dp))
                    MedicationRepeatField(
                        frequencyLabel = repeatSelection.frequency.label,
                        durationLabel = repeatSelection.summaryLabel(startDate),
                        enabled = isEditable,
                        onClick = {
                            focusManager.clearFocus()
                            showRepeatSheet = true
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        when (mode) {
                            MedicationEditorMode.Add -> {
                                MedicationPrimaryButton(
                                    label = "약 추가하기",
                                    enabled = isComplete && !isSaving,
                                    isLoading = isSaving,
                                    onClick = ::saveMedicationIfValid,
                                    iconResId = R.drawable.ic_plus,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            MedicationEditorMode.View -> {
                                MedicationDeleteButton(
                                    onClick = { showDeleteDialog = true },
                                    enabled = !isSaving,
                                    modifier = Modifier.weight(1f)
                                )
                                MedicationPrimaryButton(
                                    label = "수정하기",
                                    enabled = !isSaving,
                                    onClick = onEditClick,
                                    iconResId = R.drawable.ic_pencil,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            MedicationEditorMode.Edit -> {
                                MedicationPrimaryButton(
                                    label = "수정하기",
                                    enabled = isComplete && !isSaving,
                                    isLoading = isSaving,
                                    onClick = ::saveMedicationIfValid,
                                    iconResId = R.drawable.ic_pencil,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        snackbarMessage?.let { message ->
            MedicationSnackbar(
                message = message,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(328.dp)
                    .padding(vertical = 24.dp)
            )
        }
    }

    if (showTimeSheet) {
        ScheduleTimePickerBottomSheet(
            initialTime = times.lastOrNull() ?: LocalTime.of(8, 0),
            onDismiss = { showTimeSheet = false },
            onConfirm = { selected ->
                if (selected in times) {
                    snackbarMessage = "이미 등록된 복용 시간입니다."
                } else {
                    times = (times + selected).sorted()
                }
                showTimeSheet = false
            }
        )
    }
    if (showDateSheet) {
        ScheduleDatePickerBottomSheet(
            initialDate = startDate ?: koreaToday(),
            onDismiss = { showDateSheet = false },
            onConfirm = { selected ->
                if (isEndDateBeforeStartDate(selectedStartDate = selected)) {
                    snackbarMessage = "복용 종료일은 시작일보다 빠를 수 없어요."
                } else {
                    startDate = selected
                }
                showDateSheet = false
            }
        )
    }
    if (showRepeatSheet) {
        MedicationRepeatBottomSheet(
            initial = repeatSelection,
            onCancel = { showRepeatSheet = false },
            onConfirm = { selected ->
                if (isEndDateBeforeStartDate(selectedRepeat = selected)) {
                    snackbarMessage = "복용 종료일은 시작일보다 빠를 수 없어요."
                    showRepeatSheet = false
                    return@MedicationRepeatBottomSheet
                }
                val resolvedWeekdays = when (selected.frequency) {
                    MedicationRepeatFrequency.Daily ->
                        MedicationWeekdayLabels.indices.toSet()
                    else -> selected.weekdays.ifEmpty { weekdays }
                }
                repeatSelection = selected.copy(weekdays = resolvedWeekdays)
                weekdays = resolvedWeekdays
                showRepeatSheet = false
            }
        )
    }
    if (showExitDialog) {
        val isAddMode = mode == MedicationEditorMode.Add
        SeniorOnConfirmDialog(
            iconResId = R.drawable.ic_modal_unsaved,
            title = if (isAddMode) {
                "등록하지 않고\n나가시겠어요?"
            } else {
                "저장하지 않고\n나가시겠어요?"
            },
            description = if (isAddMode) {
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
            title = "'${category.ifBlank { initialDraft.category }}'을 삭제할까요?",
            onCancel = { showDeleteDialog = false },
            onConfirm = onDeleteClick,
            isConfirmLoading = isSaving,
        )
    }
}

@Composable
private fun MedicationTimePickerField(
    isFocused: Boolean,
    onClick: () -> Unit
) {
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
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "시간 선택",
            style = SeniorOnTextStyles.BodyMMedium,
            color = SeniorOnColors.Gray300,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_clock_1),
            contentDescription = null,
            tint = SeniorOnColors.Gray400,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun MedicationStartDateField(
    date: LocalDate?,
    isFocused: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
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
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_illust_hospital_schedule_2),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = date?.toMedicationStartDateLabel().orEmpty()
                .ifEmpty { "날짜 선택" },
            style = SeniorOnTextStyles.BodyMMedium,
            color = if (date == null) SeniorOnColors.Gray300 else SeniorOnColors.Gray800
        )
    }
}

@Composable
private fun MedicationEditableTimeChip(
    text: String,
    onDeleteClick: () -> Unit
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(43.dp)
            .clip(shape)
            .background(SeniorOnColors.Background2)
            .padding(start = 12.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.BodyMMedium,
            color = SeniorOnColors.Primary700,
            modifier = Modifier.weight(1f)
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_trash),
            contentDescription = "복용 시간 삭제",
            tint = SeniorOnColors.Gray400,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun MedicationSnackbar(
    message: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(
        topStart = SeniorOnRadius.Medium,
        topEnd = SeniorOnRadius.Medium,
        bottomEnd = SeniorOnRadius.Small,
        bottomStart = SeniorOnRadius.Medium,
    )
    Row(
        modifier = modifier
            .height(60.dp)
            .clip(shape)
            .background(SeniorOnColors.Toast.copy(alpha = 0.9f))
            .padding(horizontal = 12.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_information2),
            contentDescription = null,
            tint = SeniorOnColors.SupportWhite100,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = message,
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.SupportWhite100
        )
    }
}

@Preview(name = "복약 중복 시간 스낵바", showBackground = true, widthDp = 360)
@Composable
private fun MedicationSnackbarPreview() {
    SENIOR_ONTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(108.dp),
            contentAlignment = Alignment.Center,
        ) {
            MedicationSnackbar(
                message = "이미 등록된 복용 시간입니다.",
                modifier = Modifier.width(328.dp),
            )
        }
    }
}

@Composable
private fun MedicationSelectBadge() {
    Box(
        modifier = Modifier
            .width(45.dp)
            .height(25.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(SeniorOnColors.Gray100)
            .padding(start = 12.dp, top = 4.dp, end = 12.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "선택",
            style = SeniorOnTextStyles.CaptionMedium,
            color = SeniorOnColors.Gray500
        )
    }
}

@Composable
private fun MedicationTextInput(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    filled: Boolean,
    enabled: Boolean,
    inputHeight: Dp = 43.dp
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(SeniorOnRadius.Small)
    val focusManager = LocalFocusManager.current

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        textStyle = SeniorOnTextStyles.BodyMMedium.copy(color = SeniorOnColors.Gray800),
        cursorBrush = SolidColor(SeniorOnColors.Primary600),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(inputHeight)
            .onFocusChanged { isFocused = it.isFocused },
        singleLine = true,
        decorationBox = { inner ->
            val borderColor = when {
                !enabled -> SeniorOnColors.Gray200
                isFocused -> SeniorOnColors.Primary600
                else -> SeniorOnColors.Gray200
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(
                        if (filled) SeniorOnColors.Background3
                        else SeniorOnColors.SupportWhite100
                    )
                    .then(
                        if (filled) {
                            Modifier
                        } else {
                            Modifier.border(
                                width = 1.dp,
                                color = borderColor,
                                shape = shape
                            )
                        }
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onValueChange("") }
                    )
                }
            }
        }
    )
}

@Composable
private fun MedicationTimeChip(text: String) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(43.dp)
            .clip(shape)
            .background(SeniorOnColors.Background2)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.BodyMMedium,
            color = SeniorOnColors.Primary700,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MedicationRepeatField(
    frequencyLabel: String,
    durationLabel: String,
    enabled: Boolean,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(SeniorOnColors.Background3)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = frequencyLabel,
                style = SeniorOnTextStyles.BodyMMedium,
                color = SeniorOnColors.Gray600
            )
            Text(
                text = durationLabel,
                style = SeniorOnTextStyles.BodySRegular,
                color = SeniorOnColors.Primary700
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_sm_arrow_right),
            contentDescription = null,
            tint = SeniorOnColors.Gray500,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun MedicationDeleteButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .border(1.dp, SeniorOnColors.Red300, RoundedCornerShape(SeniorOnRadius.Small))
            .clickable(enabled = enabled, onClick = onClick),
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
private fun MedicationPrimaryButton(
    label: String,
    enabled: Boolean,
    isLoading: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconResId: Int? = null
) {
    SeniorOnActionButton(
        text = label,
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        isLoading = isLoading,
        minHeight = 48.dp,
        shape = RoundedCornerShape(SeniorOnRadius.Small),
        leadingContent = iconResId?.let { iconId ->
            {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = null,
                tint = SeniorOnColors.SupportWhite100,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            }
        },
    )
}

internal fun RegisteredMedicationUiState.toDraft() = MedicationDraft(
    category = category,
    name = name,
    times = times,
    weekdays = weekdays,
    startDate = startDate,
    repeat = repeat.copy(weekdays = weekdays),
)

@Preview(name = "복약 추가하기 - Add", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MedicationDetailAddPreview() {
    SENIOR_ONTheme {
        MedicationDetailScreen(
            mode = MedicationEditorMode.Add,
            initialDraft = MedicationDraft("", "", emptyList(), emptySet()),
            onBackClick = {},
            onSaveClick = {}
        )
    }
}

@Preview(name = "복약 정보 - View", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MedicationDetailViewPreview() {
    SENIOR_ONTheme {
        MedicationDetailScreen(
            mode = MedicationEditorMode.View,
            initialDraft = previewRegisteredMedications().first().toDraft(),
            onBackClick = {},
            onEditClick = {},
            onSaveClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(name = "복약 수정하기 - Edit", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MedicationDetailEditPreview() {
    SENIOR_ONTheme {
        MedicationDetailScreen(
            mode = MedicationEditorMode.Edit,
            initialDraft = previewRegisteredMedications()[1].toDraft(),
            onBackClick = {},
            onSaveClick = {}
        )
    }
}
