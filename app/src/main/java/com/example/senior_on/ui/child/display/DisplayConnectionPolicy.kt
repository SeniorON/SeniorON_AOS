package com.example.senior_on.ui.child.display

import com.example.senior_on.domain.model.display.DisplayDeviceConnectionStatus

internal fun DisplayDeviceConnectionStatus?.allowsDisplayPreviewAndEditing(): Boolean =
    this == DisplayDeviceConnectionStatus.Online ||
        this == DisplayDeviceConnectionStatus.Offline

internal fun DisplayDeviceConnectionStatus?.requiresDelayedApplyNotice(): Boolean =
    this == DisplayDeviceConnectionStatus.Offline
