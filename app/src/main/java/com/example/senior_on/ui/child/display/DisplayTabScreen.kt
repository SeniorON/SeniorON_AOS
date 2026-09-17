package com.example.senior_on.ui.child.display

import com.example.senior_on.ui.theme.SeniorOnDimensions
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.senior_on.R
import com.example.senior_on.data.source.mock.fixtures.MockDisplayFixtures
import com.example.senior_on.data.source.display.MockDisplayScenario
import com.example.senior_on.data.source.mock.fixtures.MockSeniorFixtures
import com.example.senior_on.domain.model.display.DisplayDevice
import com.example.senior_on.domain.model.display.DisplayHomeButton
import com.example.senior_on.domain.model.display.DisplayTodaySchedule
import com.example.senior_on.domain.model.display.DisplayDeviceConnectionStatus
import com.example.senior_on.domain.model.parent.ParentInfo
import com.example.senior_on.domain.model.parent.CaregiverRelationship
import com.example.senior_on.domain.model.senior.ManagedSenior
import com.example.senior_on.domain.model.display.SeniorScreenConfiguration
import com.example.senior_on.ui.child.ChildBottomNavigation
import com.example.senior_on.ui.child.ChildMainTab
import com.example.senior_on.ui.child.notification.ParentPhoneInternetRequiredDialog
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnBrushes
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DisplayTabScreen(
    uiState: DisplayTabUiState,
    modifier: Modifier = Modifier,
    canEditScreen: Boolean = true,
    showScreenEditActions: Boolean = canEditScreen,
    onDeviceClick: () -> Unit = {},
    onParentInfoClick: () -> Unit = {},
    onLargePreviewClick: () -> Unit = {},
    onFontEditClick: () -> Unit = {},
    onButtonEditClick: () -> Unit = {},
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    seniorAccounts: List<ManagedSenior> = emptyList(),
    onSeniorAccountClick: (ManagedSenior) -> Unit = {},
    onAddSeniorAccountClick: () -> Unit = {},
) {
    var showSeniorAccountSwitcher by rememberSaveable { mutableStateOf(false) }
    val accountItems = seniorAccounts.ifEmpty {
        listOfNotNull(uiState.parentInfo?.toManagedSenior())
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.White)
        ) {
            DisplayTopBar(
                title = uiState.resolveDisplayTopBarTitle()
                    ?: stringResource(R.string.display_default_senior_relationship),
                onAccountSelectorClick = {
                    showSeniorAccountSwitcher = true
                },
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item {
                    DisplaySummarySection(
                        parentInfo = uiState.parentInfo,
                        relationshipLabel = uiState.relationshipLabel,
                        device = uiState.device,
                        onDeviceClick = onDeviceClick,
                        onParentInfoClick = onParentInfoClick,
                    )
                }

                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(SeniorOnColors.Background1)
                    )
                }

                item {
                    ScreenEditSection(
                        parentInfo = uiState.parentInfo,
                        relationshipLabel = uiState.relationshipLabel,
                        configuration = uiState.screenConfiguration,
                        buttonItems = uiState.configuredButtonItems,
                        todaySchedule = uiState.todaySchedule,
                        canEditScreen = canEditScreen,
                        showScreenEditActions = showScreenEditActions,
                        onLargePreviewClick = onLargePreviewClick,
                        onFontEditClick = onFontEditClick,
                        onButtonEditClick = onButtonEditClick,
                    )
                }
            }
        }
    }

    if (showSeniorAccountSwitcher) {
        SeniorAccountSwitcherBottomSheet(
            accounts = accountItems,
            onDismiss = { showSeniorAccountSwitcher = false },
            onAccountClick = { account ->
                showSeniorAccountSwitcher = false
                onSeniorAccountClick(account)
            },
            onAddAccountClick = {
                showSeniorAccountSwitcher = false
                onAddSeniorAccountClick()
            },
        )
    }
}

private fun ParentInfo.toManagedSenior(): ManagedSenior = ManagedSenior(
    familyId = 0L,
    seniorId = seniorId,
    parentUserId = null,
    name = name,
    relationship = CaregiverRelationship.fromDisplayLabel(relationshipLabel),
)

internal fun DisplayTabUiState.resolveDisplayTopBarTitle(): String? =
    relationshipLabel
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?: parentInfo
            ?.relationshipLabel
            ?.trim()
            ?.takeIf(String::isNotEmpty)

@Composable
internal fun DisplayTabLoadingScreen(
    topBarTitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White),
    ) {
        DisplayTopBar(title = topBarTitle)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = SeniorOnColors.Primary600)
        }
    }
}

@Composable
internal fun DisplayTabErrorScreen(
    message: String,
    onRetryClick: () -> Unit,
    topBarTitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White),
    ) {
        DisplayTopBar(title = topBarTitle)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = message,
                style = SeniorOnTextStyles.BodyMMedium,
                color = SeniorOnColors.Gray500,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "다시 시도",
                modifier = Modifier.clickable(onClick = onRetryClick),
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.Primary600,
            )
        }
    }
}

@Composable
private fun DisplayTopBar(
    title: String?,
    onAccountSelectorClick: (() -> Unit)? = null,
) {
    val normalizedTitle = title?.trim()?.takeIf(String::isNotEmpty)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f)
            .dropShadow(
                shape = RectangleShape,
                shadow = Shadow(
                    radius = 12.dp,
                    spread = 0.dp,
                    color = SeniorOnColors.Black.copy(alpha = 0.06f),
                    offset = DpOffset(x = 0.dp, y = 4.dp),
                )
            )
            .background(SeniorOnColors.White)
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SeniorOnDimensions.TopBarHeight)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (normalizedTitle != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = normalizedTitle,
                        style = SeniorOnTextStyles.HeadingXS,
                        color = SeniorOnColors.Gray800,
                    )

                    if (onAccountSelectorClick != null) {
                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(SeniorOnRadius.Small))
                                .clickable(
                                    interactionSource = remember {
                                        MutableInteractionSource()
                                    },
                                    indication = null,
                                    role = Role.Button,
                                    onClick = onAccountSelectorClick,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = R.drawable.ic_sm_chevron_down_2
                                ),
                                contentDescription = stringResource(
                                    R.string.display_senior_account_selector_description
                                ),
                                modifier = Modifier.size(24.dp),
                                tint = SeniorOnColors.Gray800,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DisplaySummarySection(
    parentInfo: ParentInfo?,
    relationshipLabel: String?,
    device: DisplayDevice?,
    onDeviceClick: () -> Unit,
    onParentInfoClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        DeviceConnectionBanner(
            device = device,
            relationshipLabel = relationshipLabel,
            onClick = onDeviceClick,
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (parentInfo == null) {
            EmptyParentInformationCard(onClick = onParentInfoClick)
        } else {
            ParentInformationCard(
                parentInfo = parentInfo,
                relationshipLabel = relationshipLabel
                    ?: parentInfo.relationshipLabel,
                onClick = onParentInfoClick,
            )
        }
    }
}

@Composable
private fun DeviceConnectionBanner(
    device: DisplayDevice?,
    relationshipLabel: String?,
    onClick: () -> Unit,
) {
    val isNotConnected = device == null
    val isOnline = device?.connectionStatus == DisplayDeviceConnectionStatus.Online
    val isLoginExpired =
        device?.connectionStatus == DisplayDeviceConnectionStatus.LoginExpired
    val leadingContentColor = when {
        isNotConnected -> SeniorOnColors.Red300
        isLoginExpired -> SeniorOnColors.Red400
        isOnline -> SeniorOnColors.Primary600
        else -> SeniorOnColors.Gray700
    }
    val backgroundBrush = when {
        isNotConnected -> SeniorOnBrushes.DisplayDeviceNotConnected
        isLoginExpired -> SolidColor(SeniorOnColors.Beige100)
        isOnline -> SeniorOnBrushes.DisplayDeviceConnected
        else -> SolidColor(SeniorOnColors.Gray100)
    }
    val shape = RoundedCornerShape(50.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .then(
                if (isLoginExpired) {
                    Modifier.border(1.dp, SeniorOnColors.Beige200, shape)
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .background(backgroundBrush)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_sm_link),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = leadingContentColor,
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = device?.let {
                listOfNotNull(it.name, relationshipLabel)
                    .filter(String::isNotBlank)
                    .joinToString(" · ")
            } ?: "연결된 기기가 없습니다",
            modifier = Modifier.weight(1f),
            style = SeniorOnTextStyles.BodySSemiBold,
            color = when {
                isNotConnected -> SeniorOnColors.Red300
                else -> SeniorOnColors.Black
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (isOnline) {
            Icon(
                painter = painterResource(id = R.drawable.ic_sm_battery),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.Primary600,
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = device?.batteryLevelPercent?.let { "$it%" } ?: "확인 불가",
                style = SeniorOnTextStyles.BodySSemiBold,
                color = SeniorOnColors.Primary600,
            )

            Spacer(modifier = Modifier.width(4.dp))
        } else if (device != null) {
            Text(
                text = if (isLoginExpired) {
                    "로그인이 만료되었습니다"
                } else {
                    "오프라인"
                },
                style = SeniorOnTextStyles.BodySSemiBold,
                color = if (isLoginExpired) SeniorOnColors.Red400 else SeniorOnColors.Gray500,
            )

            Spacer(modifier = Modifier.width(6.dp))
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_sm_arrow_right),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = when {
                isNotConnected -> SeniorOnColors.Red300
                isLoginExpired -> SeniorOnColors.Red400
                else -> SeniorOnColors.Gray700
            },
        )
    }
}

@Composable
private fun ParentInformationCard(
    parentInfo: ParentInfo,
    relationshipLabel: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Large)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(134.dp)
            .dropShadow(
                shape = shape,
                shadow = Shadow(
                    radius = 14.dp,
                    spread = 0.dp,
                    color = Color(0xFFB5C69C).copy(alpha = 0.9f),
                    offset = DpOffset(x = 0.dp, y = 4.dp),
                )
            )
            .clip(shape)
            .background(SeniorOnColors.Primary600)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = parentInfo.name,
                style = SeniorOnTextStyles.HeadingS,
                color = SeniorOnColors.White,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(17.dp))
                    .background(SeniorOnColors.White.copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = relationshipLabel,
                    style = SeniorOnTextStyles.CaptionMedium,
                    color = SeniorOnColors.White,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                painter = painterResource(id = R.drawable.ic_sm_arrow_right),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.White,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = parentInfo.birthDescription(),
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.White,
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SeniorOnColors.SupportWhite20)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.Primary200,
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = parentInfo.fullAddress,
                modifier = Modifier.weight(1f),
                style = SeniorOnTextStyles.CaptionMedium,
                color = SeniorOnColors.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun EmptyParentInformationCard(onClick: () -> Unit) {
    val shape = RoundedCornerShape(SeniorOnRadius.Large)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp)
            .clip(shape)
            .background(SeniorOnColors.Primary600)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "부모님 정보가 없습니다",
            style = SeniorOnTextStyles.HeadingS,
            color = SeniorOnColors.White,
        )
    }
}

@Composable
private fun ScreenEditSection(
    parentInfo: ParentInfo?,
    relationshipLabel: String?,
    configuration: SeniorScreenConfiguration,
    buttonItems: List<DisplayHomeButton>,
    todaySchedule: DisplayTodaySchedule?,
    canEditScreen: Boolean,
    showScreenEditActions: Boolean,
    onLargePreviewClick: () -> Unit,
    onFontEditClick: () -> Unit,
    onButtonEditClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        if (canEditScreen) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_phone1),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = SeniorOnColors.Gray800,
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "화면 편집",
                    style = SeniorOnTextStyles.HeadingS,
                    color = SeniorOnColors.Gray800,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        } else {
            SeniorScreenPreviewHeader(
                onLargePreviewClick = onLargePreviewClick,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        SeniorScreenPreviewCard(
            phoneLabel = if (canEditScreen) {
                "${relationshipLabel ?: "부모님"} 폰"
            } else {
                null
            },
            configuration = configuration,
            buttonItems = buttonItems,
            todaySchedule = todaySchedule,
            onLargePreviewClick = onLargePreviewClick,
        )

        if (showScreenEditActions) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DisplayEditButton(
                    text = "글씨 편집",
                    iconResId = R.drawable.ic_pencil,
                    outlined = true,
                    onClick = onFontEditClick,
                    modifier = Modifier.weight(1f),
                )

                DisplayEditButton(
                    text = "버튼 편집",
                    iconResId = R.drawable.ic_pencil_box,
                    outlined = false,
                    onClick = onButtonEditClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SeniorScreenPreviewHeader(
    onLargePreviewClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_phone2),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified,
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "시니어 화면",
                    style = SeniorOnTextStyles.HeadingS,
                    color = SeniorOnColors.Gray800,
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "현재 적용 중인 화면이에요",
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray500,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        LargePreviewButton(onClick = onLargePreviewClick)
    }
}

@Composable
private fun SeniorScreenPreviewCard(
    phoneLabel: String?,
    configuration: SeniorScreenConfiguration,
    buttonItems: List<DisplayHomeButton>,
    todaySchedule: DisplayTodaySchedule?,
    onLargePreviewClick: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(328f / 336f)
            .clip(RoundedCornerShape(SeniorOnRadius.Large))
            .background(SeniorOnColors.Background1),
    ) {
        val previewScale = maxWidth.value / 328f
        val phonePreviewWidth = (116f * previewScale).dp
        val phonePreviewHeight = (263f * previewScale).dp
        val phonePreviewBottomPadding = (20f * previewScale).dp

        phoneLabel?.let { label ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 22.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = SeniorOnTextStyles.BodyMBold,
                    color = SeniorOnColors.Gray700,
                )

                LargePreviewButton(onClick = onLargePreviewClick)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = phonePreviewBottomPadding),
        ) {
            SeniorPhonePreview(
                configuration = configuration,
                buttonItems = buttonItems,
                previewWidth = phonePreviewWidth,
                previewHeight = phonePreviewHeight,
                todaySchedule = todaySchedule,
            )
        }
    }
}

@Composable
private fun LargePreviewButton(
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(31.dp))
            .border(
                width = 1.dp,
                color = SeniorOnColors.Primary600,
                shape = RoundedCornerShape(31.dp),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_sm_preview),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = SeniorOnColors.Primary600,
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = "크게 보기",
            style = SeniorOnTextStyles.CaptionMedium,
            color = SeniorOnColors.Primary600,
        )
    }
}

@Composable
private fun DisplayEditButton(
    text: String,
    @DrawableRes iconResId: Int,
    outlined: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)
    val backgroundColor = if (outlined) SeniorOnColors.White else SeniorOnColors.Primary600
    val contentColor = if (outlined) SeniorOnColors.Primary600 else SeniorOnColors.White

    Row(
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .background(backgroundColor)
            .then(
                if (outlined) {
                    Modifier.border(1.dp, SeniorOnColors.Primary600, shape)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = contentColor,
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = text,
            style = SeniorOnTextStyles.ButtonM,
            color = contentColor,
        )
    }
}

private fun ParentInfo.birthDescription(): String =
    "${birthDate.year}년 ${birthDate.monthValue}월 ${birthDate.dayOfMonth}일생 · 만 ${age()}세"

@Preview(
    name = "Display Connected",
    showBackground = true,
    widthDp = 360,
    heightDp = 888,
)
@Composable
private fun DisplayTabConnectedPreview() {
    val overview = MockDisplayFixtures.overview(MockDisplayScenario.Connected)

    SENIOR_ONTheme {
        DisplayTabPreviewFrame(
            uiState = DisplayTabUiState(
                parentInfo = MockSeniorFixtures.mother,
                device = overview.device,
                screenConfiguration = overview.screenConfiguration,
            )
        )
    }
}

@Preview(
    name = "Display Not Connected",
    showBackground = true,
    widthDp = 360,
    heightDp = 888,
)
@Composable
private fun DisplayTabNotConnectedPreview() {
    val overview = MockDisplayFixtures.overview(MockDisplayScenario.NotConnected)

    SENIOR_ONTheme {
        DisplayTabPreviewFrame(
            uiState = DisplayTabUiState(
                parentInfo = MockSeniorFixtures.mother,
                device = overview.device,
                screenConfiguration = overview.screenConfiguration,
            )
        )
    }
}

@Preview(
    name = "Display Offline",
    showBackground = true,
    widthDp = 360,
    heightDp = 888,
)
@Composable
private fun DisplayTabOfflinePreview() {
    val overview = MockDisplayFixtures.overview(MockDisplayScenario.Offline)

    SENIOR_ONTheme {
        DisplayTabPreviewFrame(
            uiState = DisplayTabUiState(
                parentInfo = MockSeniorFixtures.mother,
                device = overview.device,
                screenConfiguration = overview.screenConfiguration,
            )
        )
    }
}

@Preview(
    name = "Display Login Expired",
    showBackground = true,
    widthDp = 360,
    heightDp = 888,
)
@Composable
private fun DisplayTabLoginExpiredPreview() {
    val overview = MockDisplayFixtures.overview(MockDisplayScenario.LoginExpired)

    SENIOR_ONTheme {
        DisplayTabPreviewFrame(
            uiState = DisplayTabUiState(
                parentInfo = MockSeniorFixtures.mother,
                relationshipLabel = MockSeniorFixtures.mother.relationshipLabel,
                device = overview.device,
                screenConfiguration = overview.screenConfiguration,
            )
        )
    }
}

@Preview(
    name = "Display Offline Dialog",
    showBackground = true,
    widthDp = 360,
    heightDp = 888,
)
@Composable
private fun DisplayTabOfflineDialogPreview() {
    val overview = MockDisplayFixtures.overview(MockDisplayScenario.Offline)

    SENIOR_ONTheme {
        DisplayTabPreviewFrame(
            uiState = DisplayTabUiState(
                parentInfo = MockSeniorFixtures.mother,
                device = overview.device,
                screenConfiguration = overview.screenConfiguration,
            ),
            showInternetRequiredDialog = true,
        )
    }
}

@Composable
private fun DisplayTabPreviewFrame(
    uiState: DisplayTabUiState,
    showInternetRequiredDialog: Boolean = false,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                DisplayTabScreen(uiState = uiState)
            }

            ChildBottomNavigation(
                selectedTab = ChildMainTab.Screen,
                onTabClick = {},
            )
        }

        if (showInternetRequiredDialog) {
            ParentPhoneInternetRequiredDialog(
                onConfirmClick = {},
                obscureBackgroundContent = true,
            )
        }
    }
}
