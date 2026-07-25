package com.example.senior_on.ui.child.display.viewmodel

import com.example.senior_on.ui.child.display.DisplayTabUiState

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
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
) : ViewModel() {
    val uiState = combine(
        parentInfoRepository.parentInfo,
        displayRepository.overview,
    ) { parentInfo, overview ->
        DisplayTabUiState(
            parentInfo = parentInfo,
            device = overview.device,
            screenConfiguration = overview.screenConfiguration,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DisplayTabUiState(
            parentInfo = parentInfoRepository.parentInfo.value,
            device = displayRepository.overview.value.device,
            screenConfiguration = displayRepository.overview.value.screenConfiguration,
        ),
    )

    fun saveParentInfo(parentInfo: ParentInfo) {
        parentInfoRepository.saveParentInfo(parentInfo)
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
