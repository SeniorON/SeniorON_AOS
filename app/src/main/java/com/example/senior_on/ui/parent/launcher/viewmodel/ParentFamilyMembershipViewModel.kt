package com.example.senior_on.ui.parent.launcher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.senior_on.domain.repository.auth.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ParentFamilyMembershipStatus {
    Checking,
    Connected,
    ProfilePending,
    NotConnected,
    Error,
}

data class ParentFamilyMembershipUiState(
    val status: ParentFamilyMembershipStatus =
        ParentFamilyMembershipStatus.Checking,
)

class ParentFamilyMembershipViewModel(
    private val repository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ParentFamilyMembershipUiState())
    private var checking = false
    val uiState = _uiState.asStateFlow()

    init {
        checkFamilyMembership()
    }

    fun checkFamilyMembership() {
        if (checking) return
        checking = true
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(status = ParentFamilyMembershipStatus.Checking) }
                // Family membership alone does not mean that the senior profile exists.
                val status = repository.getOnboardingStatus()
                val families = status.families
                val hasFamily = families?.isNotEmpty() ?: status.hasFamily
                val profileReady = families?.any {
                    it.seniorProfileCompleted && it.seniorId != null
                } ?: (status.seniorProfileCompleted && status.seniorId != null)
                _uiState.update {
                    it.copy(status = when {
                        !hasFamily -> ParentFamilyMembershipStatus.NotConnected
                        profileReady -> ParentFamilyMembershipStatus.Connected
                        else -> ParentFamilyMembershipStatus.ProfilePending
                    })
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _uiState.update { it.copy(status = ParentFamilyMembershipStatus.Error) }
            } finally {
                checking = false
            }
        }
    }

    fun onFamilyJoined() {
        checkFamilyMembership()
    }

    companion object {
        fun factory(
            repository: AuthRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ParentFamilyMembershipViewModel(repository) as T
        }
    }
}
