package com.example.senior_on.ui.parent.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.model.display.SeniorFontSize
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.domain.model.display.SeniorScreenConfiguration
import com.example.senior_on.domain.model.server.ServerButton
import com.example.senior_on.domain.model.server.ServerMusicCard
import com.example.senior_on.domain.model.server.ServerTodaySchedule
import com.example.senior_on.domain.model.server.TodayHospitalSchedule
import com.example.senior_on.domain.repository.server.HomeServerRepository
import java.time.LocalTime
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ParentHomeScheduleUiState(
    val count: Int = 0,
    val title: String? = null,
    val description: String? = null,
    val scheduledTime: LocalTime? = null,
    val isLoading: Boolean = true,
)

data class ParentHomeWeatherUiState(
    val temperature: Int? = null,
    val status: String? = null,
    val text: String = "날씨 확인 중",
)

data class ParentHomeButtonUiModel(
    val id: Long,
    val type: SeniorHomeButtonType?,
    val label: String,
    val actionType: String?,
    val actionValue: String?,
    val packageName: String?,
)

data class ParentHomeUiState(
    val screenConfiguration: SeniorScreenConfiguration = SeniorScreenConfiguration(),
    val musicButton: ParentHomeButtonUiModel? = null,
    val buttons: List<ParentHomeButtonUiModel> = emptyList(),
    val schedule: ParentHomeScheduleUiState = ParentHomeScheduleUiState(),
    val weather: ParentHomeWeatherUiState = ParentHomeWeatherUiState(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class ParentHomeViewModel(
    private val repository: HomeServerRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ParentHomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    schedule = it.schedule.copy(isLoading = true),
                    errorMessage = null,
                )
            }

            val (homeResult, schedulesResult, weatherResult) = coroutineScope {
                val home = async { runCatching { repository.getSeniorHome() } }
                val schedules = async {
                    runCatching { repository.getTodayHospitalSchedules() }
                }
                val weather = async {
                    runCatching {
                        repository.getWeather(
                            latitude = SEONGDONG_DISTRICT_OFFICE_LATITUDE,
                            longitude = SEONGDONG_DISTRICT_OFFICE_LONGITUDE,
                        )
                    }
                }
                Triple(home.await(), schedules.await(), weather.await())
            }

            homeResult
                .onSuccess { home ->
                    _uiState.value = ParentHomeUiState(
                        screenConfiguration = SeniorScreenConfiguration(
                            fontSize = home.fontSize.toFontSize(),
                            buttons = emptyList(),
                        ),
                        musicButton = home.musicCard
                            ?.takeIf { musicCard -> musicCard.enabled }
                            ?.toUiModel(),
                        buttons = home.buttons.map(ServerButton::toUiModel),
                        schedule = schedulesResult
                            .getOrNull()
                            ?.toHomeScheduleUiState()
                            ?: home.todaySchedule.toUiState(),
                        weather = weatherResult.fold(
                            onSuccess = { weather ->
                                ParentHomeWeatherUiState(
                                    temperature = weather.temperature,
                                    status = weather.status,
                                    text = weather.text.ifBlank { "날씨 정보 없음" },
                                )
                            },
                            onFailure = {
                                ParentHomeWeatherUiState(text = "날씨 정보 없음")
                            },
                        ),
                        isLoading = false,
                    )
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            schedule = it.schedule.copy(isLoading = false),
                            errorMessage = throwable.message
                                ?: "부모님 홈 정보를 불러오지 못했어요.",
                        )
                    }
                }
        }
    }

    companion object {
        private const val SEONGDONG_DISTRICT_OFFICE_LATITUDE = 37.56342697
        private const val SEONGDONG_DISTRICT_OFFICE_LONGITUDE = 127.03693390

        fun factory(repository: HomeServerRepository) = viewModelFactory {
            initializer { ParentHomeViewModel(repository) }
        }
    }
}

private fun List<TodayHospitalSchedule>.toHomeScheduleUiState(): ParentHomeScheduleUiState {
    val firstSchedule = firstOrNull()
    return ParentHomeScheduleUiState(
        count = size,
        title = firstSchedule?.hospitalName,
        description = firstSchedule?.department,
        scheduledTime = firstSchedule?.time,
        isLoading = false,
    )
}

private fun ServerTodaySchedule?.toUiState() = ParentHomeScheduleUiState(
    count = this?.count ?: 0,
    title = this?.title,
    description = this?.description,
    scheduledTime = this?.scheduledTime.toLocalTimeOrNull(),
    isLoading = false,
)

private fun ServerMusicCard.toUiModel() = ParentHomeButtonUiModel(
    id = 0,
    type = resolveButtonType(actionValue, packageName),
    label = "음악 듣기",
    actionType = actionType,
    actionValue = actionValue,
    packageName = packageName,
)

private fun ServerButton.toUiModel() = ParentHomeButtonUiModel(
    id = id,
    type = toButtonType(),
    label = name,
    actionType = actionType,
    actionValue = actionValue,
    packageName = packageName,
)

private fun String.toFontSize(): SeniorFontSize = when (trim().uppercase()) {
    "SMALL" -> SeniorFontSize.Small
    "NORMAL", "MEDIUM" -> SeniorFontSize.Normal
    else -> SeniorFontSize.Large
}

private fun ServerButton.toButtonType(): SeniorHomeButtonType? =
    resolveButtonType(actionValue, packageName)

private fun resolveButtonType(
    actionValue: String?,
    packageName: String?,
): SeniorHomeButtonType? {
    val value = actionValue.orEmpty().trim().uppercase()
    val packageValue = packageName.orEmpty().trim().lowercase()

    return when (value) {
        "PHONE", "CALL" -> SeniorHomeButtonType.Call
        "MESSAGE", "SMS" -> SeniorHomeButtonType.Message
        "CALENDAR" -> SeniorHomeButtonType.Calendar
        "ALARM" -> SeniorHomeButtonType.Alarm
        "MEMO" -> SeniorHomeButtonType.Memo
        "RECORDER" -> SeniorHomeButtonType.Recorder
        "CALCULATOR" -> SeniorHomeButtonType.Calculator
        "SETTINGS" -> SeniorHomeButtonType.Settings
        "FLASHLIGHT" -> SeniorHomeButtonType.Flashlight
        "KAKAO_TALK", "KAKAOTALK" -> SeniorHomeButtonType.KakaoTalk
        "NAVER_BAND" -> SeniorHomeButtonType.NaverBand
        "NAVER_CAFE" -> SeniorHomeButtonType.NaverCafe
        "LINE" -> SeniorHomeButtonType.Line
        "COMPANION", "CHAT_BUDDY" -> SeniorHomeButtonType.ChatBuddy
        "MEDICATION" -> SeniorHomeButtonType.Medication
        "SCHEDULE", "HOSPITAL" -> SeniorHomeButtonType.Schedule
        "YOUTUBE" -> SeniorHomeButtonType.YouTube
        "NAVER" -> SeniorHomeButtonType.Naver
        "DAUM" -> SeniorHomeButtonType.Daum
        "GOOGLE" -> SeniorHomeButtonType.Google
        "TVING" -> SeniorHomeButtonType.Tving
        "NETFLIX" -> SeniorHomeButtonType.Netflix
        "NAVER_MAP" -> SeniorHomeButtonType.NaverMap
        "KAKAO_MAP" -> SeniorHomeButtonType.KakaoMap
        "KAKAO_T" -> SeniorHomeButtonType.KakaoT
        "TMAP", "T_MAP" -> SeniorHomeButtonType.TMap
        "KORAIL_TALK" -> SeniorHomeButtonType.KorailTalk
        "TOSS" -> SeniorHomeButtonType.Toss
        "KAKAO_PAY" -> SeniorHomeButtonType.KakaoPay
        "NAVER_PAY" -> SeniorHomeButtonType.NaverPay
        "SAMSUNG_WALLET" -> SeniorHomeButtonType.SamsungWallet
        "SAMSUNG_PAY" -> SeniorHomeButtonType.SamsungPay
        "CASH_WALK" -> SeniorHomeButtonType.CashWalk
        "WEATHER" -> SeniorHomeButtonType.Weather
        "COUPANG" -> SeniorHomeButtonType.Coupang
        "KARROT" -> SeniorHomeButtonType.Karrot
        "BAEMIN" -> SeniorHomeButtonType.Baemin
        "YOGIYO" -> SeniorHomeButtonType.Yogiyo
        "COUPANG_EATS" -> SeniorHomeButtonType.CoupangEats
        "HOME_SHOPPING" -> SeniorHomeButtonType.HomeShopping
        "GO_STOP" -> SeniorHomeButtonType.GoStop
        "MELON" -> SeniorHomeButtonType.Melon
        "SPOTIFY" -> SeniorHomeButtonType.Spotify
        "PHOTO", "GALLERY" -> SeniorHomeButtonType.Photo
        "CAMERA" -> SeniorHomeButtonType.Camera
        "EMERGENCY", "SOS" -> SeniorHomeButtonType.Emergency
        else -> when (packageValue) {
            "com.google.android.youtube" -> SeniorHomeButtonType.YouTube
            "com.iloen.melon" -> SeniorHomeButtonType.Melon
            "com.spotify.music" -> SeniorHomeButtonType.Spotify
            "com.kakao.talk" -> SeniorHomeButtonType.KakaoTalk
            "com.nhn.android.nmap" -> SeniorHomeButtonType.NaverMap
            "net.daum.android.map" -> SeniorHomeButtonType.KakaoMap
            "com.nhn.android.search" -> SeniorHomeButtonType.Naver
            else -> null
        }
    }
}

private fun String?.toLocalTimeOrNull(): LocalTime? {
    val value = this?.trim().orEmpty()
    if (value.isEmpty()) return null
    return runCatching { LocalTime.parse(value) }.getOrNull()
}
