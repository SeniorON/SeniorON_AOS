package com.example.senior_on.ui.family

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.data.repository.FamilyRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Immutable
data class FamilyPhotoDetailUiState(
    val photo: SharedFamilyPhotoUiModel? = null,
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val deletedPhotoId: String? = null,
    val errorMessage: String? = null,
)

class FamilyPhotoDetailViewModel(
    private val repository: FamilyRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FamilyPhotoDetailUiState())
    val uiState = _uiState.asStateFlow()
    private var loadPhotoJob: Job? = null

    fun loadPhoto(photoId: String) {
        if (_uiState.value.photo?.id == photoId) return

        loadPhotoJob?.cancel()
        loadPhotoJob = viewModelScope.launch {
            _uiState.value = FamilyPhotoDetailUiState(isLoading = true)

            try {
                val photo = repository.getSharedPhoto(photoId)
                _uiState.value = if (photo == null) {
                    FamilyPhotoDetailUiState(
                        errorMessage = "사진을 불러오지 못했어요."
                    )
                } else {
                    FamilyPhotoDetailUiState(
                        photo = photo.toFamilyPhotoUiModel()
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = FamilyPhotoDetailUiState(
                    errorMessage = "사진을 불러오지 못했어요."
                )
            }
        }
    }

    fun deletePhoto() {
        val photoId = _uiState.value.photo?.id ?: return
        if (_uiState.value.isDeleting) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDeleting = true,
                errorMessage = null,
            )
            try {
                repository.deletePhoto(photoId)
                _uiState.value = FamilyPhotoDetailUiState(deletedPhotoId = photoId)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = "사진을 삭제하지 못했어요.",
                )
            }
        }
    }

    companion object {
        fun factory(repository: FamilyRepository) = viewModelFactory {
            initializer {
                FamilyPhotoDetailViewModel(repository)
            }
        }
    }
}
