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
import com.example.senior_on.ui.child.family.viewmodel.SeniorConnectionUiState
import com.example.senior_on.ui.child.family.viewmodel.SeniorConnectionViewModel

internal enum class SeniorConnectionDestination { Intro, CodeInput, Connected }

@Composable
fun SeniorConnectionRoute(
    seniorId: Long,
    canManageConnections: Boolean,
    onBackClick: () -> Unit,
    onConnectionsChanged: () -> Unit = {},
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
    LaunchedEffect(canManageConnections) {
        if (!canManageConnections) {
            pendingDisconnectSenior = null
            if (destination == SeniorConnectionDestination.CodeInput) {
                destination = if (uiState.connectedSeniors.isEmpty()) {
                    SeniorConnectionDestination.Intro
                } else {
                    SeniorConnectionDestination.Connected
                }
            }
        }
    }
    val resolvedDestination = resolveSeniorConnectionDestination(
        requestedDestination = destination,
        uiState = uiState,
        seniorId = seniorId,
    )

    when {
        resolvedDestination == null &&
            uiState.seniorId == seniorId &&
            uiState.loadErrorMessage != null -> SeniorConnectionErrorScreen(
            message = uiState.loadErrorMessage.orEmpty(),
            onBackClick = onBackClick,
            onRetryClick = { viewModel.loadConnections(seniorId, force = true) },
            modifier = modifier,
        )
        resolvedDestination == null -> SeniorConnectionLoadingScreen(
            onBackClick = onBackClick,
            modifier = modifier,
        )
        resolvedDestination == SeniorConnectionDestination.CodeInput -> SeniorCodeInputScreen(
            onBackClick = {
                viewModel.clearConnectError()
                destination = if (uiState.connectedSeniors.isEmpty()) {
                    SeniorConnectionDestination.Intro
                } else {
                    SeniorConnectionDestination.Connected
                }
            },
            onConnectClick = { code ->
                if (canManageConnections) {
                    viewModel.connect(seniorId, code) {
                        onConnectionsChanged()
                        destination = SeniorConnectionDestination.Connected
                    }
                }
            },
            isConnecting = uiState.isConnecting,
            errorMessage = uiState.connectErrorMessage,
            onCodeChange = viewModel::clearConnectError,
            modifier = modifier,
        )
        resolvedDestination == SeniorConnectionDestination.Connected -> ConnectedSeniorListScreen(
            seniors = uiState.connectedSeniors,
            canManageConnections = canManageConnections,
            onBackClick = onBackClick,
            onAddClick = {
                if (canManageConnections) {
                    destination = SeniorConnectionDestination.CodeInput
                }
            },
            onDisconnectClick = { senior ->
                if (canManageConnections) pendingDisconnectSenior = senior
            },
            modifier = modifier,
        )
        else -> SeniorConnectionScreen(
            onBackClick = onBackClick,
            canManageConnections = canManageConnections,
            onCodeEntryClick = {
                if (canManageConnections) {
                    destination = SeniorConnectionDestination.CodeInput
                }
            },
            modifier = modifier,
        )
    }

    pendingDisconnectSenior?.takeIf { canManageConnections }?.let { senior ->
        SeniorDisconnectConfirmationDialog(
            senior = senior,
            isDisconnecting = uiState.disconnectingPhotoGroupId == senior.photoGroupId,
            onDismiss = { pendingDisconnectSenior = null },
            onConfirm = {
                viewModel.disconnect(
                    seniorId = seniorId,
                    photoGroupId = senior.photoGroupId,
                    onSuccess = { hasRemainingConnections ->
                        onConnectionsChanged()
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

internal fun resolveSeniorConnectionDestination(
    requestedDestination: SeniorConnectionDestination?,
    uiState: SeniorConnectionUiState,
    seniorId: Long,
): SeniorConnectionDestination? {
    if (requestedDestination != null) return requestedDestination
    if (
        uiState.seniorId != seniorId ||
        !uiState.hasLoaded ||
        uiState.isLoading ||
        uiState.loadErrorMessage != null
    ) {
        return null
    }
    return if (uiState.connectedSeniors.isEmpty()) {
        SeniorConnectionDestination.Intro
    } else {
        SeniorConnectionDestination.Connected
    }
}
