package com.example.senior_on.data.repository

import com.example.senior_on.data.model.MockFamilyFixtures
import com.example.senior_on.data.model.PreparedFamilyPhoto
import com.example.senior_on.domain.model.FamilyImageSource
import com.example.senior_on.domain.model.FamilyMemberRole
import com.example.senior_on.domain.model.FamilyOverview
import com.example.senior_on.domain.model.SharedFamilyPhoto
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class MockFamilyRepository(
    initialOverview: FamilyOverview = MockFamilyFixtures.primaryCaregiverOverview,
) : FamilyRepository {
    private val overview = MutableStateFlow(initialOverview)

    override fun observeFamilyOverview(): Flow<FamilyOverview> = overview

    override suspend fun getFamilyOverview(): FamilyOverview = overview.value

    override suspend fun refreshFamilyOverview() = Unit

    override suspend fun getSharedPhoto(photoId: String): SharedFamilyPhoto? =
        overview.value.sharedPhotos.firstOrNull { photo -> photo.id == photoId }

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
        val authorName = current.members
            .firstOrNull { member -> member.isCurrentUser }
            ?.name
            ?: "나"
        val uploadedPhoto = SharedFamilyPhoto(
            id = "shared-photo-${UUID.randomUUID()}",
            authorName = authorName,
            createdAt = Instant.now(),
            isOwnedByCurrentUser = true,
            imageSource = FamilyImageSource.Uri(photo.file.toURI().toString()),
            message = message,
        )

        overview.value = current.copy(
            sharedPhotos = listOf(uploadedPhoto) + current.sharedPhotos,
        )
        return uploadedPhoto
    }

    override suspend fun deletePhoto(photoId: String) {
        val current = overview.value
        val photo = current.sharedPhotos.firstOrNull { item -> item.id == photoId }
            ?: error("Family photo not found: $photoId")
        require(photo.isOwnedByCurrentUser) {
            "Only a photo owned by the current user can be deleted"
        }

        overview.value = current.copy(
            sharedPhotos = current.sharedPhotos.filterNot { item -> item.id == photoId },
        )
        val localImageSource = photo.imageSource as? FamilyImageSource.Uri
        localImageSource?.let { source ->
            runCatching { java.io.File(java.net.URI(source.value)).delete() }
        }
    }

    private fun FamilyOverview.currentUserCanManageMembers(): Boolean =
        members.any { member ->
            member.isCurrentUser && member.role == FamilyMemberRole.Primary
        }
}
