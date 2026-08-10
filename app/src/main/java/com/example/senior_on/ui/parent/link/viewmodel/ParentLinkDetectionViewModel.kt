package com.example.senior_on.ui.parent.link.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.repository.parent.ParentLinkSafetyRepository
import com.example.senior_on.domain.model.parent.ParentLinkSafetyVerdict
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ParentLinkDetectionStatus {
    Idle,
    Checking,
    Safe,
    Dangerous,
    Unknown,
    Failed
}

data class ParentLinkDetectionUiState(
    val url: String? = null,
    val status: ParentLinkDetectionStatus = ParentLinkDetectionStatus.Idle,
    val errorMessage: String? = null
)

class ParentLinkDetectionViewModel(
    private val repository: ParentLinkSafetyRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ParentLinkDetectionUiState())
    val uiState = _uiState.asStateFlow()

    private var inspectionJob: Job? = null

    fun inspectLink(url: String) {
        inspectionJob?.cancel()
        _uiState.value = ParentLinkDetectionUiState(
            url = url,
            status = ParentLinkDetectionStatus.Checking
        )

        inspectionJob = viewModelScope.launch {
            runCatching { repository.inspectLink(url) }
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            status = when (result.verdict) {
                                ParentLinkSafetyVerdict.Safe ->
                                    ParentLinkDetectionStatus.Safe
                                ParentLinkSafetyVerdict.Dangerous ->
                                    ParentLinkDetectionStatus.Dangerous
                                ParentLinkSafetyVerdict.Unknown ->
                                    ParentLinkDetectionStatus.Unknown
                            },
                            errorMessage = null
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            status = ParentLinkDetectionStatus.Failed,
                            errorMessage = "링크를 확인하지 못했어요."
                        )
                    }
                }
        }
    }

    fun reset() {
        inspectionJob?.cancel()
        inspectionJob = null
        _uiState.value = ParentLinkDetectionUiState()
    }

    override fun onCleared() {
        inspectionJob?.cancel()
        super.onCleared()
    }

    companion object {
        fun factory(repository: ParentLinkSafetyRepository) = viewModelFactory {
            initializer {
                ParentLinkDetectionViewModel(repository)
            }
        }
    }
}
