package com.example.senior_on.ui.parent.photo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.repository.parent.ParentFamilyPhotoRepository
import com.example.senior_on.domain.model.family.FamilyImageSource
import com.example.senior_on.domain.model.parent.ParentFamilyPhoto
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ParentFamilyPhotoUiModel(
    val id: String,
    val memberId: String,
    val memberName: String,
    val uploadedAt: Instant,
    val imageSource: FamilyImageSource,
    val message: String,
    val isNew: Boolean
)

data class ParentPhotoMemberUiModel(
    val memberId: String,
    val memberName: String,
    val photos: List<ParentFamilyPhotoUiModel>
) {
    val latestPhoto: ParentFamilyPhotoUiModel?
        get() = photos.firstOrNull()
}

data class ParentFamilyPhotoUiState(
    val members: List<ParentPhotoMemberUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class ParentFamilyPhotoViewModel(
    private val repository: ParentFamilyPhotoRepository,
    private val familyCode: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(ParentFamilyPhotoUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadFamilyPhotos()
    }

    fun loadFamilyPhotos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching { repository.getFamilyPhotos(familyCode) }
                .onSuccess { collections ->
                    val now = Instant.now()
                    val members = collections
                        .map { collection ->
                            ParentPhotoMemberUiModel(
                                memberId = collection.memberId,
                                memberName = collection.memberName,
                                photos = collection.photos
                                    .sortedByDescending(ParentFamilyPhoto::uploadedAt)
                                    .map { it.toUiModel(now) }
                            )
                        }
                        .filter { it.photos.isNotEmpty() }
                        .sortedByDescending { it.latestPhoto?.uploadedAt }

                    _uiState.update {
                        it.copy(members = members, isLoading = false)
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            members = emptyList(),
                            isLoading = false,
                            errorMessage = "가족사진을 불러오지 못했어요."
                        )
                    }
                }
        }
    }

    companion object {
        fun factory(
            repository: ParentFamilyPhotoRepository,
            familyCode: String
        ) = viewModelFactory {
            initializer {
                ParentFamilyPhotoViewModel(repository, familyCode)
            }
        }
    }
}

private fun ParentFamilyPhoto.toUiModel(now: Instant) =
    ParentFamilyPhotoUiModel(
        id = id,
        memberId = memberId,
        memberName = memberName,
        uploadedAt = uploadedAt,
        imageSource = imageSource,
        message = message,
        isNew = Duration.between(uploadedAt, now).let { age ->
            !age.isNegative && age <= Duration.ofHours(24)
        }
    )
