package com.example.senior_on.ui.parent.medication.route

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.senior_on.domain.repository.parent.ParentHomeUpdatesRepository
import com.example.senior_on.domain.repository.server.MedicationRepository
import com.example.senior_on.ui.parent.medication.ParentMedicationScreen
import com.example.senior_on.ui.parent.medication.viewmodel.ParentMedicationViewModel

@Composable
fun ParentMedicationRoute(
    repository: MedicationRepository,
    updatesRepository: ParentHomeUpdatesRepository,
    onBackClick: () -> Unit,
    highlightedMedicationLogId: Long? = null,
    modifier: Modifier = Modifier,
) {
    val viewModel: ParentMedicationViewModel = viewModel(
        factory = ParentMedicationViewModel.factory(repository, updatesRepository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel, highlightedMedicationLogId) {
        viewModel.loadMedication(highlightedMedicationLogId)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.observeUpdates()
        }
    }

    DisposableEffect(viewModel) {
        onDispose(viewModel::reset)
    }

    fun resetAndGoBack() {
        viewModel.reset()
        onBackClick()
    }

    BackHandler(onBack = ::resetAndGoBack)

    ParentMedicationScreen(
        uiState = uiState,
        onBackClick = ::resetAndGoBack,
        onTakenClick = viewModel::markAsTaken,
        onRefresh = viewModel::refresh,
        onMessageConsumed = viewModel::consumeMessage,
        modifier = modifier,
    )
}
