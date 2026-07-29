package com.example.senior_on.domain.repository.display

import com.example.senior_on.domain.model.display.DisplayOverview
import com.example.senior_on.domain.model.display.DisplayDevice
import com.example.senior_on.domain.model.display.SeniorFontSize
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.domain.model.parent.ParentInfo

interface DisplayRepository {
    suspend fun getOverview(currentParentInfo: ParentInfo?): DisplayOverview

    suspend fun getDevice(): DisplayDevice?

    suspend fun updateSeniorProfile(parentInfo: ParentInfo): ParentInfo

    suspend fun updateFontSize(fontSize: SeniorFontSize)

    suspend fun saveButtons(
        buttons: List<SeniorHomeButtonType>,
        customButtonLabels: Map<SeniorHomeButtonType, String>,
    )

    suspend fun disconnectDevice()
}
