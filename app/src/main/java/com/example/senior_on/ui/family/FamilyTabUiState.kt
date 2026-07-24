package com.example.senior_on.ui.family

import androidx.compose.runtime.Immutable
import com.example.senior_on.domain.model.FamilyImageSource
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val VisibleFamilyMemberCount = 3
private const val VisibleSharedPhotoCount = 4

enum class FamilyCaregiverRole(val label: String) {
    Primary("주 담당자"),
    Assistant("보조담당자")
}

@Immutable
data class FamilyMemberUiModel(
    val id: String,
    val name: String,
    val role: FamilyCaregiverRole,
    val isCurrentUser: Boolean = false,
    val imageSource: FamilyImageSource? = null
)

@Immutable
data class SharedFamilyPhotoUiModel(
    val id: String,
    val authorName: String,
    val createdAt: Instant,
    val isOwnedByCurrentUser: Boolean,
    val imageSource: FamilyImageSource? = null,
    val message: String = "",
) {
    val relativeTime: String
        get() = createdAt.toRelativeTime()

    val uploadedDate: String
        get() = SharedPhotoDateFormatter.format(
            createdAt.atZone(ZoneId.systemDefault())
        )
}

@Immutable
data class FamilyTabUiState(
    val members: List<FamilyMemberUiModel> = emptyList(),
    val sharedPhotos: List<SharedFamilyPhotoUiModel> = emptyList(),
    val invitationCode: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val changingPrimaryMemberId: String? = null,
    val deletingMemberId: String? = null,
    val memberMutationErrorMessage: String? = null,
) {
    val visibleMembers: List<FamilyMemberUiModel>
        get() = members
            .sortedBy { member -> member.role != FamilyCaregiverRole.Primary }
            .take(VisibleFamilyMemberCount)

    val invitationSlotCount: Int
        get() = (VisibleFamilyMemberCount - visibleMembers.size).coerceAtLeast(0)

    val visibleSharedPhotos: List<SharedFamilyPhotoUiModel>
        get() = sharedPhotos.take(VisibleSharedPhotoCount)

    val shouldShowMorePhotos: Boolean
        get() = sharedPhotos.size >= VisibleSharedPhotoCount

    val currentUserRole: FamilyCaregiverRole?
        get() = members.firstOrNull(FamilyMemberUiModel::isCurrentUser)?.role

    val canManageMembers: Boolean
        get() = currentUserRole == FamilyCaregiverRole.Primary

    val isMemberMutationInProgress: Boolean
        get() = changingPrimaryMemberId != null || deletingMemberId != null
}

private val SharedPhotoDateFormatter = DateTimeFormatter.ofPattern("yyyy. M. d")

internal fun Instant.toRelativeTime(now: Instant = Instant.now()): String {
    val elapsedSeconds = Duration.between(this, now).seconds.coerceAtLeast(0)

    return when {
        elapsedSeconds < 60 -> "방금 전"
        elapsedSeconds < 60 * 60 -> "${elapsedSeconds / 60}분전"
        elapsedSeconds < 24 * 60 * 60 -> "${elapsedSeconds / (60 * 60)}시간전"
        elapsedSeconds < 7 * 24 * 60 * 60 -> "${elapsedSeconds / (24 * 60 * 60)}일전"
        else -> SharedPhotoDateFormatter.format(atZone(ZoneId.systemDefault()))
    }
}
