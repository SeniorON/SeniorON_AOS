package com.example.senior_on.ui.display

import androidx.compose.runtime.Immutable
import com.example.senior_on.domain.model.DisplayDevice
import com.example.senior_on.domain.model.ParentInfo
import com.example.senior_on.domain.model.SeniorScreenConfiguration

@Immutable
data class DisplayTabUiState(
    val parentInfo: ParentInfo? = null,
    val device: DisplayDevice? = null,
    val screenConfiguration: SeniorScreenConfiguration = SeniorScreenConfiguration(),
)
