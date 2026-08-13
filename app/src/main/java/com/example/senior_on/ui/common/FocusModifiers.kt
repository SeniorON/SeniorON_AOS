package com.example.senior_on.ui.common

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.clearFocusOnBackgroundTap(
    focusManager: FocusManager
): Modifier = pointerInput(focusManager) {
    awaitEachGesture {
        awaitFirstDown(
            requireUnconsumed = true,
            pass = PointerEventPass.Final
        )
        val up = waitForUpOrCancellation(pass = PointerEventPass.Final)
        if (up != null && !up.isConsumed) {
            focusManager.clearFocus()
        }
    }
}
