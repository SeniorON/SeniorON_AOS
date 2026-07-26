package com.example.senior_on.ui.child.display.viewmodel

import com.example.senior_on.ui.child.display.DisplayTabUiState

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.model.parent.CaregiverRelationship
import com.example.senior_on.domain.repository.parent.CaregiverRelationshipRepository
import com.example.senior_on.domain.repository.display.DisplayRepository
import com.example.senior_on.domain.repository.parent.ParentInfoRepository
import com.example.senior_on.domain.model.parent.ParentInfo
import com.example.senior_on.domain.model.display.SeniorFontSize
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DisplayViewModel(
    private val parentInfoRepository: ParentInfoRepository,
    private val displayRepository: DisplayRepository,
    private val caregiverRelationshipRepository: CaregiverRelationshipRepository,
) : ViewModel() {
    val uiState = combine(
        parentInfoRepository.parentInfo,
        displayRepository.overview,
        caregiverRelationshipRepository.relationship,
    ) { parentInfo, overview, caregiverRelationship ->
        DisplayTabUiState(
            parentInfo = parentInfo,
            relationshipLabel = caregiverRelationship
                ?.displayLabel
                ?: parentInfo?.relationshipLabel,
            device = overview.device,
            screenConfiguration = overview.screenConfiguration,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DisplayTabUiState(
            parentInfo = parentInfoRepository.parentInfo.value,
            relationshipLabel =
                caregiverRelationshipRepository.relationship.value
                    ?.displayLabel
                    ?: parentInfoRepository.parentInfo.value?.relationshipLabel,
            device = displayRepository.overview.value.device,
            screenConfiguration = displayRepository.overview.value.screenConfiguration,
        ),
    )

    fun saveParentInfo(parentInfo: ParentInfo) {
        val sharedParentInfo = parentInfoRepository.parentInfo.value

        caregiverRelationshipRepository.saveRelationship(
            seniorId = parentInfo.seniorId,
            relationship = CaregiverRelationship.fromDisplayLabel(
                parentInfo.relationshipLabel,
            ),
        )
        parentInfoRepository.saveParentInfo(
            parentInfo.copy(
                relationshipLabel = sharedParentInfo
                    ?.relationshipLabel
                    ?: parentInfo.relationshipLabel
            )
        )
    }

    fun disconnectDevice() {
        displayRepository.disconnectDevice()
    }

    fun updateFontSize(fontSize: SeniorFontSize) {
        displayRepository.updateFontSize(fontSize)
    }

    fun updateButtons(
        buttons: List<SeniorHomeButtonType>,
        customButtonLabels: Map<SeniorHomeButtonType, String>,
    ) {
        displayRepository.updateButtons(
            buttons = buttons,
            customButtonLabels = customButtonLabels,
        )
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
