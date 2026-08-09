package com.example.senior_on.ui.child.display

import com.example.senior_on.ui.theme.SeniorOnDimensions
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.senior_on.R
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import kotlin.math.roundToInt

private const val FixedEmergencyGridIndex = 7

private data class ButtonOrderDragState(
    val buttonName: String,
    val pointerPosition: Offset,
    val touchOffset: Offset,
    val widthPx: Float,
    val featured: Boolean,
    val showDescription: Boolean,
)

internal fun List<SeniorHomeButtonType>.withEmergencyAtFixedGridSlot():
    List<SeniorHomeButtonType> {
    val reorderableButtons = distinct()
        .filterNot { it == SeniorHomeButtonType.Emergency }

    return buildList {
        addAll(reorderableButtons.take(FixedEmergencyGridIndex))
        add(SeniorHomeButtonType.Emergency)
        addAll(reorderableButtons.drop(FixedEmergencyGridIndex))
    }
}

@Composable
fun DisplayButtonOrderScreen(
    initialButtons: List<SeniorHomeButtonType>,
    customButtonLabels: Map<SeniorHomeButtonType, String> = emptyMap(),
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSaveClick: (List<SeniorHomeButtonType>) -> Unit = {},
) {
    var orderedButtonNames by rememberSaveable(initialButtons) {
        mutableStateOf(
            ArrayList(
                initialButtons
                    .filterNot { it == SeniorHomeButtonType.Emergency }
                    .distinct()
                    .map(SeniorHomeButtonType::name)
            )
        )
    }
    var dragState by remember { mutableStateOf<ButtonOrderDragState?>(null) }
    var contentCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var containerBounds by remember { mutableStateOf<Rect?>(null) }
    val buttonBounds = remember { mutableMapOf<String, Rect>() }
    val latestOrderedButtonNames = rememberUpdatedState(orderedButtonNames)
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
                onSaveClick = {
                    onSaveClick(
                        orderedButtonNames.map(SeniorHomeButtonType::valueOf)
                    )
                },
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .onGloballyPositioned { contentCoordinates = it }
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { position ->
                                val rootPosition = contentCoordinates
                                    ?.localToRoot(position)
                                    ?: return@detectDragGesturesAfterLongPress
                                val containerTopLeft = containerBounds
                                    ?.topLeft
                                    ?: return@detectDragGesturesAfterLongPress
                                val (draggedName, draggedBounds) =
                                    buttonBounds.entries
                                        .firstOrNull { (_, bounds) ->
                                            bounds.contains(rootPosition)
                                        }
                                        ?: return@detectDragGesturesAfterLongPress
                                val buttonsAtDragStart =
                                    latestOrderedButtonNames.value.map(
                                        SeniorHomeButtonType::valueOf
                                    )
                                val draggedButton =
                                    SeniorHomeButtonType.valueOf(draggedName)
                                val featuredAtDragStart =
                                    buttonsAtDragStart.firstOrNull() == draggedButton
                                val wideScheduleAtDragStart =
                                    buttonsAtDragStart.firstOrNull()
                                        .isMusicButton() &&
                                        buttonsAtDragStart.getOrNull(1) ==
                                        SeniorHomeButtonType.Schedule

                                dragState = ButtonOrderDragState(
                                    buttonName = draggedName,
                                    pointerPosition =
                                        rootPosition - containerTopLeft,
                                    touchOffset =
                                        rootPosition - draggedBounds.topLeft,
                                    widthPx = draggedBounds.width,
                                    featured = featuredAtDragStart,
                                    showDescription =
                                        draggedButton ==
                                        SeniorHomeButtonType.Schedule &&
                                        (featuredAtDragStart ||
                                            wideScheduleAtDragStart),
                                )
                                hapticFeedback.performHapticFeedback(
                                    HapticFeedbackType.LongPress
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
                                    pointerPosition =
                                        rootPosition - containerTopLeft,
                                )

                                val targetName = buttonBounds.entries
                                    .firstOrNull { (_, bounds) ->
                                        bounds.contains(rootPosition)
                                    }
                                    ?.key
                                    ?: return@detectDragGesturesAfterLongPress
                                if (targetName == currentDragState.buttonName) {
                                    return@detectDragGesturesAfterLongPress
                                }

                                val currentNames = latestOrderedButtonNames.value
                                val fromIndex = currentNames.indexOf(
                                    currentDragState.buttonName
                                )
                                val toIndex = currentNames.indexOf(targetName)
                                if (fromIndex < 0 || toIndex < 0) {
                                    return@detectDragGesturesAfterLongPress
                                }

                                orderedButtonNames = ArrayList(currentNames).apply {
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

            val orderedButtons = orderedButtonNames.map(
                SeniorHomeButtonType::valueOf
            )
            val featuredButton = orderedButtons.firstOrNull()

            featuredButton?.let { button ->
                val cardModifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned {
                        buttonBounds[button.name] = it.boundsInRoot()
                    }
                if (dragState?.buttonName == button.name) {
                    ButtonOrderPlaceholder(modifier = cardModifier)
                } else {
                    ButtonOrderCard(
                        button = button,
                        customLabel = customButtonLabels[button],
                        featured = true,
                        isDragging = false,
                        modifier = cardModifier,
                    )
                }
            }

            val showWideSchedule = featuredButton.isMusicButton() &&
                orderedButtons.getOrNull(1) == SeniorHomeButtonType.Schedule
            if (showWideSchedule) {
                Spacer(modifier = Modifier.height(16.dp))

                val scheduleModifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned {
                        buttonBounds[SeniorHomeButtonType.Schedule.name] =
                            it.boundsInRoot()
                    }
                if (dragState?.buttonName ==
                    SeniorHomeButtonType.Schedule.name
                ) {
                    ButtonOrderPlaceholder(modifier = scheduleModifier)
                } else {
                    ButtonOrderCard(
                        button = SeniorHomeButtonType.Schedule,
                        customLabel = customButtonLabels[
                            SeniorHomeButtonType.Schedule
                        ],
                        featured = false,
                        showDescription = true,
                        isDragging = false,
                        modifier = scheduleModifier,
                    )
                }
            }

            val gridButtons = orderedButtons
                .drop(if (showWideSchedule) 2 else 1)
                .withEmergencyAtFixedGridSlot()
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
                            if (button == SeniorHomeButtonType.Emergency) {
                                FixedEmergencyButtonOrderCard(
                                    modifier = Modifier.weight(1f),
                                )
                            } else {
                                val cardModifier = Modifier
                                    .weight(1f)
                                    .onGloballyPositioned {
                                        buttonBounds[button.name] =
                                            it.boundsInRoot()
                                    }
                                if (dragState?.buttonName == button.name) {
                                    ButtonOrderPlaceholder(
                                        modifier = cardModifier,
                                    )
                                } else {
                                    ButtonOrderCard(
                                        button = button,
                                        customLabel = customButtonLabels[button],
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
            val draggedButton = SeniorHomeButtonType.valueOf(
                currentDragState.buttonName
            )
            ButtonOrderCard(
                button = draggedButton,
                customLabel = customButtonLabels[draggedButton],
                featured = currentDragState.featured,
                showDescription = currentDragState.showDescription,
                isDragging = true,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (
                                currentDragState.pointerPosition.x -
                                    currentDragState.touchOffset.x
                                ).roundToInt(),
                            y = (
                                currentDragState.pointerPosition.y -
                                    currentDragState.touchOffset.y
                                ).roundToInt(),
                        )
                    }
                    .width(with(density) { currentDragState.widthPx.toDp() })
                    .zIndex(10f),
            )
        }
    }
}

@Composable
private fun ButtonOrderPlaceholder(
    modifier: Modifier = Modifier,
) {
    Spacer(
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .background(SeniorOnColors.Background3),
    )
}

@Composable
private fun FixedEmergencyButtonOrderCard(
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
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 6.dp, top = 7.dp)
                .size(24.dp),
            tint = SeniorOnColors.Gray400,
        )

        Text(
            text = SeniorHomeButtonType.Emergency.displayLabel(),
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
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(SeniorOnDimensions.TopBarHeight)
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

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(38.dp))
                .background(SeniorOnColors.Primary600)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSaveClick,
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

@Composable
private fun ButtonOrderCard(
    button: SeniorHomeButtonType,
    customLabel: String?,
    featured: Boolean,
    isDragging: Boolean,
    modifier: Modifier = Modifier,
    showDescription: Boolean = false,
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Medium)
    val backgroundColor = if (featured) {
        SeniorOnColors.Primary500
    } else {
        SeniorOnColors.White
    }
    val contentColor = if (featured) {
        SeniorOnColors.White
    } else {
        SeniorOnColors.Gray700
    }
    val cardScale by animateFloatAsState(
        targetValue = if (isDragging) 1.03f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "buttonOrderCardScale",
    )
    val liftOffsetY by animateDpAsState(
        targetValue = if (isDragging) (-4).dp else 0.dp,
        animationSpec = tween(durationMillis = 150),
        label = "buttonOrderCardLiftOffsetY",
    )
    val shadowRadius by animateDpAsState(
        targetValue = if (isDragging) 24.dp else 18.dp,
        animationSpec = tween(durationMillis = 150),
        label = "buttonOrderCardShadowRadius",
    )
    val shadowOffsetY by animateDpAsState(
        targetValue = if (isDragging) 8.dp else 4.dp,
        animationSpec = tween(durationMillis = 150),
        label = "buttonOrderCardShadowOffsetY",
    )
    val shadowAlpha by animateFloatAsState(
        targetValue = if (isDragging) 0.12f else 0.08f,
        animationSpec = tween(durationMillis = 150),
        label = "buttonOrderCardShadowAlpha",
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
                    color = SeniorOnColors.Black.copy(alpha = shadowAlpha),
                    offset = DpOffset(x = 0.dp, y = shadowOffsetY),
                ),
            )
            .clip(shape)
            .background(backgroundColor)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ButtonOrderDragHandle(
            color = if (featured) {
                SeniorOnColors.White
            } else {
                SeniorOnColors.Gray600
            },
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (button.isMusicButton()) {
                    "노래 듣기"
                } else {
                    customLabel ?: button.displayLabel()
                },
                style = if (featured || showDescription) {
                    SeniorOnTextStyles.HeadingM
                } else {
                    SeniorOnTextStyles.HeadingS
                },
                color = contentColor,
                maxLines = if (featured || showDescription) 1 else 2,
                softWrap = !featured && !showDescription,
                overflow = TextOverflow.Ellipsis,
            )

            val description = when {
                button == SeniorHomeButtonType.Schedule &&
                    (featured || showDescription) -> "상세 일정 표시"
                else -> null
            }
            description?.let {
                Text(
                    text = it,
                    style = SeniorOnTextStyles.BodyMSemiBold,
                    color = if (featured) {
                        SeniorOnColors.SupportWhite80
                    } else {
                        SeniorOnColors.Gray500
                    },
                    maxLines = 1,
                )
            }
        }

        if (featured && button.isMusicButton()) {
            Box(
                modifier = Modifier.size(38.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_big_play),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                    tint = SeniorOnColors.White,
                )
            }
        }
    }
}

@Composable
private fun ButtonOrderDragHandle(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_shortcut_card),
        contentDescription = null,
        modifier = modifier.size(26.dp),
        tint = color,
    )
}

internal fun SeniorHomeButtonType?.isMusicButton(): Boolean =
    this in MusicButtons

@Preview(
    name = "Button Order With Music",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun DisplayButtonOrderScreenPreview() {
    SENIOR_ONTheme {
        DisplayButtonOrderScreen(
            initialButtons = listOf(
                SeniorHomeButtonType.Melon,
                SeniorHomeButtonType.Schedule,
                SeniorHomeButtonType.Call,
                SeniorHomeButtonType.Message,
                SeniorHomeButtonType.Camera,
                SeniorHomeButtonType.ChatBuddy,
                SeniorHomeButtonType.Medication,
                SeniorHomeButtonType.Calendar,
                SeniorHomeButtonType.YouTube,
            ),
        )
    }
}
