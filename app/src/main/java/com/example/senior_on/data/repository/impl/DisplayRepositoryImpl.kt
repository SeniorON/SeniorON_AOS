package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.dto.ButtonRequest
import com.example.senior_on.data.remote.dto.ConnectionResponse
import com.example.senior_on.data.remote.dto.DeviceDetailResponse
import com.example.senior_on.data.remote.dto.HomeButtonResponse
import com.example.senior_on.data.remote.dto.HomeButtonSaveRequest
import com.example.senior_on.data.remote.dto.HomeFontSizeUpdateRequest
import com.example.senior_on.data.remote.dto.HomeResponse
import com.example.senior_on.data.remote.dto.SeniorProfileResponse
import com.example.senior_on.data.remote.dto.SeniorProfileUpdateRequest
import com.example.senior_on.data.remote.dto.SeniorProfileUpdateResponse
import com.example.senior_on.data.source.device.DeviceDataSource
import com.example.senior_on.data.source.display.DisplayDataSource
import com.example.senior_on.data.source.home.HomeDataSource
import com.example.senior_on.domain.model.display.DisplayDevice
import com.example.senior_on.domain.model.display.DisplayDeviceConnectionStatus
import com.example.senior_on.domain.model.display.DisplayOverview
import com.example.senior_on.domain.model.display.SeniorFontSize
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.domain.model.display.SeniorScreenConfiguration
import com.example.senior_on.domain.model.parent.CaregiverRelationship
import com.example.senior_on.domain.model.parent.ParentInfo
import com.example.senior_on.domain.repository.display.DisplayRepository
import java.time.LocalDate

class DisplayRepositoryImpl private constructor(
    private val homeDataSource: HomeDataSource?,
    private val deviceDataSource: DeviceDataSource?,
    private val mockDataSource: DisplayDataSource?,
) : DisplayRepository {
    constructor(
        homeDataSource: HomeDataSource,
        deviceDataSource: DeviceDataSource,
    ) : this(
        homeDataSource = homeDataSource,
        deviceDataSource = deviceDataSource,
        mockDataSource = null,
    )

    constructor(dataSource: DisplayDataSource) : this(
        homeDataSource = null,
        deviceDataSource = null,
        mockDataSource = dataSource,
    )

    override suspend fun getOverview(currentParentInfo: ParentInfo?): DisplayOverview {
        mockDataSource?.let { source ->
            return source.overview.value.copy(parentInfo = currentParentInfo)
        }

        return requireNotNull(homeDataSource).getHome().toDisplayOverview(
            currentParentInfo = currentParentInfo,
        )
    }

    override suspend fun getDevice(): DisplayDevice? {
        mockDataSource?.let { return it.overview.value.device }
        return requireNotNull(homeDataSource).getDevice().toDisplayDevice()
    }

    override suspend fun updateSeniorProfile(parentInfo: ParentInfo): ParentInfo {
        if (mockDataSource != null) {
            return parentInfo
        }

        val relationship = CaregiverRelationship.fromDisplayLabel(
            parentInfo.relationshipLabel,
        )
        val response = requireNotNull(homeDataSource).updateSeniorProfile(
            SeniorProfileUpdateRequest(
                name = parentInfo.name.trim(),
                relation = relationship.relation.name,
                customRelation = relationship.customRelation
                    ?.trim()
                    ?.takeIf(String::isNotEmpty),
                birth = parentInfo.birthDate.toString(),
                phoneNumber = parentInfo.phoneNumber.trim(),
                address = parentInfo.address.trim().takeIf(String::isNotEmpty),
                detailAddress = parentInfo.addressDetail
                    .trim()
                    .takeIf(String::isNotEmpty),
            )
        )

        return response.toParentInfo(parentInfo)
    }

    override suspend fun updateFontSize(fontSize: SeniorFontSize) {
        mockDataSource?.let {
            it.updateFontSize(fontSize)
            return
        }

        requireNotNull(homeDataSource).updateFontSize(
            HomeFontSizeUpdateRequest(font_size = fontSize.toApiValue())
        )
    }

    override suspend fun saveButtons(
        buttons: List<SeniorHomeButtonType>,
        customButtonLabels: Map<SeniorHomeButtonType, String>,
    ) {
        mockDataSource?.let {
            it.updateButtons(
                buttons = buttons.distinct(),
                customButtonLabels = customButtonLabels,
            )
            return
        }

        val distinctButtons = buttons.distinct()
        val musicButton = distinctButtons.firstOrNull(SeniorHomeButtonType::isMusic)
        val appButtons = distinctButtons.filter { button ->
            !button.isMusic() && button != SeniorHomeButtonType.Schedule
        }
        val missingButtons = appButtons.filterNot(BUTTON_API_METADATA::containsKey)
        require(missingButtons.isEmpty()) {
            "패키지 정보가 없는 앱은 저장할 수 없습니다: " +
                missingButtons.joinToString { it.name }
        }

        val requests = appButtons.mapIndexed { index, button ->
            val metadata = requireNotNull(BUTTON_API_METADATA[button])
            val buttonName = customButtonLabels[button]
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?: metadata.buttonName

            require(buttonName.length <= BUTTON_NAME_MAX_LENGTH) {
                "버튼 이름은 ${BUTTON_NAME_MAX_LENGTH}자 이하여야 합니다: $buttonName"
            }

            ButtonRequest(
                buttonOrder = index + 1,
                buttonName = buttonName,
                packageName = metadata.packageName,
            )
        }
        require(requests.map(ButtonRequest::packageName).distinct().size == requests.size) {
            "같은 앱은 중복해서 저장할 수 없습니다."
        }

        requireNotNull(homeDataSource).saveButtons(
            HomeButtonSaveRequest(
                musicApp = musicButton?.toMusicAppValue(),
                buttons = requests,
            )
        )
    }

    override suspend fun disconnectDevice() {
        mockDataSource?.let {
            it.disconnectDevice()
            return
        }
        requireNotNull(deviceDataSource).disconnect()
    }
}

private fun HomeResponse.toDisplayOverview(
    currentParentInfo: ParentInfo?,
): DisplayOverview {
    val musicButton = music_card
        ?.takeIf { it.enabled != false }
        ?.music_app
        .toMusicButtonType()
    val savedAppButtons = buttons
        .orEmpty()
        .sortedBy { it.button_order ?: Int.MAX_VALUE }
        .mapNotNull { response ->
            response.toButtonType()
                ?.takeIf {
                    !it.isMusic() && it != SeniorHomeButtonType.Schedule
                }
                ?.let { button -> button to response }
        }
        .distinctBy { (button, _) -> button }
    val customButtonLabels = savedAppButtons.mapNotNull { (button, response) ->
        val savedName = response.button_name
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?: return@mapNotNull null
        val defaultName = BUTTON_API_METADATA[button]?.buttonName
        if (savedName == defaultName) {
            null
        } else {
            button to savedName
        }
    }.toMap()
    val configuredButtons = buildList {
        musicButton?.let(::add)
        add(SeniorHomeButtonType.Schedule)
        addAll(savedAppButtons.map { (button, _) -> button })
        REQUIRED_INTERNAL_BUTTON_TYPES.forEach { requiredButton ->
            if (requiredButton !in this) {
                add(requiredButton)
            }
        }
    }.distinct()

    return DisplayOverview(
        device = connection.toDisplayDevice(),
        screenConfiguration = SeniorScreenConfiguration(
            fontSize = font_size.toSeniorFontSize(),
            buttons = configuredButtons,
            customButtonLabels = customButtonLabels,
        ),
        parentInfo = senior_profile.toParentInfo(currentParentInfo),
        availableButtonTypes = BUTTON_API_METADATA.keys + MUSIC_BUTTON_TYPES,
        hasSavedButtonConfiguration = buttons.orEmpty().isNotEmpty(),
    )
}

private fun SeniorProfileResponse?.toParentInfo(current: ParentInfo?): ParentInfo? {
    if (this == null) return current

    val resolvedName = name?.trim()?.takeIf(String::isNotEmpty)
        ?: current?.name
        ?: return null
    val resolvedBirthDate = birth.toLocalDateOrNull()
        ?: current?.birthDate
        ?: return null
    val resolvedPhoneNumber = phone?.trim()?.takeIf(String::isNotEmpty)
        ?: current?.phoneNumber
        ?: return null

    return ParentInfo(
        seniorId = current?.seniorId ?: 0L,
        name = resolvedName,
        relationshipLabel = relation.toRelationshipLabel(
            customRelation = null,
            fallback = current?.relationshipLabel,
        ),
        birthDate = resolvedBirthDate,
        phoneNumber = resolvedPhoneNumber,
        address = address?.trim() ?: current?.address.orEmpty(),
        addressDetail = current?.addressDetail.orEmpty(),
        addressLatitude = current?.addressLatitude,
        addressLongitude = current?.addressLongitude,
    )
}

private fun SeniorProfileUpdateResponse.toParentInfo(current: ParentInfo): ParentInfo =
    ParentInfo(
        seniorId = seniorId ?: current.seniorId,
        name = name?.trim()?.takeIf(String::isNotEmpty) ?: current.name,
        relationshipLabel = relation.toRelationshipLabel(
            customRelation = customRelation,
            fallback = current.relationshipLabel,
        ),
        birthDate = birth.toLocalDateOrNull() ?: current.birthDate,
        phoneNumber = phoneNumber
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?: current.phoneNumber,
        address = address?.trim() ?: current.address,
        addressDetail = detailAddress?.trim() ?: current.addressDetail,
        addressLatitude = current.addressLatitude,
        addressLongitude = current.addressLongitude,
    )

private fun ConnectionResponse?.toDisplayDevice(): DisplayDevice? {
    if (this == null) return null
    val deviceName = device_name?.trim().orEmpty()
    if (deviceName.isEmpty() && connected != true && battery == null) return null

    return DisplayDevice(
        id = deviceName.ifEmpty { DEFAULT_DEVICE_ID },
        name = deviceName.ifEmpty { DEFAULT_DEVICE_NAME },
        connectionStatus = if (connected == true) {
            DisplayDeviceConnectionStatus.Online
        } else {
            DisplayDeviceConnectionStatus.Offline
        },
        batteryLevelPercent = battery,
    )
}

private fun DeviceDetailResponse.toDisplayDevice(): DisplayDevice? {
    val resolvedName = deviceName?.trim().orEmpty()
    if (resolvedName.isEmpty() && connected != true) return null
    val normalizedStatus = connectionStatus?.trim()?.uppercase()
    val isOnline = connected == true ||
        networkConnected == true ||
        normalizedStatus == "ONLINE" ||
        normalizedStatus == "CONNECTED"

    return DisplayDevice(
        id = resolvedName.ifEmpty { DEFAULT_DEVICE_ID },
        name = resolvedName.ifEmpty { DEFAULT_DEVICE_NAME },
        connectionStatus = if (isOnline) {
            DisplayDeviceConnectionStatus.Online
        } else {
            DisplayDeviceConnectionStatus.Offline
        },
        batteryLevelPercent = batteryLevel,
        lastConnectedAtLabel = lastConnectedAt,
        lastLocationUpdatedAtLabel = lastLocationUpdatedAt,
    )
}

private fun HomeButtonResponse.toButtonType(): SeniorHomeButtonType? =
    resolveButtonType(
        name = button_name,
        actionType = action_type,
        actionValue = action_value,
    )

private fun resolveButtonType(
    name: String?,
    actionType: String?,
    actionValue: String?,
): SeniorHomeButtonType? {
    BUTTON_TYPE_BY_PACKAGE[actionValue?.trim()?.lowercase()]
        ?.let { return it }

    listOfNotNull(name, actionValue, actionType).forEach { candidate ->
        BUTTON_TYPE_BY_KEY[candidate.toButtonKey()]?.let { return it }
    }

    val actionKey = actionValue.toButtonKey()
    return when {
        "kakaotalk" in actionKey -> SeniorHomeButtonType.KakaoTalk
        "naverband" in actionKey -> SeniorHomeButtonType.NaverBand
        "navercafe" in actionKey -> SeniorHomeButtonType.NaverCafe
        "navermap" in actionKey -> SeniorHomeButtonType.NaverMap
        "kakaomap" in actionKey -> SeniorHomeButtonType.KakaoMap
        "kakaopay" in actionKey -> SeniorHomeButtonType.KakaoPay
        "naverpay" in actionKey -> SeniorHomeButtonType.NaverPay
        "samsungpay" in actionKey -> SeniorHomeButtonType.SamsungPay
        "youtube" in actionKey -> SeniorHomeButtonType.YouTube
        "netflix" in actionKey -> SeniorHomeButtonType.Netflix
        "spotify" in actionKey -> SeniorHomeButtonType.Spotify
        "melon" in actionKey -> SeniorHomeButtonType.Melon
        else -> null
    }
}

private fun SeniorFontSize.toApiValue(): String = when (this) {
    SeniorFontSize.Small -> "SMALL"
    SeniorFontSize.Normal -> "MEDIUM"
    SeniorFontSize.Large -> "LARGE"
}

private fun String?.toSeniorFontSize(): SeniorFontSize = when (
    this?.trim()?.uppercase()
) {
    "SMALL" -> SeniorFontSize.Small
    "MEDIUM", "NORMAL" -> SeniorFontSize.Normal
    else -> SeniorFontSize.Large
}

private fun SeniorHomeButtonType.isMusic(): Boolean =
    this == SeniorHomeButtonType.Melon || this == SeniorHomeButtonType.Spotify

private fun SeniorHomeButtonType.toMusicAppValue(): String = when (this) {
    SeniorHomeButtonType.Melon -> "MELON"
    SeniorHomeButtonType.Spotify -> "SPOTIFY"
    else -> error("$name 버튼은 음악 앱이 아닙니다.")
}

private fun String?.toMusicButtonType(): SeniorHomeButtonType? = when (
    this?.trim()?.uppercase()
) {
    "MELON" -> SeniorHomeButtonType.Melon
    "SPOTIFY" -> SeniorHomeButtonType.Spotify
    else -> null
}

private fun String?.toRelationshipLabel(
    customRelation: String?,
    fallback: String?,
): String = when (this?.trim()?.uppercase()) {
    "MOTHER" -> "어머니"
    "FATHER" -> "아버지"
    "GRANDPARENT" -> "조부모"
    "OTHER" -> customRelation
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?: fallback
        ?: "직접 작성"
    else -> this
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?: fallback
        ?: "부모님"
}

private fun String?.toLocalDateOrNull(): LocalDate? =
    this?.trim()?.takeIf(String::isNotEmpty)?.let { value ->
        runCatching { LocalDate.parse(value) }.getOrNull()
    }

private fun String?.toButtonKey(): String =
    this.orEmpty().lowercase().filter(Char::isLetterOrDigit)

private val MUSIC_BUTTON_TYPES = setOf(
    SeniorHomeButtonType.Melon,
    SeniorHomeButtonType.Spotify,
)

private val REQUIRED_INTERNAL_BUTTON_TYPES = listOf(
    SeniorHomeButtonType.ChatBuddy,
    SeniorHomeButtonType.Medication,
    SeniorHomeButtonType.Emergency,
)

private data class ButtonApiMetadata(
    val buttonName: String,
    val packageName: String,
)

private val BUTTON_API_METADATA = mapOf(
    SeniorHomeButtonType.ChatBuddy to ButtonApiMetadata(
        buttonName = "말벗",
        packageName = "COMPANION",
    ),
    SeniorHomeButtonType.Medication to ButtonApiMetadata(
        buttonName = "복약",
        packageName = "MEDICATION",
    ),
    SeniorHomeButtonType.Emergency to ButtonApiMetadata(
        buttonName = "긴급알림",
        packageName = "EMERGENCY",
    ),
    SeniorHomeButtonType.Call to ButtonApiMetadata(
        buttonName = "전화",
        packageName = "com.samsung.android.dialer",
    ),
    SeniorHomeButtonType.Message to ButtonApiMetadata(
        buttonName = "메시지",
        packageName = "com.samsung.android.messaging",
    ),
    SeniorHomeButtonType.Calendar to ButtonApiMetadata(
        buttonName = "캘린더",
        packageName = "com.samsung.android.calendar",
    ),
    SeniorHomeButtonType.Alarm to ButtonApiMetadata(
        buttonName = "알람",
        packageName = "com.sec.android.app.clockpackage",
    ),
    SeniorHomeButtonType.Memo to ButtonApiMetadata(
        buttonName = "메모",
        packageName = "com.samsung.android.app.notes",
    ),
    SeniorHomeButtonType.Recorder to ButtonApiMetadata(
        buttonName = "녹음",
        packageName = "com.sec.android.app.voicenote",
    ),
    SeniorHomeButtonType.Calculator to ButtonApiMetadata(
        buttonName = "계산기",
        packageName = "com.sec.android.app.popupcalculator",
    ),
    SeniorHomeButtonType.Settings to ButtonApiMetadata(
        buttonName = "설정",
        packageName = "com.android.settings",
    ),
    SeniorHomeButtonType.Flashlight to ButtonApiMetadata(
        buttonName = "손전등",
        packageName = "com.android.systemui",
    ),
    SeniorHomeButtonType.KakaoTalk to ButtonApiMetadata(
        buttonName = "카카오톡",
        packageName = "com.kakao.talk",
    ),
    SeniorHomeButtonType.NaverBand to ButtonApiMetadata(
        buttonName = "네이버밴드",
        packageName = "com.nhn.android.band",
    ),
    SeniorHomeButtonType.NaverCafe to ButtonApiMetadata(
        buttonName = "네이버카페",
        packageName = "com.nhn.android.navercafe",
    ),
    SeniorHomeButtonType.Line to ButtonApiMetadata(
        buttonName = "라인",
        packageName = "jp.naver.line.android",
    ),
    SeniorHomeButtonType.YouTube to ButtonApiMetadata(
        buttonName = "유튜브",
        packageName = "com.google.android.youtube",
    ),
    SeniorHomeButtonType.Naver to ButtonApiMetadata(
        buttonName = "네이버",
        packageName = "com.nhn.android.search",
    ),
    SeniorHomeButtonType.Daum to ButtonApiMetadata(
        buttonName = "다음",
        packageName = "net.daum.android.daum",
    ),
    SeniorHomeButtonType.Google to ButtonApiMetadata(
        buttonName = "구글",
        packageName = "com.google.android.googlequicksearchbox",
    ),
    SeniorHomeButtonType.Tving to ButtonApiMetadata(
        buttonName = "티빙",
        packageName = "net.cj.cjhv.gs.tving",
    ),
    SeniorHomeButtonType.Netflix to ButtonApiMetadata(
        buttonName = "넷플릭스",
        packageName = "com.netflix.mediaclient",
    ),
    SeniorHomeButtonType.NaverMap to ButtonApiMetadata(
        buttonName = "네이버지도",
        packageName = "com.nhn.android.nmap",
    ),
    SeniorHomeButtonType.KakaoMap to ButtonApiMetadata(
        buttonName = "카카오맵",
        packageName = "net.daum.android.map",
    ),
    SeniorHomeButtonType.KakaoT to ButtonApiMetadata(
        buttonName = "카카오T",
        packageName = "com.kakao.taxi",
    ),
    SeniorHomeButtonType.TMap to ButtonApiMetadata(
        buttonName = "티맵",
        packageName = "com.skt.tmap.ku",
    ),
    SeniorHomeButtonType.KorailTalk to ButtonApiMetadata(
        buttonName = "코레일톡",
        packageName = "com.korail.talk",
    ),
    SeniorHomeButtonType.Toss to ButtonApiMetadata(
        buttonName = "토스",
        packageName = "viva.republica.toss",
    ),
    SeniorHomeButtonType.KakaoPay to ButtonApiMetadata(
        buttonName = "카카오페이",
        packageName = "com.kakaopay.app",
    ),
    SeniorHomeButtonType.NaverPay to ButtonApiMetadata(
        buttonName = "네이버페이",
        packageName = "com.naverfin.payapp",
    ),
    SeniorHomeButtonType.SamsungWallet to ButtonApiMetadata(
        buttonName = "삼성월렛",
        packageName = "com.samsung.android.spay",
    ),
    SeniorHomeButtonType.SamsungPay to ButtonApiMetadata(
        buttonName = "삼성페이",
        packageName = "com.samsung.android.spay",
    ),
    SeniorHomeButtonType.CashWalk to ButtonApiMetadata(
        buttonName = "캐시워크",
        packageName = "com.cashwalk.cashwalk",
    ),
    SeniorHomeButtonType.Weather to ButtonApiMetadata(
        buttonName = "날씨알리미",
        packageName = "kr.go.kma.weatherapp",
    ),
    SeniorHomeButtonType.Coupang to ButtonApiMetadata(
        buttonName = "쿠팡",
        packageName = "com.coupang.mobile",
    ),
    SeniorHomeButtonType.Karrot to ButtonApiMetadata(
        buttonName = "당근",
        packageName = "com.towneers.www",
    ),
    SeniorHomeButtonType.Baemin to ButtonApiMetadata(
        buttonName = "배달의민족",
        packageName = "com.sampleapp",
    ),
    SeniorHomeButtonType.Yogiyo to ButtonApiMetadata(
        buttonName = "요기요",
        packageName = "com.fineapp.yogiyo",
    ),
    SeniorHomeButtonType.CoupangEats to ButtonApiMetadata(
        buttonName = "쿠팡이츠",
        packageName = "com.coupang.mobile.eats",
    ),
    SeniorHomeButtonType.HomeShopping to ButtonApiMetadata(
        buttonName = "홈쇼핑",
        packageName = "com.buzzni.android.subapp.shoppingmoa",
    ),
    SeniorHomeButtonType.GoStop to ButtonApiMetadata(
        buttonName = "고스톱",
        packageName = "com.buzzpowder.gostop_basic",
    ),
    SeniorHomeButtonType.Photo to ButtonApiMetadata(
        buttonName = "사진",
        packageName = "com.sec.android.gallery3d",
    ),
    SeniorHomeButtonType.Camera to ButtonApiMetadata(
        buttonName = "카메라",
        packageName = "com.sec.android.app.camera",
    ),
)

private val BUTTON_TYPE_BY_PACKAGE: Map<String, SeniorHomeButtonType> = buildMap {
    BUTTON_API_METADATA.forEach { (button, metadata) ->
        val packageName = metadata.packageName.lowercase()
        if (packageName !in this) {
            put(packageName, button)
        }
    }
}

private val BUTTON_TYPE_BY_KEY: Map<String, SeniorHomeButtonType> = buildMap {
    fun register(
        type: SeniorHomeButtonType,
        vararg aliases: String,
    ) {
        (aliases.asList() + type.name).forEach { alias ->
            put(alias.toButtonKey(), type)
        }
    }

    register(SeniorHomeButtonType.Call, "전화", "통화", "CALL", "tel")
    register(SeniorHomeButtonType.Message, "메시지", "문자", "MESSAGE", "sms")
    register(SeniorHomeButtonType.Calendar, "캘린더")
    register(SeniorHomeButtonType.Alarm, "알림", "알람")
    register(SeniorHomeButtonType.Memo, "메모")
    register(SeniorHomeButtonType.Recorder, "녹음", "음성 녹음")
    register(SeniorHomeButtonType.Calculator, "계산기")
    register(SeniorHomeButtonType.Settings, "설정")
    register(SeniorHomeButtonType.Flashlight, "손전등")
    register(SeniorHomeButtonType.KakaoTalk, "카카오톡")
    register(SeniorHomeButtonType.NaverBand, "네이버 밴드", "밴드")
    register(SeniorHomeButtonType.NaverCafe, "네이버 카페")
    register(SeniorHomeButtonType.Line, "라인")
    register(SeniorHomeButtonType.ChatBuddy, "말벗", "AI 말벗")
    register(SeniorHomeButtonType.Medication, "복약", "복약 관리")
    register(SeniorHomeButtonType.Schedule, "일정", "일정 관리")
    register(SeniorHomeButtonType.YouTube, "유튜브")
    register(SeniorHomeButtonType.Naver, "네이버")
    register(SeniorHomeButtonType.Daum, "다음")
    register(SeniorHomeButtonType.Google, "구글")
    register(SeniorHomeButtonType.Tving, "티빙")
    register(SeniorHomeButtonType.Netflix, "넷플릭스")
    register(SeniorHomeButtonType.NaverMap, "네이버 지도")
    register(SeniorHomeButtonType.KakaoMap, "카카오맵", "카카오 지도")
    register(SeniorHomeButtonType.KakaoT, "카카오 T", "카카오티")
    register(SeniorHomeButtonType.TMap, "티맵")
    register(SeniorHomeButtonType.KorailTalk, "코레일톡")
    register(SeniorHomeButtonType.Toss, "토스")
    register(SeniorHomeButtonType.KakaoPay, "카카오페이")
    register(SeniorHomeButtonType.NaverPay, "네이버페이")
    register(SeniorHomeButtonType.SamsungWallet, "삼성월렛")
    register(SeniorHomeButtonType.SamsungPay, "삼성페이")
    register(SeniorHomeButtonType.CashWalk, "캐시워크")
    register(SeniorHomeButtonType.Weather, "기상청 날씨알리미", "날씨")
    register(SeniorHomeButtonType.Coupang, "쿠팡")
    register(SeniorHomeButtonType.Karrot, "당근", "당근마켓")
    register(SeniorHomeButtonType.Baemin, "배달의민족", "배민")
    register(SeniorHomeButtonType.Yogiyo, "요기요")
    register(SeniorHomeButtonType.CoupangEats, "쿠팡이츠")
    register(SeniorHomeButtonType.HomeShopping, "홈쇼핑")
    register(SeniorHomeButtonType.GoStop, "고스톱·맞고", "고스톱", "맞고")
    register(SeniorHomeButtonType.Melon, "멜론", "멜론(Melon)")
    register(SeniorHomeButtonType.Spotify, "스포티파이", "스포티파이(Spotify)")
    register(SeniorHomeButtonType.Photo, "사진", "갤러리")
    register(SeniorHomeButtonType.Camera, "카메라")
    register(SeniorHomeButtonType.Emergency, "긴급알림", "긴급 알림", "SOS")
}

private const val DEFAULT_DEVICE_ID = "connected-senior-device"
private const val DEFAULT_DEVICE_NAME = "시니어폰"
private const val BUTTON_NAME_MAX_LENGTH = 6
