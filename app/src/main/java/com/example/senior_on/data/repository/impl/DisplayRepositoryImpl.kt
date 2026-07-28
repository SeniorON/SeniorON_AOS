package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.source.display.DisplayDataSource
import com.example.senior_on.domain.model.display.DisplayOverview
import com.example.senior_on.domain.model.display.SeniorFontSize
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.domain.repository.display.DisplayRepository
import kotlinx.coroutines.flow.StateFlow

class DisplayRepositoryImpl(
    private val dataSource: DisplayDataSource
) : DisplayRepository {
    override val overview: StateFlow<DisplayOverview> = dataSource.overview

    override fun updateFontSize(fontSize: SeniorFontSize) =
        dataSource.updateFontSize(fontSize)

    override fun updateButtons(
        buttons: List<SeniorHomeButtonType>,
        customButtonLabels: Map<SeniorHomeButtonType, String>
    ) = dataSource.updateButtons(
        buttons = buttons.distinct(),
        customButtonLabels = customButtonLabels.mapValues { (_, label) -> label.trim() }
    )

    override fun disconnectDevice() = dataSource.disconnectDevice()
}
