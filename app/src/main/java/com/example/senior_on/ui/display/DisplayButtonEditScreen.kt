package com.example.senior_on.ui.display

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.example.senior_on.R
import com.example.senior_on.domain.model.SeniorHomeButtonType
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

private const val ButtonNameMaxLength = 6

private val ProvidedButtons = listOf(
    SeniorHomeButtonType.ChatBuddy,
    SeniorHomeButtonType.Schedule,
    SeniorHomeButtonType.Medication,
)

private val ProvidedButtonNames = ProvidedButtons.map(
    SeniorHomeButtonType::displayLabel
)

private val InitialEditableButtons = listOf(
    SeniorHomeButtonType.Call,
    SeniorHomeButtonType.Message,
    SeniorHomeButtonType.Camera,
    SeniorHomeButtonType.NaverMap,
)

private fun SeniorHomeButtonType.isProtectedFromSelectedButtonEditing(): Boolean =
    this in ProvidedButtons ||
        this == SeniorHomeButtonType.Emergency ||
        isMusicButton()

internal fun mergeSelectedButtonEdits(
    initialButtons: List<SeniorHomeButtonType>,
    editableButtons: List<SeniorHomeButtonType>,
): List<SeniorHomeButtonType> {
    val uniqueEditableButtons = editableButtons
        .filterNot(SeniorHomeButtonType::isProtectedFromSelectedButtonEditing)
        .distinct()
    val editableButtonSet = uniqueEditableButtons.toSet()

    return buildList {
        initialButtons.distinct().forEach { button ->
            if (
                button.isProtectedFromSelectedButtonEditing() ||
                button in editableButtonSet
            ) {
                add(button)
            }
        }
        uniqueEditableButtons.forEach { button ->
            if (button !in this) {
                add(button)
            }
        }
    }
}

@Composable
fun DisplayButtonEditGuideScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onContinueClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.Background1)
            .statusBarsPadding(),
    ) {
        ButtonEditTopBar(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "안내사항",
                modifier = Modifier
                    .clip(RoundedCornerShape(SeniorOnRadius.XLarge))
                    .background(SeniorOnColors.Primary500)
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                style = SeniorOnTextStyles.CaptionMedium,
                color = SeniorOnColors.White,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "시니어 화면에 있는\n앱인지 확인해주세요",
                style = SeniorOnTextStyles.OnboardingHeading,
                color = SeniorOnColors.Gray800,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "시니어 휴대폰에 해당하는 앱이 있어야 연동할 수 있어요.",
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray500,
            )

            Spacer(modifier = Modifier.height(35.dp))

            Image(
                painter = painterResource(id = R.drawable.button_edit_app_check),
                contentDescription = "시니어 휴대폰 앱 확인 목록",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(930f / 783f)
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.weight(1f))

            ButtonEditPrimaryButton(
                text = "계속하기",
                onClick = onContinueClick,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DisplayButtonEditSelectedScreen(
    initialButtons: List<SeniorHomeButtonType> = InitialEditableButtons,
    initialCustomButtonLabels: Map<SeniorHomeButtonType, String> = emptyMap(),
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSaveClick: (
        buttons: List<SeniorHomeButtonType>,
        customButtonLabels: Map<SeniorHomeButtonType, String>,
    ) -> Unit = { _, _ -> },
    onAddButtonClick: (
        buttons: List<SeniorHomeButtonType>,
        customButtonLabels: Map<SeniorHomeButtonType, String>,
    ) -> Unit = { _, _ -> },
) {
    var editableButtonTypeNames by rememberSaveable(initialButtons) {
        mutableStateOf(
            ArrayList(
                initialButtons
                    .filterNot { button ->
                        button.isProtectedFromSelectedButtonEditing()
                    }
                    .map(SeniorHomeButtonType::name)
            )
        )
    }
    var customLabelsByButtonName by rememberSaveable(
        initialCustomButtonLabels
    ) {
        mutableStateOf(
            HashMap(
                initialCustomButtonLabels.mapKeys { (button, _) ->
                    button.name
                }
            )
        )
    }
    var expandedButtonIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var editingButtonIndex by rememberSaveable { mutableStateOf<Int?>(null) }

    val editableButtons = editableButtonTypeNames.map(
        SeniorHomeButtonType::valueOf
    )
    val currentButtons = mergeSelectedButtonEdits(
        initialButtons = initialButtons,
        editableButtons = editableButtons,
    )
    val currentCustomButtonLabels = customLabelsByButtonName
        .mapKeys { (buttonName, _) -> SeniorHomeButtonType.valueOf(buttonName) }
        .filterKeys(currentButtons::contains)
    val initialRelevantCustomButtonLabels = initialCustomButtonLabels
        .filterKeys(currentButtons::contains)
    val hasChanges = currentButtons != initialButtons.distinct() ||
        currentCustomButtonLabels != initialRelevantCustomButtonLabels
    val editableButtonLabels = editableButtons.map { button ->
        currentCustomButtonLabels[button] ?: button.displayLabel()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.Background1)
            .statusBarsPadding(),
    ) {
        ButtonEditTopBar(
            onBackClick = onBackClick,
            saveEnabled = hasChanges,
            onSaveClick = {
                onSaveClick(
                    currentButtons,
                    currentCustomButtonLabels,
                )
            },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "선택한 버튼",
                    style = SeniorOnTextStyles.HeadingS,
                    color = SeniorOnColors.Gray800,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "원하는 버튼을 담고, 순서도 바꿀 수 있어요.",
                    style = SeniorOnTextStyles.BodySMedium,
                    color = SeniorOnColors.Gray500,
                )

                Spacer(modifier = Modifier.height(12.dp))

                SelectedButtonCard(
                    editableButtonNames = editableButtonLabels,
                    expandedButtonIndex = expandedButtonIndex,
                    onMoreClick = { index ->
                        expandedButtonIndex =
                            if (expandedButtonIndex == index) null else index
                    },
                    onMenuDismiss = { expandedButtonIndex = null },
                    onNameEditClick = { index ->
                        expandedButtonIndex = null
                        editingButtonIndex = index
                    },
                    onDeleteClick = { index ->
                        expandedButtonIndex = null
                        val deletedButtonName = editableButtonTypeNames[index]
                        editableButtonTypeNames =
                            ArrayList(editableButtonTypeNames).apply {
                            removeAt(index)
                        }
                        customLabelsByButtonName =
                            HashMap(customLabelsByButtonName).apply {
                                remove(deletedButtonName)
                            }
                    },
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            ButtonEditPrimaryButton(
                text = "새 버튼 추가",
                leadingPlus = true,
                buttonHeight = 48.dp,
                onClick = {
                    onAddButtonClick(
                        currentButtons,
                        currentCustomButtonLabels,
                    )
                },
            )

            Spacer(modifier = Modifier.height(22.dp))
        }
    }

    editingButtonIndex?.let { index ->
        val editingButton = editableButtons[index]
        ButtonNameEditBottomSheet(
            initialName = currentCustomButtonLabels[editingButton]
                ?: editingButton.displayLabel(),
            onDismiss = { editingButtonIndex = null },
            onSave = { name ->
                customLabelsByButtonName =
                    HashMap(customLabelsByButtonName).apply {
                        if (name == editingButton.displayLabel()) {
                            remove(editingButton.name)
                        } else {
                            this[editingButton.name] = name
                        }
                }
                editingButtonIndex = null
            },
        )
    }
}

@Composable
private fun ButtonEditTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    saveEnabled: Boolean = false,
    onSaveClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(SeniorOnColors.White),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .size(26.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBackClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = "뒤로가기",
                modifier = Modifier.size(26.dp),
                tint = SeniorOnColors.Gray800,
            )
        }

        Text(
            text = "버튼 편집",
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Gray800,
        )

        onSaveClick?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(38.dp))
                    .background(
                        if (saveEnabled) {
                            SeniorOnColors.Primary600
                        } else {
                            SeniorOnColors.Primary600.copy(alpha = 0.5f)
                        }
                    )
                    .clickable(
                        enabled = saveEnabled,
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null,
                        onClick = it,
                    )
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "저장",
                    style = SeniorOnTextStyles.BodySSemiBold,
                    color = SeniorOnColors.White,
                )
            }
        }
    }
}

@Composable
private fun SelectedButtonCard(
    editableButtonNames: List<String>,
    expandedButtonIndex: Int?,
    onMoreClick: (Int) -> Unit,
    onMenuDismiss: () -> Unit,
    onNameEditClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalButtonCount = ProvidedButtonNames.size + editableButtonNames.size

    Column(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(
                shape = RoundedCornerShape(SeniorOnRadius.Medium),
                shadow = Shadow(
                    radius = 12.dp,
                    color = SeniorOnColors.Black.copy(alpha = 0.08f),
                    offset = DpOffset(x = 0.dp, y = 2.dp),
                ),
            )
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .background(SeniorOnColors.White)
            .padding(horizontal = 14.dp),
    ) {
        ProvidedButtonNames.forEachIndexed { index, name ->
            ProvidedButtonRow(name = name)
            if (index != totalButtonCount - 1) {
                SelectedButtonDivider()
            }
        }

        editableButtonNames.forEachIndexed { index, name ->
            EditableButtonRow(
                name = name,
                menuExpanded = expandedButtonIndex == index,
                onMoreClick = { onMoreClick(index) },
                onMenuDismiss = onMenuDismiss,
                onNameEditClick = { onNameEditClick(index) },
                onDeleteClick = { onDeleteClick(index) },
            )
            if (ProvidedButtonNames.size + index != totalButtonCount - 1) {
                SelectedButtonDivider()
            }
        }
    }
}

@Composable
private fun ProvidedButtonRow(
    name: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SelectedButtonName(
            name = name,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = "제공 기능",
            modifier = Modifier
                .clip(RoundedCornerShape(17.dp))
                .background(SeniorOnColors.Gray100)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            style = SeniorOnTextStyles.CaptionMedium,
            color = SeniorOnColors.Gray500,
        )
    }
}

@Composable
private fun EditableButtonRow(
    name: String,
    menuExpanded: Boolean,
    onMoreClick: () -> Unit,
    onMenuDismiss: () -> Unit,
    onNameEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SelectedButtonName(
            name = name,
            modifier = Modifier.weight(1f),
        )

        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(SeniorOnColors.Gray300)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onMoreClick,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sm_more),
                    contentDescription = "$name 편집 메뉴",
                    modifier = Modifier.size(24.dp),
                    tint = SeniorOnColors.White,
                )
            }

            ButtonEditMoreMenu(
                expanded = menuExpanded,
                onDismiss = onMenuDismiss,
                onNameEditClick = onNameEditClick,
                onDeleteClick = onDeleteClick,
            )
        }
    }
}

@Composable
private fun SelectedButtonName(
    name: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = name,
        modifier = modifier,
        style = SeniorOnTextStyles.BodyMMedium,
        color = SeniorOnColors.Gray800,
    )
}

@Composable
private fun SelectedButtonDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(SeniorOnColors.Gray100),
    )
}

@Composable
private fun ButtonEditMoreMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onNameEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    if (!expanded) return

    val density = LocalDensity.current
    val positionProvider = remember(density) {
        with(density) {
            ButtonEditMenuPositionProvider(
                horizontalGapPx = 4.dp.roundToPx(),
                verticalOffsetPx = 4.dp.roundToPx(),
            )
        }
    }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        ButtonEditMoreMenuContent(
            onNameEditClick = onNameEditClick,
            onDeleteClick = onDeleteClick,
        )
    }
}

@Composable
private fun ButtonEditMoreMenuContent(
    onNameEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val menuShape = RoundedCornerShape(SeniorOnRadius.Medium)

    Column(
        modifier = modifier
            .width(156.dp)
            .height(88.dp)
            .dropShadow(
                shape = menuShape,
                shadow = Shadow(
                    radius = 12.dp,
                    color = SeniorOnColors.Black.copy(alpha = 0.12f),
                    offset = DpOffset(x = 0.dp, y = 2.dp),
                ),
            )
            .clip(menuShape)
            .background(SeniorOnColors.Gray600.copy(alpha = 0.9f))
            .padding(vertical = 4.dp),
    ) {
        ButtonEditMenuItem(
            text = "이름 편집",
            iconResId = R.drawable.ic_pencil,
            contentColor = SeniorOnColors.White,
            onClick = onNameEditClick,
        )

        Box {
            ButtonEditMenuItem(
                text = "삭제",
                iconResId = R.drawable.ic_trash,
                contentColor = SeniorOnColors.Red200,
                onClick = onDeleteClick,
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(1.dp)
                    .background(SeniorOnColors.White.copy(alpha = 0.2f)),
            )
        }
    }
}

private class ButtonEditMenuPositionProvider(
    private val horizontalGapPx: Int,
    private val verticalOffsetPx: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        val maxY = (windowSize.height - popupContentSize.height).coerceAtLeast(0)

        return IntOffset(
            x = (anchorBounds.left - popupContentSize.width - horizontalGapPx)
                .coerceIn(0, maxX),
            y = (anchorBounds.top + verticalOffsetPx).coerceIn(0, maxY),
        )
    }
}

@Composable
private fun ButtonEditMenuItem(
    text: String,
    iconResId: Int,
    contentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = contentColor,
        )

        Spacer(modifier = Modifier.width(7.dp))

        Text(
            text = text,
            style = SeniorOnTextStyles.BodySMedium,
            color = contentColor,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ButtonNameEditBottomSheet(
    initialName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var name by rememberSaveable(initialName) {
        mutableStateOf(initialName)
    }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val saveName: () -> Unit = {
        name.trim()
            .takeIf { trimmedName ->
                trimmedName.isNotEmpty() &&
                    trimmedName.length <= ButtonNameMaxLength
            }
            ?.let(onSave)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SeniorOnColors.White,
        scrimColor = SeniorOnColors.Black.copy(alpha = 0.8f),
        dragHandle = null,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
        ),
    ) {
        ButtonNameEditSheetContent(
            name = name,
            onNameChange = { name = it },
            onCancelClick = onDismiss,
            onSaveClick = saveName,
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding(),
            inputModifier = Modifier.focusRequester(focusRequester),
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}

@Composable
private fun ButtonNameEditSheetContent(
    name: String,
    onNameChange: (String) -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
    inputModifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(154.dp)
            .padding(horizontal = 16.dp)
            .padding(top = 20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = "취소",
                    modifier = Modifier.clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null,
                        onClick = onCancelClick,
                    ),
                    style = SeniorOnTextStyles.BodyMSemiBold,
                    color = SeniorOnColors.Primary600,
                )
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "이름 편집",
                    style = SeniorOnTextStyles.BodyLMedium,
                    color = SeniorOnColors.Gray800,
                )
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = "저장",
                    modifier = Modifier.clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null,
                        onClick = onSaveClick,
                    ),
                    style = SeniorOnTextStyles.BodyMSemiBold,
                    color = SeniorOnColors.Primary600,
                )
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        BasicTextField(
            value = name,
            onValueChange = { newValue ->
                if (
                    newValue.length <= ButtonNameMaxLength ||
                    newValue.length < name.length
                ) {
                    onNameChange(newValue)
                }
            },
            modifier = inputModifier
                .fillMaxWidth()
                .height(43.dp),
            textStyle = SeniorOnTextStyles.BodyMMedium.copy(
                color = SeniorOnColors.Gray800
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSaveClick() }),
            decorationBox = { innerTextField ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (name.isEmpty()) {
                            Text(
                                text = "이름 입력(6글자 이내)",
                                style = SeniorOnTextStyles.BodyMMedium,
                                color = SeniorOnColors.Gray300,
                            )
                        }
                        innerTextField()
                    }

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(SeniorOnColors.Gray200),
                    )
                }
            },
        )
    }
}

@Composable
private fun ButtonEditPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingPlus: Boolean = false,
    buttonHeight: Dp = 50.dp,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(buttonHeight)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(SeniorOnColors.Primary600)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingPlus) {
            Icon(
                painter = painterResource(id = R.drawable.ic_plus),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.White,
            )

            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = text,
            style = SeniorOnTextStyles.ButtonM,
            color = SeniorOnColors.White,
        )
    }
}

internal fun SeniorHomeButtonType.displayLabel(): String = when (this) {
    SeniorHomeButtonType.Call -> "전화"
    SeniorHomeButtonType.Message -> "메시지"
    SeniorHomeButtonType.Calendar -> "캘린더"
    SeniorHomeButtonType.Alarm -> "알림"
    SeniorHomeButtonType.Memo -> "메모"
    SeniorHomeButtonType.Recorder -> "녹음"
    SeniorHomeButtonType.Calculator -> "계산기"
    SeniorHomeButtonType.Settings -> "설정"
    SeniorHomeButtonType.Flashlight -> "손전등"
    SeniorHomeButtonType.KakaoTalk -> "카카오톡"
    SeniorHomeButtonType.NaverBand -> "네이버 밴드"
    SeniorHomeButtonType.NaverCafe -> "네이버 카페"
    SeniorHomeButtonType.Line -> "라인"
    SeniorHomeButtonType.ChatBuddy -> "말벗"
    SeniorHomeButtonType.Medication -> "복약"
    SeniorHomeButtonType.Schedule -> "일정"
    SeniorHomeButtonType.YouTube -> "유튜브"
    SeniorHomeButtonType.Naver -> "네이버"
    SeniorHomeButtonType.Daum -> "다음"
    SeniorHomeButtonType.Google -> "구글"
    SeniorHomeButtonType.Tving -> "티빙"
    SeniorHomeButtonType.Netflix -> "넷플릭스"
    SeniorHomeButtonType.NaverMap -> "네이버 지도"
    SeniorHomeButtonType.KakaoMap -> "카카오맵"
    SeniorHomeButtonType.KakaoT -> "카카오 T"
    SeniorHomeButtonType.TMap -> "티맵"
    SeniorHomeButtonType.KorailTalk -> "코레일톡"
    SeniorHomeButtonType.Toss -> "토스"
    SeniorHomeButtonType.KakaoPay -> "카카오페이"
    SeniorHomeButtonType.NaverPay -> "네이버페이"
    SeniorHomeButtonType.SamsungWallet -> "삼성월렛"
    SeniorHomeButtonType.SamsungPay -> "삼성페이"
    SeniorHomeButtonType.CashWalk -> "캐시워크"
    SeniorHomeButtonType.Weather -> "기상청 날씨알리미"
    SeniorHomeButtonType.Coupang -> "쿠팡"
    SeniorHomeButtonType.Karrot -> "당근"
    SeniorHomeButtonType.Baemin -> "배달의민족"
    SeniorHomeButtonType.Yogiyo -> "요기요"
    SeniorHomeButtonType.CoupangEats -> "쿠팡이츠"
    SeniorHomeButtonType.HomeShopping -> "홈쇼핑"
    SeniorHomeButtonType.GoStop -> "고스톱·맞고"
    SeniorHomeButtonType.Melon -> "멜론(Melon)"
    SeniorHomeButtonType.Spotify -> "스포티파이(Spotify)"
    SeniorHomeButtonType.Photo -> "사진"
    SeniorHomeButtonType.Camera -> "카메라"
    SeniorHomeButtonType.Emergency -> "긴급알림"
}

@Preview(
    name = "Button Edit Guide",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun DisplayButtonEditGuideScreenPreview() {
    SENIOR_ONTheme {
        DisplayButtonEditGuideScreen()
    }
}

@Preview(
    name = "Selected Buttons",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun DisplayButtonEditSelectedScreenPreview() {
    SENIOR_ONTheme {
        DisplayButtonEditSelectedScreen()
    }
}

@Preview(
    name = "Button Edit More Menu",
    showBackground = true,
    backgroundColor = 0xFFF8F8F5,
    widthDp = 200,
    heightDp = 120,
)
@Composable
private fun ButtonEditMoreMenuPreview() {
    SENIOR_ONTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Background1),
            contentAlignment = Alignment.Center,
        ) {
            ButtonEditMoreMenuContent(
                onNameEditClick = {},
                onDeleteClick = {},
            )
        }
    }
}

@Preview(
    name = "Button Name Edit Sheet",
    showBackground = true,
    backgroundColor = 0xFF222222,
    widthDp = 360,
    heightDp = 240,
)
@Composable
private fun ButtonNameEditSheetContentPreview() {
    var name by rememberSaveable { mutableStateOf("") }

    SENIOR_ONTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Black.copy(alpha = 0.8f)),
            contentAlignment = Alignment.BottomCenter,
        ) {
            ButtonNameEditSheetContent(
                name = name,
                onNameChange = { name = it },
                onCancelClick = {},
                onSaveClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                        )
                    )
                    .background(SeniorOnColors.White),
            )
        }
    }
}
