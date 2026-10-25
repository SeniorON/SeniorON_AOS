package com.example.senior_on.ui.child.family.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.model.server.ServerPhotoTarget
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class FamilyPhotoTargetUiState(
    val targets: List<ServerPhotoTarget> = emptyList(),
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val errorMessage: String? = null,
)

class FamilyPhotoTargetViewModel(
    private val repository: FamilyServerRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FamilyPhotoTargetUiState())
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null

    fun loadTargets(force: Boolean = false) {
        val current = _uiState.value
        if (loadJob?.isActive == true) return
        if (!force && current.hasLoaded) return

        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val targets = repository.getPhotoTargets()
                _uiState.update {
                    it.copy(
                        targets = targets,
                        isLoading = false,
                        hasLoaded = true,
                        errorMessage = null,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasLoaded = true,
                        errorMessage = "사진 공유 대상을 불러오지 못했어요.",
                    )
                }
            }
        }
    }

    companion object {
        fun factory(repository: FamilyServerRepository) = viewModelFactory {
            initializer { FamilyPhotoTargetViewModel(repository) }
        }
    }
}
