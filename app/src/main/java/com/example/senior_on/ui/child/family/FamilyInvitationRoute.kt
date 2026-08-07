package com.example.senior_on.ui.child.family

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import com.example.senior_on.ui.child.family.viewmodel.FamilyInvitationViewModel
import com.example.senior_on.ui.common.share.KakaoShareLauncher
import com.example.senior_on.ui.common.share.MessageShareLauncher
import com.example.senior_on.ui.common.share.ShareLaunchResult

@Composable
fun FamilyInvitationRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    repository: FamilyServerRepository,
    viewModelKey: String,
) {
    val context = LocalContext.current
    val viewModel: FamilyInvitationViewModel = viewModel(
        key = viewModelKey,
        factory = FamilyInvitationViewModel.factory(repository),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.refresh()
    }

    FamilyInvitationScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onKakaoShareClick = {
            KakaoShareLauncher.launch(
                context = context,
                content = familyInvitationShareContent(uiState.invitationCode),
                onResult = { result ->
                    if (result is ShareLaunchResult.Failure) {
                        Toast.makeText(
                            context,
                            result.userMessage,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
            )
        },
        onMessageShareClick = {
            val result = MessageShareLauncher.launch(
                context = context,
                content = familyInvitationShareContent(uiState.invitationCode),
            )
            if (result is ShareLaunchResult.Failure) {
                Toast.makeText(
                    context,
                    result.userMessage,
                    Toast.LENGTH_SHORT,
                ).show()
            }
        },
        onRetryClick = viewModel::retry,
        modifier = modifier,
    )
}
