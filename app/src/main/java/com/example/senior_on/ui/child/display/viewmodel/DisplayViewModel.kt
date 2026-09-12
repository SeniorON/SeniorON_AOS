package com.example.senior_on.ui.child.display.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.model.display.DisplayOverview
import com.example.senior_on.domain.model.display.DisplayHomeButton
import com.example.senior_on.domain.model.display.SeniorFontSize
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.domain.model.location.DefaultWeatherCoordinates
import com.example.senior_on.domain.model.parent.ParentInfo
import com.example.senior_on.domain.repository.display.DisplayRepository
import com.example.senior_on.domain.repository.parent.ParentInfoRepository
import com.example.senior_on.ui.child.display.DisplayTabUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DisplayViewModel(
    private val parentInfoRepository: ParentInfoRepository,
    private val displayRepository: DisplayRepository,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    private val initialParentInfo = parentInfoRepository.parentInfo.value
    private val initialRelationshipLabel = initialParentInfo?.relationshipLabel
    private var initialLoadJob: Job? = null
    private var overviewPullRefreshJob: Job? = null
    private var editPermissionRefreshJob: Job? = null
    private var homeRefreshJob: Job? = null
    private var deviceRefreshJob: Job? = null
    private var weatherRefreshJob: Job? = null
    private var lastWeatherUpdatedAtMillis: Long? = null

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
        if (
            initialLoadJob?.isActive == true ||
            overviewPullRefreshJob?.isActive == true ||
            homeRefreshJob?.isActive == true
        ) return

        initialLoadJob = viewModelScope.launch {
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
            refreshWeatherIfStale()

            val canEditScreen = editPermissionRequest.await().getOrDefault(false)
            _uiState.update {
                it.copy(
                    canEditScreen = canEditScreen,
                    isEditPermissionLoading = false,
                )
            }
            overviewRequest.await()
                .onSuccess(::applyOverview)
                .onFailure(::handleLoadFailure)
        }
    }

    fun refreshOnScreenTabReentry() {
        refreshEditPermission()
        refreshHomeSilently()
        refreshWeatherIfStale()
    }

    fun refreshOverview() {
        if (
            initialLoadJob?.isActive == true ||
            overviewPullRefreshJob?.isActive == true ||
            _uiState.value.isSaving
        ) return

        homeRefreshJob?.cancel()
        editPermissionRefreshJob?.cancel()
        weatherRefreshJob?.cancel()
        overviewPullRefreshJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRefreshing = true,
                    isEditPermissionLoading = true,
                    isWeatherLoading = true,
                    errorMessage = null,
                )
            }

            try {
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
                val weatherRequest = async {
                    runCatching {
                        displayRepository.getWeather(
                            latitude = DefaultWeatherCoordinates.LATITUDE,
                            longitude = DefaultWeatherCoordinates.LONGITUDE,
                        )
                    }
                }

                val canEditScreen = editPermissionRequest.await().getOrDefault(false)
                _uiState.update {
                    it.copy(
                        canEditScreen = canEditScreen,
                        isEditPermissionLoading = false,
                    )
                }

                overviewRequest.await()
                    .onSuccess(::applyOverview)
                    .onFailure { throwable ->
                        _uiState.update {
                            it.copy(errorMessage = throwable.toDisplayErrorMessage())
                        }
                    }

                weatherRequest.await()
                    .onSuccess { weather ->
                        lastWeatherUpdatedAtMillis = currentTimeMillis()
                        _uiState.update { it.copy(weather = weather) }
                    }
            } finally {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        isEditPermissionLoading = false,
                        isWeatherLoading = false,
                    )
                }
            }
        }
    }

    private fun refreshEditPermission() {
        if (
            initialLoadJob?.isActive == true ||
            overviewPullRefreshJob?.isActive == true ||
            editPermissionRefreshJob?.isActive == true
        ) return

        editPermissionRefreshJob = viewModelScope.launch {
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

    private fun refreshHomeSilently(force: Boolean = false) {
        if (
            initialLoadJob?.isActive == true ||
            overviewPullRefreshJob?.isActive == true
        ) return
        if (homeRefreshJob?.isActive == true) {
            if (!force) return
            homeRefreshJob?.cancel()
        }

        homeRefreshJob = viewModelScope.launch {
            runCatching {
                displayRepository.getOverview(
                    parentInfoRepository.parentInfo.value
                )
            }.onSuccess(::applyOverview)
        }
    }

    private fun applyOverview(overview: DisplayOverview) {
        val parentInfo = overview.parentInfo
        if (parentInfo == null) {
            parentInfoRepository.clearParentInfo()
        } else {
            shareParentInfo(parentInfo)
        }
        _uiState.update {
            it.copy(
                parentInfo = parentInfo,
                relationshipLabel = parentInfo?.relationshipLabel,
                device = overview.device,
                todaySchedule = overview.todaySchedule,
                screenConfiguration = overview.screenConfiguration,
                availableButtonTypes = overview.availableButtonTypes,
                configuredButtonItems = overview.configuredButtonItems,
                availableButtonOptions = overview.availableButtonOptions,
                isLoading = false,
                hasLoadedOverview = true,
            )
        }
    }

    fun refreshDevice() {
        if (deviceRefreshJob?.isActive == true) return

        deviceRefreshJob = viewModelScope.launch {
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
            _uiState.update {
                it.copy(
                    device = null,
                    isRefreshingDevice = false,
                )
            }
            refreshHomeSilently(force = true)
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

    fun saveButtons(
        buttons: List<DisplayHomeButton>,
        onSuccess: () -> Unit = {},
    ) {
        val distinctButtons = buttons.distinctBy(DisplayHomeButton::stableKey)
        val knownButtons = distinctButtons.mapNotNull(DisplayHomeButton::type)
        val knownLabels = distinctButtons.mapNotNull { button ->
            button.type?.let { type -> type to button.name.trim() }
        }.toMap()

        launchMutation(onSuccess) {
            displayRepository.saveButtons(distinctButtons)
            _uiState.update {
                it.copy(
                    configuredButtonItems = distinctButtons,
                    screenConfiguration = it.screenConfiguration.copy(
                        buttons = knownButtons,
                        customButtonLabels = knownLabels,
                    ),
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

    private fun refreshWeatherIfStale() {
        val now = currentTimeMillis()
        val lastUpdatedAt = lastWeatherUpdatedAtMillis
        if (
            lastUpdatedAt != null &&
            now >= lastUpdatedAt &&
            now - lastUpdatedAt < WeatherRefreshIntervalMillis
        ) return

        refreshWeather()
    }

    private fun refreshWeather() {
        if (
            overviewPullRefreshJob?.isActive == true ||
            weatherRefreshJob?.isActive == true
        ) return

        weatherRefreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isWeatherLoading = true) }
            runCatching {
                displayRepository.getWeather(
                    latitude = DefaultWeatherCoordinates.LATITUDE,
                    longitude = DefaultWeatherCoordinates.LONGITUDE,
                )
            }.onSuccess { weather ->
                lastWeatherUpdatedAtMillis = currentTimeMillis()
                _uiState.update {
                    it.copy(
                        weather = weather,
                        isWeatherLoading = false,
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
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
        private const val WeatherRefreshIntervalMillis = 10 * 60 * 1_000L

        fun factory(
            parentInfoRepository: ParentInfoRepository,
            displayRepository: DisplayRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DisplayViewModel(
                    parentInfoRepository = parentInfoRepository,
                    displayRepository = displayRepository,
                )
            }
        }
    }
}

private fun Throwable.toDisplayErrorMessage(): String =
    message?.takeIf(String::isNotBlank) ?: DEFAULT_ERROR_MESSAGE

private const val DEFAULT_ERROR_MESSAGE =
    "화면 정보를 처리하는 중 문제가 발생했습니다. 다시 시도해 주세요."
