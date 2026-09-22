package com.example.senior_on.ui.child

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

/** Cancel old requests when the account, selected senior, or tab leaves composition. */
@Composable
fun rememberSeniorScopedViewModelStoreOwner(key: String): ViewModelStoreOwner {
    val owner = remember(key) {
        object : ViewModelStoreOwner { override val viewModelStore = ViewModelStore() }
    }
    DisposableEffect(owner) { onDispose { owner.viewModelStore.clear() } }
    return owner
}
