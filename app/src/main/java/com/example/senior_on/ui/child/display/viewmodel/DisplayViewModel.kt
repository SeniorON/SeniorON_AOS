package com.example.senior_on.ui.child.display.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.model.display.DisplayOverview
import com.example.senior_on.domain.model.display.InitialSeniorHomeGridButtons
import com.example.senior_on.domain.model.display.SeniorFontSize
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.domain.model.location.DefaultWeatherCoordinates
import com.example.senior_on.domain.model.parent.ParentInfo
import com.example.senior_on.domain.repository.display.DisplayRepository
import com.example.senior_on.domain.repository.parent.CaregiverRelationshipRepository
import com.example.senior_on.domain.repository.parent.ParentInfoRepository
import com.example.senior_on.ui.child.display.DisplayTabUiState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DisplayViewModel(
    private val parentInfoRepository: ParentInfoRepository,
    private val displayRepository: DisplayRepository,
    private val caregiverRelationshipRepository: CaregiverRelationshipRepository,
) : ViewModel() {
    private val initialParentInfo = parentInfoRepository.parentInfo.value
    private val initialRelationshipLabel =
        caregiverRelationshipRepository.relationship.value?.displayLabel
            ?: initialParentInfo?.relationshipLabel
    private var isInitialButtonSetupInProgress = false

    private val _uiState = MutableStateFlow(
        DisplayTabUiState(
            parentInfo = initialParentInfo,
            relationshipLabel = initialRelationshipLabel,
            isLoading = true,
        )
    )
    val uiState: StateFlow<DisplayTabUiState> = _uiState.asStateFlow()

    init {
        loadOverview()
    }

    fun loadOverview() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    canEditScreen = false,
                    isEditPermissionLoading = true,
                    errorMessage = null,
                )
            }

            val editPermissionRequest = async {
                runCatching { displayRepository.canCurrentUserEditScreen() }
            }
            val overviewRequest = async {
                runCatching {
                    displayRepository.getOverview(
                        parentInfoRepository.parentInfo.value
                    )
                }
            }
            val deviceRequest = async {
                runCatching { displayRepository.getDevice() }
            }
            refreshWeather()

            val canEditScreen = editPermissionRequest.await().getOrDefault(false)
            _uiState.update {
                it.copy(
                    canEditScreen = canEditScreen,
                    isEditPermissionLoading = false,
                )
            }
            overviewRequest.await()
                .mapCatching { overview ->
                    applyInitialButtonsIfNeeded(
                        overview = overview,
                        canEditScreen = canEditScreen,
                    )
                }
                .onSuccess { overview ->
                    val parentInfo = overview.parentInfo
                        ?: parentInfoRepository.parentInfo.value
                    parentInfo?.let(::shareParentInfo)
                    _uiState.update {
                        it.copy(
                            parentInfo = parentInfo,
                            relationshipLabel = parentInfo?.relationshipLabel,
                            device = overview.device,
                            todaySchedule = overview.todaySchedule,
                            screenConfiguration = overview.screenConfiguration,
                            availableButtonTypes = overview.availableButtonTypes,
                            isLoading = false,
                        )
                    }
                }
                .onFailure(::handleLoadFailure)

            deviceRequest.await()
                .onSuccess { device ->
                    if (device != null) {
                        _uiState.update { it.copy(device = device) }
                    }
                }
        }
    }

    fun refreshEditPermission() {
        if (_uiState.value.isEditPermissionLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isEditPermissionLoading = true) }
            val canEditScreen = runCatching {
                displayRepository.canCurrentUserEditScreen()
            }.getOrDefault(false)
            _uiState.update {
                it.copy(
                    canEditScreen = canEditScreen,
                    isEditPermissionLoading = false,
                )
            }
        }
    }

    private suspend fun applyInitialButtonsIfNeeded(
        overview: DisplayOverview,
        canEditScreen: Boolean,
    ): DisplayOverview {
        if (
            overview.hasSavedButtonConfiguration ||
            !canEditScreen ||
            isInitialButtonSetupInProgress
        ) {
            return overview
        }

        isInitialButtonSetupInProgress = true
        try {
            displayRepository.saveButtons(
                buttons = InitialSeniorHomeGridButtons,
                customButtonLabels = emptyMap(),
            )
        } catch (throwable: Throwable) {
            isInitialButtonSetupInProgress = false
            throw throwable
        }

        return displayRepository.getOverview(
            parentInfoRepository.parentInfo.value
        )
    }

    fun refreshDevice() {
        if (_uiState.value.isRefreshingDevice) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRefreshingDevice = true,
                    errorMessage = null,
                )
            }
            runCatching { displayRepository.getDevice() }
                .onSuccess { device ->
                    _uiState.update {
                        it.copy(
                            device = device,
                            isRefreshingDevice = false,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isRefreshingDevice = false,
                            errorMessage = throwable.toDisplayErrorMessage(),
                        )
                    }
                }
        }
    }

    fun saveParentInfo(
        parentInfo: ParentInfo,
        onSuccess: () -> Unit = {},
    ) {
        launchMutation(onSuccess) {
            val savedParentInfo = displayRepository.updateSeniorProfile(parentInfo)
            shareParentInfo(savedParentInfo)
            _uiState.update {
                it.copy(
                    parentInfo = savedParentInfo,
                    relationshipLabel = savedParentInfo.relationshipLabel,
                )
            }
            refreshWeather()
        }
    }

    fun disconnectDevice(onSuccess: () -> Unit = {}) {
        launchMutation(onSuccess) {
            displayRepository.disconnectDevice()
            val device = displayRepository.getDevice()
            _uiState.update { it.copy(device = device) }
        }
    }

    fun updateFontSize(
        fontSize: SeniorFontSize,
        onSuccess: () -> Unit = {},
    ) {
        launchMutation(onSuccess) {
            displayRepository.updateFontSize(fontSize)
            _uiState.update {
                it.copy(
                    screenConfiguration = it.screenConfiguration.copy(
                        fontSize = fontSize,
                    )
                )
            }
        }
    }

    fun saveButtons(
        buttons: List<SeniorHomeButtonType>,
        customButtonLabels: Map<SeniorHomeButtonType, String>,
        onSuccess: () -> Unit = {},
    ) {
        val distinctButtons = buttons.distinct()
        val normalizedLabels = customButtonLabels
            .filterKeys(distinctButtons::contains)
            .mapValues { (_, label) -> label.trim() }
            .filterValues(String::isNotEmpty)

        launchMutation(onSuccess) {
            displayRepository.saveButtons(
                buttons = distinctButtons,
                customButtonLabels = normalizedLabels,
            )
            _uiState.update {
                it.copy(
                    screenConfiguration = it.screenConfiguration.copy(
                        buttons = distinctButtons,
                        customButtonLabels = normalizedLabels,
                    )
                )
            }
        }
    }

    private fun launchMutation(
        onSuccess: () -> Unit,
        request: suspend () -> Unit,
    ) {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                )
            }
            runCatching { request() }
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false) }
                    onSuccess()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = throwable.toDisplayErrorMessage(),
                        )
                    }
                }
        }
    }

    private fun shareParentInfo(parentInfo: ParentInfo) {
        parentInfoRepository.saveParentInfo(parentInfo)
    }

    private fun refreshWeather() {
        viewModelScope.launch {
            _uiState.update { it.copy(isWeatherLoading = true) }
            runCatching {
                displayRepository.getWeather(
                    latitude = DefaultWeatherCoordinates.LATITUDE,
                    longitude = DefaultWeatherCoordinates.LONGITUDE,
                )
            }.onSuccess { weather ->
                _uiState.update {
                    it.copy(
                        weather = weather,
                        isWeatherLoading = false,
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        weather = null,
                        isWeatherLoading = false,
                    )
                }
            }
        }
    }

    private fun handleLoadFailure(throwable: Throwable) {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = throwable.toDisplayErrorMessage(),
            )
        }
    }

    companion object {
        fun factory(
            parentInfoRepository: ParentInfoRepository,
            displayRepository: DisplayRepository,
            caregiverRelationshipRepository: CaregiverRelationshipRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DisplayViewModel(
                    parentInfoRepository = parentInfoRepository,
                    displayRepository = displayRepository,
                    caregiverRelationshipRepository = caregiverRelationshipRepository,
                )
            }
        }
    }
}

private fun Throwable.toDisplayErrorMessage(): String =
    message?.takeIf(String::isNotBlank) ?: DEFAULT_ERROR_MESSAGE

private const val DEFAULT_ERROR_MESSAGE =
    "화면 정보를 처리하는 중 문제가 발생했습니다. 다시 시도해 주세요."
