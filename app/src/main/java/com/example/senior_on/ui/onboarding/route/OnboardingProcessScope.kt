package com.example.senior_on.ui.onboarding.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import java.util.UUID

private val processIdentity = UUID.randomUUID().toString()

/** Rotation keeps the key; process recreation gets a new key and discards only onboarding UI drafts. */
@Composable
internal fun OnboardingProcessScope(
    processId: String = processIdentity,
    content: @Composable (restoredFromAnotherProcess: Boolean) -> Unit,
) {
    val savedProcessId by rememberSaveable { mutableStateOf(processId) }
    key(processId) {
        content(savedProcessId != processId)
    }
}
