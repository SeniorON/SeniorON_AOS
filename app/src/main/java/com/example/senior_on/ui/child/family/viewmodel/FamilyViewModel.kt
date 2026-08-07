package com.example.senior_on.ui.child.family.viewmodel

import com.example.senior_on.ui.child.family.SharedFamilyPhotoUiModel
import com.example.senior_on.ui.child.family.FamilyTabUiState
import com.example.senior_on.ui.child.family.FamilyMemberUiModel
import com.example.senior_on.ui.child.family.FamilyCaregiverRole
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.model.family.FamilyImageSource
import com.example.senior_on.domain.model.family.FamilyMemberRole
import com.example.senior_on.domain.model.family.FamilyOverview
import com.example.senior_on.domain.model.family.SharedFamilyPhoto
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
import retrofit2.HttpException

class FamilyViewModel(
    private val repository: FamilyServerRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FamilyTabUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    private var homeLoadJob: Job? = null
    private var photoLoadJob: Job? = null
    private var detailLoadJob: Job? = null
    private var isPhotoGalleryLoaded = false
    private var nextPhotoCursorAt: String? = null
    private var nextPhotoCursorId: Long? = null
    private var hasNextPhotoPage = false
    private val photoUrlRefreshJobs = mutableMapOf<String, Job>()
    private val lastRetriedImageUrlByPhotoId = mutableMapOf<String, String>()

    init {
        loadFamilyOverview()
    }

    fun loadLatestFamilyOverview() {
        if (homeLoadJob?.isActive == true) return
        loadFamilyOverview()
    }

    fun loadFamilyOverview() {
        homeLoadJob?.cancel()
        homeLoadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val home = repository.getHome()
                val members = home.members.mapNotNull { member ->
                    if (
                        member.id <= 0L ||
                        !member.role.equals(CHILD_ROLE, ignoreCase = true)
                    ) {
                        return@mapNotNull null
                    }
                    val role = when {
                        member.managerType.equals(PRIMARY_MANAGER, ignoreCase = true) ->
                            FamilyCaregiverRole.Primary
                        member.managerType.equals(SUB_MANAGER, ignoreCase = true) ->
                            FamilyCaregiverRole.Assistant
                        else -> return@mapNotNull null
                    }
                    FamilyMemberUiModel(
                        id = member.id.toString(),
                        name = member.name,
                        role = role,
                        canBecomePrimary = member.canBecomePrimary,
                        isCurrentUser = member.isMe,
                        imageSource = member.profileImageUrl
                            ?.trim()
                            ?.takeIf(String::isNotEmpty)
                            ?.let(FamilyImageSource::Remote),
                    )
                }
                val recentPhotos = home.recentPhotos.mapNotNull { photo ->
                    if (photo.id <= 0L) return@mapNotNull null
                    SharedFamilyPhotoUiModel(
                        id = photo.id.toString(),
                        authorName = photo.uploaderName,
                        createdAt = photo.createdAt.toFamilyPhotoInstant(),
                        canDelete = photo.canDelete,
                        imageSource = photo.imageUrl
                            .trim()
                            .takeIf(String::isNotEmpty)
                            ?.let(FamilyImageSource::Remote),
                        message = photo.description,
                    )
                }
                _uiState.update { currentState ->
                    currentState.copy(
                        members = members,
                        sharedPhotos = if (isPhotoGalleryLoaded) {
                            (recentPhotos + currentState.sharedPhotos)
                                .distinctBy(SharedFamilyPhotoUiModel::id)
                        } else {
                            recentPhotos
                        },
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "가족 정보를 불러오지 못했어요."
                    )
                }
            }
        }
    }

    fun loadFamilyMembers() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = state.members.isEmpty(),
                    memberMutationErrorMessage = null,
                )
            }
            runCatching { repository.getMembers() }
                .onSuccess { serverMembers ->
                    val members = serverMembers.mapNotNull { member ->
                        if (
                            member.id <= 0L ||
                            !member.role.equals(CHILD_ROLE, ignoreCase = true)
                        ) {
                            return@mapNotNull null
                        }
                        val role = when {
                            member.managerType.equals(PRIMARY_MANAGER, ignoreCase = true) ->
                                FamilyCaregiverRole.Primary
                            member.managerType.equals(SUB_MANAGER, ignoreCase = true) ->
                                FamilyCaregiverRole.Assistant
                            else -> return@mapNotNull null
                        }
                        FamilyMemberUiModel(
                            id = member.id.toString(),
                            name = member.name,
                            role = role,
                            canBecomePrimary = member.canBecomePrimary,
                            isCurrentUser = member.isMe,
                            imageSource = member.profileImageUrl
                                ?.trim()
                                ?.takeIf(String::isNotEmpty)
                                ?.let(FamilyImageSource::Remote),
                        )
                    }
                    _uiState.update {
                        it.copy(
                            members = members,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { exception ->
                    if (exception is CancellationException) throw exception
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            memberMutationErrorMessage =
                                "구성원 정보를 불러오지 못했어요.",
                        )
                    }
                }
        }
    }

    fun changePrimaryMember(memberId: String) {
        if (_uiState.value.isMemberMutationInProgress) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    changingPrimaryMemberId = memberId,
                    memberMutationErrorMessage = null,
                )
            }
            try {
                val userId = memberId.toLongOrNull()
                    ?: error("잘못된 가족 구성원 정보입니다.")
                repository.changePrimaryManager(userId)
                val serverMembers = repository.getMembers()
                val members = serverMembers.mapNotNull { member ->
                    if (
                        member.id <= 0L ||
                        !member.role.equals(CHILD_ROLE, ignoreCase = true)
                    ) {
                        return@mapNotNull null
                    }
                    val role = when {
                        member.managerType.equals(PRIMARY_MANAGER, ignoreCase = true) ->
                            FamilyCaregiverRole.Primary
                        member.managerType.equals(SUB_MANAGER, ignoreCase = true) ->
                            FamilyCaregiverRole.Assistant
                        else -> return@mapNotNull null
                    }
                    FamilyMemberUiModel(
                        id = member.id.toString(),
                        name = member.name,
                        role = role,
                        canBecomePrimary = member.canBecomePrimary,
                        isCurrentUser = member.isMe,
                        imageSource = member.profileImageUrl
                            ?.trim()
                            ?.takeIf(String::isNotEmpty)
                            ?.let(FamilyImageSource::Remote),
                    )
                }
                _uiState.update { it.copy(members = members) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(memberMutationErrorMessage = "주 담당자를 변경하지 못했어요.")
                }
            } finally {
                _uiState.update { it.copy(changingPrimaryMemberId = null) }
            }
        }
    }

    fun deleteMember(memberId: String) {
        if (_uiState.value.isMemberMutationInProgress) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    deletingMemberId = memberId,
                    memberMutationErrorMessage = null,
                )
            }
            try {
                val userId = memberId.toLongOrNull()
                    ?: error("잘못된 가족 구성원 정보입니다.")
                repository.deleteMember(userId)
                val serverMembers = repository.getMembers()
                val members = serverMembers.mapNotNull { member ->
                    if (
                        member.id <= 0L ||
                        !member.role.equals(CHILD_ROLE, ignoreCase = true)
                    ) {
                        return@mapNotNull null
                    }
                    val role = when {
                        member.managerType.equals(PRIMARY_MANAGER, ignoreCase = true) ->
                            FamilyCaregiverRole.Primary
                        member.managerType.equals(SUB_MANAGER, ignoreCase = true) ->
                            FamilyCaregiverRole.Assistant
                        else -> return@mapNotNull null
                    }
                    FamilyMemberUiModel(
                        id = member.id.toString(),
                        name = member.name,
                        role = role,
                        canBecomePrimary = member.canBecomePrimary,
                        isCurrentUser = member.isMe,
                        imageSource = member.profileImageUrl
                            ?.trim()
                            ?.takeIf(String::isNotEmpty)
                            ?.let(FamilyImageSource::Remote),
                    )
                }
                _uiState.update { it.copy(members = members) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(memberMutationErrorMessage = "구성원을 삭제하지 못했어요.")
                }
            } finally {
                _uiState.update { it.copy(deletingMemberId = null) }
            }
        }
    }

    fun loadPhotoGallery(force: Boolean = false) {
        if (photoLoadJob?.isActive == true) {
            if (!force) return
            photoLoadJob?.cancel()
        }
        if (isPhotoGalleryLoaded && !force) return

        photoLoadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isPhotoLoading = true, photoErrorMessage = null)
            }
            runCatching {
                repository.getPhotos(size = PHOTO_PAGE_SIZE)
            }.onSuccess { page ->
                val photos = page.photos.mapNotNull { photo ->
                    if (photo.id <= 0L) return@mapNotNull null
                    SharedFamilyPhotoUiModel(
                        id = photo.id.toString(),
                        authorName = photo.uploaderName,
                        createdAt = photo.createdAt.toFamilyPhotoInstant(),
                        canDelete = photo.canDelete,
                        imageSource = photo.imageUrl
                            .trim()
                            .takeIf(String::isNotEmpty)
                            ?.let(FamilyImageSource::Remote),
                        message = photo.description,
                    )
                }
                isPhotoGalleryLoaded = true
                nextPhotoCursorAt = page.nextCursor?.createdAt
                nextPhotoCursorId = page.nextCursor?.photoId
                hasNextPhotoPage = page.hasNext && page.nextCursor != null
                _uiState.update {
                    it.copy(
                        sharedPhotos = photos,
                        isPhotoLoading = false,
                        hasLoadedPhotoGallery = true,
                        photoErrorMessage = null,
                        hasMorePhotos = hasNextPhotoPage,
                    )
                }
            }.onFailure { exception ->
                if (exception is CancellationException) throw exception
                _uiState.update {
                    it.copy(
                        isPhotoLoading = false,
                        photoErrorMessage = "가족 사진을 불러오지 못했어요.",
                    )
                }
            }
        }
    }

    fun loadMorePhotos() {
        if (!isPhotoGalleryLoaded || !hasNextPhotoPage) return
        if (photoLoadJob?.isActive == true) return
        val cursorAt = nextPhotoCursorAt ?: return
        val cursorId = nextPhotoCursorId ?: return

        photoLoadJob = viewModelScope.launch {
            _uiState.update { it.copy(isPhotoLoading = true) }
            runCatching {
                repository.getPhotos(
                    cursorAt = cursorAt,
                    cursorId = cursorId,
                    size = PHOTO_PAGE_SIZE,
                )
            }.onSuccess { page ->
                val photos = page.photos.mapNotNull { photo ->
                    if (photo.id <= 0L) return@mapNotNull null
                    SharedFamilyPhotoUiModel(
                        id = photo.id.toString(),
                        authorName = photo.uploaderName,
                        createdAt = photo.createdAt.toFamilyPhotoInstant(),
                        canDelete = photo.canDelete,
                        imageSource = photo.imageUrl
                            .trim()
                            .takeIf(String::isNotEmpty)
                            ?.let(FamilyImageSource::Remote),
                        message = photo.description,
                    )
                }
                nextPhotoCursorAt = page.nextCursor?.createdAt
                nextPhotoCursorId = page.nextCursor?.photoId
                hasNextPhotoPage = page.hasNext && page.nextCursor != null
                _uiState.update { state ->
                    state.copy(
                        sharedPhotos = (state.sharedPhotos + photos)
                            .distinctBy(SharedFamilyPhotoUiModel::id),
                        isPhotoLoading = false,
                        photoErrorMessage = null,
                        hasMorePhotos = hasNextPhotoPage,
                    )
                }
            }.onFailure { exception ->
                if (exception is CancellationException) throw exception
                _uiState.update {
                    it.copy(
                        isPhotoLoading = false,
                        photoErrorMessage = "가족 사진을 더 불러오지 못했어요.",
                    )
                }
            }
        }
    }

    fun ensurePhotoLoaded(photoId: String) {
        if (_uiState.value.sharedPhotos.any { it.id == photoId }) return
        if (detailLoadJob?.isActive == true) return
        val serverPhotoId = photoId.toLongOrNull() ?: run {
            _uiState.update {
                it.copy(photoMutationErrorMessage = "사진을 불러오지 못했어요.")
            }
            return
        }

        detailLoadJob = viewModelScope.launch {
            photoLoadJob?.cancel()
            _uiState.update {
                it.copy(
                    isPhotoLoading = false,
                    loadingPhotoId = photoId,
                    photoMutationErrorMessage = null,
                )
            }
            runCatching {
                val photo = repository.getPhoto(serverPhotoId)
                require(photo.id == serverPhotoId) {
                    "Family photo response id must match the requested id"
                }
                SharedFamilyPhotoUiModel(
                    id = photo.id.toString(),
                    authorName = photo.uploaderName,
                    createdAt = photo.createdAt.toFamilyPhotoInstant(),
                    canDelete = photo.canDelete,
                    imageSource = photo.imageUrl
                        .trim()
                        .takeIf(String::isNotEmpty)
                        ?.let(FamilyImageSource::Remote),
                    message = photo.description,
                )
            }.onSuccess { photo ->
                _uiState.update { state ->
                    state.copy(
                        sharedPhotos = (state.sharedPhotos + photo)
                            .distinctBy(SharedFamilyPhotoUiModel::id)
                            .sortedByDescending(SharedFamilyPhotoUiModel::createdAt),
                        loadingPhotoId = null,
                        photoMutationErrorMessage = null,
                    )
                }
            }.onFailure { exception ->
                if (exception is CancellationException) throw exception
                _uiState.update {
                    it.copy(
                        loadingPhotoId = null,
                        photoMutationErrorMessage = if (
                            exception is HttpException && exception.code() == 404
                        ) {
                            "사진을 찾을 수 없어요."
                        } else {
                            "사진을 불러오지 못했어요."
                        },
                    )
                }
            }
        }
    }

    fun refreshPhotoUrlAfterLoadFailure(photoId: String, failedUrl: String) {
        val normalizedPhotoId = photoId.trim()
        val normalizedFailedUrl = failedUrl.trim()
        if (normalizedPhotoId.isEmpty() || normalizedFailedUrl.isEmpty()) return
        if (photoUrlRefreshJobs[normalizedPhotoId]?.isActive == true) return

        val currentPhoto = _uiState.value.sharedPhotos
            .firstOrNull { it.id == normalizedPhotoId }
            ?: return
        val currentUrl = (currentPhoto.imageSource as? FamilyImageSource.Remote)
            ?.url
            ?.trim()
            ?: return
        if (currentUrl != normalizedFailedUrl) return
        if (lastRetriedImageUrlByPhotoId[normalizedPhotoId] == normalizedFailedUrl) return

        val serverPhotoId = normalizedPhotoId.toLongOrNull() ?: return
        lastRetriedImageUrlByPhotoId[normalizedPhotoId] = normalizedFailedUrl

        val refreshJob = viewModelScope.launch {
            runCatching {
                val photo = repository.getPhoto(serverPhotoId)
                require(photo.id == serverPhotoId) {
                    "Family photo response id must match the requested id"
                }
                SharedFamilyPhotoUiModel(
                    id = photo.id.toString(),
                    authorName = photo.uploaderName,
                    createdAt = photo.createdAt.toFamilyPhotoInstant(),
                    canDelete = photo.canDelete,
                    imageSource = photo.imageUrl
                        .trim()
                        .takeIf(String::isNotEmpty)
                        ?.let(FamilyImageSource::Remote),
                    message = photo.description,
                )
            }.onSuccess { refreshedPhoto ->
                _uiState.update { state ->
                    state.copy(
                        sharedPhotos = state.sharedPhotos.map { photo ->
                            if (photo.id == normalizedPhotoId) refreshedPhoto else photo
                        },
                    )
                }
            }.onFailure { exception ->
                if (exception is CancellationException) throw exception
                lastRetriedImageUrlByPhotoId.remove(
                    normalizedPhotoId,
                    normalizedFailedUrl,
                )
            }
        }
        photoUrlRefreshJobs[normalizedPhotoId] = refreshJob
        refreshJob.invokeOnCompletion {
            photoUrlRefreshJobs.remove(normalizedPhotoId, refreshJob)
        }
    }

    fun deletePhoto(photoId: String) {
        if (_uiState.value.deletingPhotoId != null) return
        val serverPhotoId = photoId.toLongOrNull() ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    deletingPhotoId = photoId,
                    deletedPhotoId = null,
                    photoMutationErrorMessage = null,
                )
            }
            runCatching { repository.deletePhoto(serverPhotoId) }
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            sharedPhotos = state.sharedPhotos.filterNot {
                                photo -> photo.id == photoId
                            },
                            deletingPhotoId = null,
                            deletedPhotoId = photoId,
                        )
                    }
                }
                .onFailure { exception ->
                    if (exception is CancellationException) throw exception
                    _uiState.update {
                        it.copy(
                            deletingPhotoId = null,
                            photoMutationErrorMessage = "사진을 삭제하지 못했어요.",
                        )
                    }
                }
        }
    }

    fun refreshAfterPhotoUpload() {
        isPhotoGalleryLoaded = false
        nextPhotoCursorAt = null
        nextPhotoCursorId = null
        hasNextPhotoPage = false
        _uiState.update { it.copy(hasLoadedPhotoGallery = false) }
        loadFamilyOverview()
        loadPhotoGallery(force = true)
    }

    companion object {
        private const val CHILD_ROLE = "CHILD"
        private const val PRIMARY_MANAGER = "PRIMARY"
        private const val SUB_MANAGER = "SUB"
        private const val PHOTO_PAGE_SIZE = 20

        fun factory(repository: FamilyServerRepository) = viewModelFactory {
            initializer {
                FamilyViewModel(repository)
            }
        }
    }
}

internal fun FamilyOverview.toFamilyTabUiState(): FamilyTabUiState = FamilyTabUiState(
    members = members.map { member ->
        FamilyMemberUiModel(
            id = member.id,
            name = member.name,
            role = when (member.role) {
                FamilyMemberRole.Primary -> FamilyCaregiverRole.Primary
                FamilyMemberRole.Assistant -> FamilyCaregiverRole.Assistant
            },
            canBecomePrimary = member.role == FamilyMemberRole.Assistant,
            isCurrentUser = member.isCurrentUser,
            imageSource = member.imageSource
        )
    },
    sharedPhotos = sharedPhotos.map { photo ->
        photo.toFamilyPhotoUiModel()
    },
    invitationCode = invitationCode,
    isLoading = false
)

internal fun SharedFamilyPhoto.toFamilyPhotoUiModel(): SharedFamilyPhotoUiModel =
    SharedFamilyPhotoUiModel(
        id = id,
        authorName = authorName,
        createdAt = createdAt,
        canDelete = isOwnedByCurrentUser,
        imageSource = imageSource,
        message = message,
    )

private fun String.toFamilyPhotoInstant(): Instant {
    val value = trim()
    return runCatching { Instant.parse(value) }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(value).toInstant() }.getOrNull()
        ?: runCatching {
            LocalDateTime.parse(value)
                .atZone(ZoneId.of("Asia/Seoul"))
                .toInstant()
        }.getOrNull()
        ?: Instant.now()
}
