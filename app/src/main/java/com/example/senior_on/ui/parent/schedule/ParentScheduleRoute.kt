package com.example.senior_on.ui.parent.schedule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.domain.repository.server.HomeServerRepository
import com.example.senior_on.ui.parent.schedule.viewmodel.ParentScheduleViewModel

@Composable
fun ParentScheduleRoute(
    repository: HomeServerRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ParentScheduleViewModel = viewModel(
        factory = ParentScheduleViewModel.factory(repository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ParentScheduleScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}
