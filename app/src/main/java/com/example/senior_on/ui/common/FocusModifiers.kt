package com.example.senior_on.ui.common

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.clearFocusOnBackgroundTap(
    focusManager: FocusManager
): Modifier = pointerInput(focusManager) {
    detectTapGestures {
        focusManager.clearFocus()
    }
}
