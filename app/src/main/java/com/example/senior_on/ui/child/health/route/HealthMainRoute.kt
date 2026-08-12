package com.example.senior_on.ui.child.health.route

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import com.example.senior_on.domain.repository.server.HospitalRepository
import com.example.senior_on.domain.repository.server.MedicationRepository
import com.example.senior_on.domain.repository.health.HospitalSpecialtyRepository
import com.example.senior_on.notification.MedicationCheckedEventStore
import com.example.senior_on.ui.child.health.HealthSection
import com.example.senior_on.ui.child.health.HealthMainScreen
import com.example.senior_on.ui.child.health.HospitalAppointmentUiState
import com.example.senior_on.ui.child.health.SeniorOnDeleteConfirmDialog
import com.example.senior_on.ui.child.health.viewmodel.HospitalViewModel
import com.example.senior_on.ui.child.health.viewmodel.MedicationViewModel

@Composable
fun HealthMainRoute(
    medicationRepository: MedicationRepository,
    hospitalRepository: HospitalRepository,
    familyRepository: FamilyServerRepository,
    hospitalSpecialtyRepository: HospitalSpecialtyRepository,
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
    var selectedSection by rememberSaveable { mutableStateOf(HealthSection.Health) }
    var appointmentToDelete by remember { mutableStateOf<HospitalAppointmentUiState?>(null) }
    var wasDeletingAppointment by remember { mutableStateOf(false) }
    val context = LocalContext.current

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

    LaunchedEffect(hospitalUiState.errorMessage) {
        val message = hospitalUiState.errorMessage ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        hospitalViewModel.consumeError()
    }

    LaunchedEffect(medicationUiState.errorMessage) {
        val message = medicationUiState.errorMessage ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        medicationViewModel.consumeError()
    }

    LaunchedEffect(hospitalUiState.isSaving) {
        if (hospitalUiState.isSaving && appointmentToDelete != null) {
            wasDeletingAppointment = true
        } else if (!hospitalUiState.isSaving && wasDeletingAppointment) {
            appointmentToDelete = null
            wasDeletingAppointment = false
        }
    }

    when {
        selectedSection == HealthSection.Health && medicationUiState.editorMode != null -> {
            MedicationEditorRoute(
                uiState = medicationUiState,
                onBackClick = medicationViewModel::backFromMedicationEditor,
                onEditClick = medicationViewModel::openEditMedication,
                onSaveClick = medicationViewModel::saveMedication,
                onDeleteClick = medicationViewModel::deleteMedication,
                modifier = modifier,
            )
        }

        selectedSection == HealthSection.Hospital && hospitalUiState.editorMode != null -> {
            HospitalEditorRoute(
                uiState = hospitalUiState,
                specialtyRepository = hospitalSpecialtyRepository,
                onBackClick = hospitalViewModel::closeEditor,
                onSaveClick = hospitalViewModel::saveAppointment,
                onDeleteClick = hospitalViewModel::deleteAppointment,
                modifier = modifier,
            )
        }

        else -> HealthMainScreen(
            selectedSection = selectedSection,
            medicationUiState = medicationUiState,
            hospitalUiState = hospitalUiState,
            onSectionClick = { section ->
                selectedSection = section
                medicationViewModel.backFromMedicationEditor()
                hospitalViewModel.closeEditor()
            },
            onMedicationDateSelected = medicationViewModel::selectDate,
            onAddTodayMedicationClick = medicationViewModel::openAddTodayMedication,
            onAddRegisteredMedicationClick = medicationViewModel::openAddMedication,
            onMedicationClick = medicationViewModel::openMedication,
            onMedicationRefresh = medicationViewModel::refreshMedicationData,
            onHospitalMonthSelected = hospitalViewModel::selectMonth,
            onHospitalDateSelected = hospitalViewModel::selectDate,
            onAddHospitalClick = hospitalViewModel::openAdd,
            onHospitalClick = hospitalViewModel::openView,
            onHospitalEditClick = hospitalViewModel::openEdit,
            onHospitalDeleteClick = { appointmentToDelete = it },
            onHospitalRefresh = hospitalViewModel::refreshHospitalData,
            modifier = modifier,
        )
    }

    appointmentToDelete?.let { appointment ->
        SeniorOnDeleteConfirmDialog(
            title = "${appointment.date.monthValue}월 ${appointment.date.dayOfMonth}일 진료를\n삭제할까요?",
            onCancel = { appointmentToDelete = null },
            onConfirm = {
                hospitalViewModel.deleteAppointment(appointment)
            },
            isConfirmLoading = hospitalUiState.isSaving,
        )
    }
}
