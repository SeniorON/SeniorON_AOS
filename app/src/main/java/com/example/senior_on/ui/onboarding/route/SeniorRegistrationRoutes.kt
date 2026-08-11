package com.example.senior_on.ui.onboarding.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.senior_on.data.source.mock.fixtures.MockSeniorFixtures
import com.example.senior_on.di.AppContainer
import com.example.senior_on.domain.model.address.AddressSearchResult
import com.example.senior_on.domain.model.parent.SeniorRelationType
import com.example.senior_on.domain.model.senior.SeniorRegistration
import com.example.senior_on.ui.common.seniorinfo.AddressSearchScreen
import com.example.senior_on.ui.common.seniorinfo.CaregiverRelationshipInputScreen
import com.example.senior_on.ui.common.seniorinfo.ParentInfoInputScreen
import com.example.senior_on.ui.common.seniorinfo.ParentInfoInputState
import com.example.senior_on.ui.common.seniorinfo.SeniorRelationship
import com.example.senior_on.ui.common.seniorinfo.toParentInfo

@Composable
fun CaregiverRelationshipRoute(
    appContainer: AppContainer,
    userId: String,
    seniorId: Long? = null,
    onBackClick: () -> Unit,
    onComplete: () -> Unit
) {
    val authViewModel = onboardingAuthViewModel(appContainer)
    val seniorViewModel = seniorOnboardingViewModel(appContainer)
    val uiState by seniorViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(seniorViewModel, seniorId) {
        if (seniorId != null) {
            seniorViewModel.setConnectedSenior(seniorId)
        } else {
            seniorViewModel.loadConnectedSenior()
        }
    }

    CaregiverRelationshipInputScreen(
        seniorName = uiState.connectedSeniorName,
        onBackClick = onBackClick,
        onNextClick = { relationship ->
            val accessToken = authViewModel.accessToken
            val seniorId = uiState.connectedSeniorId
            if (accessToken != null && seniorId != null) {
                seniorViewModel.updateRelation(
                    accessToken = accessToken,
                    userId = userId,
                    seniorId = seniorId,
                    relationship = relationship
                ) { result ->
                    if (result != null) {
                        onComplete()
                    }
                }
            }
        },
        isSubmitting = uiState.isLoading
    )
}

@Composable
fun ParentInfoInputRoute(
    appContainer: AppContainer,
    selectedAddress: String,
    selectedAddressLatitude: Double?,
    selectedAddressLongitude: Double?,
    onBackClick: () -> Unit,
    onSkipClick: () -> Unit,
    onSearchAddressClick: () -> Unit,
    onComplete: () -> Unit
) {
    val authViewModel = onboardingAuthViewModel(appContainer)
    val seniorViewModel = seniorOnboardingViewModel(appContainer)
    val uiState by seniorViewModel.uiState.collectAsStateWithLifecycle()

    ParentInfoInputScreen(
        selectedAddress = selectedAddress,
        selectedAddressLatitude = selectedAddressLatitude,
        selectedAddressLongitude = selectedAddressLongitude,
        isSubmitting = uiState.isLoading,
        onBackClick = onBackClick,
        onSkipClick = onSkipClick,
        onSearchAddressClick = onSearchAddressClick,
        onSaveClick = { inputState ->
            val accessToken = authViewModel.accessToken
            if (accessToken != null) {
                seniorViewModel.createSenior(
                    accessToken = accessToken,
                    registration = inputState.toSeniorRegistration(),
                    parentInfo = inputState.toParentInfo(
                        seniorId = MockSeniorFixtures.SENIOR_ID
                    )
                ) { senior ->
                    if (senior != null) {
                        onComplete()
                    }
                }
            }
        }
    )
}

@Composable
fun AddressSearchRoute(
    onBackClick: () -> Unit,
    onAddressSelected: (AddressSearchResult) -> Unit
) {
    AddressSearchScreen(
        onBackClick = onBackClick,
        onAddressSelected = onAddressSelected
    )
}

private fun ParentInfoInputState.toSeniorRegistration(): SeniorRegistration {
    val parentInfo = toParentInfo(seniorId = MockSeniorFixtures.SENIOR_ID)
    val relation = when (relationship) {
        SeniorRelationship.Mother -> SeniorRelationType.MOTHER
        SeniorRelationship.Father -> SeniorRelationType.FATHER
        SeniorRelationship.Grandparent -> SeniorRelationType.GRANDPARENT
        SeniorRelationship.Custom -> SeniorRelationType.OTHER
    }

    return SeniorRegistration(
        name = parentInfo.name,
        relation = relation,
        customRelation = customRelationship.takeIf {
            relation == SeniorRelationType.OTHER
        },
        birth = parentInfo.birthDate.toString(),
        phoneNumber = parentInfo.phoneNumber,
        address = parentInfo.address,
        detailAddress = parentInfo.addressDetail,
        latitude = parentInfo.addressLatitude,
        longitude = parentInfo.addressLongitude
    )
}
