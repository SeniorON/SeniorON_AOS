package com.example.senior_on.ui.onboarding.route

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.di.AppContainer
import com.example.senior_on.ui.common.seniorinfo.viewmodel.AddressSearchViewModel
import com.example.senior_on.ui.onboarding.viewmodel.AccountRecoveryViewModel
import com.example.senior_on.ui.onboarding.viewmodel.AuthViewModel
import com.example.senior_on.ui.onboarding.viewmodel.SeniorOnboardingViewModel

@Composable
internal fun onboardingAuthViewModel(
    appContainer: AppContainer
): AuthViewModel {
    return viewModel(
        factory = AuthViewModel.Factory(
            authRepository = appContainer.authRepository,
            socialAuthRepository = appContainer.socialAuthRepository,
            sessionRepository = appContainer.sessionRepository,
            deviceRegistrationRepository = appContainer.deviceRegistrationRepository
        )
    )
}

@Composable
internal fun accountRecoveryViewModel(
    appContainer: AppContainer
): AccountRecoveryViewModel {
    return viewModel(
        factory = AccountRecoveryViewModel.Factory(
            repository = appContainer.accountRecoveryRepository
        )
    )
}

@Composable
internal fun seniorOnboardingViewModel(
    appContainer: AppContainer
): SeniorOnboardingViewModel {
    return viewModel(
        factory = SeniorOnboardingViewModel.Factory(
            seniorRepository = appContainer.seniorRepository,
            parentInfoRepository = appContainer.parentInfoRepository,
            homeRepository = appContainer.homeServerRepository,
            caregiverRelationshipRepositoryFor =
                appContainer::caregiverRelationshipRepositoryFor
        )
    )
}

@Composable
internal fun addressSearchViewModel(
    appContainer: AppContainer
): AddressSearchViewModel {
    return viewModel(
        factory = AddressSearchViewModel.Factory(
            repository = appContainer.addressSearchRepository,
        )
    )
}
