package com.example.senior_on.ui.parent.photo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.model.family.FamilyImageSource
import com.example.senior_on.domain.model.family.FamilyPhotoReportReason
import com.example.senior_on.ui.parent.photo.SeniorReportTarget
import com.example.senior_on.domain.model.server.ServerFamilyPhoto
import com.example.senior_on.domain.model.server.ServerFamilyPhotoCursor
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import com.example.senior_on.domain.repository.parent.ParentSeniorProfileRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    val thumbnailSource: FamilyImageSource? = null,
    val canReport: Boolean = true,
)

internal data class PhotoReportSession(
    val photo: ParentFamilyPhotoUiModel,
    val target: SeniorReportTarget,
    val sending: Boolean = false,
    val accepted: Boolean = false,
    val hide: Boolean = false,
    val error: String? = null,
    val toastMessage: String? = null,
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
    val notificationPhoto: ParentFamilyPhotoUiModel? = null,
    val notificationPhotoLoading: Boolean = false,
    val notificationPhotoError: String? = null,
)

class ParentFamilyPhotoViewModel(
    private val repository: FamilyServerRepository,
    private val profileRepository: ParentSeniorProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ParentFamilyPhotoUiState())
    val uiState = _uiState.asStateFlow()

    private var photoLoadJob: Job? = null
    private var notificationPhotoJob: Job? = null
    private var notificationViewedJob: Job? = null
    private val deletedPhotoIds = mutableSetOf<Long>()
    private val hiddenPhotos = mutableSetOf<Long>()
    private val hiddenAuthors = mutableSetOf<String>()
    private val _report = MutableStateFlow<PhotoReportSession?>(null)
    internal val report = _report.asStateFlow()

    internal fun beginReport(photo: ParentFamilyPhotoUiModel, target: SeniorReportTarget) {
        if (photo.canReport && _report.value == null) _report.value = PhotoReportSession(photo, target)
    }

    internal fun dismissReport() {
        if (_report.value?.let { !it.sending && !it.accepted } == true) _report.value = null
    }

    internal fun consumeReportToast() {
        _report.update { it?.copy(toastMessage = null) }
    }

    internal fun submitReport(reason: FamilyPhotoReportReason, text: String, hide: Boolean) {
        val session = _report.value?.copy(error = null, toastMessage = null) ?: return
        if (session.sending || session.accepted) return
        _report.value = session.copy(sending = true, error = null)
        viewModelScope.launch {
            try {
                val id = session.photo.id.toLong()
                val detail = text.trim().takeIf { reason == FamilyPhotoReportReason.Etc && it.isNotEmpty() }
                when (session.target) {
                    SeniorReportTarget.Photo -> repository.reportPhoto(id, reason, detail, hide)
                    SeniorReportTarget.Author -> repository.reportUploader(id, reason, detail, hide)
                }
                _report.value = session.copy(accepted = true, hide = hide)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val duplicateMessage = when ((e as? com.example.senior_on.domain.model.auth.RemoteRequestException)?.code) {
                    "FAMILY_PHOTO409_1" -> "이미 신고한 사진이에요."
                    "FAMILY_PHOTO409_2" -> "이미 신고한 게시자예요."
                    else -> null
                }
                if (duplicateMessage != null) {
                    _report.value = session.copy(toastMessage = duplicateMessage)
                    return@launch
                }
                val message = when (e) {
                    is com.example.senior_on.domain.model.auth.RemoteRequestException -> e.message
                    is java.io.IOException -> "네트워크 연결을 확인한 후 다시 시도해 주세요."
                    else -> null
                }
                _report.value = session.copy(error = message ?: "신고를 접수하지 못했어요. 다시 시도해 주세요.")
            }
        }
    }

    internal fun confirmReport() {
        val session = _report.value?.takeIf { it.accepted } ?: return
        if (session.hide) {
            val photo = session.photo
            if (session.target == SeniorReportTarget.Author) hiddenAuthors += photo.memberId
            else hiddenPhotos += photo.id.toLong()
            _uiState.update { state ->
                state.copy(members = state.members.mapNotNull { member ->
                    if (member.memberId != photo.memberId) return@mapNotNull member
                    if (member.memberId in hiddenAuthors || member.photoCount <= 1) return@mapNotNull null
                    val kept = member.photos.filterNot { it.id == photo.id }
                    member.copy(photos = kept, photoCount = member.photoCount - 1,
                        latestPhotoSource = kept.firstOrNull()?.let { it.thumbnailSource ?: it.imageSource })
                })
            }
            loadAlbumsInternal(background = true)
        }
        _report.value = null
    }

    suspend fun isPhotoMissing(photoId: Long): Boolean = try {
        repository.getPhoto(photoId)
        false
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        // Offline/authorization/server failures are not evidence of deletion.
        error is com.example.senior_on.domain.model.auth.RemoteRequestException &&
            error.status == 404 && error.code == "FAMILY_PHOTO404"
    }

    fun applyDeletedPhotos(ids: Set<Long>) {
        deletedPhotoIds.addAll(ids)
        if (_uiState.value.notificationPhoto?.id?.toLongOrNull() in ids) closeNotificationPhoto()
        _uiState.update { state ->
            state.copy(members = state.members.map { member ->
                member.copy(photos = member.photos.filterNot { it.id.toLongOrNull() in deletedPhotoIds })
            })
        }
    }

    fun openNotificationPhoto(photoId: Long) {
        notificationPhotoJob?.cancel()
        notificationViewedJob?.cancel()
        _uiState.update { it.copy(notificationPhoto = null, notificationPhotoLoading = true, notificationPhotoError = null) }
        notificationPhotoJob = viewModelScope.launch {
            try {
                require(photoId > 0)
                val photo = repository.getPhoto(photoId)
                check(photo.id == photoId)
                check(photoId !in deletedPhotoIds) { "Deleted photo" }
                check(photoId !in hiddenPhotos && photo.uploaderId.toString() !in hiddenAuthors)
                _uiState.update { it.copy(notificationPhoto = photo.toParentUiModel(), notificationPhotoLoading = false) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _uiState.update { it.copy(notificationPhotoLoading = false,
                    notificationPhotoError = "사진을 불러오지 못했어요.\n인터넷 연결을 확인해 주세요.\n삭제되었거나 볼 수 없는 사진일 수도 있어요.") }
            }
        }
    }

    fun closeNotificationPhoto() {
        notificationPhotoJob?.cancel()
        _uiState.update { it.copy(notificationPhoto = null, notificationPhotoLoading = false, notificationPhotoError = null) }
    }

    fun markNotificationPhotoViewed(photoId: String) {
        val photo = _uiState.value.notificationPhoto ?: return
        if (photo.id != photoId || !photo.isNew || notificationViewedJob?.isActive == true) return
        notificationViewedJob = viewModelScope.launch {
            try {
                repository.markPhotoViewed(photoId.toLong(), requireOwnSeniorId())
                _uiState.update { state ->
                    state.copy(notificationPhoto = state.notificationPhoto?.let {
                        if (it.id == photoId) it.copy(isNew = false) else it
                    })
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // A failed viewed receipt must not hide a successfully loaded photo.
            }
        }
    }
    private var albumLoadJob: Job? = null
    private var nextCursor: ServerFamilyPhotoCursor? = null
    private val seniorIdMutex = Mutex()
    private var ownSeniorId: Long? = null

    private suspend fun requireOwnSeniorId(): Long = seniorIdMutex.withLock {
        ownSeniorId ?: profileRepository.getOwnSeniorId().also { ownSeniorId = it }
    }

    fun loadAlbums(isRefresh: Boolean = false) {
        if (_uiState.value.isRefreshing) return
        loadAlbumsInternal(isRefresh = isRefresh)
    }

    private fun loadAlbumsInternal(isRefresh: Boolean = false, background: Boolean = false) {
        albumLoadJob?.cancel()
        albumLoadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = if (background) it.isLoading else !isRefresh,
                    isRefreshing = if (background) it.isRefreshing else isRefresh,
                    errorMessage = null,
                )
            }
            runCatching { repository.getPhotoAlbums(requireOwnSeniorId()) }
                .onSuccess { albums ->
                    _uiState.update { state ->
                        state.copy(
                            members = albums.filterNot { it.uploaderId.toString() in hiddenAuthors }.map { album ->
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

    fun refreshFromNotification(refreshSelectedPhotos: Boolean) {
        loadAlbumsInternal(background = true)
        if (!refreshSelectedPhotos) return
        val memberId = _uiState.value.selectedMemberId?.toLongOrNull() ?: return
        photoLoadJob?.cancel()
        nextCursor = null
        loadPhotoPage(memberId, append = false, background = true)
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
        if (photoLoadJob?.isActive == true || state.isPhotoLoading || !state.hasMorePhotos) return
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
            runCatching { repository.markPhotoViewed(serverPhotoId, requireOwnSeniorId()) }
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
                .onFailure { exception ->
                    if (exception is CancellationException) throw exception
                    _uiState.update {
                        it.copy(photoErrorMessage = "사진 확인 상태를 저장하지 못했어요. 다시 시도해 주세요.")
                    }
                }
        }
    }

    private fun loadPhotoPage(uploaderId: Long, append: Boolean, background: Boolean = false) {
        val cursor = if (append) nextCursor else null
        photoLoadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isPhotoLoading = if (background) it.isPhotoLoading else true, photoErrorMessage = null)
            }
            runCatching {
                repository.getPhotos(
                    seniorId = requireOwnSeniorId(),
                    uploaderId = uploaderId,
                    cursorAt = cursor?.createdAt,
                    cursorId = cursor?.photoId,
                    size = PHOTO_PAGE_SIZE,
                )
            }.onSuccess { page ->
                nextCursor = page.nextCursor
                val mapped = page.photos.filterNot {
                    it.id in deletedPhotoIds || it.id in hiddenPhotos || it.uploaderId.toString() in hiddenAuthors
                }.map(ServerFamilyPhoto::toParentUiModel)
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

        fun factory(repository: FamilyServerRepository, profileRepository: ParentSeniorProfileRepository) = viewModelFactory {
            initializer { ParentFamilyPhotoViewModel(repository, profileRepository) }
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
    canReport = !canDelete,
    thumbnailSource = thumbnailUrl
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?.let(FamilyImageSource::Remote),
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
