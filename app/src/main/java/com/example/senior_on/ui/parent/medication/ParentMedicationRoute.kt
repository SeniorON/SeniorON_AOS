package com.example.senior_on.ui.parent.medication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.domain.repository.parent.ParentMedicationRepository
import com.example.senior_on.ui.parent.medication.viewmodel.ParentMedicationViewModel

@Composable
fun ParentMedicationRoute(
    repository: ParentMedicationRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ParentMedicationViewModel = viewModel(
        factory = ParentMedicationViewModel.factory(repository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ParentMedicationScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onTakenClick = viewModel::markAsTaken,
        modifier = modifier,
    )
}
