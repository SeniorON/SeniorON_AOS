package com.example.senior_on.domain.repository.display

import com.example.senior_on.domain.model.display.DisplayOverview
import com.example.senior_on.domain.model.display.DisplayDevice
import com.example.senior_on.domain.model.display.DisplayHomeButton
import com.example.senior_on.domain.model.display.SeniorFontSize
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.domain.model.display.SeniorScreenConfiguration
import com.example.senior_on.domain.model.parent.ParentInfo

interface DisplayRepository {
    suspend fun canCurrentUserEditScreen(seniorId: Long): Boolean

    suspend fun getOverview(
        seniorId: Long,
        currentParentInfo: ParentInfo?,
    ): DisplayOverview

    suspend fun getSeniorScreenConfiguration(): SeniorScreenConfiguration

    suspend fun getDevice(seniorId: Long): DisplayDevice?

    suspend fun updateSeniorProfile(parentInfo: ParentInfo): ParentInfo

    suspend fun updateFontSize(seniorId: Long, fontSize: SeniorFontSize)

    suspend fun saveButtons(
        seniorId: Long,
        buttons: List<SeniorHomeButtonType>,
        customButtonLabels: Map<SeniorHomeButtonType, String>,
    )

    suspend fun saveButtons(seniorId: Long, buttons: List<DisplayHomeButton>)

    suspend fun disconnectDevice(seniorId: Long)
}
