@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.senior_on.ui.parent.medication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.domain.model.parent.ParentMedication
import com.example.senior_on.ui.parent.component.ParentDetailTopBar
import com.example.senior_on.ui.parent.medication.viewmodel.ParentMedicationContent
import com.example.senior_on.ui.parent.medication.viewmodel.ParentMedicationMessageType
import com.example.senior_on.ui.parent.medication.viewmodel.ParentMedicationUiState
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.Instant
import java.time.LocalTime

@Composable
fun ParentMedicationScreen(
    uiState: ParentMedicationUiState,
    onBackClick: () -> Unit,
    onTakenClick: (String) -> Unit,
    onRefresh: () -> Unit = {},
    onMessageConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onMessageConsumed()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Background1),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SeniorOnColors.SupportWhite100)
                    .statusBarsPadding(),
            ) {
                ParentDetailTopBar(
                    title = "복약",
                    onBackClick = onBackClick,
                )
            }

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                when (uiState.content) {
                    ParentMedicationContent.Loading -> ParentMedicationLoadingContent()
                    ParentMedicationContent.Empty -> ParentMedicationEmptyContent(
                        onBackClick = onBackClick,
                    )
                    ParentMedicationContent.Completed -> ParentMedicationCompletedContent(
                        onBackClick = onBackClick,
                    )
                    ParentMedicationContent.List -> ParentMedicationListContent(
                        uiState = uiState,
                        onTakenClick = onTakenClick,
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(
                    if (uiState.messageType == ParentMedicationMessageType.OutsideTakingWindow) {
                        Alignment.Center
                    } else {
                        Alignment.BottomCenter
                    },
                )
                .padding(horizontal = 16.dp, vertical = 30.5.dp),
        ) { data ->
            if (uiState.messageType == ParentMedicationMessageType.OutsideTakingWindow) {
                ParentMedicationTakingWindowWarning(message = data.visuals.message)
            } else {
                Snackbar(containerColor = SeniorOnColors.Gray700) {
                    Text(
                        text = data.visuals.message,
                        style = SeniorOnTextStyles.HeadingL,
                        color = SeniorOnColors.SupportWhite100,
                    )
                }
            }
        }
    }
}

@Composable
private fun ParentMedicationTakingWindowWarning(
    message: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .background(Color(0xE54B4B4B))
            .padding(horizontal = 12.dp, vertical = 30.5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_big_alert_filled),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = SeniorOnColors.SupportWhite100,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message,
            style = SeniorOnTextStyles.HeadingL,
            color = SeniorOnColors.SupportWhite100,
        )
    }
}

@Composable
private fun ParentMedicationLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = SeniorOnColors.Primary600)
    }
}

@Composable
private fun ParentMedicationListContent(
    uiState: ParentMedicationUiState,
    onTakenClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 24.dp,
            end = 16.dp,
            bottom = 32.dp,
        ),
    ) {
        item {
            ParentMedicationSectionTitle()
            Spacer(modifier = Modifier.height(12.dp))
        }
        items(
            items = uiState.medications,
            key = ParentMedication::id,
        ) { medication ->
            ParentMedicationCard(
                medication = medication,
                highlighted = medication.id == uiState.highlightedMedicationId,
                isSubmitting = medication.id == uiState.submittingMedicationId,
                onTakenClick = { onTakenClick(medication.id) },
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(
    name = "Parent medication - Taking window warning",
    showBackground = true,
    backgroundColor = 0xFFF7F8F5,
    widthDp = 360,
    heightDp = 140,
)
@Composable
private fun ParentMedicationTakingWindowWarningPreview() {
    SENIOR_ONTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            ParentMedicationTakingWindowWarning(
                message = "복용 시간이 아니에요.",
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ParentMedicationListPreview() {
    SENIOR_ONTheme {
        ParentMedicationScreen(
            uiState = ParentMedicationUiState(
                content = ParentMedicationContent.List,
                medications = listOf(
                    ParentMedication("1", "혈압약", LocalTime.of(8, 0), Instant.EPOCH),
                    ParentMedication("2", "혈압약", LocalTime.of(14, 0)),
                    ParentMedication("3", "당뇨약", LocalTime.of(19, 0)),
                ),
                highlightedMedicationId = "2",
            ),
            onBackClick = {},
            onTakenClick = {},
            onMessageConsumed = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ParentMedicationEmptyPreview() {
    SENIOR_ONTheme {
        ParentMedicationScreen(
            uiState = ParentMedicationUiState(content = ParentMedicationContent.Empty),
            onBackClick = {},
            onTakenClick = {},
            onMessageConsumed = {},
        )
    }
}
