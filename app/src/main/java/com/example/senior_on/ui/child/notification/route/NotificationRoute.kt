package com.example.senior_on.ui.child.notification.route

import androidx.compose.runtime.Composable
import com.example.senior_on.ui.child.notification.canAccess
import com.example.senior_on.ui.child.notification.visibleMessage
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.collect
import com.example.senior_on.data.remote.websocket.matches
import com.example.senior_on.data.remote.api.SeniorOnNetwork
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import com.example.senior_on.ui.child.notification.NotificationTopBar
import com.example.senior_on.ui.child.notification.NotificationHistoryTopBar
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import com.example.senior_on.domain.repository.server.HomeServerRepository
import com.example.senior_on.domain.repository.server.EventRepository
import com.example.senior_on.domain.repository.server.NotificationRepository
import com.example.senior_on.domain.repository.location.LocationRepository
import com.example.senior_on.notification.NotificationNavigationEvent
import com.example.senior_on.ui.child.notification.NotificationCategory
import com.example.senior_on.ui.child.notification.NotificationMessageUiState
import com.example.senior_on.ui.child.notification.NotificationSeverity
import com.example.senior_on.ui.child.notification.toNotificationCategory

private enum class NotificationDestination {
    Home,
    History,
    Detail,
    InactivitySetting,
}

@Composable
fun NotificationRoute(
    repository: NotificationRepository,
    seniorId: Long? = null,
    sessionKey: String = "",
    familyRepository: FamilyServerRepository? = null,
    homeRepository: HomeServerRepository? = null,
    eventRepository: EventRepository? = null,
    locationRepository: LocationRepository? = null,
    navigationEvent: NotificationNavigationEvent? = null,
    onNavigationEventConsumed: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel = notificationViewModel(
        repository = repository,
        seniorId = seniorId,
        sessionKey = sessionKey,
        familyRepository = familyRepository,
        homeRepository = homeRepository,
        eventRepository = eventRepository,
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var destination by rememberSaveable(seniorId, sessionKey) {
        mutableStateOf(NotificationDestination.Home)
    }
    var selectedCategory by rememberSaveable(seniorId, sessionKey) {
        mutableStateOf<NotificationCategory?>(null)
    }
    var selectedMessage by remember(seniorId, sessionKey) {
        mutableStateOf<NotificationMessageUiState?>(null)
    }
    var detailReturnDestination by rememberSaveable(seniorId, sessionKey) {
        mutableStateOf(NotificationDestination.Home)
    }
    var isHistoryDetail by rememberSaveable(seniorId, sessionKey) { mutableStateOf(false) }

    val currentDestination by rememberUpdatedState(destination)
    val currentCategory by rememberUpdatedState(selectedCategory)
    val currentMessage by rememberUpdatedState(selectedMessage)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, seniorId, sessionKey, lifecycleOwner) {
        if (seniorId == null || seniorId <= 0) return@LaunchedEffect
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            SeniorOnNetwork.notificationStompSource.observe()
                .filter { signal ->
                    signal.matches(seniorId, currentMessage?.eventId.takeIf { currentDestination == NotificationDestination.Detail })
                }
                // Filter before conflating so another senior/event cannot displace a relevant update.
                .map { Unit }
                .conflate()
                .collect {
                    delay(150)
                    when (currentDestination) {
                        NotificationDestination.Home -> viewModel.refreshFromSocket(null, null)
                        NotificationDestination.History -> currentCategory?.let { viewModel.refreshFromSocket(it, null) }
                        NotificationDestination.Detail -> currentCategory?.let { category ->
                            currentMessage?.let { viewModel.refreshFromSocket(category, it) }
                        }
                        NotificationDestination.InactivitySetting -> Unit
                    }
                }
        }
    }

    fun openHistory(category: NotificationCategory) {
        if (!uiState.hasLoadedContent || !uiState.home.canAccess(category)) return
        selectedCategory = category
        viewModel.loadHistory(category)
        destination = NotificationDestination.History
    }

    fun openDetail(
        category: NotificationCategory,
        message: NotificationMessageUiState,
        loadDetail: Boolean = true,
        fromHistory: Boolean = false,
    ) {
        if (!uiState.hasLoadedContent || !uiState.home.canAccess(category)) return
        detailReturnDestination = destination
        isHistoryDetail = fromHistory
        selectedCategory = category
        selectedMessage = message
        if (loadDetail) {
            viewModel.openNotification(category, message)
        }
        destination = NotificationDestination.Detail
    }

    LaunchedEffect(navigationEvent, uiState.hasLoadedContent) {
        if (!uiState.hasLoadedContent) return@LaunchedEffect
        val event = navigationEvent ?: return@LaunchedEffect
        val eventId = event.eventId
        val category = event.type.toNotificationCategory()

        if (category != null && eventId != null) {
            openDetail(
                category = category,
                message = NotificationMessageUiState(
                    time = "",
                    title = event.title.orEmpty(),
                    severity = if (category == NotificationCategory.Outing) {
                        NotificationSeverity.Normal
                    } else {
                        NotificationSeverity.Danger
                    },
                    tintBackground = category != NotificationCategory.Outing,
                    notificationId = event.notificationId,
                    eventId = eventId,
                ),
            )
        } else if (eventId != null) {
            val resolved = viewModel.resolveNotificationNavigation(
                eventId = eventId,
                notificationId = event.notificationId,
                title = event.title,
            )
            if (resolved != null) {
                openDetail(
                    category = resolved.first,
                    message = resolved.second,
                    loadDetail = false,
                )
            } else {
                destination = NotificationDestination.Home
            }
        } else {
            destination = NotificationDestination.Home
        }
        onNavigationEventConsumed()
    }

    LaunchedEffect(viewModel) {
        viewModel.loadLatestHome()
    }

    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        viewModel.loadLatestHome()
    }
    LaunchedEffect(uiState.home) {
        val category = if (destination == NotificationDestination.InactivitySetting)
            NotificationCategory.Inactivity else selectedCategory
        if (category != null && !uiState.home.canAccess(category)) {
            selectedMessage = null
            destination = NotificationDestination.Home
        }
    }

    val queryError = when (destination) {
        NotificationDestination.Home -> uiState.homeQueryError
        NotificationDestination.History -> uiState.historyQueryErrors[selectedCategory]
        NotificationDestination.Detail -> uiState.detailQueryErrors[selectedMessage?.eventId]
        NotificationDestination.InactivitySetting -> uiState.settingQueryError
    }
    val queryLoading = when (destination) {
        NotificationDestination.Home -> uiState.isLoading || uiState.isRefreshing
        NotificationDestination.History -> uiState.isLoading || uiState.isHistoryRefreshing
        NotificationDestination.Detail -> uiState.isDetailLoading
        NotificationDestination.InactivitySetting -> uiState.isInactivitySettingLoading
    }
    val hasQueryContent = when (destination) {
        NotificationDestination.Home -> uiState.hasLoadedContent
        NotificationDestination.History -> uiState.histories.containsKey(selectedCategory)
        NotificationDestination.Detail -> uiState.detailMessages.containsKey(selectedMessage?.eventId)
        NotificationDestination.InactivitySetting -> uiState.hasLoadedSetting
    }
    androidx.activity.compose.BackHandler(enabled = destination != NotificationDestination.Home) {
        destination = if (destination == NotificationDestination.Detail) detailReturnDestination else NotificationDestination.Home
    }
    com.example.senior_on.ui.common.QueryRetryContent(
        error = if (destination == NotificationDestination.Home) null else queryError,
        loading = queryLoading,
        hasContent = destination == NotificationDestination.Home || hasQueryContent,
        onRetry = {
            when (destination) {
                NotificationDestination.Home -> viewModel.loadHome()
                NotificationDestination.History -> selectedCategory?.let(viewModel::loadHistory)
                NotificationDestination.Detail -> selectedCategory?.let { category ->
                    selectedMessage?.let { viewModel.openNotification(category, it, silent = true) }
                }
                NotificationDestination.InactivitySetting -> viewModel.loadInactivitySetting()
            }
        },
        modifier = modifier.fillMaxSize(),
        placeholderHeader = {
            if (destination == NotificationDestination.Home) {
                NotificationTopBar(NotificationSeverity.Empty, 0, Modifier.statusBarsPadding())
            } else {
                NotificationHistoryTopBar(
                    title = if (destination == NotificationDestination.InactivitySetting) "감지 기준 시간" else "알림",
                    onBackClick = {
                        destination = if (destination == NotificationDestination.Detail) detailReturnDestination else NotificationDestination.Home
                    },
                    modifier = Modifier.statusBarsPadding(),
                )
            }
        },
    ) {
    when (destination) {
        NotificationDestination.Home -> NotificationHomeRoute(
            queryError = uiState.homeQueryError,
            hasLoadedContent = uiState.hasLoadedContent,
            onRetry = viewModel::loadHome,
            uiState = uiState.home,
            onSectionClick = ::openHistory,
            onNotificationClick = ::openDetail,
            onNotificationToggle = viewModel::updateSetting,
            onDetectionTimeClick = {
                viewModel.loadInactivitySetting()
                destination = NotificationDestination.InactivitySetting
            },
            isLoading = uiState.isLoading && !uiState.hasLoadedContent,
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refreshHome,
            modifier = Modifier.fillMaxSize(),
        )

        NotificationDestination.History -> {
            val category = selectedCategory
            if (category == null || !uiState.home.canAccess(category)) {
                destination = NotificationDestination.Home
            } else {
                NotificationHistoryRoute(
                    category = category,
                    messages = uiState.histories[category].orEmpty(),
                    isRefreshing = uiState.isHistoryRefreshing,
                    onRefresh = { viewModel.refreshHistory(category) },
                    onBackClick = {
                        viewModel.loadLatestHome()
                        destination = NotificationDestination.Home
                    },
                    onMessageClick = { message ->
                        openDetail(category, message, fromHistory = true)
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        NotificationDestination.Detail -> {
            val category = selectedCategory
            val selected = selectedMessage
            val message = selected?.eventId
                ?.let(uiState.detailMessages::get)
                ?: selected
            if (category == null || message == null || !uiState.home.canAccess(category)) {
                destination = NotificationDestination.Home
            } else {
                NotificationDetailRoute(
                    category = category,
                    message = uiState.home.visibleMessage(message),
                    sharingStatusKnown = uiState.home.sharingStatusKnown,
                    locationSharingEnabled = uiState.home.sharingStatusKnown && uiState.home.locationSharingEnabled && !uiState.home.isSeniorSharingRevoked,
                    showLocationUpdate = !isHistoryDetail && uiState.home.sharingStatusKnown && uiState.home.locationSharingEnabled,
                    parentPhoneNumber = uiState.parentPhoneNumber,
                    locationRepository = locationRepository,
                    onBackClick = {
                        if (detailReturnDestination == NotificationDestination.History) viewModel.loadHistory(category)
                        else viewModel.loadLatestHome()
                        destination = detailReturnDestination
                    },
                    onRefreshClick = {
                        viewModel.openNotification(category, message)
                    },
                    isRefreshing = uiState.isDetailLoading,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        NotificationDestination.InactivitySetting -> InactivitySettingRoute(
            initialHours = uiState.inactivityThresholdHours,
            isLoading = uiState.isInactivitySettingLoading,
            isSaving = uiState.isInactivitySettingSaving,
            onBackClick = {
                destination = NotificationDestination.Home
            },
            onSaveClick = { thresholdHours ->
                viewModel.updateInactivitySetting(thresholdHours) {
                    destination = NotificationDestination.Home
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
    }
}
