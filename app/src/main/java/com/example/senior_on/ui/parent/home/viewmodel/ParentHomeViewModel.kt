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
import com.example.senior_on.domain.repository.server.HomeServerRepository
import com.example.senior_on.domain.repository.parent.ParentHomeUpdatesRepository
import com.example.senior_on.domain.repository.parent.ParentHomeUpdateEvent
import java.time.LocalTime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class ParentHomeScheduleUiState(
    val count: Int = 0,
    val title: String? = null,
    val description: String? = null,
    val scheduledTime: LocalTime? = null,
    val isLoading: Boolean = true,
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
    val buttons: List<ParentHomeButtonUiModel> = defaultOfflineHomeButtons(),
    val schedule: ParentHomeScheduleUiState = ParentHomeScheduleUiState(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

class ParentHomeViewModel(
    private val repository: HomeServerRepository,
    private val updatesRepository: ParentHomeUpdatesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ParentHomeUiState())
    val uiState = _uiState.asStateFlow()

    private val refreshSignals = Channel<Unit>(Channel.CONFLATED)
    private var pendingFullRefresh = false
    private var pendingVisibleRefresh = false
    private var pendingScheduleRefresh = false
    private var pendingConfigurationRefresh = false
    private var hasStartedObserving = false

    init {
        viewModelScope.launch {
            for (signal in refreshSignals) {
                // Coalesce a burst without dropping an update received during an HTTP request.
                delay(150)
                val fullRefresh = pendingFullRefresh
                val visibleRefresh = pendingVisibleRefresh
                val scheduleRefresh = pendingScheduleRefresh
                val configurationRefresh = pendingConfigurationRefresh
                pendingFullRefresh = false
                pendingVisibleRefresh = false
                pendingScheduleRefresh = false
                pendingConfigurationRefresh = false
                refreshSignals.tryReceive()
                if (fullRefresh) loadHome(homeOnly = false, showRefresh = visibleRefresh)
                else {
                    if (configurationRefresh) refreshHomeConfiguration()
                    if (scheduleRefresh && !configurationRefresh) refreshSchedule()
                }
            }
        }
        requestRefresh(full = true)
    }

    fun refresh() {
        if (_uiState.value.isLoading || _uiState.value.isRefreshing) return
        requestRefresh(full = true, visible = true)
    }

    /** Called inside the route's repeatOnLifecycle; cancellation closes the socket. */
    suspend fun observeHomeUpdates() {
        if (hasStartedObserving) requestRefresh(full = true)
        hasStartedObserving = true
        updatesRepository.observeUpdates().collect { event ->
            when (event) {
                ParentHomeUpdateEvent.Subscribed -> {
                    requestRefresh(full = false)
                }
                ParentHomeUpdateEvent.HomeUpdated -> requestRefresh(full = false)
                ParentHomeUpdateEvent.ScheduleUpdated -> requestScheduleRefresh()
                else -> Unit
            }
        }
    }

    private fun requestRefresh(full: Boolean, visible: Boolean = false) {
        pendingConfigurationRefresh = true
        pendingFullRefresh = pendingFullRefresh || full
        pendingVisibleRefresh = pendingVisibleRefresh || visible
        refreshSignals.trySend(Unit)
    }

    private fun requestScheduleRefresh() {
        pendingScheduleRefresh = true
        refreshSignals.trySend(Unit)
    }

    private suspend fun refreshSchedule() {
        cancellableResult { repository.getSeniorHome().todaySchedule }
            .onSuccess { schedule ->
                _uiState.update { it.copy(schedule = schedule.toUiState(), errorMessage = null) }
            }
            .onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "일정을 불러오지 못했어요.") }
            }
    }

    private suspend fun loadHome(homeOnly: Boolean, showRefresh: Boolean) {
        if (homeOnly) {
            refreshHomeConfiguration()
            return
        }
        _uiState.update {
            it.copy(
                isRefreshing = showRefresh,
                schedule = it.schedule.copy(isLoading = it.isLoading || showRefresh),
                errorMessage = null,
            )
        }

        val homeResult = cancellableResult { repository.getSeniorHome() }

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
                    buttons = home.buttons
                        .map(ServerButton::toUiModel)
                        .ifEmpty(::defaultOfflineHomeButtons),
                    schedule = home.todaySchedule.toUiState(),
                    isLoading = false,
                    isRefreshing = false,
                )
            }
            .onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        buttons = it.buttons.ifEmpty(::defaultOfflineHomeButtons),
                        isLoading = false,
                        isRefreshing = false,
                        schedule = it.schedule.copy(isLoading = false),
                        errorMessage = throwable.message
                            ?: "부모님 홈 정보를 불러오지 못했어요.",
                    )
                }
            }
    }

    private suspend fun refreshHomeConfiguration() {
        cancellableResult { repository.getSeniorHome() }
            .onSuccess { home ->
                _uiState.update {
                    it.copy(
                        screenConfiguration = it.screenConfiguration.copy(fontSize = home.fontSize.toFontSize()),
                        musicButton = home.musicCard?.takeIf { card -> card.enabled }?.toUiModel(),
                        buttons = home.buttons.map(ServerButton::toUiModel).ifEmpty(::defaultOfflineHomeButtons),
                        schedule = home.todaySchedule.toUiState(),
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            }
            .onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "홈 변경사항을 불러오지 못했어요.") }
            }
    }

    private suspend fun <T> cancellableResult(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        Result.failure(error)
    }

    companion object {
        fun factory(repository: HomeServerRepository, updatesRepository: ParentHomeUpdatesRepository) = viewModelFactory {
            initializer { ParentHomeViewModel(repository, updatesRepository) }
        }
    }
}

private fun defaultOfflineHomeButtons(): List<ParentHomeButtonUiModel> = listOf(
    offlineButton(-1, SeniorHomeButtonType.Call, "전화", "PHONE"),
    offlineButton(-2, SeniorHomeButtonType.Message, "메시지", "MESSAGE"),
    offlineButton(-3, SeniorHomeButtonType.Camera, "카메라", "CAMERA"),
    offlineButton(-4, SeniorHomeButtonType.Photo, "사진", "PHOTO"),
    offlineButton(-5, SeniorHomeButtonType.YouTube, "유튜브", "YOUTUBE"),
    offlineButton(-6, SeniorHomeButtonType.Settings, "설정", "SETTINGS"),
    offlineButton(-7, SeniorHomeButtonType.Medication, "복약", "MEDICATION"),
    offlineButton(-8, SeniorHomeButtonType.Calendar, "캘린더", "CALENDAR"),
    offlineButton(-9, SeniorHomeButtonType.Emergency, "긴급알림", "EMERGENCY"),
)

private fun offlineButton(
    id: Long,
    type: SeniorHomeButtonType,
    label: String,
    actionValue: String,
) = ParentHomeButtonUiModel(
    id = id,
    type = type,
    label = label,
    actionType = "DEFAULT",
    actionValue = actionValue,
    packageName = null,
)

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
    label = "노래 듣기",
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
        "VOICE_MEMO", "RECORDER", "VOICE_RECORDER" -> SeniorHomeButtonType.Recorder
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
        "GENIE" -> SeniorHomeButtonType.Genie
        "YOUTUBE_MUSIC" -> SeniorHomeButtonType.YouTubeMusic
        "SPOTIFY" -> SeniorHomeButtonType.Spotify
        "FLO" -> SeniorHomeButtonType.Flo
        "VIBE" -> SeniorHomeButtonType.Vibe
        "BUGS" -> SeniorHomeButtonType.Bugs
        "SAMSUNG_MUSIC" -> SeniorHomeButtonType.SamsungMusic
        "KAKAO_MUSIC" -> SeniorHomeButtonType.KakaoMusic
        "PHOTO", "GALLERY" -> SeniorHomeButtonType.Photo
        "CAMERA" -> SeniorHomeButtonType.Camera
        "EMERGENCY", "SOS" -> SeniorHomeButtonType.Emergency
        else -> when (packageValue) {
            "com.google.android.youtube" -> SeniorHomeButtonType.YouTube
            "com.iloen.melon" -> SeniorHomeButtonType.Melon
            "com.ktmusic.geniemusic" -> SeniorHomeButtonType.Genie
            "com.google.android.apps.youtube.music" -> SeniorHomeButtonType.YouTubeMusic
            "com.spotify.music" -> SeniorHomeButtonType.Spotify
            "skplanet.musicmate" -> SeniorHomeButtonType.Flo
            "com.naver.vibe" -> SeniorHomeButtonType.Vibe
            "com.neowiz.android.bugs" -> SeniorHomeButtonType.Bugs
            "com.sec.android.app.music" -> SeniorHomeButtonType.SamsungMusic
            "com.kakao.music" -> SeniorHomeButtonType.KakaoMusic
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
