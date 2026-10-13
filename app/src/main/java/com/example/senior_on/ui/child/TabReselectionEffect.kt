package com.example.senior_on.ui.child

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

/** A new route consumes the current token without replaying an earlier tap. */
@Composable
internal fun TabReselectionEffect(token: Int, onReselect: () -> Unit) {
    val gate = remember { TabReselectionGate(token) }
    val action by rememberUpdatedState(onReselect)
    LaunchedEffect(token) {
        if (gate.consume(token)) {
            action()
        }
    }
}

internal class TabReselectionGate(private var consumed: Int) {
    fun consume(token: Int): Boolean {
        if (consumed == token) return false
        consumed = token
        return true
    }
}
