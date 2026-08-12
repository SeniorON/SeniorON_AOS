package com.example.senior_on.ui.child.health

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.senior_on.ui.child.health.viewmodel.HospitalUiState
import com.example.senior_on.ui.child.health.viewmodel.MedicationUiState
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import java.time.LocalDate
import java.time.YearMonth

enum class HealthSection {
    Health,
    Hospital
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthMainScreen(
    modifier: Modifier = Modifier,
    initialSection: HealthSection = HealthSection.Health,
    medicationUiState: MedicationUiState = MedicationUiState(),
    hospitalUiState: HospitalUiState = HospitalUiState(),
    onMedicationDateSelected: (LocalDate) -> Unit = {},
    onAddMedicationClick: () -> Unit = {},
    onMedicationClick: (RegisteredMedicationUiState) -> Unit = {},
    onMedicationEditorBackClick: () -> Unit = {},
    onMedicationEditClick: () -> Unit = {},
    onMedicationSaveClick: (MedicationDraft) -> Unit = {},
    onMedicationDeleteClick: () -> Unit = {},
    onConsumeMedicationError: () -> Unit = {},
    onMedicationRefresh: () -> Unit = {},
    onHospitalMonthSelected: (YearMonth) -> Unit = {},
    onHospitalDateSelected: (LocalDate) -> Unit = {},
    onAddHospitalClick: (LocalDate) -> Unit = {},
    onHospitalClick: (HospitalAppointmentUiState) -> Unit = {},
    onHospitalEditClick: (HospitalAppointmentUiState) -> Unit = {},
    onHospitalSaveClick: (HospitalAppointmentDraft) -> Unit = {},
    onHospitalDeleteClick: (HospitalAppointmentUiState) -> Unit = {},
    onHospitalEditorBackClick: () -> Unit = {},
    onConsumeHospitalError: () -> Unit = {},
    onHospitalRefresh: () -> Unit = {},
) {
    var selectedSection by rememberSaveable(initialSection) {
        mutableStateOf(initialSection)
    }
    var appointmentToDelete by remember { mutableStateOf<HospitalAppointmentUiState?>(null) }
    val context = LocalContext.current

    LaunchedEffect(hospitalUiState.errorMessage) {
        val message = hospitalUiState.errorMessage ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        onConsumeHospitalError()
    }

    LaunchedEffect(medicationUiState.errorMessage) {
        val message = medicationUiState.errorMessage ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        onConsumeMedicationError()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.SupportWhite100)
    ) {
        val activeHospitalMode = hospitalUiState.editorMode
        val activeMedicationMode = medicationUiState.editorMode
        val activeMedication = medicationUiState.editingMedication
        val activeHospital = hospitalUiState.editingAppointment

        when {
            selectedSection == HealthSection.Hospital && activeHospitalMode != null -> {
                HospitalAppointmentScreen(
                    mode = activeHospitalMode,
                    initialDate = hospitalUiState.editorDate,
                    initialDraft = activeHospital?.toDraft(),
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    onBackClick = onHospitalEditorBackClick,
                    onSaveClick = onHospitalSaveClick,
                    onDeleteClick = {
                        activeHospital?.let(onHospitalDeleteClick)
                    },
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
                        if (activeHospitalMode != null) {
                            onHospitalEditorBackClick()
                        }
                        if (activeMedicationMode != null) {
                            onMedicationEditorBackClick()
                        }
                    }
                )

                when (selectedSection) {
                    HealthSection.Health -> PullToRefreshBox(
                        isRefreshing = medicationUiState.isRefreshing,
                        onRefresh = onMedicationRefresh,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    ) {
                        HealthScreen(
                            registeredMedications = medicationUiState.registeredMedications,
                            todayMedications = medicationUiState.todayMedications,
                            medicationMarkedDates = medicationUiState.medicationMarkedDates,
                            selectedDate = medicationUiState.selectedDate,
                            modifier = Modifier.fillMaxSize(),
                            onSelectedDateChange = onMedicationDateSelected,
                            onAddRegisteredMedicationClick = onAddMedicationClick,
                            onAddTodayMedicationClick = onAddMedicationClick,
                            onRegisteredMedicationClick = onMedicationClick,
                        )
                    }
                    HealthSection.Hospital -> PullToRefreshBox(
                        isRefreshing = hospitalUiState.isRefreshing,
                        onRefresh = onHospitalRefresh,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    ) {
                        HospitalScreen(
                            appointments = hospitalUiState.monthlyAppointments,
                            upcomingAppointments = hospitalUiState.upcomingAppointments,
                            displayedMonth = hospitalUiState.displayedMonth,
                            selectedDate = hospitalUiState.selectedDate,
                            modifier = Modifier.fillMaxSize(),
                            onDisplayedMonthChange = onHospitalMonthSelected,
                            onSelectedDateChange = onHospitalDateSelected,
                            onAddAppointmentClick = onAddHospitalClick,
                            onAppointmentClick = onHospitalClick,
                            onEditAppointmentClick = onHospitalEditClick,
                            onDeleteAppointmentClick = { appointmentToDelete = it },
                        )
                    }
                }
            }
        }
    }

    appointmentToDelete?.let { appointment ->
        SeniorOnDeleteConfirmDialog(
            title = "${appointment.date.monthValue}월 ${appointment.date.dayOfMonth}일 진료를\n삭제할까요?",
            onCancel = { appointmentToDelete = null },
            onConfirm = {
                onHospitalDeleteClick(appointment)
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
    reminder = reminder,
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
