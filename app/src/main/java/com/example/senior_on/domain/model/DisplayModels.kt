package com.example.senior_on.domain.model

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
    Spotify,
    Photo,
    Camera,
    Emergency,
}

data class SeniorScreenConfiguration(
    val fontSize: SeniorFontSize = SeniorFontSize.Large,
    val buttons: List<SeniorHomeButtonType> = listOf(
        SeniorHomeButtonType.Melon,
        SeniorHomeButtonType.Schedule,
        SeniorHomeButtonType.Call,
        SeniorHomeButtonType.Message,
        SeniorHomeButtonType.ChatBuddy,
        SeniorHomeButtonType.Medication,
        SeniorHomeButtonType.YouTube,
        SeniorHomeButtonType.Photo,
        SeniorHomeButtonType.Naver,
        SeniorHomeButtonType.Emergency,
    ),
    val customButtonLabels: Map<SeniorHomeButtonType, String> = emptyMap(),
)

data class DisplayOverview(
    val device: DisplayDevice?,
    val screenConfiguration: SeniorScreenConfiguration,
)
