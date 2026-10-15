package com.example.senior_on.ui.child.family.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.model.server.ServerConnectedSenior
import com.example.senior_on.domain.model.auth.RemoteRequestException
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Immutable
data class SeniorConnectionUiState(
    val seniorId: Long? = null,
    val connectedSeniors: List<ServerConnectedSenior> = emptyList(),
    val isLoading: Boolean = false,
    val isConnecting: Boolean = false,
    val disconnectingPhotoGroupId: Long? = null,
    val hasLoaded: Boolean = false,
    val loadErrorMessage: String? = null,
    val connectErrorMessage: String? = null,
)

class SeniorConnectionViewModel(
    private val repository: FamilyServerRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SeniorConnectionUiState())
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null
    private var connectJob: Job? = null
    private var disconnectJob: Job? = null
    private var activeSeniorId: Long? = null

    fun loadConnections(seniorId: Long, force: Boolean = false) {
        require(seniorId > 0L) { "선택된 시니어 정보가 올바르지 않습니다." }
        activateSenior(seniorId)
        val current = _uiState.value
        if (loadJob?.isActive == true) {
            if (current.seniorId == seniorId) return
            loadJob?.cancel()
        }
        if (!force && current.hasLoaded && current.seniorId == seniorId) return

        loadJob = viewModelScope.launch {
            _uiState.update {
                if (it.seniorId == seniorId) {
                    it.copy(isLoading = true, loadErrorMessage = null)
                } else {
                    SeniorConnectionUiState(seniorId = seniorId, isLoading = true)
                }
            }
            try {
                val connections = repository.getConnectedSeniors(seniorId)
                if (!isActiveSenior(seniorId)) return@launch
                _uiState.update {
                    if (!isActiveSenior(seniorId)) return@update it
                    it.copy(
                        seniorId = seniorId,
                        connectedSeniors = connections,
                        isLoading = false,
                        hasLoaded = true,
                        loadErrorMessage = null,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _uiState.update {
                    if (!isActiveSenior(seniorId)) return@update it
                    it.copy(
                        seniorId = seniorId,
                        isLoading = false,
                        hasLoaded = true,
                        loadErrorMessage = "연결된 시니어를 불러오지 못했어요.",
                    )
                }
            }
        }
    }

    fun connect(
        seniorId: Long,
        seniorCode: String,
        onSuccess: () -> Unit = {},
    ) {
        if (!isActiveSenior(seniorId)) return
        if (connectJob?.isActive == true || _uiState.value.isConnecting) return
        connectJob = viewModelScope.launch {
            _uiState.update {
                if (isActiveSenior(seniorId)) {
                    it.copy(isConnecting = true, connectErrorMessage = null)
                } else it
            }
            try {
                repository.connectPhotoGroup(seniorId, seniorCode)
                val connections = repository.getConnectedSeniors(seniorId)
                if (!isActiveSenior(seniorId)) return@launch
                _uiState.update {
                    if (!isActiveSenior(seniorId)) return@update it
                    it.copy(
                        seniorId = seniorId,
                        connectedSeniors = connections,
                        isConnecting = false,
                        hasLoaded = true,
                    )
                }
                onSuccess()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    if (!isActiveSenior(seniorId)) return@update it
                    it.copy(
                        isConnecting = false,
                        connectErrorMessage = exception.connectionErrorMessage(),
                    )
                }
            }
        }
    }

    fun clearConnectError() {
        _uiState.update { it.copy(connectErrorMessage = null) }
    }

    fun disconnect(
        seniorId: Long,
        photoGroupId: Long,
        onSuccess: (hasRemainingConnections: Boolean) -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        if (!isActiveSenior(seniorId)) return
        if (disconnectJob?.isActive == true) return
        disconnectJob = viewModelScope.launch {
            _uiState.update {
                if (isActiveSenior(seniorId)) {
                    it.copy(disconnectingPhotoGroupId = photoGroupId)
                } else it
            }
            try {
                repository.disconnectPhotoGroup(seniorId, photoGroupId)
                if (!isActiveSenior(seniorId)) return@launch
                val remainingConnections = _uiState.value.connectedSeniors.filterNot {
                    it.photoGroupId == photoGroupId
                }
                _uiState.update {
                    if (!isActiveSenior(seniorId)) return@update it
                    it.copy(
                        connectedSeniors = remainingConnections,
                        disconnectingPhotoGroupId = null,
                    )
                }
                onSuccess(remainingConnections.isNotEmpty())
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                if (!isActiveSenior(seniorId)) return@launch
                _uiState.update {
                    if (isActiveSenior(seniorId)) {
                        it.copy(disconnectingPhotoGroupId = null)
                    } else it
                }
                onError(exception.disconnectionErrorMessage())
            }
        }
    }

    private fun activateSenior(seniorId: Long) {
        if (activeSeniorId == seniorId) return

        activeSeniorId = seniorId
        loadJob?.cancel()
        connectJob?.cancel()
        disconnectJob?.cancel()
        _uiState.value = SeniorConnectionUiState(seniorId = seniorId)
    }

    private fun isActiveSenior(seniorId: Long): Boolean = activeSeniorId == seniorId

    companion object {
        fun factory(repository: FamilyServerRepository) = viewModelFactory {
            initializer { SeniorConnectionViewModel(repository) }
        }
    }
}

internal fun Exception.connectionErrorMessage(): String = when {
    this is RemoteRequestException && code == "FAMILY400" ->
        "시니어 코드를 확인해 주세요."
    this is RemoteRequestException && code == "PHOTO_GROUP400" ->
        "현재 가족의 시니어 코드는 연결할 수 없어요."
    this is RemoteRequestException && code == "PHOTO_GROUP403" ->
        "주 담당자만 시니어를 연결할 수 있어요."
    this is RemoteRequestException && code == "PHOTO_GROUP409" ->
        "이미 연결된 시니어예요."
    this is RemoteRequestException && code == "PHOTO_GROUP404" ->
        "시니어 연결 정보를 찾을 수 없어요."
    this is HttpException && code() == 404 -> "시니어 코드를 확인해 주세요."
    this is HttpException && code() == 409 -> "이미 연결된 시니어예요."
    this is IllegalArgumentException -> message ?: "시니어 코드를 확인해 주세요."
    else -> "시니어를 연결하지 못했어요. 다시 시도해 주세요."
}

internal fun Exception.disconnectionErrorMessage(): String = when {
    this is RemoteRequestException && code == "PHOTO_GROUP403" ->
        "주 담당자만 연결을 해제할 수 있어요."
    this is RemoteRequestException && code == "PHOTO_GROUP404" ->
        "이미 해제되었거나 찾을 수 없는 연결이에요."
    this is HttpException && code() == 403 -> "주 담당자만 연결을 해제할 수 있어요."
    this is HttpException && code() == 404 -> "이미 해제되었거나 찾을 수 없는 연결이에요."
    this is IllegalArgumentException -> message ?: "연결 정보를 확인해 주세요."
    else -> "시니어 연결을 해제하지 못했어요. 다시 시도해 주세요."
}
