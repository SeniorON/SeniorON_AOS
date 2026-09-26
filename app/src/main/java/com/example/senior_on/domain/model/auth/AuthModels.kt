package com.example.senior_on.domain.model.auth

data class SignupCredentials(
    val loginId: String,
    val email: String,
    val password: String,
    val passwordCheck: String,
    val name: String,
    val birth: String,
    val mode: AppUserMode,
    val agreeServiceTerms: Boolean,
    val agreePrivacyPolicy: Boolean,
    val agreeAgeOver14: Boolean,
    val agreeMarketing: Boolean
)

data class SignupResult(
    val usersId: Long,
    val name: String,
    val loginId: String,
    val mode: AppUserMode,
)

data class LoginCredentials(
    val loginId: String,
    val password: String,
    val fcmToken: String,
    val deviceIdentifier: String
)

data class LoginResult(
    val usersId: Long,
    val name: String,
    val loginId: String,
    val accessToken: String,
    val mode: AppUserMode? = null,
    val refreshToken: String? = null,
)

data class RoleUpdateResult(
    val usersId: Long,
    val name: String,
    val mode: AppUserMode
)

enum class CareManagerType {
    Primary,
    Sub,
    None,
}

data class OnboardingStatus(
    val hasFamily: Boolean,
    val managerType: CareManagerType,
    val seniorId: Long?,
    val seniorProfileCompleted: Boolean,
    val relationRegistered: Boolean,
    val onboardingCompleted: Boolean,
    val currentUserMode: AppUserMode? = null,
    val families: List<OnboardingFamilyStatus>? = null,
    val familyId: Long? = null,
)

data class OnboardingFamilyStatus(
    val familyId: Long,
    val managerType: CareManagerType,
    val parentUserId: Long?,
    val seniorId: Long?,
    val seniorName: String?,
    val seniorProfileCompleted: Boolean,
    val relationRegistered: Boolean,
)

/** Select a resumable family without mixing one family's role with another's senior ID. */
fun OnboardingStatus.forUser(mode: AppUserMode, userId: Long? = null): OnboardingStatus {
    val available = families ?: return this
    // Older saved sessions may not contain the numeric usersId. Trust server membership
    // for parent entry, but never guess an ID from the first family. Feature screens use /me/profile.
    if (mode == AppUserMode.Senior && userId == null) return copy(
        managerType = CareManagerType.None, seniorId = null, familyId = null,
        seniorProfileCompleted = false, relationRegistered = false,
    )
    val candidates = if (mode == AppUserMode.Senior) {
        available.filter { it.parentUserId != null && it.parentUserId == userId }
    } else available.filter { it.managerType != CareManagerType.None }
    val completed = candidates.firstOrNull {
        it.seniorId != null && it.seniorProfileCompleted && it.relationRegistered
    }
    val selected = completed
        ?: candidates.firstOrNull { it.managerType == CareManagerType.Primary && !it.seniorProfileCompleted }
        ?: candidates.firstOrNull { it.seniorId != null }
        ?: candidates.firstOrNull()
    return copy(
        hasFamily = selected != null,
        onboardingCompleted = completed != null,
        managerType = selected?.managerType ?: CareManagerType.None,
        seniorId = selected?.seniorId,
        seniorProfileCompleted = selected?.seniorProfileCompleted == true,
        relationRegistered = selected?.relationRegistered == true,
        familyId = selected?.familyId,
    )
}
