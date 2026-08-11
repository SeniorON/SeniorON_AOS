package com.example.senior_on.ui.child.display

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.domain.model.display.DisplayHomeButton
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnDimensions
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import kotlinx.coroutines.delay

internal const val MinimumButtonSelectionCount = 8
private const val MaximumButtonSelectionCount = 18

internal val ProvidedFeatureButtons = listOf(
    SeniorHomeButtonType.Medication,
    SeniorHomeButtonType.ChatBuddy,
    SeniorHomeButtonType.Emergency,
    SeniorHomeButtonType.Photo,
    SeniorHomeButtonType.Schedule,
)

internal val MusicButtons = listOf(
    SeniorHomeButtonType.Melon,
    SeniorHomeButtonType.Genie,
    SeniorHomeButtonType.YouTubeMusic,
    SeniorHomeButtonType.Spotify,
    SeniorHomeButtonType.Flo,
    SeniorHomeButtonType.Vibe,
    SeniorHomeButtonType.Bugs,
    SeniorHomeButtonType.SamsungMusic,
    SeniorHomeButtonType.KakaoMusic,
)

internal fun buttonAddSelectedCount(selectedAppCount: Int): Int =
    ProvidedFeatureButtons.count { it != SeniorHomeButtonType.Schedule } + selectedAppCount

internal fun buttonAddMaximumCount(): Int = MaximumButtonSelectionCount

internal fun buttonAddCanContinue(selectedAppCount: Int): Boolean =
    buttonAddSelectedCount(selectedAppCount) >= MinimumButtonSelectionCount

internal enum class ButtonAddExit {
    Cancel,
    Continue,
}

internal fun resolveButtonEditDraftAfterButtonAdd(
    buttonsAtEntry: List<DisplayHomeButton>,
    selectedButtons: List<DisplayHomeButton>,
    exit: ButtonAddExit,
): List<DisplayHomeButton> = when (exit) {
    ButtonAddExit.Cancel -> buttonsAtEntry
    ButtonAddExit.Continue -> selectedButtons
}

@Composable
fun DisplayButtonAddScreen(
    initialSelectedButtons: List<DisplayHomeButton>,
    modifier: Modifier = Modifier,
    initialMusicButton: SeniorHomeButtonType? = null,
    availableDefaultButtons: List<DisplayHomeButton> = emptyList(),
    transientImportedButtons: List<DisplayHomeButton> = emptyList(),
    autoSelectKey: String? = null,
    autoSelectEvent: Int = 0,
    onBackClick: () -> Unit = {},
    onImportAppClick: () -> Unit = {},
    onSaveClick: (
        musicButton: SeniorHomeButtonType?,
        appButtons: List<DisplayHomeButton>,
    ) -> Unit = { _, _ -> },
) {
    val candidateButtons = remember(
        availableDefaultButtons,
        initialSelectedButtons,
        transientImportedButtons,
    ) {
        (initialSelectedButtons + availableDefaultButtons + transientImportedButtons)
            .filterNot(DisplayHomeButton::isProtectedButton)
            .distinctBy(DisplayHomeButton::stableKey)
    }
    var selectedButtonKeys by remember(initialSelectedButtons) {
        mutableStateOf(
            initialSelectedButtons
                .filterNot(DisplayHomeButton::isProtectedButton)
                .map(DisplayHomeButton::stableKey)
                .distinct(),
        )
    }
    var selectedMusicName by remember(initialMusicButton) {
        mutableStateOf(initialMusicButton?.takeIf(MusicButtons::contains)?.name)
    }
    var limitMessageEvent by remember { mutableIntStateOf(0) }
    var showLimitMessage by remember { mutableStateOf(false) }

    val candidateByKey = candidateButtons.associateBy(DisplayHomeButton::stableKey)
    val selectedButtonCount = buttonAddSelectedCount(selectedButtonKeys.size)
    val maximumButtonCount = buttonAddMaximumCount()
    val canSave = buttonAddCanContinue(selectedButtonKeys.size)
    val orderedButtons = selectedButtonKeys.mapNotNull(candidateByKey::get) +
        candidateButtons.filterNot { it.stableKey in selectedButtonKeys }

    LaunchedEffect(limitMessageEvent) {
        if (limitMessageEvent > 0) {
            showLimitMessage = true
            delay(2_000)
            showLimitMessage = false
        }
    }

    LaunchedEffect(autoSelectEvent, autoSelectKey) {
        val key = autoSelectKey ?: return@LaunchedEffect
        if (key in selectedButtonKeys || key !in candidateByKey) {
            return@LaunchedEffect
        }
        if (buttonAddSelectedCount(selectedButtonKeys.size) >= maximumButtonCount) {
            limitMessageEvent += 1
            return@LaunchedEffect
        }
        selectedButtonKeys = selectedButtonKeys + key
    }

    fun toggleMusic(button: SeniorHomeButtonType) {
        selectedMusicName = if (selectedMusicName == button.name) null else button.name
    }

    fun toggleAppButton(button: DisplayHomeButton) {
        val key = button.stableKey
        if (key in selectedButtonKeys) {
            selectedButtonKeys = selectedButtonKeys - key
            return
        }
        if (selectedButtonCount >= maximumButtonCount) {
            limitMessageEvent += 1
            return
        }
        selectedButtonKeys = selectedButtonKeys + key
        if (selectedButtonCount + 1 == maximumButtonCount) {
            limitMessageEvent += 1
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .statusBarsPadding()
            .background(SeniorOnColors.Background1),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ButtonAddTopBar(
                enabled = canSave,
                onBackClick = onBackClick,
                onSaveClick = {
                    onSaveClick(
                        selectedMusicName?.let(SeniorHomeButtonType::valueOf),
                        selectedButtonKeys.mapNotNull(candidateByKey::get),
                    )
                },
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 24.dp,
                    end = 16.dp,
                    bottom = 88.dp,
                ),
            ) {
                item {
                    FixedButtonSection(
                        title = "제공 기능",
                        buttons = ProvidedFeatureButtons,
                        selectedButtonNames = emptySet(),
                        rowType = ButtonAddRowType.Provided,
                        onButtonClick = {},
                    )
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
                item {
                    FixedButtonSection(
                        title = "상단 섹션",
                        buttons = MusicButtons,
                        selectedButtonNames = selectedMusicName?.let(::setOf).orEmpty(),
                        rowType = ButtonAddRowType.Music,
                        onButtonClick = ::toggleMusic,
                    )
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
                item {
                    AppButtonSection(
                        buttons = orderedButtons,
                        selectedButtonKeys = selectedButtonKeys.toSet(),
                        onImportAppClick = onImportAppClick,
                        onButtonClick = ::toggleAppButton,
                    )
                }
            }
        }

        if (showLimitMessage) {
            ButtonsFullMessage(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            )
        } else {
            SelectedButtonCounter(
                selectedCount = selectedButtonCount,
                maximumCount = maximumButtonCount,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
            )
        }
    }
}

private fun DisplayHomeButton.isProtectedButton(): Boolean =
    actionValue.uppercase() in setOf(
        "SCHEDULE",
        "COMPANION",
        "MEDICATION",
        "PHOTO",
        "EMERGENCY",
    ) || type in MusicButtons

@Composable
private fun AppButtonSection(
    buttons: List<DisplayHomeButton>,
    selectedButtonKeys: Set<String>,
    onImportAppClick: () -> Unit,
    onButtonClick: (DisplayHomeButton) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "앱",
            modifier = Modifier.weight(1f),
            style = SeniorOnTextStyles.BodyMSemiBold,
            color = SeniorOnColors.Primary700,
        )
        Row(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onImportAppClick,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_plus),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = SeniorOnColors.Primary700,
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "앱 불러오기",
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Primary700,
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (buttons.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(SeniorOnRadius.Medium))
                .background(SeniorOnColors.White),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "앱 불러오기로 버튼을 추가해보세요.",
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray500,
            )
        }
        return
    }

    val cardShape = RoundedCornerShape(SeniorOnRadius.Medium)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .dropShadow(
                shape = cardShape,
                shadow = Shadow(
                    radius = 12.dp,
                    color = SeniorOnColors.Black.copy(alpha = 0.08f),
                    offset = DpOffset(x = 0.dp, y = 2.dp),
                ),
            )
            .clip(cardShape)
            .background(SeniorOnColors.White)
            .padding(vertical = 8.dp),
    ) {
        buttons.forEachIndexed { index, button ->
            Column(
                modifier = Modifier
                    .padding(horizontal = 14.dp),
            ) {
                DynamicAppButtonRow(
                    button = button,
                    selected = button.stableKey in selectedButtonKeys,
                    onClick = { onButtonClick(button) },
                )
                if (index != buttons.lastIndex) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(SeniorOnColors.Gray100),
                    )
                }
            }
        }
    }
}

@Composable
private fun DynamicAppButtonRow(
    button: DisplayHomeButton,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = button.name,
            modifier = Modifier.weight(1f),
            style = if (selected) SeniorOnTextStyles.BodyLSemiBold
            else SeniorOnTextStyles.BodyMMedium,
            color = if (selected) SeniorOnColors.Gray300 else SeniorOnColors.Gray800,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            painter = painterResource(
                id = if (selected) R.drawable.ic_check else R.drawable.ic_plus,
            ),
            contentDescription = if (selected) "${button.name} 선택됨" else "${button.name} 추가",
            modifier = Modifier.size(24.dp),
            tint = if (selected) SeniorOnColors.Gray800 else SeniorOnColors.SupportBlue,
        )
    }
}

@Composable
private fun ButtonAddTopBar(
    enabled: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(SeniorOnDimensions.TopBarHeight)
            .background(SeniorOnColors.White)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
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
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "새 버튼 추가하기",
            modifier = Modifier.weight(1f),
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Gray800,
        )
        Row(
            modifier = Modifier
                .height(36.dp)
                .clip(RoundedCornerShape(38.dp))
                .background(
                    if (enabled) SeniorOnColors.Primary600
                    else SeniorOnColors.Primary600.copy(alpha = 0.5f),
                )
                .clickable(
                    enabled = enabled,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSaveClick,
                )
                .padding(start = 16.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "다음",
                style = SeniorOnTextStyles.BodySSemiBold,
                color = SeniorOnColors.White,
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_next),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = SeniorOnColors.White,
            )
        }
    }
}

private enum class ButtonAddRowType { Provided, Music }

@Composable
private fun FixedButtonSection(
    title: String,
    buttons: List<SeniorHomeButtonType>,
    selectedButtonNames: Set<String>,
    rowType: ButtonAddRowType,
    onButtonClick: (SeniorHomeButtonType) -> Unit,
) {
    Text(
        text = title,
        style = SeniorOnTextStyles.BodyMSemiBold,
        color = SeniorOnColors.Primary700,
    )
    Spacer(modifier = Modifier.height(8.dp))
    val cardShape = RoundedCornerShape(SeniorOnRadius.Medium)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .dropShadow(
                shape = cardShape,
                shadow = Shadow(
                    radius = 12.dp,
                    color = SeniorOnColors.Black.copy(alpha = 0.08f),
                    offset = DpOffset(x = 0.dp, y = 2.dp),
                ),
            )
            .clip(cardShape)
            .background(SeniorOnColors.White)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        buttons.forEachIndexed { index, button ->
            FixedButtonRow(
                button = button,
                selected = button.name in selectedButtonNames,
                rowType = rowType,
                onClick = { onButtonClick(button) },
            )
            if (index != buttons.lastIndex) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(SeniorOnColors.Gray100),
                )
            }
        }
    }
}

@Composable
private fun FixedButtonRow(
    button: SeniorHomeButtonType,
    selected: Boolean,
    rowType: ButtonAddRowType,
    onClick: () -> Unit,
) {
    val clickable = rowType != ButtonAddRowType.Provided
    val label = if (
        rowType == ButtonAddRowType.Provided &&
        button == SeniorHomeButtonType.Emergency
    ) {
        "긴급 알림"
    } else {
        button.displayLabel()
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .then(
                if (clickable) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ) else Modifier,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = if (selected) SeniorOnTextStyles.BodyLSemiBold
            else SeniorOnTextStyles.BodyMMedium,
            color = when {
                rowType == ButtonAddRowType.Provided -> SeniorOnColors.Gray300
                selected -> SeniorOnColors.Gray300
                else -> SeniorOnColors.Gray800
            },
        )
        if (selected) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = "$label 선택됨",
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.Gray800,
            )
        }
    }
}

@Composable
private fun SelectedButtonCounter(
    selectedCount: Int,
    maximumCount: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(280.dp)
            .height(46.dp)
            .dropShadow(
                shape = RoundedCornerShape(43.dp),
                shadow = Shadow(
                    radius = 20.dp,
                    color = SeniorOnColors.Black.copy(alpha = 0.18f),
                    offset = DpOffset(x = 0.dp, y = 7.dp),
                ),
            )
            .clip(RoundedCornerShape(43.dp))
            .background(SeniorOnColors.SupportWhite80),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$selectedCount / $maximumCount",
            style = SeniorOnTextStyles.BodyMBold,
            color = SeniorOnColors.Primary700,
        )
    }
}

@Composable
private fun ButtonsFullMessage(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .background(SeniorOnColors.Gray700.copy(alpha = 0.9f))
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_alert_filled),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = SeniorOnColors.White,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "버튼을 모두 담았어요.",
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.White,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun DisplayButtonAddScreenPreview() {
    val phone = DisplayHomeButton(
        name = "전화",
        actionType = "DEFAULT",
        actionValue = "PHONE",
        type = SeniorHomeButtonType.Call,
    )
    val camera = DisplayHomeButton(
        name = "카메라",
        actionType = "DEFAULT",
        actionValue = "CAMERA",
        type = SeniorHomeButtonType.Camera,
    )
    SENIOR_ONTheme {
        DisplayButtonAddScreen(
            initialSelectedButtons = listOf(phone, camera),
            availableDefaultButtons = listOf(phone, camera),
        )
    }
}
