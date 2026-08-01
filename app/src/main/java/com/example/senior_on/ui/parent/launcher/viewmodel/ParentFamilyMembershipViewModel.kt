package com.example.senior_on.ui.parent.launcher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ParentFamilyMembershipStatus {
    Checking,
    Connected,
    NotConnected,
    Error,
}

data class ParentFamilyMembershipUiState(
    val status: ParentFamilyMembershipStatus =
        ParentFamilyMembershipStatus.Checking,
)

class ParentFamilyMembershipViewModel(
    private val repository: FamilyServerRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ParentFamilyMembershipUiState())
    val uiState = _uiState.asStateFlow()

    init {
        checkFamilyMembership()
    }

    fun checkFamilyMembership() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(status = ParentFamilyMembershipStatus.Checking)
            }
            runCatching { repository.hasFamily() }
                .onSuccess { hasFamily ->
                    _uiState.update {
                        it.copy(
                            status = if (hasFamily) {
                                ParentFamilyMembershipStatus.Connected
                            } else {
                                ParentFamilyMembershipStatus.NotConnected
                            },
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(status = ParentFamilyMembershipStatus.Error)
                    }
                }
        }
    }

    fun onFamilyJoined() {
        _uiState.update {
            it.copy(status = ParentFamilyMembershipStatus.Connected)
        }
    }

    companion object {
        fun factory(
            repository: FamilyServerRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ParentFamilyMembershipViewModel(repository) as T
        }
    }
}
