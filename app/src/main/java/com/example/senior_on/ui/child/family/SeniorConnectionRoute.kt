package com.example.senior_on.ui.child.family

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.senior_on.domain.model.server.ServerConnectedSenior
import com.example.senior_on.ui.child.family.viewmodel.SeniorConnectionViewModel

private enum class SeniorConnectionDestination { Intro, CodeInput, Connected }

@Composable
fun SeniorConnectionRoute(
    seniorId: Long,
    onBackClick: () -> Unit,
    viewModel: SeniorConnectionViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var destination by remember(seniorId) {
        mutableStateOf<SeniorConnectionDestination?>(null)
    }
    var pendingDisconnectSenior by remember(seniorId) {
        mutableStateOf<ServerConnectedSenior?>(null)
    }

    LaunchedEffect(seniorId) {
        viewModel.loadConnections(seniorId)
    }
    LaunchedEffect(uiState.hasLoaded, uiState.connectedSeniors, seniorId) {
        if (uiState.hasLoaded && uiState.seniorId == seniorId && destination == null) {
            destination = if (uiState.connectedSeniors.isEmpty()) {
                SeniorConnectionDestination.Intro
            } else {
                SeniorConnectionDestination.Connected
            }
        }
    }

    when {
        uiState.isLoading && destination == null -> SeniorConnectionLoadingScreen(
            onBackClick = onBackClick,
            modifier = modifier,
        )
        uiState.loadErrorMessage != null && destination == null -> SeniorConnectionErrorScreen(
            message = uiState.loadErrorMessage.orEmpty(),
            onBackClick = onBackClick,
            onRetryClick = { viewModel.loadConnections(seniorId, force = true) },
            modifier = modifier,
        )
        destination == SeniorConnectionDestination.CodeInput -> SeniorCodeInputScreen(
            onBackClick = {
                viewModel.clearConnectError()
                destination = if (uiState.connectedSeniors.isEmpty()) {
                    SeniorConnectionDestination.Intro
                } else {
                    SeniorConnectionDestination.Connected
                }
            },
            onConnectClick = { code ->
                viewModel.connect(seniorId, code) {
                    destination = SeniorConnectionDestination.Connected
                }
            },
            isConnecting = uiState.isConnecting,
            errorMessage = uiState.connectErrorMessage,
            onCodeChange = viewModel::clearConnectError,
            modifier = modifier,
        )
        destination == SeniorConnectionDestination.Connected -> ConnectedSeniorListScreen(
            seniors = uiState.connectedSeniors,
            onBackClick = onBackClick,
            onAddClick = { destination = SeniorConnectionDestination.CodeInput },
            onDisconnectClick = { senior -> pendingDisconnectSenior = senior },
            modifier = modifier,
        )
        else -> SeniorConnectionScreen(
            onBackClick = onBackClick,
            onCodeEntryClick = { destination = SeniorConnectionDestination.CodeInput },
            modifier = modifier,
        )
    }

    pendingDisconnectSenior?.let { senior ->
        SeniorDisconnectConfirmationDialog(
            senior = senior,
            isDisconnecting = uiState.disconnectingPhotoGroupId == senior.photoGroupId,
            onDismiss = { pendingDisconnectSenior = null },
            onConfirm = {
                viewModel.disconnect(
                    seniorId = seniorId,
                    photoGroupId = senior.photoGroupId,
                    onSuccess = { hasRemainingConnections ->
                        pendingDisconnectSenior = null
                        if (!hasRemainingConnections) {
                            destination = SeniorConnectionDestination.Intro
                        }
                    },
                    onError = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    },
                )
            },
        )
    }
}
