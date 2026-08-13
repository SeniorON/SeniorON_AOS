package com.example.senior_on.ui.parent.link

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.domain.repository.parent.ParentLinkSafetyRepository
import com.example.senior_on.ui.parent.home.openExternalBrowser
import com.example.senior_on.ui.parent.link.viewmodel.ParentLinkDetectionStatus
import com.example.senior_on.ui.parent.link.viewmodel.ParentLinkDetectionViewModel

@Composable
fun ParentLinkDetectionRoute(
    repository: ParentLinkSafetyRepository,
    url: String? = null,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val viewModel: ParentLinkDetectionViewModel = viewModel(
        factory = ParentLinkDetectionViewModel.factory(repository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(url) {
        url?.takeIf(String::isNotBlank)?.let(viewModel::inspectLink)
    }

    LaunchedEffect(uiState.status) {
        if (uiState.status == ParentLinkDetectionStatus.Safe) {
            uiState.url?.let { openExternalBrowser(context, it) }
            viewModel.reset()
            onBackClick()
        }
    }

    ParentLinkDetectionScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}
