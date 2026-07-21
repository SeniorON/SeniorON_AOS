package com.example.senior_on.ui.senior_info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.senior_on.data.repository.AddressSearchRepository
import com.example.senior_on.data.repository.MissingKakaoRestApiKeyException
import com.example.senior_on.domain.model.AddressSearchResult
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class AddressSearchUiState(
    val query: String = "",
    val results: List<AddressSearchResult> = emptyList(),
    val isLoading: Boolean = false,
    val isLocating: Boolean = false,
    val hasSearched: Boolean = false,
    val errorMessage: String? = null,
    val currentLocationResult: AddressSearchResult? = null
)

@OptIn(FlowPreview::class)
class AddressSearchViewModel(
    private val repository: AddressSearchRepository = AddressSearchRepository()
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val _uiState = MutableStateFlow(AddressSearchUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            searchQuery
                .debounce(SearchDebounceMillis)
                .map(String::trim)
                .collectLatest(::search)
        }
    }

    fun onQueryChange(query: String) {
        val limitedQuery = query.take(MaxSearchQueryLength)

        _uiState.update {
            it.copy(
                query = limitedQuery,
                results = emptyList(),
                isLoading = false,
                isLocating = false,
                hasSearched = false,
                errorMessage = null,
                currentLocationResult = null
            )
        }
        searchQuery.value = limitedQuery
    }

    fun onLocationRequestStarted() {
        _uiState.update {
            it.copy(
                isLocating = true,
                errorMessage = null,
                currentLocationResult = null
            )
        }
    }

    fun onLocationPermissionDenied() {
        _uiState.update {
            it.copy(
                isLocating = false,
                errorMessage = "현재 위치를 사용하려면 위치 권한을 허용해 주세요."
            )
        }
    }

    fun onLocationSettingsRequired() {
        _uiState.update {
            it.copy(
                isLocating = false,
                errorMessage = "기기의 위치 서비스를 켜 주세요."
            )
        }
    }

    fun onLocationRequestFailed() {
        _uiState.update {
            it.copy(
                isLocating = false,
                errorMessage = "현재 위치를 가져오지 못했어요. 잠시 후 다시 시도해 주세요."
            )
        }
    }

    fun onCurrentLocationFound(
        latitude: Double,
        longitude: Double
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLocating = true,
                    errorMessage = null,
                    currentLocationResult = null
                )
            }

            try {
                val result = repository.getAddressFromCoordinates(
                    latitude = latitude,
                    longitude = longitude
                )

                _uiState.update {
                    if (result == null) {
                        it.copy(
                            isLocating = false,
                            errorMessage = "현재 위치의 주소를 찾지 못했어요."
                        )
                    } else {
                        it.copy(
                            isLocating = false,
                            currentLocationResult = result
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLocating = false,
                        errorMessage = exception.toUserMessage()
                    )
                }
            }
        }
    }

    fun consumeCurrentLocationResult() {
        _uiState.update { it.copy(currentLocationResult = null) }
    }

    private suspend fun search(query: String) {
        if (query.length < MinSearchQueryLength) {
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                hasSearched = false,
                errorMessage = null
            )
        }

        try {
            val results = repository.searchAddress(query)
            _uiState.update {
                it.copy(
                    results = results,
                    isLoading = false,
                    hasSearched = true
                )
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            _uiState.update {
                it.copy(
                    results = emptyList(),
                    isLoading = false,
                    hasSearched = true,
                    errorMessage = exception.toUserMessage()
                )
            }
        }
    }

    private fun Exception.toUserMessage(): String = when (this) {
        is MissingKakaoRestApiKeyException ->
            "local.properties에 KAKAO_REST_API_KEY를 입력해 주세요."

        is UnknownHostException ->
            "인터넷 연결을 확인해 주세요."

        is SocketTimeoutException ->
            "요청 시간이 초과됐어요. 잠시 후 다시 시도해 주세요."

        is HttpException -> when (code()) {
            401, 403 -> "카카오 REST API 키와 카카오맵 사용 설정을 확인해 주세요."
            429 -> "검색 요청이 많아요. 잠시 후 다시 시도해 주세요."
            else -> "주소 검색 중 오류가 발생했어요. (${code()})"
        }

        else -> "주소 검색 중 오류가 발생했어요."
    }

    private companion object {
        const val SearchDebounceMillis = 400L
        const val MinSearchQueryLength = 2
        const val MaxSearchQueryLength = 100
    }
}
