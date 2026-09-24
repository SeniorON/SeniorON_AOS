package com.example.senior_on.ui.parent.settings

enum class ParentSettingsDestination {
    Main, Account, ChangeName, ChangePassword, ShareCode, PermissionControl;

    fun back(): ParentSettingsDestination = when (this) {
        ChangeName, ChangePassword -> Account
        else -> Main
    }
}

data class ParentSettingsProfile(
    val name: String = "시니어",
    val email: String = "연동 전",
    val imageUrl: String? = null,
    val imageRevision: Long = 0,
)
