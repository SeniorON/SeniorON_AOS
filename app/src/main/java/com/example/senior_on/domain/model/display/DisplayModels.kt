package com.example.senior_on.domain.model.display

import com.example.senior_on.domain.model.parent.ParentInfo

enum class DisplayDeviceConnectionStatus {
    Online,
    Offline,
}

data class DisplayDevice(
    val id: String,
    val name: String,
    val connectionStatus: DisplayDeviceConnectionStatus,
    val batteryLevelPercent: Int? = null,
    val lastConnectedAtLabel: String? = null,
    val lastLocationUpdatedAtLabel: String? = null,
)

enum class SeniorFontSize {
    Small,
    Normal,
    Large,
}

enum class SeniorHomeButtonType {
    Call,
    Message,
    Calendar,
    Alarm,
    Memo,
    Recorder,
    Calculator,
    Settings,
    Flashlight,
    KakaoTalk,
    NaverBand,
    NaverCafe,
    Line,
    ChatBuddy,
    Medication,
    Schedule,
    YouTube,
    Naver,
    Daum,
    Google,
    Tving,
    Netflix,
    NaverMap,
    KakaoMap,
    KakaoT,
    TMap,
    KorailTalk,
    Toss,
    KakaoPay,
    NaverPay,
    SamsungWallet,
    SamsungPay,
    CashWalk,
    Weather,
    Coupang,
    Karrot,
    Baemin,
    Yogiyo,
    CoupangEats,
    HomeShopping,
    GoStop,
    Melon,
    Genie,
    YouTubeMusic,
    Spotify,
    Flo,
    Vibe,
    Bugs,
    SamsungMusic,
    KakaoMusic,
    Photo,
    Camera,
    Emergency,
}

data class DisplayHomeButton(
    val id: Long = 0L,
    val optionId: Long? = null,
    val order: Int = 0,
    val name: String,
    val icon: String? = null,
    val actionType: String,
    val actionValue: String,
    val packageName: String? = null,
    val type: SeniorHomeButtonType? = null,
) {
    val stableKey: String
        get() = when {
            actionType.equals("DEFAULT", ignoreCase = true) ->
                "DEFAULT:${actionValue.trim().uppercase()}"
            !packageName.isNullOrBlank() ->
                "APP:${packageName.trim().lowercase()}"
            else -> "APP:${actionValue.trim().lowercase()}"
        }

    fun isDefaultAction(action: String): Boolean =
        actionType.equals("DEFAULT", ignoreCase = true) &&
            actionValue.equals(action, ignoreCase = true)
}

val InitialSeniorHomeGridButtons = listOf(
    SeniorHomeButtonType.Call,
    SeniorHomeButtonType.Message,
    SeniorHomeButtonType.Camera,
    SeniorHomeButtonType.Photo,
    SeniorHomeButtonType.YouTube,
    SeniorHomeButtonType.ChatBuddy,
    SeniorHomeButtonType.Medication,
    SeniorHomeButtonType.Emergency,
    SeniorHomeButtonType.KakaoTalk,
    SeniorHomeButtonType.Naver,
)

data class SeniorScreenConfiguration(
    val fontSize: SeniorFontSize = SeniorFontSize.Large,
    val buttons: List<SeniorHomeButtonType> =
        listOf(
            SeniorHomeButtonType.Melon,
            SeniorHomeButtonType.Schedule,
        ) + InitialSeniorHomeGridButtons,
    val customButtonLabels: Map<SeniorHomeButtonType, String> = emptyMap(),
)

data class DisplayWeather(
    val temperatureCelsius: Int?,
    val status: String?,
    val description: String?,
    val observedAt: String?,
)

data class DisplayTodaySchedule(
    val title: String?,
    val description: String?,
    val count: Int,
    val displayType: String?,
    val id: Long?,
    val scheduledTime: String?,
)

data class DisplayOverview(
    val device: DisplayDevice?,
    val screenConfiguration: SeniorScreenConfiguration,
    val parentInfo: ParentInfo? = null,
    val todaySchedule: DisplayTodaySchedule? = null,
    val availableButtonTypes: Set<SeniorHomeButtonType> = emptySet(),
    val configuredButtonItems: List<DisplayHomeButton> = emptyList(),
    val availableButtonOptions: List<DisplayHomeButton> = emptyList(),
    val hasSavedButtonConfiguration: Boolean = true,
)
