package com.example.senior_on.ui.child.health

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.common.component.SeniorOnLoadingIndicator
import com.example.senior_on.ui.child.health.viewmodel.HospitalUiState
import com.example.senior_on.ui.child.health.viewmodel.MedicationUiState
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.delay

enum class HealthSection {
    Health,
    Hospital,
}

/**
 * 건강 탭의 메인 목록만 그리는 화면입니다.
 * 편집 화면 전환, 오류 처리, 삭제 확인은 Route 계층에서 담당합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthMainScreen(
    selectedSection: HealthSection = HealthSection.Health,
    medicationUiState: MedicationUiState = MedicationUiState(),
    hospitalUiState: HospitalUiState = HospitalUiState(),
    onSectionClick: (HealthSection) -> Unit = {},
    onMedicationDateSelected: (LocalDate) -> Unit = {},
    onAddTodayMedicationClick: () -> Unit = {},
    onAddRegisteredMedicationClick: () -> Unit = {},
    onMedicationClick: (RegisteredMedicationUiState) -> Unit = {},
    onMedicationRefresh: () -> Unit = {},
    onHospitalMonthSelected: (YearMonth) -> Unit = {},
    onHospitalDateSelected: (LocalDate) -> Unit = {},
    onAddHospitalClick: (LocalDate) -> Unit = {},
    onHospitalClick: (HospitalAppointmentUiState) -> Unit = {},
    onHospitalEditClick: (HospitalAppointmentUiState) -> Unit = {},
    onHospitalDeleteClick: (HospitalAppointmentUiState) -> Unit = {},
    onHospitalRefresh: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.SupportWhite100),
    ) {
        HealthSectionHeader(
            selectedSection = selectedSection,
            onSectionClick = onSectionClick,
        )

        when (selectedSection) {
            HealthSection.Health -> PullToRefreshBox(
                isRefreshing = medicationUiState.isRefreshing,
                onRefresh = onMedicationRefresh,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                HealthLoadingContainer(isLoading = medicationUiState.isLoading) {
                    HealthScreen(
                        registeredMedications = medicationUiState.registeredMedications,
                        todayMedications = medicationUiState.todayMedications,
                        medicationMarkedDates = medicationUiState.medicationMarkedDates,
                        selectedDate = medicationUiState.selectedDate,
                        modifier = Modifier.fillMaxSize(),
                        onSelectedDateChange = onMedicationDateSelected,
                        onAddRegisteredMedicationClick = onAddRegisteredMedicationClick,
                        onAddTodayMedicationClick = onAddTodayMedicationClick,
                        onRegisteredMedicationClick = onMedicationClick,
                    )
                }
            }

            HealthSection.Hospital -> PullToRefreshBox(
                isRefreshing = hospitalUiState.isRefreshing,
                onRefresh = onHospitalRefresh,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                HealthLoadingContainer(isLoading = hospitalUiState.isLoading) {
                    HospitalScreen(
                        appointments = hospitalUiState.monthlyAppointments,
                        selectedAppointments = hospitalUiState.selectedDateAppointments,
                        upcomingAppointments = hospitalUiState.upcomingAppointments,
                        displayedMonth = hospitalUiState.displayedMonth,
                        selectedDate = hospitalUiState.selectedDate,
                        modifier = Modifier.fillMaxSize(),
                        onDisplayedMonthChange = onHospitalMonthSelected,
                        onSelectedDateChange = onHospitalDateSelected,
                        onAddAppointmentClick = onAddHospitalClick,
                        onAppointmentClick = onHospitalClick,
                        onEditAppointmentClick = onHospitalEditClick,
                        onDeleteAppointmentClick = onHospitalDeleteClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthLoadingContainer(
    isLoading: Boolean,
    content: @Composable () -> Unit,
) {
    var showIndicator by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading) {
        showIndicator = false
        if (isLoading) {
            delay(300)
            showIndicator = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        if (showIndicator) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SeniorOnColors.SupportWhite100),
                contentAlignment = Alignment.Center,
            ) {
                SeniorOnLoadingIndicator(
                    color = SeniorOnColors.Primary600,
                    size = 32.dp,
                    strokeWidth = 3.dp,
                )
            }
        }
    }
}

@Preview(name = "Health Tab - Medication", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun HealthMainMedicationPreview() {
    SENIOR_ONTheme {
        HealthMainScreen(
            selectedSection = HealthSection.Health,
            medicationUiState = MedicationUiState(),
            hospitalUiState = HospitalUiState(),
            onSectionClick = {},
            onMedicationDateSelected = {},
            onAddTodayMedicationClick = {},
            onAddRegisteredMedicationClick = {},
            onMedicationClick = {},
            onMedicationRefresh = {},
            onHospitalMonthSelected = {},
            onHospitalDateSelected = {},
            onAddHospitalClick = {},
            onHospitalClick = {},
            onHospitalEditClick = {},
            onHospitalDeleteClick = {},
            onHospitalRefresh = {},
        )
    }
}

@Preview(name = "Health Tab - Hospital", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun HealthMainHospitalPreview() {
    SENIOR_ONTheme {
        HealthMainScreen(
            selectedSection = HealthSection.Hospital,
            medicationUiState = MedicationUiState(),
            hospitalUiState = HospitalUiState(),
            onSectionClick = {},
            onMedicationDateSelected = {},
            onAddTodayMedicationClick = {},
            onAddRegisteredMedicationClick = {},
            onMedicationClick = {},
            onMedicationRefresh = {},
            onHospitalMonthSelected = {},
            onHospitalDateSelected = {},
            onAddHospitalClick = {},
            onHospitalClick = {},
            onHospitalEditClick = {},
            onHospitalDeleteClick = {},
            onHospitalRefresh = {},
        )
    }
}
