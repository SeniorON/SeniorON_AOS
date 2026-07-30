package com.example.senior_on.ui.child.notification.route

import androidx.compose.runtime.Composable
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
import com.example.senior_on.ui.child.notification.NotificationCategory
import com.example.senior_on.ui.child.notification.NotificationMessageUiState

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
    ) {
        detailReturnDestination = destination
        selectedCategory = category
        selectedMessage = message
        viewModel.openNotification(category, message)
        destination = NotificationDestination.Detail
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
                    onBackClick = {
                        destination = detailReturnDestination
                    },
                    modifier = modifier,
                )
            }
        }

        NotificationDestination.InactivitySetting -> InactivitySettingRoute(
            initialHours = uiState.inactivityThresholdHours,
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
