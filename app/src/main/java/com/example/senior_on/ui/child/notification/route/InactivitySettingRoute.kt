package com.example.senior_on.ui.child.notification.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.senior_on.ui.child.notification.NotificationDetectionTimeSettingScreen

@Composable
internal fun InactivitySettingRoute(
    initialHours: Int,
    isLoading: Boolean,
    isSaving: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    NotificationDetectionTimeSettingScreen(
        initialHours = initialHours,
        isLoading = isLoading,
        isSaving = isSaving,
        modifier = modifier,
        onBackClick = onBackClick,
        onSaveClick = onSaveClick,
    )
}
