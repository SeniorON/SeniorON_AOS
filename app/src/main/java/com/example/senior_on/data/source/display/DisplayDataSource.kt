package com.example.senior_on.data.source.display

import com.example.senior_on.domain.model.display.DisplayOverview
import com.example.senior_on.domain.model.display.SeniorFontSize
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import kotlinx.coroutines.flow.StateFlow

interface DisplayDataSource {
    val overview: StateFlow<DisplayOverview>
    fun updateFontSize(fontSize: SeniorFontSize)
    fun updateButtons(
        buttons: List<SeniorHomeButtonType>,
        customButtonLabels: Map<SeniorHomeButtonType, String>
    )
    fun disconnectDevice()
}
