package com.example.senior_on.ui.parent.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.domain.repository.parent.ChatBuddyRepository
import com.example.senior_on.ui.parent.chat.viewmodel.ChatBuddyViewModel

@Composable
fun ParentChatBuddyRoute(
    repository: ChatBuddyRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ChatBuddyViewModel = viewModel(
        factory = ChatBuddyViewModel.factory(repository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChatBuddyScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onVoiceButtonClick = viewModel::onVoiceButtonClick,
        modifier = modifier,
    )
}
