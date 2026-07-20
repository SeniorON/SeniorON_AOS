package com.example.senior_on.ui.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.data.repository.FamilyRepository
import com.example.senior_on.domain.model.FamilyMemberRole
import com.example.senior_on.domain.model.FamilyOverview
import com.example.senior_on.domain.model.SharedFamilyPhoto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FamilyViewModel(
    private val repository: FamilyRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FamilyTabUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        observeFamilyOverview()
        loadFamilyOverview()
    }

    private fun observeFamilyOverview() {
        viewModelScope.launch {
            repository.observeFamilyOverview()
                .catch { exception ->
                    if (exception is CancellationException) throw exception
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "가족 정보를 불러오지 못했어요.",
                        )
                    }
                }
                .collectLatest { overview ->
                    _uiState.update { currentState ->
                        overview.toFamilyTabUiState().copy(
                            changingPrimaryMemberId = currentState.changingPrimaryMemberId,
                            deletingMemberId = currentState.deletingMemberId,
                            memberMutationErrorMessage =
                                currentState.memberMutationErrorMessage,
                        )
                    }
                }
        }
    }

    fun loadFamilyOverview() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                repository.refreshFamilyOverview()
                val overview = repository.getFamilyOverview()
                _uiState.update { currentState ->
                    overview.toFamilyTabUiState().copy(
                        changingPrimaryMemberId = currentState.changingPrimaryMemberId,
                        deletingMemberId = currentState.deletingMemberId,
                        memberMutationErrorMessage =
                            currentState.memberMutationErrorMessage,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "가족 정보를 불러오지 못했어요."
                    )
                }
            }
        }
    }

    fun changePrimaryMember(memberId: String) {
        if (_uiState.value.isMemberMutationInProgress) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    changingPrimaryMemberId = memberId,
                    memberMutationErrorMessage = null,
                )
            }
            try {
                repository.changePrimaryMember(memberId)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(memberMutationErrorMessage = "주 담당자를 변경하지 못했어요.")
                }
            } finally {
                _uiState.update { it.copy(changingPrimaryMemberId = null) }
            }
        }
    }

    fun deleteMember(memberId: String) {
        if (_uiState.value.isMemberMutationInProgress) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    deletingMemberId = memberId,
                    memberMutationErrorMessage = null,
                )
            }
            try {
                repository.deleteMember(memberId)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(memberMutationErrorMessage = "구성원을 삭제하지 못했어요.")
                }
            } finally {
                _uiState.update { it.copy(deletingMemberId = null) }
            }
        }
    }

    companion object {
        fun factory(repository: FamilyRepository) = viewModelFactory {
            initializer {
                FamilyViewModel(repository)
            }
        }
    }
}

internal fun FamilyOverview.toFamilyTabUiState(): FamilyTabUiState = FamilyTabUiState(
    members = members.map { member ->
        FamilyMemberUiModel(
            id = member.id,
            name = member.name,
            role = when (member.role) {
                FamilyMemberRole.Primary -> FamilyCaregiverRole.Primary
                FamilyMemberRole.Assistant -> FamilyCaregiverRole.Assistant
            },
            isCurrentUser = member.isCurrentUser,
            imageSource = member.imageSource
        )
    },
    sharedPhotos = sharedPhotos.map { photo ->
        photo.toFamilyPhotoUiModel()
    },
    invitationCode = invitationCode,
    isLoading = false
)

internal fun SharedFamilyPhoto.toFamilyPhotoUiModel(): SharedFamilyPhotoUiModel =
    SharedFamilyPhotoUiModel(
        id = id,
        authorName = authorName,
        createdAt = createdAt,
        isOwnedByCurrentUser = isOwnedByCurrentUser,
        imageSource = imageSource,
        message = message,
    )
