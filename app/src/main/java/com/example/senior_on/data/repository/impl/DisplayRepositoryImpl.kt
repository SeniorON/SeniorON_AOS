package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.dto.ButtonRequest
import com.example.senior_on.data.remote.dto.ButtonOptionResponse
import com.example.senior_on.data.remote.dto.ConnectionResponse
import com.example.senior_on.data.remote.dto.DeviceDetailResponse
import com.example.senior_on.data.remote.dto.FamilyMemberResponse
import com.example.senior_on.data.remote.dto.HomeButtonResponse
import com.example.senior_on.data.remote.dto.HomeButtonSaveRequest
import com.example.senior_on.data.remote.dto.HomeFontSizeUpdateRequest
import com.example.senior_on.data.remote.dto.HomeResponse
import com.example.senior_on.data.remote.dto.MusicCardResponse
import com.example.senior_on.data.remote.dto.SeniorHomeResponse
import com.example.senior_on.data.remote.dto.SeniorProfileResponse
import com.example.senior_on.data.remote.dto.SeniorProfileUpdateRequest
import com.example.senior_on.data.remote.dto.SeniorProfileUpdateResponse
import com.example.senior_on.data.remote.dto.TodayScheduleResponse
import com.example.senior_on.data.remote.dto.WeatherResponse
import com.example.senior_on.data.source.device.DeviceDataSource
import com.example.senior_on.data.source.display.DisplayDataSource
import com.example.senior_on.data.source.family.RemoteFamilySource
import com.example.senior_on.data.source.home.HomeDataSource
import com.example.senior_on.domain.model.display.DisplayDevice
import com.example.senior_on.domain.model.display.DisplayDeviceConnectionStatus
import com.example.senior_on.domain.model.display.DisplayHomeButton
import com.example.senior_on.domain.model.display.DisplayOverview
import com.example.senior_on.domain.model.display.DisplayTodaySchedule
import com.example.senior_on.domain.model.display.DisplayWeather
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
    private val familyDataSource: RemoteFamilySource?,
    private val mockDataSource: DisplayDataSource?,
) : DisplayRepository {
    constructor(
        homeDataSource: HomeDataSource,
        deviceDataSource: DeviceDataSource,
        familyDataSource: RemoteFamilySource? = null,
    ) : this(
        homeDataSource = homeDataSource,
        deviceDataSource = deviceDataSource,
        familyDataSource = familyDataSource,
        mockDataSource = null,
    )

    constructor(dataSource: DisplayDataSource) : this(
        homeDataSource = null,
        deviceDataSource = null,
        familyDataSource = null,
        mockDataSource = dataSource,
    )

    override suspend fun canCurrentUserEditScreen(): Boolean {
        if (mockDataSource != null) return true

        return familyDataSource
            ?.getMembers()
            .orEmpty()
            .canCurrentUserEditScreen()
    }

    override suspend fun getOverview(currentParentInfo: ParentInfo?): DisplayOverview {
        mockDataSource?.let { source ->
            val overview = source.overview.value.copy(parentInfo = currentParentInfo)
            return if (overview.configuredButtonItems.isNotEmpty()) {
                overview
            } else {
                overview.copy(
                    configuredButtonItems = overview.screenConfiguration.buttons
                        .map { button ->
                            button.toDisplayHomeButton(
                                customName = overview.screenConfiguration
                                    .customButtonLabels[button],
                            )
                        },
                )
            }
        }

        val source = requireNotNull(homeDataSource)
        val buttonOptions = runCatching { source.getButtonOptions() }
            .getOrDefault(emptyList())
        return source.getHome().toDisplayOverview(
            currentParentInfo = currentParentInfo,
            buttonOptions = buttonOptions,
        )
    }

    override suspend fun getSeniorScreenConfiguration(): SeniorScreenConfiguration {
        mockDataSource?.let { source ->
            return source.overview.value.screenConfiguration
        }

        return requireNotNull(homeDataSource)
            .getSeniorHome()
            .toSeniorScreenConfiguration()
    }

    override suspend fun getWeather(
        latitude: Double,
        longitude: Double,
    ): DisplayWeather {
        if (mockDataSource != null) {
            return DisplayWeather(
                temperatureCelsius = 20,
                status = "CLEAR",
                description = "맑음",
                observedAt = null,
            )
        }

        return requireNotNull(homeDataSource)
            .getWeather(latitude = latitude, longitude = longitude)
            .toDisplayWeather()
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
        val musicButtons = distinctButtons.filter(SeniorHomeButtonType::isMusic)
        require(musicButtons.size <= 1) {
            "음악 앱은 하나만 선택할 수 있습니다."
        }
        val musicButton = musicButtons.singleOrNull()
        val normalizedButtons = distinctButtons.normalizeButtonsForSave()
        require(normalizedButtons.size >= MINIMUM_BUTTON_COUNT) {
            "홈 화면 일반 버튼은 최소 ${MINIMUM_BUTTON_COUNT}개 이상이어야 합니다."
        }
        require(normalizedButtons.size <= MAXIMUM_BUTTON_COUNT) {
            "홈 화면 일반 버튼은 최대 ${MAXIMUM_BUTTON_COUNT}개까지 저장할 수 있습니다."
        }
        val missingButtons = normalizedButtons.filterNot(
            BUTTON_API_METADATA::containsKey
        )
        require(missingButtons.isEmpty()) {
            "액션 정보가 없는 버튼은 저장할 수 없습니다: " +
                missingButtons.joinToString { it.name }
        }

        val requests = normalizedButtons.mapIndexed { index, button ->
            val metadata = requireNotNull(BUTTON_API_METADATA[button])
            val buttonName = customButtonLabels[button]
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?: metadata.buttonName

            ButtonRequest(
                buttonOrder = index + 1,
                buttonName = buttonName,
                actionType = button.toApiActionType(),
                actionValue = button.toApiActionValue(),
                packageName = metadata.packageName,
            )
        }
        require(
            requests
                .map { Triple(it.actionType, it.actionValue, it.packageName) }
                .distinct()
                .size == requests.size
        ) {
            "같은 기능은 중복해서 저장할 수 없습니다."
        }

        requireNotNull(homeDataSource).saveButtons(
            HomeButtonSaveRequest(
                musicApp = musicButton?.toMusicAppValue(),
                buttons = requests,
            )
        )
    }

    override suspend fun saveButtons(buttons: List<DisplayHomeButton>) {
        mockDataSource?.let { source ->
            val knownButtons = buttons.mapNotNull(DisplayHomeButton::type)
            source.updateButtons(
                buttons = knownButtons,
                customButtonLabels = buttons.mapNotNull { button ->
                    val type = button.type ?: return@mapNotNull null
                    val defaultName = BUTTON_API_METADATA[type]?.buttonName
                    button.name.takeIf { it != defaultName }?.let { type to it }
                }.toMap(),
            )
            return
        }

        val distinctButtons = buttons.distinctBy(DisplayHomeButton::stableKey)
        val musicButtons = distinctButtons.filter(DisplayHomeButton::isMusicButton)
        require(musicButtons.size <= 1) {
            "음악 앱은 하나만 선택할 수 있습니다."
        }
        val normalizedButtons = distinctButtons.normalizeButtonItemsForSave()
        require(normalizedButtons.size >= MINIMUM_BUTTON_COUNT) {
            "홈 화면 일반 버튼은 최소 ${MINIMUM_BUTTON_COUNT}개 이상이어야 합니다."
        }
        require(normalizedButtons.size <= MAXIMUM_BUTTON_COUNT) {
            "홈 화면 일반 버튼은 최대 ${MAXIMUM_BUTTON_COUNT}개까지 저장할 수 있습니다."
        }

        val requests = normalizedButtons.mapIndexed { index, button ->
            val actionType = button.actionType.trim().uppercase()
            val packageName = button.packageName?.trim()?.takeIf(String::isNotEmpty)
            require(actionType == DEFAULT_ACTION_TYPE || actionType == APP_ACTION_TYPE) {
                "지원하지 않는 액션 타입입니다: ${button.actionType}"
            }
            require(button.actionValue.isNotBlank()) {
                "액션 값이 없는 버튼은 저장할 수 없습니다: ${button.name}"
            }
            if (actionType == APP_ACTION_TYPE) {
                require(packageName != null) {
                    "앱 버튼은 패키지명이 필요합니다: ${button.name}"
                }
            }

            val buttonName = button.name.trim()
            require(buttonName.isNotEmpty()) {
                "버튼 이름은 비어 있을 수 없습니다."
            }

            ButtonRequest(
                buttonOrder = index + 1,
                buttonName = buttonName,
                actionType = actionType,
                actionValue = button.actionValue.trim(),
                packageName = if (actionType == APP_ACTION_TYPE) packageName else null,
            )
        }
        require(requests.distinctBy { request ->
            Triple(request.actionType, request.actionValue, request.packageName)
        }.size == requests.size) {
            "같은 기능은 중복해서 저장할 수 없습니다."
        }

        requireNotNull(homeDataSource).saveButtons(
            HomeButtonSaveRequest(
                musicApp = musicButtons.singleOrNull()?.type?.toMusicAppValue(),
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

internal fun List<FamilyMemberResponse>.canCurrentUserEditScreen(): Boolean =
    firstOrNull { member -> member.me == true }
        ?.managerType
        ?.trim()
        .equals(PRIMARY_MANAGER_TYPE, ignoreCase = true)

private fun HomeResponse.toDisplayOverview(
    currentParentInfo: ParentInfo?,
    buttonOptions: List<ButtonOptionResponse>,
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
        addAll(
            savedAppButtons
                .map { (button, _) -> button }
                .normalizeButtonsForSave()
        )
    }.distinct()

    val configuredButtonItems = buildList {
        music_card.toDisplayHomeButton()?.let(::add)
        add(SeniorHomeButtonType.Schedule.toDisplayHomeButton())
        buttons.orEmpty()
            .sortedBy { it.button_order ?: Int.MAX_VALUE }
            .mapNotNullTo(this, HomeButtonResponse::toDisplayHomeButton)
    }.distinctBy(DisplayHomeButton::stableKey)

    return DisplayOverview(
        device = connection.toDisplayDevice(),
        screenConfiguration = SeniorScreenConfiguration(
            fontSize = font_size.toSeniorFontSize(),
            buttons = configuredButtons,
            customButtonLabels = customButtonLabels,
        ),
        parentInfo = senior_profile.toParentInfo(currentParentInfo),
        todaySchedule = today_schedule.toDisplayTodaySchedule(),
        availableButtonTypes = BUTTON_API_METADATA.keys + MUSIC_BUTTON_TYPES,
        configuredButtonItems = configuredButtonItems,
        availableButtonOptions = (
            buttonOptions
                .mapNotNull(ButtonOptionResponse::toDisplayHomeButton)
                .filter { option ->
                    option.actionType.equals(DEFAULT_ACTION_TYPE, ignoreCase = true) &&
                        option.actionValue in DEFAULT_INTENT_ACTION_VALUES
                } +
                DEFAULT_INTENT_BUTTON_OPTIONS
            )
            .distinctBy(DisplayHomeButton::stableKey),
        hasSavedButtonConfiguration = buttons.orEmpty().isNotEmpty(),
    )
}

private fun SeniorHomeResponse.toSeniorScreenConfiguration():
    SeniorScreenConfiguration {
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
        addAll(
            savedAppButtons
                .map { (button, _) -> button }
                .normalizeButtonsForSave()
        )
    }.distinct()

    return SeniorScreenConfiguration(
        fontSize = font_size.toSeniorFontSize(),
        buttons = configuredButtons,
        customButtonLabels = customButtonLabels,
    )
}

private fun WeatherResponse.toDisplayWeather(): DisplayWeather = DisplayWeather(
    temperatureCelsius = temperature,
    status = weatherStatus?.trim()?.takeIf(String::isNotEmpty),
    description = weatherText?.trim()?.takeIf(String::isNotEmpty),
    observedAt = observedAt,
)

private fun TodayScheduleResponse?.toDisplayTodaySchedule(): DisplayTodaySchedule? {
    if (this == null) return null

    val hasScheduleContent =
        !title.isNullOrBlank() ||
        !description.isNullOrBlank() ||
        !scheduled_time.isNullOrBlank()
    val normalizedCount = schedule_count ?: if (hasScheduleContent) 1 else 0
    val hasSchedule = normalizedCount > 0 || hasScheduleContent
    if (!hasSchedule) return null

    return DisplayTodaySchedule(
        title = title?.trim()?.takeIf(String::isNotEmpty),
        description = description?.trim()?.takeIf(String::isNotEmpty),
        count = normalizedCount.coerceAtLeast(1),
        displayType = display_type?.trim()?.takeIf(String::isNotEmpty),
        id = schedule_id,
        scheduledTime = scheduled_time?.trim()?.takeIf(String::isNotEmpty),
    )
}

private fun SeniorProfileResponse?.toParentInfo(current: ParentInfo?): ParentInfo? {
    if (this == null || isEmptyProfile()) return null

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
        seniorId = senior_id ?: current?.seniorId ?: 0L,
        name = resolvedName,
        relationshipLabel = relation.toRelationshipLabel(
            customRelation = null,
            fallback = current?.relationshipLabel,
        ),
        birthDate = resolvedBirthDate,
        phoneNumber = resolvedPhoneNumber,
        address = address?.trim() ?: current?.address.orEmpty(),
        addressDetail = detail_address?.trim() ?: current?.addressDetail.orEmpty(),
        addressLatitude = current?.addressLatitude,
        addressLongitude = current?.addressLongitude,
    )
}

private fun SeniorProfileResponse.isEmptyProfile(): Boolean =
    senior_id == null &&
        name.isNullOrBlank() &&
        relation.isNullOrBlank() &&
        birth.isNullOrBlank() &&
        address.isNullOrBlank() &&
        phone.isNullOrBlank() &&
        detail_address.isNullOrBlank()

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
    val normalizedStatus = connection_status.normalizedConnectionStatus()
    if (normalizedStatus == DEVICE_STATUS_DISCONNECTED) return null

    val hasKnownDevice =
        deviceName.isNotEmpty() ||
            connected == true ||
            battery != null ||
            normalizedStatus == DEVICE_STATUS_ONLINE ||
            normalizedStatus == DEVICE_STATUS_OFFLINE
    if (!hasKnownDevice) return null

    val isOnline = when (normalizedStatus) {
        DEVICE_STATUS_ONLINE,
        DEVICE_STATUS_CONNECTED -> true
        DEVICE_STATUS_OFFLINE -> false
        else -> connected == true
    }

    return DisplayDevice(
        id = deviceName.ifEmpty { DEFAULT_DEVICE_ID },
        name = deviceName.ifEmpty { DEFAULT_DEVICE_NAME },
        connectionStatus = if (isOnline) {
            DisplayDeviceConnectionStatus.Online
        } else {
            DisplayDeviceConnectionStatus.Offline
        },
        batteryLevelPercent = battery,
    )
}

private fun DeviceDetailResponse.toDisplayDevice(): DisplayDevice? {
    val resolvedName = deviceName?.trim().orEmpty()
    val hasConnectionHistory =
        !lastConnectedAt.isNullOrBlank() ||
            !lastLocationUpdatedAt.isNullOrBlank()
    val hasKnownDevice =
        resolvedName.isNotEmpty() ||
            connected == true ||
            batteryLevel != null ||
            hasConnectionHistory
    if (!hasKnownDevice) return null
    val normalizedStatus = connectionStatus?.trim()?.uppercase()
    val isOnline = connected == true ||
        networkConnected == true ||
        normalizedStatus == DEVICE_STATUS_ONLINE ||
        normalizedStatus == DEVICE_STATUS_CONNECTED

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

private fun String?.normalizedConnectionStatus(): String? =
    this?.trim()?.uppercase()

private fun HomeButtonResponse.toButtonType(): SeniorHomeButtonType? =
    resolveButtonType(
        name = button_name,
        actionType = action_type,
        actionValue = action_value,
        packageName = package_name,
    )

private fun HomeButtonResponse.toDisplayHomeButton(): DisplayHomeButton? {
    val resolvedType = toButtonType()
    val resolvedActionType = action_type
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?: resolvedType?.toApiActionType()
        ?: return null
    val resolvedActionValue = action_value
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?: resolvedType?.toApiActionValue()
        ?: return null
    val resolvedName = button_name
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?: resolvedType?.let { BUTTON_API_METADATA[it]?.buttonName }
        ?: return null

    return DisplayHomeButton(
        id = button_id ?: 0L,
        order = button_order ?: 0,
        name = resolvedName,
        icon = icon,
        actionType = resolvedActionType,
        actionValue = resolvedActionValue,
        packageName = package_name?.trim()?.takeIf(String::isNotEmpty),
        type = resolvedType,
    )
}

private fun ButtonOptionResponse.toDisplayHomeButton(): DisplayHomeButton? {
    val resolvedName = button_name?.trim()?.takeIf(String::isNotEmpty)
        ?: return null
    val resolvedActionType = action_type?.trim()?.takeIf(String::isNotEmpty)
        ?: return null
    val resolvedActionValue = action_value
        ?.canonicalDefaultActionValue()
        ?.takeIf(String::isNotEmpty)
        ?: return null
    val resolvedType = resolveButtonType(
        name = resolvedName,
        actionType = resolvedActionType,
        actionValue = resolvedActionValue,
        packageName = null,
    )

    return DisplayHomeButton(
        optionId = option_id,
        name = resolvedName,
        icon = icon,
        actionType = resolvedActionType,
        actionValue = resolvedActionValue,
        type = resolvedType,
    )
}

private fun MusicCardResponse?.toDisplayHomeButton(): DisplayHomeButton? {
    if (this == null || enabled == false) return null
    val musicType = music_app.toMusicButtonType() ?: return null
    return DisplayHomeButton(
        name = app_name?.trim()?.takeIf(String::isNotEmpty)
            ?: musicType.toMusicAppValue(),
        icon = icon,
        actionType = action_type?.trim()?.takeIf(String::isNotEmpty)
            ?: APP_ACTION_TYPE,
        actionValue = action_value?.trim()?.takeIf(String::isNotEmpty)
            ?: musicType.toMusicAppValue(),
        packageName = package_name?.trim()?.takeIf(String::isNotEmpty),
        type = musicType,
    )
}

private fun resolveButtonType(
    name: String?,
    actionType: String?,
    actionValue: String?,
    packageName: String?,
): SeniorHomeButtonType? {
    BUTTON_TYPE_BY_PACKAGE[packageName?.trim()?.lowercase()]
        ?.let { return it }
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
        "samsungmusic" in actionKey -> SeniorHomeButtonType.SamsungMusic
        "kakaomusic" in actionKey -> SeniorHomeButtonType.KakaoMusic
        "youtubemusic" in actionKey -> SeniorHomeButtonType.YouTubeMusic
        "youtube" in actionKey -> SeniorHomeButtonType.YouTube
        "netflix" in actionKey -> SeniorHomeButtonType.Netflix
        "spotify" in actionKey -> SeniorHomeButtonType.Spotify
        "melon" in actionKey -> SeniorHomeButtonType.Melon
        "genie" in actionKey -> SeniorHomeButtonType.Genie
        "flo" in actionKey -> SeniorHomeButtonType.Flo
        "vibe" in actionKey -> SeniorHomeButtonType.Vibe
        "bugs" in actionKey -> SeniorHomeButtonType.Bugs
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

private fun SeniorHomeButtonType.isMusic(): Boolean = this in MUSIC_BUTTON_TYPES

private fun SeniorHomeButtonType.toMusicAppValue(): String = when (this) {
    SeniorHomeButtonType.Melon -> "MELON"
    SeniorHomeButtonType.Genie -> "GENIE"
    SeniorHomeButtonType.YouTubeMusic -> "YOUTUBE_MUSIC"
    SeniorHomeButtonType.Spotify -> "SPOTIFY"
    SeniorHomeButtonType.Flo -> "FLO"
    SeniorHomeButtonType.Vibe -> "VIBE"
    SeniorHomeButtonType.Bugs -> "BUGS"
    SeniorHomeButtonType.SamsungMusic -> "SAMSUNG_MUSIC"
    SeniorHomeButtonType.KakaoMusic -> "KAKAO_MUSIC"
    else -> error("$name 버튼은 음악 앱이 아닙니다.")
}

private fun String?.toMusicButtonType(): SeniorHomeButtonType? = when (
    this?.trim()?.uppercase()
) {
    "MELON" -> SeniorHomeButtonType.Melon
    "GENIE" -> SeniorHomeButtonType.Genie
    "YOUTUBE_MUSIC" -> SeniorHomeButtonType.YouTubeMusic
    "SPOTIFY" -> SeniorHomeButtonType.Spotify
    "FLO" -> SeniorHomeButtonType.Flo
    "VIBE" -> SeniorHomeButtonType.Vibe
    "BUGS" -> SeniorHomeButtonType.Bugs
    "SAMSUNG_MUSIC" -> SeniorHomeButtonType.SamsungMusic
    "KAKAO_MUSIC" -> SeniorHomeButtonType.KakaoMusic
    else -> null
}

private fun List<SeniorHomeButtonType>.normalizeButtonsForSave():
    List<SeniorHomeButtonType> {
    val gridButtons = distinct()
        .filterNot { button ->
            button.isMusic() ||
                button == SeniorHomeButtonType.Schedule ||
                button == SeniorHomeButtonType.Emergency
        }
        .toMutableList()
    REQUIRED_GRID_BUTTON_TYPES.forEach { requiredButton ->
        if (requiredButton !in gridButtons) {
            gridButtons.add(requiredButton)
        }
    }
    val emergencyIndex = FIXED_EMERGENCY_GRID_INDEX.coerceAtMost(
        gridButtons.size
    )
    gridButtons.add(
        index = emergencyIndex,
        element = SeniorHomeButtonType.Emergency,
    )

    return gridButtons
}

private fun List<DisplayHomeButton>.normalizeButtonItemsForSave():
    List<DisplayHomeButton> {
    val gridButtons = distinctBy(DisplayHomeButton::stableKey)
        .filterNot { button ->
            button.isMusicButton() ||
                button.isDefaultAction("SCHEDULE") ||
                button.isDefaultAction("EMERGENCY")
        }
        .toMutableList()

    REQUIRED_GRID_BUTTON_TYPES.forEach { requiredType ->
        if (gridButtons.none { it.type == requiredType }) {
            gridButtons.add(requiredType.toDisplayHomeButton())
        }
    }
    val emergency = firstOrNull { it.isDefaultAction("EMERGENCY") }
        ?: SeniorHomeButtonType.Emergency.toDisplayHomeButton()
    gridButtons.add(
        index = FIXED_EMERGENCY_GRID_INDEX.coerceAtMost(gridButtons.size),
        element = emergency,
    )
    return gridButtons
}

private fun DisplayHomeButton.isMusicButton(): Boolean =
    type in MUSIC_BUTTON_TYPES

private fun SeniorHomeButtonType.toDisplayHomeButton(
    customName: String? = null,
): DisplayHomeButton {
    val isMusic = this in MUSIC_BUTTON_TYPES
    val metadata = BUTTON_API_METADATA[this]
    return DisplayHomeButton(
        name = customName?.trim()?.takeIf(String::isNotEmpty)
            ?: metadata?.buttonName
            ?: toMusicAppValue(),
        actionType = if (isMusic) APP_ACTION_TYPE else toApiActionType(),
        actionValue = if (isMusic) toMusicAppValue() else toApiActionValue(),
        packageName = if (isMusic) null else metadata?.packageName,
        type = this,
    )
}

private fun SeniorHomeButtonType.toApiActionType(): String =
    if (this in DEFAULT_BUTTON_ACTION_VALUES) {
        DEFAULT_ACTION_TYPE
    } else {
        APP_ACTION_TYPE
    }

private fun SeniorHomeButtonType.toApiActionValue(): String =
    DEFAULT_BUTTON_ACTION_VALUES[this]
        ?: APP_BUTTON_ACTION_VALUES[this]
        ?: error("$name 버튼의 액션 값이 정의되지 않았습니다.")

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
    SeniorHomeButtonType.Genie,
    SeniorHomeButtonType.YouTubeMusic,
    SeniorHomeButtonType.Spotify,
    SeniorHomeButtonType.Flo,
    SeniorHomeButtonType.Vibe,
    SeniorHomeButtonType.Bugs,
    SeniorHomeButtonType.SamsungMusic,
    SeniorHomeButtonType.KakaoMusic,
)

private val DEFAULT_INTENT_BUTTON_OPTIONS = listOf(
    defaultIntentButton("전화", "PHONE", SeniorHomeButtonType.Call),
    defaultIntentButton("메시지", "MESSAGE", SeniorHomeButtonType.Message),
    defaultIntentButton("카메라", "CAMERA", SeniorHomeButtonType.Camera),
    defaultIntentButton("사진", "PHOTO", SeniorHomeButtonType.Photo),
    defaultIntentButton("메모", "MEMO", SeniorHomeButtonType.Memo),
    defaultIntentButton("알림", "ALARM", SeniorHomeButtonType.Alarm),
    defaultIntentButton("계산기", "CALCULATOR", SeniorHomeButtonType.Calculator),
    defaultIntentButton("설정", "SETTINGS", SeniorHomeButtonType.Settings),
    defaultIntentButton("음성메모", "VOICE_MEMO", SeniorHomeButtonType.Recorder),
    defaultIntentButton("타이머", "TIMER"),
    defaultIntentButton("플레이스토어", "PLAY_STORE"),
    defaultIntentButton("인터넷", "INTERNET"),
)

private val DEFAULT_INTENT_ACTION_VALUES = DEFAULT_INTENT_BUTTON_OPTIONS
    .mapTo(hashSetOf(), DisplayHomeButton::actionValue)

private fun String.canonicalDefaultActionValue(): String = when (trim().uppercase()) {
    "CALL" -> "PHONE"
    "SMS" -> "MESSAGE"
    "GALLERY" -> "PHOTO"
    "NOTE" -> "MEMO"
    "RECORDER", "VOICE_RECORDER" -> "VOICE_MEMO"
    "MARKET" -> "PLAY_STORE"
    "BROWSER" -> "INTERNET"
    else -> trim().uppercase()
}

private fun defaultIntentButton(
    name: String,
    actionValue: String,
    type: SeniorHomeButtonType? = null,
): DisplayHomeButton = DisplayHomeButton(
    name = name,
    actionType = DEFAULT_ACTION_TYPE,
    actionValue = actionValue,
    type = type,
)

private val REQUIRED_GRID_BUTTON_TYPES = listOf(
    SeniorHomeButtonType.ChatBuddy,
    SeniorHomeButtonType.Medication,
    SeniorHomeButtonType.Photo,
)

private val DEFAULT_BUTTON_ACTION_VALUES = mapOf(
    SeniorHomeButtonType.Schedule to "SCHEDULE",
    SeniorHomeButtonType.ChatBuddy to "COMPANION",
    SeniorHomeButtonType.Medication to "MEDICATION",
    SeniorHomeButtonType.Photo to "PHOTO",
    SeniorHomeButtonType.Emergency to "EMERGENCY",
    SeniorHomeButtonType.Call to "PHONE",
    SeniorHomeButtonType.Message to "MESSAGE",
    SeniorHomeButtonType.Calendar to "CALENDAR",
    SeniorHomeButtonType.Alarm to "ALARM",
    SeniorHomeButtonType.Memo to "MEMO",
    SeniorHomeButtonType.Recorder to "VOICE_MEMO",
    SeniorHomeButtonType.Calculator to "CALCULATOR",
    SeniorHomeButtonType.Settings to "SETTINGS",
    SeniorHomeButtonType.Flashlight to "FLASHLIGHT",
    SeniorHomeButtonType.Camera to "CAMERA",
)

private val APP_BUTTON_ACTION_VALUES = mapOf(
    SeniorHomeButtonType.KakaoTalk to "KAKAO_TALK",
    SeniorHomeButtonType.NaverBand to "NAVER_BAND",
    SeniorHomeButtonType.NaverCafe to "NAVER_CAFE",
    SeniorHomeButtonType.Line to "LINE",
    SeniorHomeButtonType.YouTube to "YOUTUBE",
    SeniorHomeButtonType.Naver to "NAVER",
    SeniorHomeButtonType.Daum to "DAUM",
    SeniorHomeButtonType.Google to "GOOGLE",
    SeniorHomeButtonType.Tving to "TVING",
    SeniorHomeButtonType.Netflix to "NETFLIX",
    SeniorHomeButtonType.NaverMap to "NAVER_MAP",
    SeniorHomeButtonType.KakaoMap to "KAKAO_MAP",
    SeniorHomeButtonType.KakaoT to "KAKAO_T",
    SeniorHomeButtonType.TMap to "TMAP",
    SeniorHomeButtonType.KorailTalk to "KORAIL_TALK",
    SeniorHomeButtonType.Toss to "TOSS",
    SeniorHomeButtonType.KakaoPay to "KAKAO_PAY",
    SeniorHomeButtonType.NaverPay to "NAVER_PAY",
    SeniorHomeButtonType.SamsungWallet to "SAMSUNG_WALLET",
    SeniorHomeButtonType.SamsungPay to "SAMSUNG_PAY",
    SeniorHomeButtonType.CashWalk to "CASH_WALK",
    SeniorHomeButtonType.Weather to "WEATHER",
    SeniorHomeButtonType.Coupang to "COUPANG",
    SeniorHomeButtonType.Karrot to "KARROT",
    SeniorHomeButtonType.Baemin to "BAEMIN",
    SeniorHomeButtonType.Yogiyo to "YOGIYO",
    SeniorHomeButtonType.CoupangEats to "COUPANG_EATS",
    SeniorHomeButtonType.HomeShopping to "HOME_SHOPPING",
    SeniorHomeButtonType.GoStop to "GO_STOP",
)

private data class ButtonApiMetadata(
    val buttonName: String,
    val packageName: String?,
)

private val BUTTON_API_METADATA = mapOf(
    SeniorHomeButtonType.Schedule to ButtonApiMetadata(
        buttonName = "일정",
        packageName = null,
    ),
    SeniorHomeButtonType.ChatBuddy to ButtonApiMetadata(
        buttonName = "말벗",
        packageName = null,
    ),
    SeniorHomeButtonType.Medication to ButtonApiMetadata(
        buttonName = "복약",
        packageName = null,
    ),
    SeniorHomeButtonType.Emergency to ButtonApiMetadata(
        buttonName = "긴급알림",
        packageName = null,
    ),
    SeniorHomeButtonType.Call to ButtonApiMetadata(
        buttonName = "전화",
        packageName = null,
    ),
    SeniorHomeButtonType.Message to ButtonApiMetadata(
        buttonName = "메시지",
        packageName = null,
    ),
    SeniorHomeButtonType.Calendar to ButtonApiMetadata(
        buttonName = "캘린더",
        packageName = null,
    ),
    SeniorHomeButtonType.Alarm to ButtonApiMetadata(
        buttonName = "알람",
        packageName = null,
    ),
    SeniorHomeButtonType.Memo to ButtonApiMetadata(
        buttonName = "메모",
        packageName = null,
    ),
    SeniorHomeButtonType.Recorder to ButtonApiMetadata(
        buttonName = "녹음",
        packageName = null,
    ),
    SeniorHomeButtonType.Calculator to ButtonApiMetadata(
        buttonName = "계산기",
        packageName = null,
    ),
    SeniorHomeButtonType.Settings to ButtonApiMetadata(
        buttonName = "설정",
        packageName = null,
    ),
    SeniorHomeButtonType.Flashlight to ButtonApiMetadata(
        buttonName = "손전등",
        packageName = null,
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
        packageName = null,
    ),
    SeniorHomeButtonType.Camera to ButtonApiMetadata(
        buttonName = "카메라",
        packageName = null,
    ),
)

private val BUTTON_TYPE_BY_PACKAGE: Map<String, SeniorHomeButtonType> = buildMap {
    BUTTON_API_METADATA.forEach { (button, metadata) ->
        val packageName = metadata.packageName?.lowercase()
            ?: return@forEach
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

    register(SeniorHomeButtonType.Call, "전화", "통화", "PHONE", "CALL", "tel")
    register(SeniorHomeButtonType.Message, "메시지", "문자", "MESSAGE", "sms")
    register(SeniorHomeButtonType.Calendar, "캘린더")
    register(SeniorHomeButtonType.Alarm, "알림", "알람")
    register(SeniorHomeButtonType.Memo, "메모")
    register(
        SeniorHomeButtonType.Recorder,
        "녹음",
        "음성 녹음",
        "음성메모",
        "VOICE_MEMO",
        "RECORDER",
        "VOICE_RECORDER",
    )
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
    register(SeniorHomeButtonType.Genie, "지니뮤직", "지니", "Genie")
    register(
        SeniorHomeButtonType.YouTubeMusic,
        "유튜브 뮤직",
        "YouTube Music",
        "YOUTUBE_MUSIC",
    )
    register(SeniorHomeButtonType.Spotify, "스포티파이", "스포티파이(Spotify)")
    register(SeniorHomeButtonType.Flo, "플로", "FLO")
    register(SeniorHomeButtonType.Vibe, "바이브", "VIBE")
    register(SeniorHomeButtonType.Bugs, "벅스", "Bugs", "Bugs!")
    register(SeniorHomeButtonType.SamsungMusic, "삼성 뮤직", "Samsung Music")
    register(SeniorHomeButtonType.KakaoMusic, "카카오뮤직", "KakaoMusic")
    register(SeniorHomeButtonType.Photo, "사진", "갤러리")
    register(SeniorHomeButtonType.Camera, "카메라")
    register(SeniorHomeButtonType.Emergency, "긴급알림", "긴급 알림", "SOS")
}

private const val DEFAULT_DEVICE_ID = "connected-senior-device"
private const val DEVICE_STATUS_ONLINE = "ONLINE"
private const val DEVICE_STATUS_OFFLINE = "OFFLINE"
private const val DEVICE_STATUS_DISCONNECTED = "DISCONNECTED"
private const val DEVICE_STATUS_CONNECTED = "CONNECTED"
private const val DEFAULT_DEVICE_NAME = "시니어폰"
private const val PRIMARY_MANAGER_TYPE = "PRIMARY"
private const val MINIMUM_BUTTON_COUNT = 8
private const val MAXIMUM_BUTTON_COUNT = 18
private const val FIXED_EMERGENCY_GRID_INDEX = 7
private const val DEFAULT_ACTION_TYPE = "DEFAULT"
private const val APP_ACTION_TYPE = "APP"
