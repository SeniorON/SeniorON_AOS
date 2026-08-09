package com.example.senior_on.ui.child.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.senior_on.data.local.FamilyPhotoUploadPreparer
import com.example.senior_on.domain.repository.server.UserSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileImageUiState(
    val profileImageUrl: String? = null,
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val loadErrorMessage: String? = null,
    val uploadErrorMessage: String? = null,
)

class ProfileImageViewModel(
    private val userSettingsRepository: UserSettingsRepository,
    private val photoUploadPreparer: FamilyPhotoUploadPreparer,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileImageUiState())
    val uiState: StateFlow<ProfileImageUiState> = _uiState.asStateFlow()

    fun loadProfileImage() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadErrorMessage = null,
                )
            }
            runCatching {
                userSettingsRepository.getProfileImageUrl()
            }.onSuccess { url ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profileImageUrl = url?.takeIf { value -> value.isNotBlank() },
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadErrorMessage = throwable.message
                            ?: "프로필 이미지를 불러오지 못했습니다.",
                    )
                }
            }
        }
    }

    fun updateProfileImage(imageUri: String) {
        if (_uiState.value.isUploading) return
        if (imageUri.isBlank()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUploading = true,
                    uploadErrorMessage = null,
                )
            }
            runCatching {
                val prepared = photoUploadPreparer.prepare(imageUri)
                try {
                    userSettingsRepository.updateProfileImage(prepared)
                } finally {
                    runCatching { prepared.file.delete() }
                }
            }.onSuccess { url ->
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        profileImageUrl = url?.takeIf { value -> value.isNotBlank() }
                            ?: it.profileImageUrl,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        uploadErrorMessage = throwable.message
                            ?: "프로필 이미지 변경에 실패했습니다.",
                    )
                }
            }
        }
    }

    fun clearLocalProfileImage() {
        _uiState.update { it.copy(profileImageUrl = null) }
    }

    fun consumeLoadError() {
        _uiState.update { it.copy(loadErrorMessage = null) }
    }

    fun consumeUploadError() {
        _uiState.update { it.copy(uploadErrorMessage = null) }
    }

    companion object {
        fun factory(
            userSettingsRepository: UserSettingsRepository,
            photoUploadPreparer: FamilyPhotoUploadPreparer,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ProfileImageViewModel(
                    userSettingsRepository = userSettingsRepository,
                    photoUploadPreparer = photoUploadPreparer,
                ) as T
        }
    }
}
