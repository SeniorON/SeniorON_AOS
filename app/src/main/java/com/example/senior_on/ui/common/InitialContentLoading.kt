package com.example.senior_on.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.senior_on.ui.theme.SeniorOnColors

/** Initial content loading is independent of the pull-to-refresh indicator. */
@Composable
internal fun InitialContentLoading(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = SeniorOnColors.Primary600)
    }
}
