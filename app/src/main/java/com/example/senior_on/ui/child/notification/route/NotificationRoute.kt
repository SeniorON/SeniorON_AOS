package com.example.senior_on.ui.child.notification.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
        familyRepository = familyRepository,
        homeRepository = homeRepository,
        eventRepository = eventRepository,
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var destination by rememberSaveable {
        mutableStateOf(NotificationDestination.Home)
    }
    var selectedCategory by rememberSaveable {
        mutableStateOf<NotificationCategory?>(null)
    }
    var selectedMessage by remember {
        mutableStateOf<NotificationMessageUiState?>(null)
    }
    var detailReturnDestination by rememberSaveable {
        mutableStateOf(NotificationDestination.Home)
    }

    fun openHistory(category: NotificationCategory) {
        selectedCategory = category
        viewModel.loadHistory(category)
        destination = NotificationDestination.History
    }

    fun openDetail(
        category: NotificationCategory,
        message: NotificationMessageUiState,
        loadDetail: Boolean = true,
    ) {
        detailReturnDestination = destination
        selectedCategory = category
        selectedMessage = message
        if (loadDetail) {
            viewModel.openNotification(category, message)
        }
        destination = NotificationDestination.Detail
    }

    LaunchedEffect(navigationEvent) {
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

    when (destination) {
        NotificationDestination.Home -> NotificationHomeRoute(
            uiState = uiState.home,
            onSectionClick = ::openHistory,
            onNotificationClick = ::openDetail,
            onNotificationToggle = viewModel::updateSetting,
            onDetectionTimeClick = {
                viewModel.loadInactivitySetting()
                destination = NotificationDestination.InactivitySetting
            },
            isLoading = uiState.isLoading,
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refreshHome,
            modifier = modifier,
        )

        NotificationDestination.History -> {
            val category = selectedCategory
            if (category == null) {
                destination = NotificationDestination.Home
            } else {
                NotificationHistoryRoute(
                    category = category,
                    messages = uiState.histories[category].orEmpty(),
                    isRefreshing = uiState.isHistoryRefreshing,
                    onRefresh = { viewModel.refreshHistory(category) },
                    onBackClick = {
                        destination = NotificationDestination.Home
                    },
                    onMessageClick = { message ->
                        openDetail(category, message)
                    },
                    modifier = modifier,
                )
            }
        }

        NotificationDestination.Detail -> {
            val category = selectedCategory
            val selected = selectedMessage
            val message = selected?.eventId
                ?.let(uiState.detailMessages::get)
                ?: selected
            if (category == null || message == null) {
                destination = NotificationDestination.Home
            } else {
                NotificationDetailRoute(
                    category = category,
                    message = message,
                    parentPhoneNumber = uiState.parentPhoneNumber,
                    locationRepository = locationRepository,
                    onBackClick = {
                        destination = detailReturnDestination
                    },
                    onRefreshClick = {
                        viewModel.openNotification(category, message)
                    },
                    isRefreshing = uiState.isDetailLoading,
                    modifier = modifier,
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
            modifier = modifier,
        )
    }
}
