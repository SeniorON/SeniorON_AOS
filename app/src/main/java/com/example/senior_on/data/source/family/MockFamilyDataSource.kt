package com.example.senior_on.data.source.family

import com.example.senior_on.data.source.mock.fixtures.MockAuthFixtures
import com.example.senior_on.data.source.mock.fixtures.MockFamilyFixtures
import com.example.senior_on.data.source.mock.fixtures.MockFamilyPhotoFixtures
import com.example.senior_on.domain.model.family.PreparedFamilyPhoto
import com.example.senior_on.domain.model.family.FamilyImageSource
import com.example.senior_on.domain.model.family.FamilyJoinResult
import com.example.senior_on.domain.model.family.FamilyMemberRole
import com.example.senior_on.domain.model.family.FamilyOverview
import com.example.senior_on.domain.model.family.SharedFamilyPhoto
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class MockFamilyDataSource(
    initialOverview: FamilyOverview = MockFamilyFixtures.primaryCaregiverOverview,
    private val photoStore: MockFamilyPhotoStore = MockFamilyPhotoStore(
        MockFamilyPhotoFixtures.initialPhotos()
    ),
) : FamilyDataSource {
    private val overview = MutableStateFlow(initialOverview)

    override suspend fun joinFamily(familyCode: String): FamilyJoinResult {
        require(familyCode == MockAuthFixtures.VALID_FAMILY_SHARE_CODE) {
            "Invalid family share code"
        }
        val currentMemberRole = overview.value.members
            .firstOrNull { member -> member.isCurrentUser }
            ?.role
            ?: FamilyMemberRole.Assistant

        return MockFamilyFixtures.assistantJoinResult.copy(
            memberRole = currentMemberRole,
        )
    }

    override fun observeFamilyOverview(): Flow<FamilyOverview> =
        combine(overview, photoStore.photos) { current, photos ->
            current.withSharedPhotos(photos)
        }

    override suspend fun getFamilyOverview(): FamilyOverview =
        overview.value.withSharedPhotos(photoStore.photos.value)

    override suspend fun refreshFamilyOverview() = Unit

    override suspend fun getSharedPhoto(photoId: String): SharedFamilyPhoto? {
        val current = overview.value
        return photoStore.photos.value
            .firstOrNull { photo -> photo.id == photoId }
            ?.toSharedFamilyPhoto(current)
    }

    override suspend fun changePrimaryMember(memberId: String) {
        val current = overview.value
        require(current.currentUserCanManageMembers()) {
            "Only the current primary member can change roles"
        }
        val target = current.members.firstOrNull { member -> member.id == memberId }
            ?: error("Family member not found: $memberId")
        require(target.role == FamilyMemberRole.Assistant) {
            "Only an assistant member can become primary"
        }

        overview.value = current.copy(
            members = current.members.map { member ->
                when {
                    member.id == memberId -> member.copy(role = FamilyMemberRole.Primary)
                    member.role == FamilyMemberRole.Primary -> {
                        member.copy(role = FamilyMemberRole.Assistant)
                    }
                    else -> member
                }
            },
        )
    }

    override suspend fun deleteMember(memberId: String) {
        val current = overview.value
        require(current.currentUserCanManageMembers()) {
            "Only the current primary member can delete members"
        }
        val target = current.members.firstOrNull { member -> member.id == memberId }
            ?: error("Family member not found: $memberId")
        require(target.role == FamilyMemberRole.Assistant && !target.isCurrentUser) {
            "The primary or current member cannot be deleted"
        }

        overview.value = current.copy(
            members = current.members.filterNot { member -> member.id == memberId },
        )
    }

    override suspend fun uploadPhoto(
        photo: PreparedFamilyPhoto,
        message: String,
    ): SharedFamilyPhoto {
        val current = overview.value
        val author = current.members
            .firstOrNull { member -> member.isCurrentUser }
            ?: error("현재 가족 구성원 정보를 찾을 수 없습니다.")
        val uploadedPhoto = MockFamilyPhotoRecord(
            id = "shared-photo-${UUID.randomUUID()}",
            authorMemberId = author.id,
            createdAt = Instant.now(),
            imageSource = FamilyImageSource.Uri(photo.file.toURI().toString()),
            message = message,
        )

        photoStore.addPhoto(uploadedPhoto)
        return uploadedPhoto.toSharedFamilyPhoto(current)
    }

    override suspend fun deletePhoto(photoId: String) {
        val current = overview.value
        val currentMemberId = current.members
            .firstOrNull { member -> member.isCurrentUser }
            ?.id
            ?: error("현재 가족 구성원 정보를 찾을 수 없습니다.")
        val photo = photoStore.photos.value.firstOrNull { item -> item.id == photoId }
            ?: error("Family photo not found: $photoId")
        require(photo.authorMemberId == currentMemberId) {
            "Only a photo owned by the current user can be deleted"
        }

        photoStore.removePhoto(photoId)
        val localImageSource = photo.imageSource as? FamilyImageSource.Uri
        localImageSource?.let { source ->
            runCatching { java.io.File(java.net.URI(source.value)).delete() }
        }
    }

    private fun FamilyOverview.currentUserCanManageMembers(): Boolean =
        members.any { member ->
            member.isCurrentUser && member.role == FamilyMemberRole.Primary
        }

    private fun FamilyOverview.withSharedPhotos(
        photos: List<MockFamilyPhotoRecord>,
    ): FamilyOverview {
        return copy(
            sharedPhotos = photos.map { photo ->
                photo.toSharedFamilyPhoto(this)
            }
        )
    }

    private fun MockFamilyPhotoRecord.toSharedFamilyPhoto(
        overview: FamilyOverview,
    ): SharedFamilyPhoto {
        val currentMemberId = overview.members
            .firstOrNull { member -> member.isCurrentUser }
            ?.id
        val authorName = overview.members
            .firstOrNull { member -> member.id == authorMemberId }
            ?.name
            ?: MockFamilyFixtures.memberNameFor(authorMemberId)
            ?: "가족"

        return SharedFamilyPhoto(
            id = id,
            authorName = authorName,
            createdAt = createdAt,
            isOwnedByCurrentUser = authorMemberId == currentMemberId,
            imageSource = imageSource,
            message = message,
        )
    }
}
