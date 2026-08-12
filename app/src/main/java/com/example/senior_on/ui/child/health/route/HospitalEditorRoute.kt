package com.example.senior_on.ui.child.health.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.senior_on.ui.child.health.HospitalAppointmentDraft
import com.example.senior_on.ui.child.health.HospitalAppointmentScreen
import com.example.senior_on.ui.child.health.HospitalAppointmentUiState
import com.example.senior_on.ui.child.health.viewmodel.HospitalUiState
import com.example.senior_on.domain.repository.health.HospitalSpecialtyRepository

@Composable
internal fun HospitalEditorRoute(
    uiState: HospitalUiState,
    specialtyRepository: HospitalSpecialtyRepository,
    onBackClick: () -> Unit,
    onSaveClick: (HospitalAppointmentDraft) -> Unit,
    onDeleteClick: (HospitalAppointmentUiState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val mode = uiState.editorMode ?: return
    val appointment = uiState.editingAppointment

    HospitalAppointmentScreen(
        mode = mode,
        initialDate = uiState.editorDate,
        initialDraft = appointment?.toDraft(),
        specialtyRepository = specialtyRepository,
        modifier = modifier,
        onBackClick = onBackClick,
        onSaveClick = onSaveClick,
        onDeleteClick = { appointment?.let(onDeleteClick) },
    )
}

private fun HospitalAppointmentUiState.toDraft() = HospitalAppointmentDraft(
    hospitalName = hospitalName,
    specialty = specialty,
    date = date,
    time = time,
    reminder = reminder,
)
