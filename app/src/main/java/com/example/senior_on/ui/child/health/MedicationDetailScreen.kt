package com.example.senior_on.ui.child.health

import com.example.senior_on.ui.theme.SeniorOnDimensions
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class MedicationEditorMode { Add, View, Edit }

data class MedicationDraft(
    val category: String,
    val name: String,
    val times: List<LocalTime>,
    val weekdays: Set<Int>,
    val startDate: LocalDate? = null
)

@Composable
fun MedicationDetailScreen(
    mode: MedicationEditorMode,
    initialDraft: MedicationDraft,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onSaveClick: (MedicationDraft) -> Unit,
    onDeleteClick: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    var category by rememberSaveable(initialDraft) { mutableStateOf(initialDraft.category) }
    var name by rememberSaveable(initialDraft) { mutableStateOf(initialDraft.name) }
    var times by remember(initialDraft) { mutableStateOf(initialDraft.times) }
    var weekdays by remember(initialDraft) {
        mutableStateOf(
            initialDraft.weekdays.ifEmpty { MedicationWeekdayLabels.indices.toSet() }
        )
    }
    var startDate by remember(initialDraft) { mutableStateOf(initialDraft.startDate) }
    var repeatSelection by remember(initialDraft) {
        mutableStateOf(
            MedicationRepeatSelection(
                weekdays = initialDraft.weekdays.ifEmpty { MedicationWeekdayLabels.indices.toSet() }
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
    val isComplete = category.isNotBlank() && times.isNotEmpty() && weekdays.isNotEmpty()
    val hasInput =
        category.isNotBlank() || name.isNotBlank() || times.isNotEmpty() || weekdays.isNotEmpty()
    val hasChanges =
        category != initialDraft.category ||
            name != initialDraft.name ||
            times != initialDraft.times ||
            weekdays != initialDraft.weekdays ||
            startDate != initialDraft.startDate

    fun resolvedWeekdaysForSave(): Set<Int> =
        when (repeatSelection.frequency) {
            MedicationRepeatFrequency.Daily -> MedicationWeekdayLabels.indices.toSet()
            else -> weekdays.ifEmpty { MedicationWeekdayLabels.indices.toSet() }
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
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            MedicationEditorTopBar(
                title = when (mode) {
                    MedicationEditorMode.Add -> "복약 등록하기"
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
                MedicationFormSection(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp)
                ) {
                    MedicationFormLabel("약 이름")
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

                MedicationFormSection(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MedicationFormLabel("성분명")
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

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MedicationFormLabel(
                            text = "복용 시간",
                            modifier = Modifier.weight(1f)
                        )
                        if (isEditable) {
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
                                    tint = SeniorOnColors.Gray300,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "복용 시간 추가",
                                    style = SeniorOnTextStyles.BodySMedium,
                                    color = SeniorOnColors.Gray300
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

                    MedicationFormLabel("복용 시작일")
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

                    MedicationFormLabel("반복")
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

                    snackbarMessage?.let { message ->
                        Spacer(modifier = Modifier.height(12.dp))
                        MedicationSnackbar(
                            message = message,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (mode) {
                            MedicationEditorMode.Add -> {
                                MedicationPrimaryButton(
                                    label = "약 추가하기",
                                    enabled = isComplete,
                                    onClick = {
                                        onSaveClick(
                                            MedicationDraft(
                                                category = category.trim(),
                                                name = name.trim(),
                                                times = times,
                                                weekdays = resolvedWeekdaysForSave(),
                                                startDate = startDate
                                            )
                                        )
                                    },
                                    iconResId = R.drawable.ic_plus,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            MedicationEditorMode.View -> {
                                MedicationDeleteButton(
                                    onClick = { showDeleteDialog = true },
                                    modifier = Modifier.weight(1f)
                                )
                                MedicationPrimaryButton(
                                    label = "수정하기",
                                    enabled = true,
                                    onClick = onEditClick,
                                    iconResId = R.drawable.ic_pencil,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            MedicationEditorMode.Edit -> {
                                MedicationPrimaryButton(
                                    label = "수정하기",
                                    enabled = isComplete,
                                    onClick = {
                                        onSaveClick(
                                            MedicationDraft(
                                                category = category.trim(),
                                                name = name.trim(),
                                                times = times,
                                                weekdays = resolvedWeekdaysForSave(),
                                                startDate = startDate
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
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
            initialDate = startDate ?: LocalDate.now(),
            onDismiss = { showDateSheet = false },
            onConfirm = { selected ->
                startDate = selected
                showDateSheet = false
            }
        )
    }
    if (showRepeatSheet) {
        MedicationRepeatBottomSheet(
            initial = repeatSelection,
            onCancel = { showRepeatSheet = false },
            onConfirm = { selected ->
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
            onConfirm = onDeleteClick
        )
    }
}

@Composable
private fun MedicationEditorTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
            .statusBarsPadding()
            .height(SeniorOnDimensions.TopBarHeight)
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
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Gray800
        )
    }
}

@Composable
private fun MedicationFormSection(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth(), content = content)
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
            .border(width = 1.dp, color = SeniorOnColors.Gray200, shape = shape)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.BodyMSemiBold,
            color = SeniorOnColors.Primary700,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_trash),
            contentDescription = "복용 시간 삭제",
            tint = SeniorOnColors.Gray400,
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onDeleteClick)
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
            .background(SeniorOnColors.Toast)
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

@Composable
private fun MedicationFormLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = SeniorOnTextStyles.BodyMSemiBold,
        color = SeniorOnColors.Gray800,
        modifier = modifier
    )
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

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        textStyle = SeniorOnTextStyles.BodyMMedium.copy(color = SeniorOnColors.Gray800),
        cursorBrush = SolidColor(SeniorOnColors.Primary600),
        modifier = Modifier
            .fillMaxWidth()
            .height(inputHeight)
            .onFocusChanged { isFocused = it.isFocused },
        singleLine = true,
        decorationBox = { inner ->
            val borderColor = when {
                !enabled -> SeniorOnColors.Gray200
                isFocused || (!filled && value.isNotEmpty()) -> SeniorOnColors.Primary600
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
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = shape
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(43.dp)
            .clip(shape)
            .background(SeniorOnColors.Background2)
            .border(width = 1.dp, color = SeniorOnColors.Gray200, shape = shape)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.BodyMMedium,
            color = SeniorOnColors.Primary700
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
            .background(SeniorOnColors.Background2)
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
            tint = SeniorOnColors.Gray400,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun MedicationDeleteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .border(1.dp, SeniorOnColors.Red300, RoundedCornerShape(SeniorOnRadius.Small))
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
private fun MedicationPrimaryButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconResId: Int? = null
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(SeniorOnColors.Primary600)
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        iconResId?.let {
            Icon(
                painter = painterResource(id = it),
                contentDescription = null,
                tint = SeniorOnColors.SupportWhite100,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = label,
            style = SeniorOnTextStyles.ButtonM,
            color = SeniorOnColors.SupportWhite100
        )
    }
}

internal fun RegisteredMedicationUiState.toDraft() = MedicationDraft(
    category = category,
    name = name,
    times = times,
    weekdays = weekdays,
    startDate = startDate
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
