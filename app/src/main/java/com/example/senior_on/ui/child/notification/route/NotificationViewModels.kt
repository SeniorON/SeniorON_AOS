package com.example.senior_on.ui.child.notification.route

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import com.example.senior_on.domain.repository.server.HomeServerRepository
import com.example.senior_on.domain.repository.server.EventRepository
import com.example.senior_on.domain.repository.server.NotificationRepository
import com.example.senior_on.domain.repository.server.DeviceRepository
import com.example.senior_on.data.repository.impl.AddressSearchRepository
import com.example.senior_on.ui.child.notification.viewmodel.NotificationViewModel

@Composable
internal fun notificationViewModel(
    repository: NotificationRepository,
    familyRepository: FamilyServerRepository?,
    homeRepository: HomeServerRepository?,
    eventRepository: EventRepository?,
    deviceRepository: DeviceRepository?,
    addressSearchRepository: AddressSearchRepository?,
): NotificationViewModel {
    return viewModel(
        key = "notification",
        factory = NotificationViewModel.Factory(
            repository = repository,
            familyRepository = familyRepository,
            homeRepository = homeRepository,
            eventRepository = eventRepository,
            deviceRepository = deviceRepository,
            addressSearchRepository = addressSearchRepository,
        ),
    )
}
