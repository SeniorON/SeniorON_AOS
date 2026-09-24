package com.example.senior_on.ui.parent.permission

/** Dismissal affects automatic presentation only, never the actual permission state. */
internal fun shouldOfferPermissionGuide(
    dismissed: Boolean,
    status: (ParentPermissionStep) -> ParentPermissionStatus,
): Boolean = !dismissed && ParentPermissionStep.entries.any { status(it) == ParentPermissionStatus.Required }

internal fun firstMissingPermissionStep(
    status: (ParentPermissionStep) -> ParentPermissionStatus,
): ParentPermissionStep? = ParentPermissionStep.entries.firstOrNull {
    status(it) == ParentPermissionStatus.Required || status(it) == ParentPermissionStatus.Manual
}
