package com.example.senior_on.ui.child.health.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import com.example.senior_on.domain.repository.server.HospitalRepository
import com.example.senior_on.domain.repository.server.MedicationRepository
import com.example.senior_on.notification.MedicationCheckedEventStore
import com.example.senior_on.ui.child.health.HealthMainScreen
import com.example.senior_on.ui.child.health.viewmodel.HospitalViewModel
import com.example.senior_on.ui.child.health.viewmodel.MedicationViewModel

@Composable
fun HealthMainRoute(
    medicationRepository: MedicationRepository,
    hospitalRepository: HospitalRepository,
    familyRepository: FamilyServerRepository,
    modifier: Modifier = Modifier,
) {
    val medicationViewModel: MedicationViewModel = viewModel(
        factory = MedicationViewModel.factory(
            medicationRepository = medicationRepository,
            familyRepository = familyRepository,
        ),
    )
    val hospitalViewModel: HospitalViewModel = viewModel(
        factory = HospitalViewModel.factory(
            hospitalRepository = hospitalRepository,
            familyRepository = familyRepository,
        ),
    )
    val medicationUiState by medicationViewModel.uiState.collectAsStateWithLifecycle()
    val hospitalUiState by hospitalViewModel.uiState.collectAsStateWithLifecycle()
    val medicationCheckedEvent by MedicationCheckedEventStore.pendingEvent
        .collectAsStateWithLifecycle()

    LaunchedEffect(medicationCheckedEvent) {
        medicationCheckedEvent?.let { event ->
            medicationViewModel.onMedicationChecked(
                checkedParentUserId = event.parentUserId,
                medicationLogId = event.medicationLogId,
            )
            MedicationCheckedEventStore.consume()
        }
    }

    LaunchedEffect(medicationViewModel, hospitalViewModel) {
        medicationViewModel.loadLatestMedicationData()
        hospitalViewModel.loadLatestHospitalData()
    }

    HealthMainScreen(
        medicationUiState = medicationUiState,
        hospitalUiState = hospitalUiState,
        onMedicationDateSelected = medicationViewModel::selectDate,
        onAddMedicationClick = medicationViewModel::openAddMedication,
        onMedicationClick = medicationViewModel::openMedication,
        onMedicationEditorBackClick = medicationViewModel::backFromMedicationEditor,
        onMedicationEditClick = medicationViewModel::openEditMedication,
        onMedicationSaveClick = medicationViewModel::saveMedication,
        onMedicationDeleteClick = medicationViewModel::deleteMedication,
        onConsumeMedicationError = medicationViewModel::consumeError,
        onMedicationRefresh = medicationViewModel::refreshMedicationData,
        onHospitalMonthSelected = hospitalViewModel::selectMonth,
        onHospitalDateSelected = hospitalViewModel::selectDate,
        onAddHospitalClick = hospitalViewModel::openAdd,
        onHospitalClick = hospitalViewModel::openView,
        onHospitalEditClick = hospitalViewModel::openEdit,
        onHospitalSaveClick = hospitalViewModel::saveAppointment,
        onHospitalDeleteClick = hospitalViewModel::deleteAppointment,
        onHospitalEditorBackClick = hospitalViewModel::closeEditor,
        onConsumeHospitalError = hospitalViewModel::consumeError,
        onHospitalRefresh = hospitalViewModel::refreshHospitalData,
        modifier = modifier,
    )
}
