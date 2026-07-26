package com.example.senior_on.ui.child.display

import androidx.compose.runtime.Immutable
import com.example.senior_on.domain.model.display.DisplayDevice
import com.example.senior_on.domain.model.parent.ParentInfo
import com.example.senior_on.domain.model.display.SeniorScreenConfiguration

@Immutable
data class DisplayTabUiState(
    val parentInfo: ParentInfo? = null,
    val relationshipLabel: String? = null,
    val device: DisplayDevice? = null,
    val screenConfiguration: SeniorScreenConfiguration = SeniorScreenConfiguration(),
)
