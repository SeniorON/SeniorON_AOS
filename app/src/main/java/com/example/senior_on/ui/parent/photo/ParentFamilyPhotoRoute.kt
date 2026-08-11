package com.example.senior_on.ui.parent.photo

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.data.source.mock.fixtures.MockAuthFixtures
import com.example.senior_on.domain.repository.parent.ParentFamilyPhotoRepository
import com.example.senior_on.ui.parent.photo.viewmodel.ParentFamilyPhotoViewModel

private enum class ParentPhotoDestination {
    FamilyMembers,
    MemberPhotos,
    Viewer,
}

@Composable
fun ParentFamilyPhotoRoute(
    repository: ParentFamilyPhotoRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO(parent API): Replace the fixture with the authenticated user's family identifier.
    val viewModel: ParentFamilyPhotoViewModel = viewModel(
        factory = ParentFamilyPhotoViewModel.factory(
            repository = repository,
            familyCode = MockAuthFixtures.DISPLAY_FAMILY_SHARE_CODE,
        )
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var destination by rememberSaveable {
        mutableStateOf(ParentPhotoDestination.FamilyMembers)
    }
    var selectedMemberId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedPhotoId by rememberSaveable { mutableStateOf<String?>(null) }

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
                destination = ParentPhotoDestination.MemberPhotos
            },
            onRetryClick = viewModel::loadFamilyPhotos,
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
                        destination = ParentPhotoDestination.MemberPhotos
                    },
                    onRetryClick = viewModel::loadFamilyPhotos,
                    modifier = modifier,
                )
            } else {
                ParentMemberPhotoGridScreen(
                    member = member,
                    onBackClick = ::goBack,
                    onPhotoClick = { photoId ->
                        selectedPhotoId = photoId
                        destination = ParentPhotoDestination.Viewer
                    },
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
                    onRetryClick = viewModel::loadFamilyPhotos,
                    modifier = modifier,
                )
            } else {
                ParentPhotoViewerScreen(
                    member = member,
                    initialPhotoId = photoId,
                    onBackClick = ::goBack,
                    modifier = modifier,
                )
            }
        }
    }
}
