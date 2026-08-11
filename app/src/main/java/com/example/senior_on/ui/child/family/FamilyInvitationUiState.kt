package com.example.senior_on.ui.child.family

import androidx.compose.runtime.Immutable

@Immutable
data class FamilyInvitationUiState(
    val invitationCode: String = "",
    val memberCount: Long? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
