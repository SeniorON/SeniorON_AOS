package com.example.senior_on.ui.child.health.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import com.example.senior_on.domain.repository.server.MedicationRepository
import com.example.senior_on.notification.MedicationCheckedEventStore
import com.example.senior_on.ui.child.health.HealthMainScreen
import com.example.senior_on.ui.child.health.viewmodel.MedicationViewModel

@Composable
fun HealthMainRoute(
    medicationRepository: MedicationRepository,
    familyRepository: FamilyServerRepository,
    modifier: Modifier = Modifier,
) {
    val viewModel: MedicationViewModel = viewModel(
        factory = MedicationViewModel.factory(
            medicationRepository = medicationRepository,
            familyRepository = familyRepository,
        ),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val medicationCheckedEvent by MedicationCheckedEventStore.pendingEvent
        .collectAsStateWithLifecycle()

    LaunchedEffect(medicationCheckedEvent) {
        medicationCheckedEvent?.let { event ->
            viewModel.onMedicationChecked(
                checkedParentUserId = event.parentUserId,
                medicationLogId = event.medicationLogId,
            )
            MedicationCheckedEventStore.consume()
        }
    }

    HealthMainScreen(
        medicationUiState = uiState,
        onMedicationDateSelected = viewModel::selectDate,
        onAddMedicationClick = viewModel::openAddMedication,
        onMedicationClick = viewModel::openMedication,
        onMedicationEditorBackClick = viewModel::backFromMedicationEditor,
        onMedicationEditClick = viewModel::openEditMedication,
        onMedicationSaveClick = viewModel::saveMedication,
        onMedicationDeleteClick = viewModel::deleteMedication,
        modifier = modifier,
    )
}
