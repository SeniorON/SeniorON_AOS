package com.example.senior_on.ui.child.health.route

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.example.senior_on.ui.child.health.MedicationDetailScreen
import com.example.senior_on.ui.child.health.MedicationDraft
import com.example.senior_on.ui.child.health.MedicationEditorMode
import com.example.senior_on.ui.child.health.toDraft
import com.example.senior_on.ui.child.health.viewmodel.MedicationUiState

@Composable
internal fun MedicationEditorRoute(
    uiState: MedicationUiState,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: (MedicationDraft) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mode = uiState.editorMode ?: return
    val medication = uiState.editingMedication

    if (mode == MedicationEditorMode.Add) {
        MedicationDetailScreen(
            mode = MedicationEditorMode.Add,
            initialDraft = MedicationDraft(
                category = "",
                name = "",
                times = emptyList(),
                weekdays = emptySet(),
                startDate = uiState.addMedicationStartDate,
            ),
            modifier = modifier,
            isSaving = uiState.isSaving,
            onBackClick = onBackClick,
            onSaveClick = onSaveClick,
        )
        return
    }

    medication ?: return
    Box(modifier = modifier) {
        key(mode, medication.id, medication) {
            MedicationDetailScreen(
                mode = mode,
                initialDraft = medication.toDraft(),
                modifier = Modifier.fillMaxSize(),
                isSaving = uiState.isSaving,
                onBackClick = onBackClick,
                onEditClick = onEditClick,
                onSaveClick = onSaveClick,
                onDeleteClick = onDeleteClick,
            )
        }
    }
}
