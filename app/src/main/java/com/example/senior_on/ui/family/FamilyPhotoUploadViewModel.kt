package com.example.senior_on.ui.family

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.data.local.FamilyPhotoUploadPreparer
import com.example.senior_on.data.repository.FamilyRepository
import com.example.senior_on.domain.model.FamilyImageSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Immutable
data class FamilyPhotoUploadUiState(
    val photoUri: String? = null,
    val isUploading: Boolean = false,
    val isUploaded: Boolean = false,
    val errorMessage: String? = null,
)

class FamilyPhotoUploadViewModel(
    private val repository: FamilyRepository,
    private val uploadPreparer: FamilyPhotoUploadPreparer,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FamilyPhotoUploadUiState())
    val uiState = _uiState.asStateFlow()

    fun selectPhoto(photoUri: String) {
        if (_uiState.value.photoUri == photoUri && !_uiState.value.isUploaded) return
        _uiState.value = FamilyPhotoUploadUiState(photoUri = photoUri)
    }

    fun uploadPhoto(message: String) {
        val photoUri = _uiState.value.photoUri ?: return
        if (_uiState.value.isUploading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUploading = true,
                errorMessage = null,
            )
            var preparedFile: java.io.File? = null
            var discardPreparedFileOnFailure = true
            try {
                val preparedPhoto = uploadPreparer.prepare(photoUri)
                preparedFile = preparedPhoto.file
                val uploadedPhoto = repository.uploadPhoto(
                    photo = preparedPhoto,
                    message = message,
                )
                discardPreparedFileOnFailure = false
                if (uploadedPhoto.imageSource !is FamilyImageSource.Uri) {
                    preparedFile.delete()
                }
                try {
                    uploadPreparer.deleteOwnedSource(photoUri)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    // Source cleanup is best-effort after the repository accepted the upload.
                }
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    isUploaded = true,
                )
            } catch (exception: CancellationException) {
                if (discardPreparedFileOnFailure) preparedFile?.delete()
                throw exception
            } catch (exception: Exception) {
                if (discardPreparedFileOnFailure) preparedFile?.delete()
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    errorMessage = "사진을 올리지 못했어요. 다시 시도해 주세요.",
                )
            }
        }
    }

    companion object {
        fun factory(
            repository: FamilyRepository,
            uploadPreparer: FamilyPhotoUploadPreparer,
        ) = viewModelFactory {
            initializer {
                FamilyPhotoUploadViewModel(repository, uploadPreparer)
            }
        }
    }
}
