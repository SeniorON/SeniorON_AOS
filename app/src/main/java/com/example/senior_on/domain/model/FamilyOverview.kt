package com.example.senior_on.domain.model

import java.time.Instant

enum class FamilyMemberRole {
    Primary,
    Assistant
}

sealed interface FamilyImageSource {
    data class Local(val drawableResId: Int) : FamilyImageSource

    data class Remote(val url: String) : FamilyImageSource

    data class Uri(val value: String) : FamilyImageSource
}

data class FamilyMember(
    val id: String,
    val name: String,
    val role: FamilyMemberRole,
    val isCurrentUser: Boolean,
    val imageSource: FamilyImageSource? = null
)

data class SharedFamilyPhoto(
    val id: String,
    val authorName: String,
    val createdAt: Instant,
    val isOwnedByCurrentUser: Boolean,
    val imageSource: FamilyImageSource? = null,
    val message: String = ""
)

data class FamilyOverview(
    val members: List<FamilyMember>,
    val sharedPhotos: List<SharedFamilyPhoto>,
    val invitationCode: String
)
