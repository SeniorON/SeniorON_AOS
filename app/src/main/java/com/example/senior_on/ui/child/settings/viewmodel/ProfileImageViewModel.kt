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
    val profileName: String? = null,
    val profileRole: String? = null,
    val profileEmail: String? = null,
    val profileImageUrl: String? = null,
    val profileImageRevision: Long = 0L,
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
    private var hasLoadedSettingsProfile = false

    fun loadSettingsProfile() {
        if (hasLoadedSettingsProfile || _uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadErrorMessage = null,
                )
            }
            runCatching {
                userSettingsRepository.getSettings()
            }.onSuccess { settings ->
                hasLoadedSettingsProfile = true
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profileName = settings.name.takeIf(String::isNotBlank),
                        profileRole = settings.role.takeIf(String::isNotBlank),
                        profileEmail = settings.email.takeIf(String::isNotBlank),
                        profileImageUrl = settings.profileImageUrl
                            ?.takeIf { value -> value.isNotBlank() },
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadErrorMessage = throwable.message
                            ?: "프로필 정보를 불러오지 못했습니다.",
                    )
                }
            }
        }
    }

    fun updateProfileImage(imageUri: String) {
        if (_uiState.value.isUploading) return
        if (imageUri.isBlank()) return
        val previousImageUrl = _uiState.value.profileImageUrl
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUploading = true,
                    uploadErrorMessage = null,
                    profileImageUrl = imageUri,
                    profileImageRevision = it.profileImageRevision + 1,
                )
            }
            runCatching {
                val prepared = photoUploadPreparer.prepareProfileImage(imageUri)
                try {
                    val uploadedUrl = userSettingsRepository.updateProfileImage(prepared)
                    val verifiedUrl = runCatching {
                        userSettingsRepository.getProfileImageUrl()
                    }.getOrNull()
                    verifiedUrl
                        ?.takeIf(String::isNotBlank)
                        ?: uploadedUrl?.takeIf(String::isNotBlank)
                        ?: error("서버에서 프로필 이미지 주소를 받지 못했습니다.")
                } finally {
                    runCatching { prepared.file.delete() }
                }
            }.onSuccess { url ->
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        profileImageUrl = url,
                        profileImageRevision = it.profileImageRevision + 1,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        profileImageUrl = previousImageUrl,
                        profileImageRevision = it.profileImageRevision + 1,
                        uploadErrorMessage = throwable.message
                            ?: "프로필 이미지 변경에 실패했습니다.",
                    )
                }
            }
        }
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
