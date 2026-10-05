package com.example.senior_on.ui.parent.permission

/** Back navigation includes deferred permissions; only actual completion skips a step. */
internal fun ParentPermissionStep.previousRequired(
    status: (ParentPermissionStep) -> ParentPermissionStatus,
): ParentPermissionStep? = ParentPermissionStep.entries.take(ordinal).lastOrNull {
    status(it) == ParentPermissionStatus.Required || status(it) == ParentPermissionStatus.Manual
}

/** Dismissal affects automatic presentation only, never the actual permission state. */
internal fun shouldOfferPermissionGuide(
    dismissed: Set<ParentPermissionStep> = emptySet(),
    status: (ParentPermissionStep) -> ParentPermissionStatus,
): Boolean = ParentPermissionStep.entries.any { it !in dismissed && status(it) == ParentPermissionStatus.Required }

internal fun firstMissingPermissionStep(
    dismissed: Set<ParentPermissionStep> = emptySet(),
    status: (ParentPermissionStep) -> ParentPermissionStatus,
): ParentPermissionStep? = ParentPermissionStep.entries.firstOrNull {
    it !in dismissed && (status(it) == ParentPermissionStatus.Required || status(it) == ParentPermissionStatus.Manual)
}

/** A subsequently granted permission must not retain an old refusal if revoked again. */
internal fun remainingPermissionDismissals(
    dismissed: Set<ParentPermissionStep>,
    status: (ParentPermissionStep) -> ParentPermissionStatus,
): Set<ParentPermissionStep> = dismissed.filterTo(mutableSetOf()) {
    status(it) != ParentPermissionStatus.Granted && status(it) != ParentPermissionStatus.NotApplicable
}
