package com.example.senior_on.ui.parent.chat

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.domain.model.parent.ChatBuddyAudio
import com.example.senior_on.domain.repository.parent.ChatBuddyRepository
import com.example.senior_on.ui.parent.chat.audio.ChatBuddyAudioPlayer
import com.example.senior_on.ui.parent.chat.audio.ChatBuddyAudioRecorder
import com.example.senior_on.ui.parent.chat.viewmodel.ChatBuddyPhase
import com.example.senior_on.ui.parent.chat.viewmodel.ChatBuddyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val recorder = remember(context) { ChatBuddyAudioRecorder(context.applicationContext) }
    val player = remember(context) { ChatBuddyAudioPlayer(context.applicationContext) }
    var isFinalizingRecording by remember { mutableStateOf(false) }

    val startRecording: () -> Unit = {
        if (uiState.conversationId != null && !uiState.isStartingConversation) {
            try {
                player.release()
                recorder.start()
                viewModel.onRecordingStarted()
            } catch (_: SecurityException) {
                viewModel.onMicrophonePermissionDenied()
            } catch (exception: Exception) {
                viewModel.onAudioFailure(exception.message ?: "녹음을 시작하지 못했어요.")
            }
        }
    }
    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) startRecording() else viewModel.onMicrophonePermissionDenied()
    }

    LaunchedEffect(uiState.responseAudio) {
        val responseAudio = uiState.responseAudio ?: return@LaunchedEffect
        player.play(
            bytes = responseAudio,
            format = uiState.responseAudioFormat,
            contentType = uiState.responseAudioContentType,
            onCompleted = viewModel::onResponsePlaybackCompleted,
            onError = { viewModel.onResponsePlaybackFailed() },
        )
    }

    DisposableEffect(recorder, player) {
        onDispose {
            recorder.cancel()
            player.release()
        }
    }

    val onVoiceButtonClick: () -> Unit = {
        when (uiState.phase) {
            ChatBuddyPhase.Idle -> {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO,
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    startRecording()
                } else {
                    microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }

            ChatBuddyPhase.Listening -> if (!isFinalizingRecording) {
                isFinalizingRecording = true
                coroutineScope.launch {
                    try {
                        val audioFile = recorder.stop()
                        val audio = withContext(Dispatchers.IO) {
                            try {
                                ChatBuddyAudio(
                                    fileName = audioFile.name,
                                    contentType = ChatBuddyAudio.M4A_CONTENT_TYPE,
                                    bytes = audioFile.readBytes(),
                                )
                            } finally {
                                audioFile.delete()
                            }
                        }
                        viewModel.submitRecordedAudio(audio)
                    } catch (exception: Exception) {
                        recorder.cancel()
                        viewModel.onAudioFailure(
                            exception.message ?: "녹음을 완료하지 못했어요. 다시 말씀해 주세요.",
                        )
                    } finally {
                        isFinalizingRecording = false
                    }
                }
            }

            ChatBuddyPhase.Thinking,
            ChatBuddyPhase.Responding -> Unit
        }
    }

    ChatBuddyScreen(
        uiState = uiState,
        onBackClick = {
            recorder.cancel()
            player.release()
            viewModel.endConversation(onBackClick)
        },
        onVoiceButtonClick = onVoiceButtonClick,
        onErrorConsumed = viewModel::consumeError,
        modifier = modifier,
    )
}
