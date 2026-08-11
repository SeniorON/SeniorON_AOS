package com.example.senior_on.ui.child.display

import androidx.compose.runtime.Immutable
import com.example.senior_on.domain.model.display.DisplayDevice
import com.example.senior_on.domain.model.display.DisplayHomeButton
import com.example.senior_on.domain.model.display.DisplayTodaySchedule
import com.example.senior_on.domain.model.display.DisplayWeather
import com.example.senior_on.domain.model.parent.ParentInfo
import com.example.senior_on.domain.model.display.SeniorScreenConfiguration
import com.example.senior_on.domain.model.display.SeniorHomeButtonType

@Immutable
data class DisplayTabUiState(
    val parentInfo: ParentInfo? = null,
    val relationshipLabel: String? = null,
    val device: DisplayDevice? = null,
    val weather: DisplayWeather? = null,
    val todaySchedule: DisplayTodaySchedule? = null,
    val screenConfiguration: SeniorScreenConfiguration = SeniorScreenConfiguration(),
    val availableButtonTypes: Set<SeniorHomeButtonType> = emptySet(),
    val configuredButtonItems: List<DisplayHomeButton> = emptyList(),
    val availableButtonOptions: List<DisplayHomeButton> = emptyList(),
    val canEditScreen: Boolean = false,
    val isEditPermissionLoading: Boolean = true,
    val isLoading: Boolean = false,
    val isWeatherLoading: Boolean = false,
    val isRefreshingDevice: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val hasLoadedOverview: Boolean = false,
)
