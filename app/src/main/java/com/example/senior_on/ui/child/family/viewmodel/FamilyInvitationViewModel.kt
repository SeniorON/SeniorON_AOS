package com.example.senior_on.ui.child.family.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import com.example.senior_on.ui.child.family.FamilyInvitationUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FamilyInvitationViewModel(
    private val repository: FamilyServerRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        FamilyInvitationUiState(isLoading = true),
    )
    val uiState = _uiState.asStateFlow()

    init {
        fetchFamilyCode()
    }

    fun retry() {
        if (_uiState.value.isLoading) return
        fetchFamilyCode()
    }

    private fun fetchFamilyCode() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }

            try {
                val familyCodeInfo = repository.getCode()
                if (familyCodeInfo.code.isBlank()) {
                    error("Family code response was empty")
                }
                _uiState.value = FamilyInvitationUiState(
                    invitationCode = familyCodeInfo.code,
                    memberCount = familyCodeInfo.memberCount,
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = FamilyInvitationUiState(
                    errorMessage = FAMILY_CODE_LOAD_ERROR_MESSAGE,
                )
            }
        }
    }

    companion object {
        private const val FAMILY_CODE_LOAD_ERROR_MESSAGE =
            "가족 공유 코드를 불러오지 못했어요."

        fun factory(repository: FamilyServerRepository) = viewModelFactory {
            initializer {
                FamilyInvitationViewModel(repository)
            }
        }
    }
}
