package com.example.senior_on.ui.display

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.domain.model.SeniorHomeButtonType
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import kotlinx.coroutines.delay

private const val MinimumButtonSelectionCount = 10
private const val MaximumButtonCountWithoutMusic = 12
private const val MaximumButtonCountWithMusic = 11

private val ProvidedFeatureButtons = listOf(
    SeniorHomeButtonType.Medication,
    SeniorHomeButtonType.ChatBuddy,
    SeniorHomeButtonType.Schedule,
)

private val MusicButtons = listOf(
    SeniorHomeButtonType.Melon,
    SeniorHomeButtonType.Spotify,
)

private val CommunicationAppButtons = listOf(
    SeniorHomeButtonType.Call,
    SeniorHomeButtonType.Message,
    SeniorHomeButtonType.Calendar,
    SeniorHomeButtonType.Alarm,
    SeniorHomeButtonType.Memo,
    SeniorHomeButtonType.Camera,
    SeniorHomeButtonType.Photo,
    SeniorHomeButtonType.Recorder,
    SeniorHomeButtonType.Calculator,
    SeniorHomeButtonType.Settings,
    SeniorHomeButtonType.Flashlight,
    SeniorHomeButtonType.KakaoTalk,
    SeniorHomeButtonType.NaverBand,
    SeniorHomeButtonType.NaverCafe,
    SeniorHomeButtonType.Line,
)

private val ContentAppButtons = listOf(
    SeniorHomeButtonType.YouTube,
    SeniorHomeButtonType.Naver,
    SeniorHomeButtonType.Daum,
    SeniorHomeButtonType.Google,
    SeniorHomeButtonType.Tving,
    SeniorHomeButtonType.Netflix,
)

private val TransportationAppButtons = listOf(
    SeniorHomeButtonType.NaverMap,
    SeniorHomeButtonType.KakaoMap,
    SeniorHomeButtonType.KakaoT,
    SeniorHomeButtonType.TMap,
    SeniorHomeButtonType.KorailTalk,
)

private val FinanceAppButtons = listOf(
    SeniorHomeButtonType.Toss,
    SeniorHomeButtonType.KakaoPay,
    SeniorHomeButtonType.NaverPay,
    SeniorHomeButtonType.SamsungWallet,
    SeniorHomeButtonType.SamsungPay,
)

private val HealthAppButtons = listOf(
    SeniorHomeButtonType.CashWalk,
    SeniorHomeButtonType.Weather,
)

private val LifestyleAppButtons = listOf(
    SeniorHomeButtonType.Coupang,
    SeniorHomeButtonType.Karrot,
    SeniorHomeButtonType.Yogiyo,
    SeniorHomeButtonType.CoupangEats,
    SeniorHomeButtonType.HomeShopping,
)

internal val ButtonAppCatalog = (
    CommunicationAppButtons +
        ContentAppButtons +
        TransportationAppButtons +
        FinanceAppButtons +
        HealthAppButtons +
        LifestyleAppButtons +
        SeniorHomeButtonType.GoStop
    ).also { buttons ->
    check(buttons.size == 39) {
        "Button app catalog must contain exactly 39 apps."
    }
}

internal fun buttonAddSelectedCount(selectedAppCount: Int): Int =
    ProvidedFeatureButtons.size + selectedAppCount

internal fun buttonAddMaximumCount(hasMusicButton: Boolean): Int =
    if (hasMusicButton) {
        MaximumButtonCountWithMusic
    } else {
        MaximumButtonCountWithoutMusic
    }

internal fun buttonAddCanContinue(selectedAppCount: Int): Boolean =
    buttonAddSelectedCount(selectedAppCount) >= MinimumButtonSelectionCount

@Composable
fun DisplayButtonAddScreen(
    initialSelectedButtons: List<SeniorHomeButtonType>,
    initialMusicButton: SeniorHomeButtonType? = null,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSaveClick: (
        musicButton: SeniorHomeButtonType?,
        appButtons: List<SeniorHomeButtonType>,
    ) -> Unit = { _, _ -> },
) {
    val availableButtonNames = remember {
        ButtonAppCatalog.mapTo(hashSetOf(), SeniorHomeButtonType::name)
    }
    var selectedButtonNames by rememberSaveable(initialSelectedButtons) {
        mutableStateOf(
            ArrayList(
                initialSelectedButtons
                    .map(SeniorHomeButtonType::name)
                    .filter(availableButtonNames::contains)
                    .distinct()
            )
        )
    }
    var selectedMusicName by rememberSaveable(initialMusicButton) {
        mutableStateOf(
            initialMusicButton
                ?.takeIf(MusicButtons::contains)
                ?.name
        )
    }
    var limitMessageEvent by remember { mutableIntStateOf(0) }
    var showLimitMessage by remember { mutableStateOf(false) }

    val selectedButtonCount = buttonAddSelectedCount(selectedButtonNames.size)
    val maximumButtonCount = buttonAddMaximumCount(
        hasMusicButton = selectedMusicName != null
    )
    val canSave = buttonAddCanContinue(selectedButtonNames.size)

    LaunchedEffect(limitMessageEvent) {
        if (limitMessageEvent > 0) {
            showLimitMessage = true
            delay(2_000)
            showLimitMessage = false
        }
    }

    fun showButtonsFullMessage() {
        limitMessageEvent += 1
    }

    fun toggleMusic(button: SeniorHomeButtonType) {
        if (selectedMusicName == button.name) {
            selectedMusicName = null
            return
        }

        if (selectedButtonCount > MaximumButtonCountWithMusic) {
            showButtonsFullMessage()
            return
        }

        selectedMusicName = button.name
        if (selectedButtonCount == MaximumButtonCountWithMusic) {
            showButtonsFullMessage()
        }
    }

    fun toggleAppButton(button: SeniorHomeButtonType) {
        if (selectedButtonNames.contains(button.name)) {
            selectedButtonNames = ArrayList(selectedButtonNames).apply {
                remove(button.name)
            }
            return
        }

        if (selectedButtonCount >= maximumButtonCount) {
            showButtonsFullMessage()
            return
        }

        selectedButtonNames = ArrayList(selectedButtonNames).apply {
            add(button.name)
        }
        if (selectedButtonCount + 1 == maximumButtonCount) {
            showButtonsFullMessage()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.Background1)
            .statusBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ButtonAddTopBar(
                enabled = canSave,
                onBackClick = onBackClick,
                onSaveClick = {
                    onSaveClick(
                        selectedMusicName?.let(SeniorHomeButtonType::valueOf),
                        selectedButtonNames.map(SeniorHomeButtonType::valueOf),
                    )
                },
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 24.dp,
                    end = 16.dp,
                    bottom = 88.dp,
                ),
            ) {
                item {
                    ButtonAddSection(
                        title = "제공 기능",
                        buttons = ProvidedFeatureButtons,
                        selectedButtonNames = emptySet(),
                        rowType = ButtonAddRowType.Provided,
                        onButtonClick = {},
                    )
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }

                item {
                    ButtonAddSection(
                        title = "상단 섹션",
                        buttons = MusicButtons,
                        selectedButtonNames = selectedMusicName
                            ?.let(::setOf)
                            .orEmpty(),
                        rowType = ButtonAddRowType.Music,
                        onButtonClick = ::toggleMusic,
                    )
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }

                item {
                    ButtonAddSection(
                        title = "앱",
                        buttons = ButtonAppCatalog,
                        selectedButtonNames = selectedButtonNames.toSet(),
                        rowType = ButtonAddRowType.App,
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
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
            )
        }
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
            .height(54.dp)
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
                    if (enabled) {
                        SeniorOnColors.Primary600
                    } else {
                        SeniorOnColors.Primary600.copy(alpha = 0.5f)
                    }
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

private enum class ButtonAddRowType {
    Provided,
    Music,
    App,
}

@Composable
private fun ButtonAddSection(
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
            ButtonAddRow(
                button = button,
                selected = selectedButtonNames.contains(button.name),
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
private fun ButtonAddRow(
    button: SeniorHomeButtonType,
    selected: Boolean,
    rowType: ButtonAddRowType,
    onClick: () -> Unit,
) {
    val clickable = rowType != ButtonAddRowType.Provided
    val labelColor = when {
        rowType == ButtonAddRowType.Provided -> SeniorOnColors.Gray300
        selected -> SeniorOnColors.Gray300
        else -> SeniorOnColors.Gray800
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .then(
                if (clickable) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = button.displayLabel(),
            modifier = Modifier.weight(1f),
            style = if (selected) {
                SeniorOnTextStyles.BodyLSemiBold
            } else {
                SeniorOnTextStyles.BodyMMedium
            },
            color = labelColor,
        )

        when {
            selected -> Icon(
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = "${button.displayLabel()} 선택됨",
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.Gray800,
            )

            rowType == ButtonAddRowType.App -> Icon(
                painter = painterResource(id = R.drawable.ic_plus),
                contentDescription = "${button.displayLabel()} 추가",
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.SupportBlue,
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
private fun ButtonsFullMessage(
    modifier: Modifier = Modifier,
) {
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

@Preview(
    name = "Add Buttons - 8 of 12",
    showBackground = true,
    widthDp = 360,
    heightDp = 720,
)
@Composable
private fun DisplayButtonAddScreenPreview() {
    SENIOR_ONTheme {
        DisplayButtonAddScreen(
            initialSelectedButtons = ButtonAppCatalog.take(8),
        )
    }
}
