package com.example.senior_on.ui.child.display

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.senior_on.domain.model.parent.SeniorRelationType
import com.example.senior_on.domain.model.senior.ManagedSenior
import com.example.senior_on.domain.model.senior.SeniorRegistration
import com.example.senior_on.ui.child.display.viewmodel.SeniorManagementUiState
import com.example.senior_on.ui.child.display.viewmodel.SeniorManagementViewModel
import com.example.senior_on.ui.common.seniorinfo.AddressSearchScreen
import com.example.senior_on.ui.common.seniorinfo.ParentInfoInputScreen
import com.example.senior_on.ui.common.seniorinfo.ParentInfoInputState
import com.example.senior_on.ui.common.seniorinfo.SeniorRelationship
import com.example.senior_on.ui.common.seniorinfo.parseBirthDate
import com.example.senior_on.ui.common.seniorinfo.viewmodel.AddressSearchViewModel
import com.example.senior_on.ui.onboarding.familycode.FamilyShareCodeCreatedScreen

internal enum class SeniorManagementDestination {
    FamilyCodeCreated,
    ParentInfoInput,
    AddressSearch,
}

@Composable
internal fun SeniorManagementRoute(
    uiState: SeniorManagementUiState,
    viewModel: SeniorManagementViewModel,
    addressSearchViewModel: AddressSearchViewModel,
    onClose: () -> Unit,
    onSeniorCreated: (ManagedSenior) -> Unit,
    modifier: Modifier = Modifier,
) {
    var destination by rememberSaveable {
        mutableStateOf(SeniorManagementDestination.FamilyCodeCreated)
    }
    var selectedAddress by rememberSaveable { mutableStateOf("") }
    var selectedAddressLatitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var selectedAddressLongitude by rememberSaveable { mutableStateOf<Double?>(null) }
    val saveableStateHolder = rememberSaveableStateHolder()

    LaunchedEffect(viewModel) {
        viewModel.beginSeniorAddition()
    }

    fun navigateBack() {
        if (uiState.isSubmitting || uiState.isCreatingFamilyCode) return
        val backDestination = resolveSeniorManagementBackDestination(destination)
        if (backDestination == null) {
            onClose()
        } else {
            viewModel.clearError()
            destination = backDestination
        }
    }

    fun finish(senior: ManagedSenior) {
        SeniorManagementDestination.entries.forEach { savedDestination ->
            saveableStateHolder.removeState(savedDestination.name)
        }
        onSeniorCreated(senior)
        onClose()
    }

    BackHandler(onBack = ::navigateBack)

    saveableStateHolder.SaveableStateProvider(destination.name) {
        when (destination) {
            SeniorManagementDestination.FamilyCodeCreated ->
                FamilyShareCodeCreatedScreen(
                    onBackClick = ::navigateBack,
                    onNextClick = {
                        if (uiState.createdFamilyId != null) {
                            viewModel.clearError()
                            destination = SeniorManagementDestination.ParentInfoInput
                        }
                    },
                    familyShareCode = uiState.familyCode.orEmpty(),
                    isLoading = uiState.isCreatingFamilyCode,
                    errorMessage = uiState.errorMessage,
                    onRetryClick = viewModel::retryCreateFamilyCode,
                    modifier = modifier,
                )

            SeniorManagementDestination.ParentInfoInput -> ParentInfoInputScreen(
                selectedAddress = selectedAddress,
                selectedAddressLatitude = selectedAddressLatitude,
                selectedAddressLongitude = selectedAddressLongitude,
                isSubmitting = uiState.isSubmitting,
                errorMessage = uiState.errorMessage,
                onBackClick = ::navigateBack,
                onSkipClick = onClose,
                onSearchAddressClick = {
                    destination = SeniorManagementDestination.AddressSearch
                },
                onInputChange = viewModel::clearError,
                onSaveClick = { inputState ->
                    viewModel.createSenior(
                        registration = inputState.toSeniorRegistration(),
                        onSuccess = ::finish,
                    )
                },
                modifier = modifier,
            )

            SeniorManagementDestination.AddressSearch -> AddressSearchScreen(
                viewModel = addressSearchViewModel,
                onBackClick = ::navigateBack,
                onAddressSelected = { result ->
                    selectedAddress = result.selectedAddress
                    selectedAddressLatitude = result.latitude
                    selectedAddressLongitude = result.longitude
                    destination = SeniorManagementDestination.ParentInfoInput
                },
                modifier = modifier,
            )
        }
    }
}

internal fun resolveSeniorManagementBackDestination(
    destination: SeniorManagementDestination,
): SeniorManagementDestination? = when (destination) {
    SeniorManagementDestination.FamilyCodeCreated -> null
    SeniorManagementDestination.ParentInfoInput ->
        SeniorManagementDestination.FamilyCodeCreated
    SeniorManagementDestination.AddressSearch ->
        SeniorManagementDestination.ParentInfoInput
}

private fun ParentInfoInputState.toSeniorRegistration(): SeniorRegistration {
    val relation = when (relationship) {
        SeniorRelationship.Mother -> SeniorRelationType.MOTHER
        SeniorRelationship.Father -> SeniorRelationType.FATHER
        SeniorRelationship.Grandparent -> SeniorRelationType.GRANDPARENT
        SeniorRelationship.Custom -> SeniorRelationType.OTHER
    }

    return SeniorRegistration(
        name = name.trim(),
        relation = relation,
        customRelation = customRelationship
            .trim()
            .takeIf { relation == SeniorRelationType.OTHER && it.isNotEmpty() },
        birth = requireNotNull(parseBirthDate(birthDate)).toString(),
        phoneNumber = phoneNumber,
        address = address.trim(),
        detailAddress = addressDetail.trim(),
        latitude = addressLatitude,
        longitude = addressLongitude,
    )
}
