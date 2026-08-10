package com.example.senior_on.ui.child.family.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.data.local.FamilyPhotoUploadPreparer
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Immutable
data class FamilyPhotoUploadUiState(
    val sessionId: String? = null,
    val photoUri: String? = null,
    val isUploading: Boolean = false,
    val isUploaded: Boolean = false,
    val errorMessage: String? = null,
)

class FamilyPhotoUploadViewModel(
    private val repository: FamilyServerRepository,
    private val uploadPreparer: FamilyPhotoUploadPreparer,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FamilyPhotoUploadUiState())
    val uiState = _uiState.asStateFlow()
    private var uploadIdempotencyKey: String? = null
    private var uploadJob: Job? = null

    fun startUploadSession(sessionId: String, photoUri: String) {
        if (_uiState.value.sessionId == sessionId) return

        uploadJob?.cancel()
        uploadIdempotencyKey = sessionId
        _uiState.value = FamilyPhotoUploadUiState(
            sessionId = sessionId,
            photoUri = photoUri,
        )
    }

    fun uploadPhoto(message: String) {
        val currentState = _uiState.value
        val sessionId = currentState.sessionId ?: return
        val photoUri = currentState.photoUri ?: return
        if (uploadJob?.isActive == true || currentState.isUploading) return
        val idempotencyKey = uploadIdempotencyKey
            ?: sessionId.also { uploadIdempotencyKey = it }

        uploadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUploading = true,
                errorMessage = null,
            )
            var preparedFile: java.io.File? = null
            var discardPreparedFileOnFailure = true
            try {
                val preparedPhoto = uploadPreparer.prepare(photoUri)
                preparedFile = preparedPhoto.file
                repository.uploadPhoto(
                    photo = preparedPhoto,
                    description = message,
                    idempotencyKey = idempotencyKey,
                )
                discardPreparedFileOnFailure = false
                preparedFile.delete()
                try {
                    uploadPreparer.deleteOwnedSource(photoUri)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    // Source cleanup is best-effort after the repository accepted the upload.
                }
                if (_uiState.value.sessionId == sessionId) {
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        isUploaded = true,
                    )
                }
            } catch (exception: CancellationException) {
                if (discardPreparedFileOnFailure) preparedFile?.delete()
                throw exception
            } catch (exception: Exception) {
                if (discardPreparedFileOnFailure) preparedFile?.delete()
                if (_uiState.value.sessionId == sessionId) {
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        errorMessage = "사진을 올리지 못했어요. 다시 시도해 주세요.",
                    )
                }
            }
        }
    }

    fun consumeUploadSuccess(sessionId: String) {
        val currentState = _uiState.value
        if (currentState.sessionId != sessionId || !currentState.isUploaded) return

        uploadIdempotencyKey = null
        _uiState.value = FamilyPhotoUploadUiState()
    }

    companion object {
        fun factory(
            repository: FamilyServerRepository,
            uploadPreparer: FamilyPhotoUploadPreparer,
        ) = viewModelFactory {
            initializer {
                FamilyPhotoUploadViewModel(repository, uploadPreparer)
            }
        }
    }
}
