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
import java.time.LocalDate

enum class HealthSection {
    Health,
    Hospital
}

@Composable
fun HealthMainScreen(
    modifier: Modifier = Modifier,
    initialSection: HealthSection = HealthSection.Health
) {
    var selectedSection by rememberSaveable(initialSection) {
        mutableStateOf(initialSection)
    }
    var editorMode by rememberSaveable { mutableStateOf<HospitalEditorMode?>(null) }
    var editorDate by remember { mutableStateOf(LocalDate.of(2026, 6, 17)) }
    var editingAppointment by remember { mutableStateOf<HospitalAppointmentUiState?>(null) }
    var appointmentToDelete by remember { mutableStateOf<HospitalAppointmentUiState?>(null) }
    var appointments by remember { mutableStateOf(previewHospitalAppointments()) }

    var medicationEditorMode by rememberSaveable { mutableStateOf<MedicationEditorMode?>(null) }
    var editingMedication by remember { mutableStateOf<RegisteredMedicationUiState?>(null) }
    var medications by remember { mutableStateOf(previewRegisteredMedications()) }

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

    fun openMedicationView(medication: RegisteredMedicationUiState) {
        editingMedication = medication
        medicationEditorMode = MedicationEditorMode.View
    }

    fun openMedicationAdd() {
        editingMedication = null
        medicationEditorMode = MedicationEditorMode.Add
    }

    fun closeMedicationEditor() {
        medicationEditorMode = null
        editingMedication = null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.SupportWhite100)
    ) {
        val activeEditorMode = editorMode
        val activeMedicationMode = medicationEditorMode
        val activeMedication = editingMedication

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
                    onBackClick = ::closeMedicationEditor,
                    onSaveClick = { draft ->
                        val newMedication = RegisteredMedicationUiState(
                            id = (medications.size + 1).toString(),
                            category = draft.category,
                            name = draft.name,
                            times = draft.times,
                            weekdays = draft.weekdays
                        )
                        medications = medications + newMedication
                        closeMedicationEditor()
                    }
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
                            onBackClick = {
                                when (activeMedicationMode) {
                                    MedicationEditorMode.Add -> closeMedicationEditor()
                                    MedicationEditorMode.Edit -> {
                                        medicationEditorMode = MedicationEditorMode.View
                                    }
                                    MedicationEditorMode.View -> closeMedicationEditor()
                                }
                            },
                            onEditClick = {
                                medicationEditorMode = MedicationEditorMode.Edit
                            },
                            onSaveClick = { draft ->
                                val saved = activeMedication.copy(
                                    category = draft.category,
                                    name = draft.name,
                                    times = draft.times,
                                    weekdays = draft.weekdays
                                )
                                medications = medications.map {
                                    if (it.id == activeMedication.id) saved else it
                                }
                                editingMedication = saved
                                medicationEditorMode = MedicationEditorMode.View
                            },
                            onDeleteClick = {
                                medications = medications.filterNot { it.id == activeMedication.id }
                                closeMedicationEditor()
                            }
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
                        closeMedicationEditor()
                    }
                )

                when (selectedSection) {
                    HealthSection.Health -> HealthScreen(
                        registeredMedications = medications,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        onAddRegisteredMedicationClick = ::openMedicationAdd,
                        onAddTodayMedicationClick = ::openMedicationAdd,
                        onRegisteredMedicationClick = ::openMedicationView
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
