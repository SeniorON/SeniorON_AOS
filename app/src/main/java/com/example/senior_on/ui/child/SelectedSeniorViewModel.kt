package com.example.senior_on.ui.child

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.repository.senior.SelectedSeniorRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class SelectedSeniorState(
    val seniorId: Long? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

/** Account-scoped durable selection, validated against a successfully loaded server list. */
class SelectedSeniorViewModel(
    private val accountId: String,
    private val repository: SelectedSeniorRepository,
) : ViewModel() {
    private val availableIds = MutableStateFlow<List<Long>?>(null)
    private val _state = MutableStateFlow(SelectedSeniorState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                combine(repository.observe(accountId), availableIds) { saved, ids -> saved to ids }
                    .collect { (saved, ids) ->
                        if (ids != null) {
                            val selected = saved?.takeIf { it in ids } ?: ids.firstOrNull()
                            if (saved != selected) repository.select(accountId, selected)
                            _state.value = SelectedSeniorState(selected, isLoading = false)
                        }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = SelectedSeniorState(isLoading = false, error = "시니어 선택 정보를 불러오지 못했어요.")
            }
        }
    }

    fun reconcile(ids: List<Long>) { availableIds.value = ids.filter { it > 0 }.distinct() }

    fun select(seniorId: Long) {
        if (seniorId !in availableIds.value.orEmpty()) return
        viewModelScope.launch {
            try {
                repository.select(accountId, seniorId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "시니어 선택을 저장하지 못했어요.")
            }
        }
    }

    companion object {
        fun factory(accountId: String, repository: SelectedSeniorRepository) = viewModelFactory {
            initializer { SelectedSeniorViewModel(accountId, repository) }
        }
    }
}
