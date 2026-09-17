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
) : ViewModel() {
    private val initialParentInfo = parentInfoRepository.parentInfo.value
    private val initialRelationshipLabel = initialParentInfo?.relationshipLabel
    private var selectedSeniorId = initialParentInfo
        ?.seniorId
        ?.takeIf { it > 0L }
    private var selectedRelationshipLabel = initialRelationshipLabel
    private var selectedParentInfoSeed = initialParentInfo
    private var initialLoadJob: Job? = null
    private var overviewPullRefreshJob: Job? = null
    private var editPermissionRefreshJob: Job? = null
    private var homeRefreshJob: Job? = null
    private var deviceRefreshJob: Job? = null
    private var mutationJob: Job? = null

    private val _uiState = MutableStateFlow(
        DisplayTabUiState(
            parentInfo = initialParentInfo,
            relationshipLabel = initialRelationshipLabel,
            selectedSeniorId = selectedSeniorId,
            isLoading = selectedSeniorId != null,
            isEditPermissionLoading = selectedSeniorId != null,
        )
    )
    val uiState: StateFlow<DisplayTabUiState> = _uiState.asStateFlow()

    init {
        if (selectedSeniorId != null) {
            loadOverview()
        }
    }

    fun selectSenior(
        seniorId: Long,
        relationshipLabel: String? = null,
        parentInfoSeed: ParentInfo? = null,
    ) {
        if (seniorId <= 0L) {
            _uiState.update {
                it.copy(errorMessage = MISSING_SELECTED_SENIOR_ERROR_MESSAGE)
            }
            return
        }

        val normalizedRelationshipLabel = relationshipLabel
            ?.trim()
            ?.takeIf(String::isNotEmpty)
        val validatedParentInfoSeed = parentInfoSeed?.takeIf {
            it.seniorId == seniorId
        }
        if (selectedSeniorId == seniorId) {
            if (validatedParentInfoSeed != null) {
                selectedParentInfoSeed = validatedParentInfoSeed
            }
            if (
                _uiState.value.relationshipLabel.isNullOrBlank() &&
                normalizedRelationshipLabel != null
            ) {
                selectedRelationshipLabel = normalizedRelationshipLabel
                _uiState.update {
                    it.copy(relationshipLabel = normalizedRelationshipLabel)
                }
            }
            if (!_uiState.value.hasLoadedOverview) {
                loadOverview()
            }
            return
        }

        cancelSeniorScopedRequests()
        selectedSeniorId = seniorId
        selectedRelationshipLabel = normalizedRelationshipLabel
        selectedParentInfoSeed = validatedParentInfoSeed
        _uiState.value = DisplayTabUiState(
            selectedSeniorId = seniorId,
            relationshipLabel = normalizedRelationshipLabel,
            isLoading = true,
            isEditPermissionLoading = true,
        )
        loadOverview()
    }

    fun loadOverview() {
        val requestSeniorId = selectedSeniorId ?: return
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
                runCatching {
                    displayRepository.canCurrentUserEditScreen(requestSeniorId)
                }
            }
            val overviewRequest = async {
                runCatching {
                    displayRepository.getOverview(
                        seniorId = requestSeniorId,
                        currentParentInfo = cachedParentInfoFor(requestSeniorId),
                    )
                }
            }

            val canEditScreen = editPermissionRequest.await().getOrDefault(false)
            if (isCurrentSenior(requestSeniorId)) {
                _uiState.update {
                    it.copy(
                        canEditScreen = canEditScreen,
                        isEditPermissionLoading = false,
                    )
                }
            }
            overviewRequest.await()
                .onSuccess { overview ->
                    applyOverview(requestSeniorId, overview)
                }
                .onFailure { throwable ->
                    handleLoadFailure(requestSeniorId, throwable)
                }
        }
    }

    fun refreshOnScreenTabReentry() {
        refreshEditPermission()
        refreshHomeSilently()
    }

    fun refreshOverview() {
        val requestSeniorId = selectedSeniorId ?: return
        if (
            initialLoadJob?.isActive == true ||
            overviewPullRefreshJob?.isActive == true ||
            _uiState.value.isSaving
        ) return

        homeRefreshJob?.cancel()
        editPermissionRefreshJob?.cancel()
        overviewPullRefreshJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRefreshing = true,
                    isEditPermissionLoading = true,
                    errorMessage = null,
                )
            }

            try {
                val editPermissionRequest = async {
                    runCatching {
                        displayRepository.canCurrentUserEditScreen(requestSeniorId)
                    }
                }
                val overviewRequest = async {
                    runCatching {
                        displayRepository.getOverview(
                            seniorId = requestSeniorId,
                            currentParentInfo = cachedParentInfoFor(requestSeniorId),
                        )
                    }
                }

                val canEditScreen = editPermissionRequest.await().getOrDefault(false)
                if (isCurrentSenior(requestSeniorId)) {
                    _uiState.update {
                        it.copy(
                            canEditScreen = canEditScreen,
                            isEditPermissionLoading = false,
                        )
                    }
                }

                overviewRequest.await()
                    .onSuccess { overview ->
                        applyOverview(requestSeniorId, overview)
                    }
                    .onFailure { throwable ->
                        if (isCurrentSenior(requestSeniorId)) {
                            _uiState.update {
                                it.copy(errorMessage = throwable.toDisplayErrorMessage())
                            }
                        }
                    }
            } finally {
                if (isCurrentSenior(requestSeniorId)) {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            isEditPermissionLoading = false,
                        )
                    }
                }
            }
        }
    }

    private fun refreshEditPermission() {
        val requestSeniorId = selectedSeniorId ?: return
        if (
            initialLoadJob?.isActive == true ||
            overviewPullRefreshJob?.isActive == true ||
            editPermissionRefreshJob?.isActive == true
        ) return

        editPermissionRefreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isEditPermissionLoading = true) }
            val canEditScreen = runCatching {
                displayRepository.canCurrentUserEditScreen(requestSeniorId)
            }.getOrDefault(false)
            if (isCurrentSenior(requestSeniorId)) {
                _uiState.update {
                    it.copy(
                        canEditScreen = canEditScreen,
                        isEditPermissionLoading = false,
                    )
                }
            }
        }
    }

    private fun refreshHomeSilently(force: Boolean = false) {
        val requestSeniorId = selectedSeniorId ?: return
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
                    seniorId = requestSeniorId,
                    currentParentInfo = cachedParentInfoFor(requestSeniorId),
                )
            }.onSuccess { overview ->
                applyOverview(requestSeniorId, overview)
            }
        }
    }

    private fun applyOverview(
        requestSeniorId: Long,
        overview: DisplayOverview,
    ) {
        if (!isCurrentSenior(requestSeniorId)) return

        val receivedParentInfo = overview.parentInfo
        if (
            receivedParentInfo != null &&
            receivedParentInfo.seniorId > 0L &&
            receivedParentInfo.seniorId != requestSeniorId
        ) {
            handleLoadFailure(
                requestSeniorId,
                IllegalStateException("선택한 시니어와 다른 홈 정보가 응답되었습니다."),
            )
            return
        }
        val parentInfo = receivedParentInfo?.let {
            receivedParentInfo.copy(
                seniorId = requestSeniorId,
                relationshipLabel = selectedRelationshipLabel
                    ?: receivedParentInfo.relationshipLabel,
            )
        }
        if (parentInfo == null) {
            parentInfoRepository.clearParentInfo()
        } else {
            shareParentInfo(parentInfo)
        }
        _uiState.update {
            it.copy(
                selectedSeniorId = requestSeniorId,
                parentInfo = parentInfo,
                relationshipLabel = parentInfo?.relationshipLabel
                    ?: selectedRelationshipLabel,
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
        val requestSeniorId = selectedSeniorId ?: return
        if (deviceRefreshJob?.isActive == true) return

        deviceRefreshJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRefreshingDevice = true,
                    errorMessage = null,
                )
            }
            runCatching { displayRepository.getDevice(requestSeniorId) }
                .onSuccess { device ->
                    if (isCurrentSenior(requestSeniorId)) {
                        _uiState.update {
                            it.copy(
                                device = device,
                                isRefreshingDevice = false,
                            )
                        }
                    }
                }
                .onFailure { throwable ->
                    if (isCurrentSenior(requestSeniorId)) {
                        _uiState.update {
                            it.copy(
                                isRefreshingDevice = false,
                                errorMessage = throwable.toDisplayErrorMessage(),
                            )
                        }
                    }
                }
        }
    }

    fun saveParentInfo(
        parentInfo: ParentInfo,
        onSuccess: () -> Unit = {},
    ) {
        launchMutation(onSuccess) { selectedSeniorId ->
            require(parentInfo.seniorId == selectedSeniorId) {
                "선택한 시니어의 프로필만 수정할 수 있습니다."
            }
            val savedParentInfo = displayRepository.updateSeniorProfile(parentInfo)
            shareParentInfo(savedParentInfo)
            _uiState.update {
                it.copy(
                    parentInfo = savedParentInfo,
                    relationshipLabel = savedParentInfo.relationshipLabel,
                )
            }
        }
    }

    fun disconnectDevice(onSuccess: () -> Unit = {}) {
        launchMutation(onSuccess) { selectedSeniorId ->
            displayRepository.disconnectDevice(selectedSeniorId)
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
        launchMutation(onSuccess) { selectedSeniorId ->
            displayRepository.updateFontSize(selectedSeniorId, fontSize)
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

        launchMutation(onSuccess) { selectedSeniorId ->
            displayRepository.saveButtons(
                seniorId = selectedSeniorId,
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

        launchMutation(onSuccess) { selectedSeniorId ->
            displayRepository.saveButtons(selectedSeniorId, distinctButtons)
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
        request: suspend (seniorId: Long) -> Unit,
    ) {
        val requestSeniorId = selectedSeniorId
        if (requestSeniorId == null) {
            _uiState.update {
                it.copy(errorMessage = MISSING_SELECTED_SENIOR_ERROR_MESSAGE)
            }
            return
        }
        if (_uiState.value.isSaving) return

        mutationJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                )
            }
            runCatching { request(requestSeniorId) }
                .onSuccess {
                    if (isCurrentSenior(requestSeniorId)) {
                        _uiState.update { it.copy(isSaving = false) }
                        onSuccess()
                    }
                }
                .onFailure { throwable ->
                    if (isCurrentSenior(requestSeniorId)) {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = throwable.toDisplayErrorMessage(),
                            )
                        }
                    }
                }
        }
    }

    private fun shareParentInfo(parentInfo: ParentInfo) {
        selectedParentInfoSeed = parentInfo
        parentInfoRepository.saveParentInfo(parentInfo)
    }

    private fun handleLoadFailure(requestSeniorId: Long, throwable: Throwable) {
        if (isCurrentSenior(requestSeniorId)) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = throwable.toDisplayErrorMessage(),
                )
            }
        }
    }

    private fun cachedParentInfoFor(seniorId: Long): ParentInfo? =
        selectedParentInfoSeed?.takeIf { it.seniorId == seniorId }
            ?: parentInfoRepository.parentInfo.value
                ?.takeIf { it.seniorId == seniorId }

    private fun isCurrentSenior(seniorId: Long): Boolean =
        selectedSeniorId == seniorId

    private fun cancelSeniorScopedRequests() {
        initialLoadJob?.cancel()
        overviewPullRefreshJob?.cancel()
        editPermissionRefreshJob?.cancel()
        homeRefreshJob?.cancel()
        deviceRefreshJob?.cancel()
        mutationJob?.cancel()
        initialLoadJob = null
        overviewPullRefreshJob = null
        editPermissionRefreshJob = null
        homeRefreshJob = null
        deviceRefreshJob = null
        mutationJob = null
    }

    companion object {
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

private const val MISSING_SELECTED_SENIOR_ERROR_MESSAGE =
    "관리할 시니어를 먼저 선택해 주세요."
