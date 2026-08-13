package com.example.senior_on.ui.parent.photo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.model.family.FamilyImageSource
import com.example.senior_on.domain.model.server.ServerFamilyPhoto
import com.example.senior_on.domain.model.server.ServerFamilyPhotoCursor
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
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
    val isNew: Boolean,
)

data class ParentPhotoMemberUiModel(
    val memberId: String,
    val memberName: String,
    val photoCount: Long,
    val hasNewPhotos: Boolean,
    val latestPhotoSource: FamilyImageSource?,
    val photos: List<ParentFamilyPhotoUiModel> = emptyList(),
) {
    val latestPhoto: ParentFamilyPhotoUiModel?
        get() = photos.firstOrNull()
}

data class ParentFamilyPhotoUiState(
    val members: List<ParentPhotoMemberUiModel> = emptyList(),
    val selectedMemberId: String? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isPhotoLoading: Boolean = false,
    val hasMorePhotos: Boolean = false,
    val errorMessage: String? = null,
    val photoErrorMessage: String? = null,
)

class ParentFamilyPhotoViewModel(
    private val repository: FamilyServerRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ParentFamilyPhotoUiState())
    val uiState = _uiState.asStateFlow()

    private var photoLoadJob: Job? = null
    private var nextCursor: ServerFamilyPhotoCursor? = null

    fun loadAlbums(isRefresh: Boolean = false) {
        if (_uiState.value.isRefreshing) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    errorMessage = null,
                )
            }
            runCatching { repository.getPhotoAlbums() }
                .onSuccess { albums ->
                    _uiState.update { state ->
                        state.copy(
                            members = albums.map { album ->
                                val previous = state.members.firstOrNull {
                                    it.memberId == album.uploaderId.toString()
                                }
                                ParentPhotoMemberUiModel(
                                    memberId = album.uploaderId.toString(),
                                    memberName = album.uploaderName,
                                    photoCount = album.photoCount,
                                    hasNewPhotos = album.hasNewPhotos,
                                    latestPhotoSource = album.latestPhotoUrl
                                        .trim()
                                        .takeIf(String::isNotEmpty)
                                        ?.let(FamilyImageSource::Remote),
                                    photos = previous?.photos.orEmpty(),
                                )
                            },
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { exception ->
                    if (exception is CancellationException) throw exception
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = "가족 사진을 불러오지 못했어요.",
                        )
                    }
                }
        }
    }

    fun refresh() {
        val memberId = _uiState.value.selectedMemberId
        if (memberId == null) loadAlbums(isRefresh = true)
        else selectMember(memberId, isRefresh = true)
    }

    fun selectMember(memberId: String, isRefresh: Boolean = false) {
        val normalizedId = memberId.toLongOrNull() ?: return
        photoLoadJob?.cancel()
        nextCursor = null
        _uiState.update {
            it.copy(
                selectedMemberId = memberId,
                isPhotoLoading = !isRefresh,
                isRefreshing = isRefresh,
                hasMorePhotos = false,
                photoErrorMessage = null,
                members = it.members.map { member ->
                    if (member.memberId == memberId && !isRefresh) {
                        member.copy(photos = emptyList())
                    } else {
                        member
                    }
                },
            )
        }
        loadPhotoPage(normalizedId, append = false)
    }

    fun loadMorePhotos() {
        val state = _uiState.value
        if (state.isPhotoLoading || !state.hasMorePhotos) return
        val memberId = state.selectedMemberId?.toLongOrNull() ?: return
        if (nextCursor == null) return
        loadPhotoPage(memberId, append = true)
    }

    fun retrySelectedMember() {
        _uiState.value.selectedMemberId?.let(::selectMember)
    }

    fun markPhotoViewed(photoId: String) {
        val serverPhotoId = photoId.toLongOrNull() ?: return
        val memberId = _uiState.value.selectedMemberId ?: return
        val photo = _uiState.value.members
            .firstOrNull { it.memberId == memberId }
            ?.photos
            ?.firstOrNull { it.id == photoId }
            ?: return
        if (!photo.isNew) return

        viewModelScope.launch {
            runCatching { repository.markPhotoViewed(serverPhotoId) }
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            members = state.members.map { member ->
                                if (member.memberId != memberId) return@map member
                                val updatedPhotos = member.photos.map {
                                    if (it.id == photoId) it.copy(isNew = false) else it
                                }
                                member.copy(
                                    photos = updatedPhotos,
                                    hasNewPhotos = updatedPhotos.any(ParentFamilyPhotoUiModel::isNew),
                                )
                            },
                        )
                    }
                    loadAlbums()
                }
        }
    }

    private fun loadPhotoPage(uploaderId: Long, append: Boolean) {
        val cursor = if (append) nextCursor else null
        photoLoadJob = viewModelScope.launch {
            _uiState.update { it.copy(isPhotoLoading = true, photoErrorMessage = null) }
            runCatching {
                repository.getPhotos(
                    uploaderId = uploaderId,
                    cursorAt = cursor?.createdAt,
                    cursorId = cursor?.photoId,
                    size = PHOTO_PAGE_SIZE,
                )
            }.onSuccess { page ->
                nextCursor = page.nextCursor
                val mapped = page.photos.map(ServerFamilyPhoto::toParentUiModel)
                _uiState.update { state ->
                    state.copy(
                        members = state.members.map { member ->
                            if (member.memberId != uploaderId.toString()) return@map member
                            member.copy(
                                photos = (if (append) member.photos + mapped else mapped)
                                    .distinctBy(ParentFamilyPhotoUiModel::id),
                            )
                        },
                        isPhotoLoading = false,
                        isRefreshing = false,
                        hasMorePhotos = page.hasNext && page.nextCursor != null,
                        photoErrorMessage = null,
                    )
                }
            }.onFailure { exception ->
                if (exception is CancellationException) throw exception
                _uiState.update {
                    it.copy(
                        isPhotoLoading = false,
                        isRefreshing = false,
                        photoErrorMessage = "사진을 불러오지 못했어요.",
                    )
                }
            }
        }
    }

    companion object {
        private const val PHOTO_PAGE_SIZE = 20

        fun factory(repository: FamilyServerRepository) = viewModelFactory {
            initializer { ParentFamilyPhotoViewModel(repository) }
        }
    }
}

private fun ServerFamilyPhoto.toParentUiModel() = ParentFamilyPhotoUiModel(
    id = id.toString(),
    memberId = uploaderId.toString(),
    memberName = uploaderName,
    uploadedAt = createdAt.toParentPhotoInstant(),
    imageSource = FamilyImageSource.Remote(imageUrl),
    message = description,
    isNew = isNew,
)

private fun String.toParentPhotoInstant(): Instant {
    val value = trim()
    return runCatching { Instant.parse(value) }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(value).toInstant() }.getOrNull()
        ?: runCatching {
            LocalDateTime.parse(value).atZone(ZoneId.of("Asia/Seoul")).toInstant()
        }.getOrNull()
        ?: Instant.EPOCH
}
