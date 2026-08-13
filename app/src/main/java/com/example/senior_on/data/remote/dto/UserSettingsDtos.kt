package com.example.senior_on.data.remote.dto

data class PasswordChangeRequest(
    val currentPassword: String, val newPassword: String, val newPasswordCheck: String
)
data class PasswordChangeResponse(val changed: Boolean?)
data class NameUpdateRequest(val name: String)
data class NameUpdateResponse(val name: String?)
data class ProfileImageResponse(
    val profileImageUrl: String?,
    val isDefaultProfileImage: Boolean?,
)
data class ProfileImageUpdateResponse(val profileImageUrl: String?)
data class UserAccountResponse(
    val name: String?,
    val role: String?,
    val email: String?,
)
