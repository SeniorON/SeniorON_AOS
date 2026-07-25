package com.example.senior_on.ui.display

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.senior_on.domain.model.DisplayDeviceConnectionStatus
import com.example.senior_on.domain.model.SeniorHomeButtonType
import com.example.senior_on.ui.notification.ParentPhoneInternetRequiredDialog
import com.example.senior_on.ui.senior_info.AddressSearchScreen
import com.example.senior_on.ui.senior_info.ParentInfoEditScreen
import com.example.senior_on.ui.senior_info.toParentInfo

private enum class DisplayDestination {
    Overview,
    DeviceConnection,
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
    modifier: Modifier = Modifier,
    canEditScreen: Boolean = true,
    onRefreshClick: () -> Unit = {},
    onInstallGuideClick: () -> Unit = {},
    onLargePreviewClick: () -> Unit = {},
    onFontEditClick: () -> Unit = {},
    onButtonEditClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val saveableStateHolder = rememberSaveableStateHolder()
    var destination by rememberSaveable { mutableStateOf(DisplayDestination.Overview) }
    var selectedAddress by rememberSaveable { mutableStateOf("") }
    var selectedAddressLatitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var selectedAddressLongitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var showInternetRequiredDialog by remember { mutableStateOf(false) }
    var showLargePreview by rememberSaveable { mutableStateOf(false) }
    var buttonEditDraftNames by rememberSaveable {
        mutableStateOf(arrayListOf<String>())
    }
    var buttonEditDraftCustomLabels by rememberSaveable {
        mutableStateOf(hashMapOf<String, String>())
    }

    fun navigateBack() {
        destination = when (destination) {
            DisplayDestination.AddressSearch -> DisplayDestination.ParentInfoEdit
            DisplayDestination.ButtonOrder -> DisplayDestination.ButtonAdd
            DisplayDestination.ButtonAdd -> DisplayDestination.ButtonEditSelected
            DisplayDestination.ButtonEditSelected -> DisplayDestination.ButtonEditGuide
            DisplayDestination.DeviceConnection,
            DisplayDestination.ParentInfoEdit,
            DisplayDestination.FontEdit,
            DisplayDestination.ButtonEditGuide,
            DisplayDestination.Overview -> DisplayDestination.Overview
        }
    }

    fun runWhenParentPhoneOnline(action: () -> Unit) {
        when (uiState.device?.connectionStatus) {
            DisplayDeviceConnectionStatus.Online -> action()
            DisplayDeviceConnectionStatus.Offline -> showInternetRequiredDialog = true
            null -> destination = DisplayDestination.DeviceConnection
        }
    }

    fun clearButtonEditFlowState() {
        buttonEditDraftNames = arrayListOf()
        buttonEditDraftCustomLabels = hashMapOf()
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
            DisplayDestination.Overview -> DisplayTabScreen(
                uiState = uiState,
                canEditScreen = canEditScreen,
                modifier = modifier,
                onDeviceClick = {
                    destination = DisplayDestination.DeviceConnection
                },
                onParentInfoClick = {
                    saveableStateHolder.removeState(DisplayDestination.ParentInfoEdit.name)
                    selectedAddress = uiState.parentInfo?.address.orEmpty()
                    selectedAddressLatitude = uiState.parentInfo?.addressLatitude
                    selectedAddressLongitude = uiState.parentInfo?.addressLongitude
                    destination = DisplayDestination.ParentInfoEdit
                },
                onLargePreviewClick = {
                    runWhenParentPhoneOnline {
                        showLargePreview = true
                        onLargePreviewClick()
                    }
                },
                onFontEditClick = {
                    runWhenParentPhoneOnline {
                        saveableStateHolder.removeState(DisplayDestination.FontEdit.name)
                        destination = DisplayDestination.FontEdit
                        onFontEditClick()
                    }
                },
                onButtonEditClick = {
                    runWhenParentPhoneOnline {
                        saveableStateHolder.removeState(
                            DisplayDestination.ButtonEditGuide.name
                        )
                        buttonEditDraftNames = arrayListOf()
                        buttonEditDraftCustomLabels = hashMapOf()
                        destination = DisplayDestination.ButtonEditGuide
                        onButtonEditClick()
                    }
                },
            )

            DisplayDestination.DeviceConnection -> DeviceConnectionScreen(
                device = uiState.device,
                relationshipLabel = uiState.parentInfo?.relationshipLabel ?: "부모님",
                modifier = modifier,
                onBackClick = ::navigateBack,
                onRefreshClick = onRefreshClick,
                onDisconnectClick = viewModel::disconnectDevice,
                onInstallGuideClick = onInstallGuideClick,
            )

            DisplayDestination.ParentInfoEdit -> ParentInfoEditScreen(
                parentInfo = uiState.parentInfo,
                modifier = modifier,
                selectedAddress = selectedAddress,
                selectedAddressLatitude = selectedAddressLatitude,
                selectedAddressLongitude = selectedAddressLongitude,
                onBackClick = ::navigateBack,
                onSearchAddressClick = {
                    destination = DisplayDestination.AddressSearch
                },
                onSaveClick = { inputState ->
                    viewModel.saveParentInfo(inputState.toParentInfo())
                    saveableStateHolder.removeState(DisplayDestination.ParentInfoEdit.name)
                    destination = DisplayDestination.Overview
                },
            )

            DisplayDestination.AddressSearch -> AddressSearchScreen(
                modifier = modifier,
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
                customButtonLabels =
                    uiState.screenConfiguration.customButtonLabels,
                modifier = modifier,
                onBackClick = ::navigateBack,
                onSaveClick = { fontSize ->
                    viewModel.updateFontSize(fontSize)
                    saveableStateHolder.removeState(DisplayDestination.FontEdit.name)
                    destination = DisplayDestination.Overview
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
                initialButtons = buttonEditDraftNames
                    .map(SeniorHomeButtonType::valueOf)
                    .ifEmpty { uiState.screenConfiguration.buttons },
                initialCustomButtonLabels = if (buttonEditDraftNames.isEmpty()) {
                    uiState.screenConfiguration.customButtonLabels
                } else {
                    buttonEditDraftCustomLabels.mapKeys { (buttonName, _) ->
                        SeniorHomeButtonType.valueOf(buttonName)
                    }
                },
                modifier = modifier,
                onBackClick = ::navigateBack,
                onSaveClick = { buttons, customButtonLabels ->
                    val savedButtons = buttons.withRequiredSeniorHomeButtons()
                    viewModel.updateButtons(
                        buttons = savedButtons,
                        customButtonLabels = customButtonLabels
                            .filterKeys(savedButtons::contains),
                    )
                    clearButtonEditFlowState()
                    destination = DisplayDestination.Overview
                },
                onAddButtonClick = { buttons, customButtonLabels ->
                    buttonEditDraftNames = ArrayList(
                        buttons.map(SeniorHomeButtonType::name)
                    )
                    buttonEditDraftCustomLabels = HashMap(
                        customButtonLabels.mapKeys { (button, _) ->
                            button.name
                        }
                    )
                    saveableStateHolder.removeState(
                        DisplayDestination.ButtonAdd.name
                    )
                    destination = DisplayDestination.ButtonAdd
                },
            )

            DisplayDestination.ButtonAdd -> DisplayButtonAddScreen(
                initialSelectedButtons = buttonEditDraftNames
                    .map(SeniorHomeButtonType::valueOf)
                    .ifEmpty { uiState.screenConfiguration.buttons }
                    .filter(ButtonAppCatalog::contains),
                initialMusicButton = buttonEditDraftNames
                    .map(SeniorHomeButtonType::valueOf)
                    .ifEmpty { uiState.screenConfiguration.buttons }
                    .firstOrNull {
                        it == SeniorHomeButtonType.Melon ||
                            it == SeniorHomeButtonType.Spotify
                    },
                modifier = modifier,
                onBackClick = ::navigateBack,
                onSaveClick = { musicButton, appButtons ->
                    val currentDraftButtons = buttonEditDraftNames
                        .map(SeniorHomeButtonType::valueOf)
                        .ifEmpty { uiState.screenConfiguration.buttons }
                    buttonEditDraftNames = ArrayList(
                        createInitialButtonOrder(
                            currentButtons = currentDraftButtons,
                            musicButton = musicButton,
                            appButtons = appButtons,
                        ).map(SeniorHomeButtonType::name)
                    )
                    saveableStateHolder.removeState(
                        DisplayDestination.ButtonOrder.name
                    )
                    destination = DisplayDestination.ButtonOrder
                },
            )

            DisplayDestination.ButtonOrder -> DisplayButtonOrderScreen(
                initialButtons = buttonEditDraftNames
                    .map(SeniorHomeButtonType::valueOf)
                    .ifEmpty { uiState.screenConfiguration.buttons },
                customButtonLabels = if (buttonEditDraftNames.isEmpty()) {
                    uiState.screenConfiguration.customButtonLabels
                } else {
                    buttonEditDraftCustomLabels.mapKeys { (buttonName, _) ->
                        SeniorHomeButtonType.valueOf(buttonName)
                    }
                },
                modifier = modifier,
                onBackClick = ::navigateBack,
                onSaveClick = { orderedButtons ->
                    val savedButtons =
                        orderedButtons.withRequiredSeniorHomeButtons()
                    val customButtonLabels =
                        buttonEditDraftCustomLabels.mapKeys {
                            (buttonName, _) ->
                            SeniorHomeButtonType.valueOf(buttonName)
                        }
                    viewModel.updateButtons(
                        buttons = savedButtons,
                        customButtonLabels = customButtonLabels
                            .filterKeys(savedButtons::contains),
                    )
                    clearButtonEditFlowState()
                    destination = DisplayDestination.Overview
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

    if (showLargePreview) {
        SeniorScreenLargePreviewDialog(
            configuration = uiState.screenConfiguration,
            onDismiss = { showLargePreview = false },
        )
    }
}

internal fun createInitialButtonOrder(
    currentButtons: List<SeniorHomeButtonType>,
    musicButton: SeniorHomeButtonType?,
    appButtons: List<SeniorHomeButtonType>,
): List<SeniorHomeButtonType> {
    val leadingButtons = buildList {
        musicButton?.let(::add)
        add(SeniorHomeButtonType.Schedule)
    }
    val selectedButtons = (
        leadingButtons +
            appButtons +
            SeniorHomeButtonType.ChatBuddy +
            SeniorHomeButtonType.Medication
        ).distinct()
    val preservedButtons = currentButtons.filter { currentButton ->
        currentButton in selectedButtons && currentButton !in leadingButtons
    }
    val newlySelectedButtons = selectedButtons.filter { selectedButton ->
        selectedButton !in leadingButtons && selectedButton !in preservedButtons
    }

    return leadingButtons + preservedButtons + newlySelectedButtons
}

internal fun List<SeniorHomeButtonType>.withRequiredSeniorHomeButtons():
    List<SeniorHomeButtonType> {
    val editableButtons = distinct()
        .filterNot { it == SeniorHomeButtonType.Emergency }
        .toMutableList()

    if (SeniorHomeButtonType.Schedule !in editableButtons) {
        val musicButtonIndex = editableButtons.indexOfFirst {
            it == SeniorHomeButtonType.Melon ||
                it == SeniorHomeButtonType.Spotify
        }
        editableButtons.add(
            index = if (musicButtonIndex >= 0) musicButtonIndex + 1 else 0,
            element = SeniorHomeButtonType.Schedule,
        )
    }

    return editableButtons + SeniorHomeButtonType.Emergency
}
