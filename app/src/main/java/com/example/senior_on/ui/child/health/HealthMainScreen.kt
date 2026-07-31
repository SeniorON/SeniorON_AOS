package com.example.senior_on.ui.child.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.child.health.viewmodel.MedicationUiState
import java.time.LocalDate

enum class HealthSection {
    Health,
    Hospital
}

@Composable
fun HealthMainScreen(
    modifier: Modifier = Modifier,
    initialSection: HealthSection = HealthSection.Health,
    medicationUiState: MedicationUiState = MedicationUiState(),
    onMedicationDateSelected: (LocalDate) -> Unit = {},
    onAddMedicationClick: () -> Unit = {},
    onMedicationClick: (RegisteredMedicationUiState) -> Unit = {},
    onMedicationEditorBackClick: () -> Unit = {},
    onMedicationEditClick: () -> Unit = {},
    onMedicationSaveClick: (MedicationDraft) -> Unit = {},
    onMedicationDeleteClick: () -> Unit = {},
) {
    var selectedSection by rememberSaveable(initialSection) {
        mutableStateOf(initialSection)
    }
    var editorMode by rememberSaveable { mutableStateOf<HospitalEditorMode?>(null) }
    var editorDate by remember { mutableStateOf(LocalDate.of(2026, 6, 17)) }
    var editingAppointment by remember { mutableStateOf<HospitalAppointmentUiState?>(null) }
    var appointmentToDelete by remember { mutableStateOf<HospitalAppointmentUiState?>(null) }
    var appointments by remember { mutableStateOf(previewHospitalAppointments()) }

    fun openAdd(date: LocalDate) {
        editorDate = date
        editingAppointment = null
        editorMode = HospitalEditorMode.Add
    }

    fun openView(appointment: HospitalAppointmentUiState) {
        editorDate = appointment.date
        editingAppointment = appointment
        editorMode = HospitalEditorMode.View
    }

    fun openEdit(appointment: HospitalAppointmentUiState) {
        editorDate = appointment.date
        editingAppointment = appointment
        editorMode = HospitalEditorMode.Edit
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.SupportWhite100)
    ) {
        val activeEditorMode = editorMode
        val activeMedicationMode = medicationUiState.editorMode
        val activeMedication = medicationUiState.editingMedication

        when {
            selectedSection == HealthSection.Hospital && activeEditorMode != null -> {
                HospitalAppointmentScreen(
                    mode = activeEditorMode,
                    initialDate = editorDate,
                    initialDraft = editingAppointment?.toDraft(),
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    onBackClick = { editorMode = null },
                    onSaveClick = { draft ->
                        val previous = editingAppointment
                        val saved = HospitalAppointmentUiState(
                            date = draft.date,
                            hospitalName = draft.hospitalName,
                            specialty = draft.specialty,
                            time = draft.time,
                            reminder = draft.reminder,
                            daysLeft = previous?.daysLeft ?: 0,
                            highlighted = previous?.highlighted ?: false
                        )
                        appointments = if (previous == null) {
                            (appointments + saved).sortedBy { it.date }
                        } else {
                            appointments.map { if (it == previous) saved else it }
                        }
                        editorMode = null
                    },
                    onDeleteClick = {
                        editingAppointment?.let { target ->
                            appointments = appointments.filterNot { it == target }
                        }
                        editorMode = null
                    }
                )
            }

            selectedSection == HealthSection.Health && activeMedicationMode == MedicationEditorMode.Add -> {
                MedicationDetailScreen(
                    mode = MedicationEditorMode.Add,
                    initialDraft = MedicationDraft("", "", emptyList(), emptySet()),
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    onBackClick = onMedicationEditorBackClick,
                    onSaveClick = onMedicationSaveClick,
                )
            }

            selectedSection == HealthSection.Health &&
                activeMedicationMode != null &&
                activeMedication != null -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    key(activeMedicationMode, activeMedication.id, activeMedication) {
                        MedicationDetailScreen(
                            mode = activeMedicationMode,
                            initialDraft = activeMedication.toDraft(),
                            modifier = Modifier.fillMaxSize(),
                            onBackClick = onMedicationEditorBackClick,
                            onEditClick = onMedicationEditClick,
                            onSaveClick = onMedicationSaveClick,
                            onDeleteClick = onMedicationDeleteClick,
                        )
                    }
                }
            }

            else -> {
                HealthSectionHeader(
                    selectedSection = selectedSection,
                    onSectionClick = {
                        selectedSection = it
                        editorMode = null
                        if (activeMedicationMode != null) {
                            onMedicationEditorBackClick()
                        }
                    }
                )

                when (selectedSection) {
                    HealthSection.Health -> HealthScreen(
                        registeredMedications = medicationUiState.registeredMedications,
                        todayMedications = medicationUiState.todayMedications,
                        medicationMarkedDates = medicationUiState.medicationMarkedDates,
                        selectedDate = medicationUiState.selectedDate,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        onSelectedDateChange = onMedicationDateSelected,
                        onAddRegisteredMedicationClick = onAddMedicationClick,
                        onAddTodayMedicationClick = onAddMedicationClick,
                        onRegisteredMedicationClick = onMedicationClick,
                    )
                    HealthSection.Hospital -> HospitalScreen(
                        appointments = appointments,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        onAddAppointmentClick = ::openAdd,
                        onAppointmentClick = ::openView,
                        onEditAppointmentClick = ::openEdit,
                        onDeleteAppointmentClick = { appointmentToDelete = it }
                    )
                }
            }
        }
    }

    appointmentToDelete?.let { appointment ->
        SeniorOnDeleteConfirmDialog(
            title = "${appointment.date.monthValue}월 ${appointment.date.dayOfMonth}일 진료를\n삭제할까요?",
            onCancel = { appointmentToDelete = null },
            onConfirm = {
                appointments = appointments.filterNot { it == appointment }
                appointmentToDelete = null
            }
        )
    }
}

private fun HospitalAppointmentUiState.toDraft() = HospitalAppointmentDraft(
    hospitalName = hospitalName,
    specialty = specialty,
    date = date,
    time = time,
    reminder = reminder
)

@Preview(name = "Health Tab - Health", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun HealthMainHealthPreview() {
    SENIOR_ONTheme {
        HealthMainScreen()
    }
}

@Preview(name = "Health Tab - Hospital", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun HealthMainHospitalPreview() {
    SENIOR_ONTheme {
        HealthMainScreen(initialSection = HealthSection.Hospital)
    }
}
