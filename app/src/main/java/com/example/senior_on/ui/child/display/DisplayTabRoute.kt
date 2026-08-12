package com.example.senior_on.ui.child.display

import com.example.senior_on.ui.child.display.viewmodel.DisplayViewModel

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.senior_on.domain.model.display.DisplayDeviceConnectionStatus
import com.example.senior_on.domain.model.display.DisplayHomeButton
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.ui.child.notification.ParentPhoneInternetRequiredDialog
import com.example.senior_on.ui.common.seniorinfo.AddressSearchScreen
import com.example.senior_on.ui.common.seniorinfo.viewmodel.AddressSearchViewModel
import com.example.senior_on.ui.common.seniorinfo.ParentInfoEditScreen
import com.example.senior_on.ui.common.seniorinfo.toParentInfo
import com.example.senior_on.ui.common.share.KakaoShareLauncher
import com.example.senior_on.ui.common.share.ShareLaunchResult

private enum class DisplayDestination {
    Overview,
    DeviceConnection,
    SeniorAppInstallGuide,
    ConnectionGuide,
    ParentInfoEdit,
    AddressSearch,
    FontEdit,
    ButtonEditGuide,
    ButtonEditSelected,
    ButtonAdd,
    ButtonOrder,
}

@Composable
fun DisplayTabRoute(
    viewModel: DisplayViewModel,
    addressSearchViewModel: AddressSearchViewModel,
    modifier: Modifier = Modifier,
    onRefreshClick: () -> Unit = {},
    onInstallGuideClick: () -> Unit = {},
    onLargePreviewClick: () -> Unit = {},
    onFontEditClick: () -> Unit = {},
    onButtonEditClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val saveableStateHolder = rememberSaveableStateHolder()
    var destination by rememberSaveable { mutableStateOf(DisplayDestination.Overview) }
    var selectedAddress by rememberSaveable { mutableStateOf("") }
    var selectedAddressLatitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var selectedAddressLongitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var showInternetRequiredDialog by remember { mutableStateOf(false) }
    var showLargePreview by rememberSaveable { mutableStateOf(false) }
    var buttonEditDraftItems by remember {
        mutableStateOf<List<DisplayHomeButton>>(emptyList())
    }
    var buttonAddEntryItems by remember {
        mutableStateOf<List<DisplayHomeButton>>(emptyList())
    }
    var transientImportedButtons by remember {
        mutableStateOf<List<DisplayHomeButton>>(emptyList())
    }
    var pendingAppNameInput by remember {
        mutableStateOf<PickedInstalledApp?>(null)
    }
    var autoSelectKey by remember { mutableStateOf<String?>(null) }
    var autoSelectMusicButton by remember {
        mutableStateOf<SeniorHomeButtonType?>(null)
    }
    var autoSelectEvent by remember { mutableIntStateOf(0) }

    fun importButton(button: DisplayHomeButton) {
        val importedMusicButton = button.importedMusicButtonTypeOrNull()
        if (importedMusicButton != null) {
            autoSelectKey = null
            autoSelectMusicButton = importedMusicButton
            autoSelectEvent += 1
            return
        }

        if (button.actionType.equals("APP", ignoreCase = true)) {
            transientImportedButtons = (
                transientImportedButtons + button
                ).distinctBy(DisplayHomeButton::stableKey)
        }
        autoSelectMusicButton = null
        autoSelectKey = button.stableKey
        autoSelectEvent += 1
    }

    val selectableDefaultButtons = uiState.availableButtonOptions
        .filter(DisplayHomeButton::isSelectableDefaultOption)
    val appPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val pickedApp = readPickedInstalledApp(context, result.data)
            ?: return@rememberLauncherForActivityResult
        if (pickedApp.requiresManualNameInput) {
            pendingAppNameInput = pickedApp
            return@rememberLauncherForActivityResult
        }
        val importedButton = pickedApp.toDisplayHomeButton(
            context = context,
            defaultButtons = uiState.availableButtonOptions,
        )
        importedButton?.let(::importButton)
    }

    fun navigateBack() {
        if (destination == DisplayDestination.ButtonAdd) {
            buttonEditDraftItems = resolveButtonEditDraftAfterButtonAdd(
                buttonsAtEntry = buttonAddEntryItems,
                selectedButtons = buttonEditDraftItems,
                exit = ButtonAddExit.Cancel,
            )
            transientImportedButtons = emptyList()
            autoSelectKey = null
            autoSelectMusicButton = null
            saveableStateHolder.removeState(DisplayDestination.ButtonAdd.name)
        }
        destination = when (destination) {
            DisplayDestination.AddressSearch -> DisplayDestination.ParentInfoEdit
            DisplayDestination.ConnectionGuide -> DisplayDestination.SeniorAppInstallGuide
            DisplayDestination.ButtonOrder -> DisplayDestination.ButtonAdd
            DisplayDestination.ButtonAdd -> DisplayDestination.ButtonEditSelected
            DisplayDestination.ButtonEditSelected -> DisplayDestination.ButtonEditGuide
            DisplayDestination.DeviceConnection,
            DisplayDestination.SeniorAppInstallGuide,
            DisplayDestination.ParentInfoEdit,
            DisplayDestination.FontEdit,
            DisplayDestination.ButtonEditGuide,
            DisplayDestination.Overview -> DisplayDestination.Overview
        }
    }

    fun navigateToSeniorAppInstallGuide() {
        destination = DisplayDestination.SeniorAppInstallGuide
    }

    fun runWhenParentPhoneOnline(action: () -> Unit) {
        when (uiState.device?.connectionStatus) {
            DisplayDeviceConnectionStatus.Online -> action()
            DisplayDeviceConnectionStatus.Offline -> showInternetRequiredDialog = true
            null -> navigateToSeniorAppInstallGuide()
        }
    }

    fun clearButtonEditFlowState() {
        buttonEditDraftItems = emptyList()
        buttonAddEntryItems = emptyList()
        transientImportedButtons = emptyList()
        autoSelectKey = null
        autoSelectMusicButton = null
        listOf(
            DisplayDestination.ButtonEditGuide,
            DisplayDestination.ButtonEditSelected,
            DisplayDestination.ButtonAdd,
            DisplayDestination.ButtonOrder,
        ).forEach { savedDestination ->
            saveableStateHolder.removeState(savedDestination.name)
        }
    }

    BackHandler(
        enabled = destination != DisplayDestination.Overview,
        onBack = ::navigateBack,
    )

    saveableStateHolder.SaveableStateProvider(destination.name) {
        when (destination) {
            DisplayDestination.Overview -> when {
                uiState.isLoading && !uiState.hasLoadedOverview ->
                    DisplayTabLoadingScreen(modifier = modifier)

                uiState.errorMessage != null && !uiState.hasLoadedOverview ->
                    DisplayTabErrorScreen(
                        message = uiState.errorMessage.orEmpty(),
                        onRetryClick = viewModel::loadOverview,
                        modifier = modifier,
                    )

                else -> DisplayTabScreen(
                    uiState = uiState,
                    canEditScreen = uiState.canEditScreen,
                    showScreenEditActions = uiState.canEditScreen &&
                        !uiState.isEditPermissionLoading &&
                        !uiState.isLoading &&
                        !uiState.isSaving,
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = viewModel::refreshOverview,
                    modifier = modifier,
                    onDeviceClick = {
                        if (uiState.device == null) {
                            navigateToSeniorAppInstallGuide()
                        } else {
                            viewModel.refreshDevice()
                            destination = DisplayDestination.DeviceConnection
                        }
                    },
                    onParentInfoClick = {
                        if (uiState.device == null) {
                            navigateToSeniorAppInstallGuide()
                        } else {
                            saveableStateHolder.removeState(
                                DisplayDestination.ParentInfoEdit.name
                            )
                            selectedAddress = uiState.parentInfo?.address.orEmpty()
                            selectedAddressLatitude = uiState.parentInfo?.addressLatitude
                            selectedAddressLongitude = uiState.parentInfo?.addressLongitude
                            destination = DisplayDestination.ParentInfoEdit
                        }
                    },
                    onLargePreviewClick = {
                        runWhenParentPhoneOnline {
                            showLargePreview = true
                            onLargePreviewClick()
                        }
                    },
                    onFontEditClick = {
                        runWhenParentPhoneOnline {
                            saveableStateHolder.removeState(
                                DisplayDestination.FontEdit.name
                            )
                            destination = DisplayDestination.FontEdit
                            onFontEditClick()
                        }
                    },
                    onButtonEditClick = {
                        runWhenParentPhoneOnline {
                            saveableStateHolder.removeState(
                                DisplayDestination.ButtonEditGuide.name
                            )
                            buttonEditDraftItems = emptyList()
                            buttonAddEntryItems = emptyList()
                            transientImportedButtons = emptyList()
                            autoSelectKey = null
                            autoSelectMusicButton = null
                            destination = DisplayDestination.ButtonEditGuide
                            onButtonEditClick()
                        }
                    },
                )
            }

            DisplayDestination.DeviceConnection -> DeviceConnectionScreen(
                device = uiState.device,
                relationshipLabel = uiState.relationshipLabel ?: "부모님",
                isRefreshing = uiState.isRefreshingDevice,
                isDisconnecting = uiState.isSaving,
                modifier = modifier,
                onBackClick = ::navigateBack,
                onRefreshClick = {
                    viewModel.refreshDevice()
                    onRefreshClick()
                },
                onDisconnectClick = {
                    viewModel.disconnectDevice(
                        onSuccess = {
                            saveableStateHolder.removeState(
                                DisplayDestination.DeviceConnection.name
                            )
                            destination = DisplayDestination.Overview
                        },
                    )
                },
                onInstallGuideClick = onInstallGuideClick,
            )

            DisplayDestination.SeniorAppInstallGuide -> SeniorAppInstallGuideScreen(
                modifier = modifier,
                onBackClick = ::navigateBack,
                onKakaoSendClick = {
                    KakaoShareLauncher.launch(
                        context = context,
                        content = seniorAppInstallShareContent(),
                        onResult = { result ->
                            if (result is ShareLaunchResult.Failure) {
                                Toast.makeText(
                                    context,
                                    result.userMessage,
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        },
                    )
                },
                onAppInstallMethodClick = {
                    saveableStateHolder.removeState(
                        DisplayDestination.ConnectionGuide.name
                    )
                    destination = DisplayDestination.ConnectionGuide
                },
            )

            DisplayDestination.ConnectionGuide -> ConnectionGuideScreen(
                modifier = modifier,
                onBackClick = ::navigateBack,
            )

            DisplayDestination.ParentInfoEdit -> ParentInfoEditScreen(
                parentInfo = uiState.parentInfo?.let { parentInfo ->
                    parentInfo.copy(
                        relationshipLabel = uiState.relationshipLabel
                            ?: parentInfo.relationshipLabel,
                    )
                },
                modifier = modifier,
                selectedAddress = selectedAddress,
                selectedAddressLatitude = selectedAddressLatitude,
                selectedAddressLongitude = selectedAddressLongitude,
                isSubmitting = uiState.isSaving,
                onBackClick = ::navigateBack,
                onSearchAddressClick = {
                    destination = DisplayDestination.AddressSearch
                },
                onSaveClick = { inputState ->
                    val seniorId = requireNotNull(uiState.parentInfo).seniorId
                    viewModel.saveParentInfo(
                        parentInfo = inputState.toParentInfo(seniorId = seniorId),
                        onSuccess = {
                            saveableStateHolder.removeState(
                                DisplayDestination.ParentInfoEdit.name
                            )
                            destination = DisplayDestination.Overview
                        },
                    )
                },
            )

            DisplayDestination.AddressSearch -> AddressSearchScreen(
                modifier = modifier,
                viewModel = addressSearchViewModel,
                onBackClick = ::navigateBack,
                onAddressSelected = { result ->
                    selectedAddress = result.selectedAddress
                    selectedAddressLatitude = result.latitude
                    selectedAddressLongitude = result.longitude
                    destination = DisplayDestination.ParentInfoEdit
                },
            )

            DisplayDestination.FontEdit -> DisplayFontEditScreen(
                initialFontSize = uiState.screenConfiguration.fontSize,
                buttons = uiState.screenConfiguration.buttons,
                buttonItems = uiState.configuredButtonItems,
                customButtonLabels =
                    uiState.screenConfiguration.customButtonLabels,
                weather = uiState.weather,
                isWeatherLoading = uiState.isWeatherLoading,
                todaySchedule = uiState.todaySchedule,
                modifier = modifier,
                isSaving = uiState.isSaving,
                onBackClick = ::navigateBack,
                onSaveClick = { fontSize ->
                    viewModel.updateFontSize(
                        fontSize = fontSize,
                        onSuccess = {
                            saveableStateHolder.removeState(
                                DisplayDestination.FontEdit.name
                            )
                            destination = DisplayDestination.Overview
                        },
                    )
                },
            )

            DisplayDestination.ButtonEditGuide -> DisplayButtonEditGuideScreen(
                modifier = modifier,
                onBackClick = ::navigateBack,
                onContinueClick = {
                    saveableStateHolder.removeState(
                        DisplayDestination.ButtonEditSelected.name
                    )
                    destination = DisplayDestination.ButtonEditSelected
                },
            )

            DisplayDestination.ButtonEditSelected -> DisplayButtonEditSelectedScreen(
                initialButtons = buttonEditDraftItems
                    .ifEmpty { uiState.configuredButtonItems },
                modifier = modifier,
                isSaving = uiState.isSaving,
                onBackClick = ::navigateBack,
                onSaveClick = { buttons ->
                    val savedButtons = buttons.withRequiredSeniorHomeButtons()
                    viewModel.saveButtons(
                        buttons = savedButtons,
                        onSuccess = {
                            clearButtonEditFlowState()
                            destination = DisplayDestination.Overview
                        },
                    )
                },
                onAddButtonClick = { buttons ->
                    buttonEditDraftItems = buttons
                    buttonAddEntryItems = buttons
                    transientImportedButtons = emptyList()
                    autoSelectKey = null
                    autoSelectMusicButton = null
                    saveableStateHolder.removeState(
                        DisplayDestination.ButtonAdd.name
                    )
                    destination = DisplayDestination.ButtonAdd
                },
            )

            DisplayDestination.ButtonAdd -> DisplayButtonAddScreen(
                availableDefaultButtons = selectableDefaultButtons,
                initialSelectedButtons = buttonEditDraftItems
                    .ifEmpty { uiState.configuredButtonItems }
                    .filter(DisplayHomeButton::isSelectableAppButton),
                initialMusicButton = buttonEditDraftItems
                    .ifEmpty { uiState.configuredButtonItems }
                    .mapNotNull(DisplayHomeButton::type)
                    .firstOrNull(SeniorHomeButtonType::isMusicButton),
                transientImportedButtons = transientImportedButtons,
                autoSelectKey = autoSelectKey,
                autoSelectMusicButton = autoSelectMusicButton,
                autoSelectEvent = autoSelectEvent,
                modifier = modifier,
                onBackClick = ::navigateBack,
                onImportAppClick = {
                    val pickerIntent = createInstalledAppPickerIntent()
                    if (pickerIntent.resolveActivity(context.packageManager) == null) {
                        Toast.makeText(
                            context,
                            "앱 선택 화면을 열 수 없어요.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        appPickerLauncher.launch(pickerIntent)
                    }
                },
                onSaveClick = { musicButton, appButtons ->
                    val currentDraftButtons = buttonEditDraftItems
                        .ifEmpty { uiState.configuredButtonItems }
                    val selectedButtons = createInitialButtonOrder(
                        currentButtons = currentDraftButtons,
                        musicButton = musicButton,
                        appButtons = appButtons,
                    )
                    buttonEditDraftItems = resolveButtonEditDraftAfterButtonAdd(
                        buttonsAtEntry = buttonAddEntryItems,
                        selectedButtons = selectedButtons,
                        exit = ButtonAddExit.Continue,
                    )
                    transientImportedButtons = emptyList()
                    autoSelectKey = null
                    autoSelectMusicButton = null
                    saveableStateHolder.removeState(
                        DisplayDestination.ButtonAdd.name
                    )
                    saveableStateHolder.removeState(
                        DisplayDestination.ButtonOrder.name
                    )
                    destination = DisplayDestination.ButtonOrder
                },
            )

            DisplayDestination.ButtonOrder -> DisplayButtonOrderScreen(
                initialButtons = buttonEditDraftItems
                    .ifEmpty { uiState.configuredButtonItems },
                modifier = modifier,
                isSaving = uiState.isSaving,
                onBackClick = ::navigateBack,
                onSaveClick = { orderedButtons ->
                    val savedButtons = orderedButtons.withRequiredSeniorHomeButtons()
                    viewModel.saveButtons(
                        buttons = savedButtons,
                        onSuccess = {
                            clearButtonEditFlowState()
                            destination = DisplayDestination.Overview
                        },
                    )
                },
            )
        }
    }

    if (showInternetRequiredDialog) {
        ParentPhoneInternetRequiredDialog(
            onConfirmClick = { showInternetRequiredDialog = false },
            obscureBackgroundContent = true,
        )
    }

    pendingAppNameInput?.let { pickedApp ->
        MissingAppNameDialog(
            onDismiss = { pendingAppNameInput = null },
            onConfirm = { name ->
                pickedApp.toDisplayHomeButton(
                    context = context,
                    defaultButtons = uiState.availableButtonOptions,
                    buttonName = name,
                )?.let(::importButton)
                pendingAppNameInput = null
            },
        )
    }

    if (showLargePreview) {
        SeniorScreenLargePreviewDialog(
            configuration = uiState.screenConfiguration,
            buttonItems = uiState.configuredButtonItems,
            onDismiss = { showLargePreview = false },
            weather = uiState.weather,
            isWeatherLoading = uiState.isWeatherLoading,
            todaySchedule = uiState.todaySchedule,
        )
    }
}

internal fun createInitialButtonOrder(
    currentButtons: List<DisplayHomeButton>,
    musicButton: SeniorHomeButtonType?,
    appButtons: List<DisplayHomeButton>,
): List<DisplayHomeButton> {
    val musicItem = musicButton?.let { selectedMusic ->
        currentButtons.firstOrNull { it.type == selectedMusic }
            ?: selectedMusic.toMusicDisplayButton()
    }
    val schedule = currentButtons.firstOrNull { it.isDefaultAction("SCHEDULE") }
        ?: requiredDisplayButton(SeniorHomeButtonType.Schedule)
    val leadingButtons = buildList {
        musicItem?.let(::add)
        add(schedule)
    }
    val requiredButtons = listOf(
        SeniorHomeButtonType.ChatBuddy,
        SeniorHomeButtonType.Medication,
        SeniorHomeButtonType.Photo,
    ).map { type ->
        currentButtons.firstOrNull { it.type == type }
            ?: requiredDisplayButton(type)
    }
    val selectedButtons = (leadingButtons + appButtons + requiredButtons)
        .distinctBy(DisplayHomeButton::stableKey)
    val leadingKeys = leadingButtons.mapTo(hashSetOf(), DisplayHomeButton::stableKey)
    val selectedKeys = selectedButtons.mapTo(hashSetOf(), DisplayHomeButton::stableKey)
    val preservedButtons = currentButtons.filter { currentButton ->
        currentButton.stableKey in selectedKeys &&
            currentButton.stableKey !in leadingKeys
    }
    val newlySelectedButtons = selectedButtons.filter { selectedButton ->
        selectedButton.stableKey !in leadingKeys &&
            preservedButtons.none { it.stableKey == selectedButton.stableKey }
    }

    return (leadingButtons + preservedButtons + newlySelectedButtons)
        .withRequiredSeniorHomeButtons()
}

internal fun List<DisplayHomeButton>.withRequiredSeniorHomeButtons():
    List<DisplayHomeButton> {
    val distinctButtons = distinctBy(DisplayHomeButton::stableKey)
        .filterNot { it.isDefaultAction("EMERGENCY") }
    val musicButton = distinctButtons.firstOrNull(DisplayHomeButton::isMusicButton)
    val schedule = distinctButtons.firstOrNull { it.isDefaultAction("SCHEDULE") }
        ?: requiredDisplayButton(SeniorHomeButtonType.Schedule)
    val gridButtons = distinctButtons
        .filterNot { button ->
            button.isMusicButton() ||
                button.isDefaultAction("SCHEDULE")
        }
        .toMutableList()

    listOf(
        SeniorHomeButtonType.ChatBuddy,
        SeniorHomeButtonType.Medication,
        SeniorHomeButtonType.Photo,
    ).forEach { requiredType ->
        if (gridButtons.none { it.type == requiredType }) {
            gridButtons.add(requiredDisplayButton(requiredType))
        }
    }

    val emergency = firstOrNull { it.isDefaultAction("EMERGENCY") }
        ?: requiredDisplayButton(SeniorHomeButtonType.Emergency)

    return buildList {
        musicButton?.let(::add)
        add(schedule)
        addAll(gridButtons.withEmergencyAtFixedGridSlot(emergency))
    }
}

private fun DisplayHomeButton.isSelectableDefaultOption(): Boolean =
    actionType.equals("DEFAULT", ignoreCase = true) &&
        actionValue.uppercase() !in setOf(
            "SCHEDULE",
            "COMPANION",
            "MEDICATION",
            "PHOTO",
            "EMERGENCY",
        )

private fun DisplayHomeButton.isSelectableAppButton(): Boolean =
    !isMusicButton() &&
        actionValue.uppercase() !in setOf(
            "SCHEDULE",
            "COMPANION",
            "MEDICATION",
            "PHOTO",
            "EMERGENCY",
        )

private fun requiredDisplayButton(type: SeniorHomeButtonType): DisplayHomeButton {
    val (name, actionValue) = when (type) {
        SeniorHomeButtonType.Schedule -> "일정" to "SCHEDULE"
        SeniorHomeButtonType.ChatBuddy -> "말벗" to "COMPANION"
        SeniorHomeButtonType.Medication -> "복약" to "MEDICATION"
        SeniorHomeButtonType.Photo -> "사진" to "PHOTO"
        SeniorHomeButtonType.Emergency -> "긴급알림" to "EMERGENCY"
        else -> error("필수 버튼이 아닙니다: $type")
    }
    return DisplayHomeButton(
        name = name,
        actionType = "DEFAULT",
        actionValue = actionValue,
        type = type,
    )
}

private fun SeniorHomeButtonType.toMusicDisplayButton(): DisplayHomeButton {
    val (label, actionValue, packageName) = when (this) {
        SeniorHomeButtonType.Melon -> Triple("멜론", "MELON", "com.iloen.melon")
        SeniorHomeButtonType.Genie -> Triple("지니뮤직", "GENIE", "com.ktmusic.geniemusic")
        SeniorHomeButtonType.YouTubeMusic -> Triple(
            "유튜브뮤직",
            "YOUTUBE_MUSIC",
            "com.google.android.apps.youtube.music",
        )
        SeniorHomeButtonType.Spotify -> Triple("스포티파이", "SPOTIFY", "com.spotify.music")
        SeniorHomeButtonType.Flo -> Triple("플로", "FLO", "skplanet.musicmate")
        SeniorHomeButtonType.Vibe -> Triple("바이브", "VIBE", "com.naver.vibe")
        SeniorHomeButtonType.Bugs -> Triple("벅스", "BUGS", "com.neowiz.android.bugs")
        SeniorHomeButtonType.SamsungMusic -> Triple(
            "삼성뮤직",
            "SAMSUNG_MUSIC",
            "com.sec.android.app.music",
        )
        SeniorHomeButtonType.KakaoMusic -> Triple("카카오뮤직", "KAKAO_MUSIC", "com.kakao.music")
        else -> error("음악 버튼이 아닙니다: $this")
    }
    return DisplayHomeButton(
        name = label,
        actionType = "APP",
        actionValue = actionValue,
        packageName = packageName,
        type = this,
    )
}
