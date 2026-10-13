package com.example.senior_on.ui.parent.photo

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.senior_on.notification.FamilyPhotoSharedEventStore
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import com.example.senior_on.domain.repository.parent.ParentSeniorProfileRepository
import com.example.senior_on.ui.parent.photo.viewmodel.ParentFamilyPhotoViewModel

private enum class ParentPhotoDestination {
    FamilyMembers,
    MemberPhotos,
    Viewer,
}

@Composable
fun ParentFamilyPhotoRoute(
    repository: FamilyServerRepository,
    profileRepository: ParentSeniorProfileRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    notificationPhotoId: Long? = null,
    onNotificationPhotoClosed: () -> Unit = {},
) {
    val viewModel: ParentFamilyPhotoViewModel = viewModel(
        factory = ParentFamilyPhotoViewModel.factory(repository, profileRepository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var destination by rememberSaveable {
        mutableStateOf(ParentPhotoDestination.FamilyMembers)
    }
    var selectedMemberId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedPhotoId by rememberSaveable { mutableStateOf<String?>(null) }
    var visiblePhotoId by rememberSaveable { mutableStateOf<String?>(null) }
    val deletedIds by FamilyPhotoSharedEventStore.deletedPhotoIds.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(deletedIds, notificationPhotoId, destination) {
        val activePhoto = notificationPhotoId ?: visiblePhotoId?.toLongOrNull()
        if (activePhoto in deletedIds &&
            (notificationPhotoId != null || destination == ParentPhotoDestination.Viewer)) {
            android.widget.Toast.makeText(context, "삭제된 사진이에요.", android.widget.Toast.LENGTH_SHORT).show()
            visiblePhotoId = null
            selectedPhotoId = null
            destination = ParentPhotoDestination.FamilyMembers
            if (notificationPhotoId != null) onNotificationPhotoClosed()
        }
        viewModel.applyDeletedPhotos(deletedIds)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val currentVisiblePhotoId by androidx.compose.runtime.rememberUpdatedState(visiblePhotoId)
    LaunchedEffect(viewModel, notificationPhotoId, lifecycleOwner) {
        if (notificationPhotoId != null) {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.openNotificationPhoto(notificationPhotoId)
                kotlinx.coroutines.awaitCancellation()
            }
        } else viewModel.closeNotificationPhoto()
    }
    if (notificationPhotoId != null) {
        val photo = uiState.notificationPhoto?.takeIf { it.id == notificationPhotoId.toString() && notificationPhotoId !in deletedIds }
        BackHandler(onBack = onNotificationPhotoClosed)
        com.example.senior_on.ui.parent.component.ParentQueryRetryContent(
            error = uiState.notificationPhotoError,
            loading = uiState.notificationPhotoLoading,
            hasContent = photo != null,
            onRetry = { viewModel.openNotificationPhoto(notificationPhotoId) },
            modifier = modifier,
            title = "가족 사진",
            onBackClick = onNotificationPhotoClosed,
        ) {
            if (photo != null) ParentPhotoViewerScreen(
                member = com.example.senior_on.ui.parent.photo.viewmodel.ParentPhotoMemberUiModel(
                    memberId = photo.memberId, memberName = photo.memberName, photoCount = 1,
                    hasNewPhotos = photo.isNew, latestPhotoSource = photo.imageSource, photos = listOf(photo),
                ),
                initialPhotoId = photo.id,
                onBackClick = onNotificationPhotoClosed,
                onPhotoViewed = viewModel::markNotificationPhotoViewed,
                modifier = modifier,
            )
        }
        return
    }

    LaunchedEffect(viewModel, lifecycleOwner, destination) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            // StateFlow also triggers a refresh on entry/resume after a missed FCM.
            FamilyPhotoSharedEventStore.revision.collect {
                viewModel.refreshFromNotification(
                    refreshSelectedPhotos = destination == ParentPhotoDestination.MemberPhotos,
                )
                if (destination == ParentPhotoDestination.Viewer) {
                    val id = currentVisiblePhotoId?.toLongOrNull()
                    if (id != null && viewModel.isPhotoMissing(id)) {
                        FamilyPhotoSharedEventStore.publish(mapOf(
                            "type" to "FAMILY_PHOTO_DELETED", "familyPhotoId" to id.toString(),
                        ))
                    }
                }
            }
        }
    }

    fun goBack() {
        when (destination) {
            ParentPhotoDestination.FamilyMembers -> onBackClick()
            ParentPhotoDestination.MemberPhotos ->
                destination = ParentPhotoDestination.FamilyMembers
            ParentPhotoDestination.Viewer ->
                destination = ParentPhotoDestination.MemberPhotos
        }
    }

    BackHandler(onBack = ::goBack)

    when (destination) {
        ParentPhotoDestination.FamilyMembers -> ParentFamilyMembersPhotoScreen(
            uiState = uiState,
            onBackClick = onBackClick,
            onMemberClick = { memberId ->
                selectedMemberId = memberId
                viewModel.selectMember(memberId)
                destination = ParentPhotoDestination.MemberPhotos
            },
            onRetryClick = viewModel::loadAlbums,
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = modifier,
        )

        ParentPhotoDestination.MemberPhotos -> {
            val member = uiState.members.firstOrNull {
                it.memberId == selectedMemberId
            }
            if (member == null) {
                ParentFamilyMembersPhotoScreen(
                    uiState = uiState,
                    onBackClick = onBackClick,
                    onMemberClick = { memberId ->
                        selectedMemberId = memberId
                        viewModel.selectMember(memberId)
                        destination = ParentPhotoDestination.MemberPhotos
                    },
                    onRetryClick = viewModel::loadAlbums,
                    modifier = modifier,
                )
            } else {
                ParentMemberPhotoGridScreen(
                    member = member,
                    isLoading = uiState.isPhotoLoading,
                    errorMessage = uiState.photoErrorMessage,
                    hasMorePhotos = uiState.hasMorePhotos,
                    isRefreshing = uiState.isRefreshing,
                    onBackClick = ::goBack,
                    onPhotoClick = { photoId ->
                        selectedPhotoId = photoId
                        visiblePhotoId = photoId
                        destination = ParentPhotoDestination.Viewer
                    },
                    onLoadMore = viewModel::loadMorePhotos,
                    onRetryClick = viewModel::retrySelectedMember,
                    onRefresh = viewModel::refresh,
                    modifier = modifier,
                )
            }
        }

        ParentPhotoDestination.Viewer -> {
            val member = uiState.members.firstOrNull {
                it.memberId == selectedMemberId
            }
            val photoId = selectedPhotoId
            if (member == null || photoId == null) {
                ParentFamilyMembersPhotoScreen(
                    uiState = uiState,
                    onBackClick = onBackClick,
                    onMemberClick = { memberId ->
                        selectedMemberId = memberId
                        destination = ParentPhotoDestination.MemberPhotos
                    },
                    onRetryClick = viewModel::loadAlbums,
                    modifier = modifier,
                )
            } else {
                ParentPhotoViewerScreen(
                    member = member,
                    initialPhotoId = photoId,
                    onBackClick = ::goBack,
                    onPhotoViewed = { id ->
                        visiblePhotoId = id
                        viewModel.markPhotoViewed(id)
                    },
                    modifier = modifier,
                )
            }
        }
    }
}
