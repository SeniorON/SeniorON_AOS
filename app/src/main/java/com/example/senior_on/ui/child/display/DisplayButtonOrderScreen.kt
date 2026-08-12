package com.example.senior_on.ui.child.display

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.senior_on.R
import com.example.senior_on.domain.model.display.DisplayHomeButton
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.ui.common.component.SeniorOnActionButton
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnDimensions
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import kotlin.math.roundToInt

private const val FixedEmergencyGridIndex = 7

private data class ButtonOrderDragState(
    val buttonKey: String,
    val pointerPosition: Offset,
    val touchOffset: Offset,
    val widthPx: Float,
)

internal fun List<DisplayHomeButton>.withEmergencyAtFixedGridSlot(
    emergencyButton: DisplayHomeButton,
): List<DisplayHomeButton> {
    val reorderableButtons = distinctBy(DisplayHomeButton::stableKey)
        .filterNot(DisplayHomeButton::isEmergencyButton)
    return buildList {
        addAll(reorderableButtons.take(FixedEmergencyGridIndex))
        add(emergencyButton)
        addAll(reorderableButtons.drop(FixedEmergencyGridIndex))
    }
}

internal fun List<DisplayHomeButton>.withFixedButtonOrderSections():
    List<DisplayHomeButton> {
    val distinctButtons = distinctBy(DisplayHomeButton::stableKey)
    val musicButton = distinctButtons.firstOrNull(DisplayHomeButton::isMusicButton)
    val scheduleButton = distinctButtons.firstOrNull(DisplayHomeButton::isScheduleButton)
    val emergencyButton = distinctButtons.firstOrNull(DisplayHomeButton::isEmergencyButton)
    val gridButtons = distinctButtons.filterNot { button ->
        button.isMusicButton() ||
            button.isScheduleButton() ||
            button.isEmergencyButton()
    }

    return buildList {
        musicButton?.let(::add)
        scheduleButton?.let(::add)
        if (emergencyButton == null) {
            addAll(gridButtons)
        } else {
            addAll(gridButtons.withEmergencyAtFixedGridSlot(emergencyButton))
        }
    }
}

@Composable
fun DisplayButtonOrderScreen(
    initialButtons: List<DisplayHomeButton>,
    modifier: Modifier = Modifier,
    isSaving: Boolean = false,
    onBackClick: () -> Unit = {},
    onSaveClick: (List<DisplayHomeButton>) -> Unit = {},
) {
    val normalizedInitialButtons = remember(initialButtons) {
        initialButtons.withFixedButtonOrderSections()
    }
    val buttonByKey = remember(normalizedInitialButtons) {
        normalizedInitialButtons.associateBy(DisplayHomeButton::stableKey)
    }
    val musicButton = normalizedInitialButtons
        .firstOrNull(DisplayHomeButton::isMusicButton)
    val scheduleButton = normalizedInitialButtons
        .firstOrNull(DisplayHomeButton::isScheduleButton)
    val emergencyButton = normalizedInitialButtons
        .firstOrNull(DisplayHomeButton::isEmergencyButton)
    var orderedButtonKeys by remember(normalizedInitialButtons) {
        mutableStateOf(
            normalizedInitialButtons
                .filterNot { button ->
                    button.isMusicButton() ||
                        button.isScheduleButton() ||
                        button.isEmergencyButton()
                }
                .map(DisplayHomeButton::stableKey),
        )
    }
    var dragState by remember { mutableStateOf<ButtonOrderDragState?>(null) }
    var contentCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var containerBounds by remember { mutableStateOf<Rect?>(null) }
    val buttonBounds = remember(normalizedInitialButtons) { mutableMapOf<String, Rect>() }
    val latestOrderedButtonKeys = rememberUpdatedState(orderedButtonKeys)
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .onGloballyPositioned { containerBounds = it.boundsInRoot() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(SeniorOnColors.Background1),
        ) {
            ButtonOrderTopBar(
                onBackClick = onBackClick,
                isSaving = isSaving,
                onSaveClick = {
                    onSaveClick(
                        buildList {
                            musicButton?.let(::add)
                            scheduleButton?.let(::add)
                            addAll(orderedButtonKeys.mapNotNull(buttonByKey::get))
                            emergencyButton?.let(::add)
                        }.withFixedButtonOrderSections(),
                    )
                },
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .onGloballyPositioned { contentCoordinates = it }
                    .pointerInput(buttonByKey) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { position ->
                                val rootPosition = contentCoordinates
                                    ?.localToRoot(position)
                                    ?: return@detectDragGesturesAfterLongPress
                                val containerTopLeft = containerBounds
                                    ?.topLeft
                                    ?: return@detectDragGesturesAfterLongPress
                                val (draggedKey, draggedBounds) = buttonBounds.entries
                                    .firstOrNull { (_, bounds) -> bounds.contains(rootPosition) }
                                    ?: return@detectDragGesturesAfterLongPress
                                dragState = ButtonOrderDragState(
                                    buttonKey = draggedKey,
                                    pointerPosition = rootPosition - containerTopLeft,
                                    touchOffset = rootPosition - draggedBounds.topLeft,
                                    widthPx = draggedBounds.width,
                                )
                                hapticFeedback.performHapticFeedback(
                                    HapticFeedbackType.LongPress,
                                )
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val currentDragState = dragState
                                    ?: return@detectDragGesturesAfterLongPress
                                val rootPosition = contentCoordinates
                                    ?.localToRoot(change.position)
                                    ?: return@detectDragGesturesAfterLongPress
                                val containerTopLeft = containerBounds
                                    ?.topLeft
                                    ?: return@detectDragGesturesAfterLongPress
                                dragState = currentDragState.copy(
                                    pointerPosition = rootPosition - containerTopLeft,
                                )
                                val targetKey = buttonBounds.entries
                                    .firstOrNull { (_, bounds) -> bounds.contains(rootPosition) }
                                    ?.key
                                    ?: return@detectDragGesturesAfterLongPress
                                if (targetKey == currentDragState.buttonKey) {
                                    return@detectDragGesturesAfterLongPress
                                }
                                val currentKeys = latestOrderedButtonKeys.value
                                val fromIndex = currentKeys.indexOf(currentDragState.buttonKey)
                                val toIndex = currentKeys.indexOf(targetKey)
                                if (fromIndex < 0 || toIndex < 0) {
                                    return@detectDragGesturesAfterLongPress
                                }
                                orderedButtonKeys = currentKeys.toMutableList().apply {
                                    add(toIndex, removeAt(fromIndex))
                                }
                            },
                            onDragEnd = { dragState = null },
                            onDragCancel = { dragState = null },
                        )
                    }
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(modifier = Modifier.height(22.dp))
                Text(
                    text = "버튼 순서 변경",
                    style = SeniorOnTextStyles.HeadingS,
                    color = SeniorOnColors.Gray800,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "버튼을 길게 눌러 순서를 변경할 수 있어요",
                    style = SeniorOnTextStyles.BodySMedium,
                    color = SeniorOnColors.Gray500,
                )
                Spacer(modifier = Modifier.height(16.dp))

                musicButton?.let { button ->
                    ButtonOrderCard(
                        button = button,
                        featured = true,
                        fixed = true,
                        isDragging = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                scheduleButton?.let { schedule ->
                    if (musicButton != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    ButtonOrderCard(
                        button = schedule,
                        featured = musicButton == null,
                        fixed = true,
                        showDescription = true,
                        isDragging = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                val orderedButtons = orderedButtonKeys.mapNotNull(buttonByKey::get)
                val gridButtons = emergencyButton?.let {
                    orderedButtons.withEmergencyAtFixedGridSlot(it)
                } ?: orderedButtons
                if (gridButtons.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(SeniorOnColors.Background4),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    gridButtons.chunked(2).forEach { rowButtons ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            rowButtons.forEach { button ->
                                if (button.isEmergencyButton()) {
                                    FixedEmergencyButtonOrderCard(
                                        label = button.name,
                                        modifier = Modifier.weight(1f),
                                    )
                                } else {
                                    val cardModifier = Modifier
                                        .weight(1f)
                                        .onGloballyPositioned {
                                            buttonBounds[button.stableKey] = it.boundsInRoot()
                                        }
                                    if (dragState?.buttonKey == button.stableKey) {
                                        ButtonOrderPlaceholder(modifier = cardModifier)
                                    } else {
                                        ButtonOrderCard(
                                            button = button,
                                            featured = false,
                                            isDragging = false,
                                            modifier = cardModifier,
                                        )
                                    }
                                }
                            }
                            if (rowButtons.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        dragState?.let { currentDragState ->
            val draggedButton = buttonByKey[currentDragState.buttonKey]
                ?: return@let
            ButtonOrderCard(
                button = draggedButton,
                featured = false,
                isDragging = true,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (currentDragState.pointerPosition.x -
                                currentDragState.touchOffset.x).roundToInt(),
                            y = (currentDragState.pointerPosition.y -
                                currentDragState.touchOffset.y).roundToInt(),
                        )
                    }
                    .width(with(density) { currentDragState.widthPx.toDp() })
                    .zIndex(10f),
            )
        }
    }
}

@Composable
private fun ButtonOrderPlaceholder(modifier: Modifier = Modifier) {
    Spacer(
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .background(SeniorOnColors.Background3),
    )
}

@Composable
private fun FixedEmergencyButtonOrderCard(
    label: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Medium)
    Box(
        modifier = modifier
            .height(84.dp)
            .dropShadow(
                shape = shape,
                shadow = Shadow(
                    radius = 18.dp,
                    color = SeniorOnColors.Black.copy(alpha = 0.08f),
                    offset = DpOffset(x = 0.dp, y = 4.dp),
                ),
            )
            .clip(shape)
            .background(SeniorOnColors.White.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_push_pin),
            contentDescription = null,
            modifier = Modifier.align(Alignment.TopStart)
                .padding(start = 6.dp, top = 7.dp).size(24.dp),
            tint = SeniorOnColors.Gray400,
        )
        Text(
            text = label,
            style = SeniorOnTextStyles.HeadingS,
            color = SeniorOnColors.Gray700,
            maxLines = 1,
        )
    }
}

@Composable
private fun ButtonOrderTopBar(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    isSaving: Boolean,
) {
    Box(
        modifier = Modifier.fillMaxWidth().height(SeniorOnDimensions.TopBarHeight)
            .background(SeniorOnColors.White),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                .size(26.dp).clickable(
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
        SeniorOnActionButton(
            text = "저장",
            onClick = onSaveClick,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .height(36.dp),
            enabled = !isSaving,
            isLoading = isSaving,
            shape = RoundedCornerShape(38.dp),
            minHeight = 36.dp,
            horizontalPadding = 18.dp,
            textStyle = SeniorOnTextStyles.BodySSemiBold,
            loadingIndicatorSize = 18.dp,
        )
    }
}

@Composable
private fun ButtonOrderCard(
    button: DisplayHomeButton,
    featured: Boolean,
    isDragging: Boolean,
    modifier: Modifier = Modifier,
    showDescription: Boolean = false,
    fixed: Boolean = false,
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Medium)
    val backgroundColor = if (featured) SeniorOnColors.Primary500 else SeniorOnColors.White
    val contentColor = if (featured) SeniorOnColors.White else SeniorOnColors.Gray700
    val cardScale by animateFloatAsState(
        targetValue = if (isDragging) 1.03f else 1f,
        animationSpec = tween(150),
        label = "buttonOrderCardScale",
    )
    val liftOffsetY by animateDpAsState(
        targetValue = if (isDragging) (-4).dp else 0.dp,
        animationSpec = tween(150),
        label = "buttonOrderCardLiftOffsetY",
    )
    val shadowRadius by animateDpAsState(
        targetValue = if (isDragging) 24.dp else 18.dp,
        animationSpec = tween(150),
        label = "buttonOrderCardShadowRadius",
    )
    Row(
        modifier = modifier
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
                translationY = liftOffsetY.toPx()
            }
            .height(84.dp)
            .dropShadow(
                shape = shape,
                shadow = Shadow(
                    radius = shadowRadius,
                    color = SeniorOnColors.Black.copy(alpha = if (isDragging) 0.12f else 0.08f),
                    offset = DpOffset(x = 0.dp, y = if (isDragging) 8.dp else 4.dp),
                ),
            )
            .clip(shape)
            .background(backgroundColor)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(
                id = if (fixed) R.drawable.ic_push_pin else R.drawable.ic_shortcut_card,
            ),
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            tint = when {
                featured -> SeniorOnColors.White
                fixed -> SeniorOnColors.Gray400
                else -> SeniorOnColors.Gray600
            },
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (button.isMusicButton()) "노래 듣기" else button.name,
                style = if (featured || showDescription) SeniorOnTextStyles.HeadingM
                else SeniorOnTextStyles.HeadingS,
                color = contentColor,
                maxLines = if (featured || showDescription) 1 else 2,
                softWrap = !featured && !showDescription,
                overflow = TextOverflow.Ellipsis,
            )
            if (button.isScheduleButton() && (featured || showDescription)) {
                Text(
                    text = "상세 일정 표시",
                    style = SeniorOnTextStyles.BodyMSemiBold,
                    color = if (featured) SeniorOnColors.SupportWhite80 else SeniorOnColors.Gray500,
                    maxLines = 1,
                )
            }
        }
        if (featured && button.isMusicButton()) {
            Icon(
                painter = painterResource(id = R.drawable.ic_big_play),
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                tint = SeniorOnColors.White,
            )
        }
    }
}

internal fun SeniorHomeButtonType?.isMusicButton(): Boolean = this in MusicButtons

internal fun DisplayHomeButton?.isMusicButton(): Boolean =
    this?.type.isMusicButton()

private fun DisplayHomeButton.isScheduleButton(): Boolean =
    isDefaultAction("SCHEDULE")

private fun DisplayHomeButton.isEmergencyButton(): Boolean =
    isDefaultAction("EMERGENCY")

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun DisplayButtonOrderScreenPreview() {
    val buttons = listOf(
        DisplayHomeButton(
            name = "일정",
            actionType = "DEFAULT",
            actionValue = "SCHEDULE",
            type = SeniorHomeButtonType.Schedule,
        ),
        DisplayHomeButton(
            name = "전화",
            actionType = "DEFAULT",
            actionValue = "PHONE",
            type = SeniorHomeButtonType.Call,
        ),
        DisplayHomeButton(
            name = "유튜브",
            actionType = "APP",
            actionValue = "com.google.android.youtube",
            packageName = "com.google.android.youtube",
        ),
        DisplayHomeButton(
            name = "긴급알림",
            actionType = "DEFAULT",
            actionValue = "EMERGENCY",
            type = SeniorHomeButtonType.Emergency,
        ),
    )
    SENIOR_ONTheme {
        DisplayButtonOrderScreen(initialButtons = buttons)
    }
}
