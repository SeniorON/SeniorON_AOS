package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.dto.ButtonOptionResponse
import com.example.senior_on.data.remote.dto.ConnectionResponse
import com.example.senior_on.data.remote.dto.DeviceDetailResponse
import com.example.senior_on.data.remote.dto.DeviceStatusUpdateRequest
import com.example.senior_on.data.remote.dto.DeviceLocationResponse
import com.example.senior_on.data.remote.dto.DeviceLocationUpdateRequest
import com.example.senior_on.data.remote.dto.FcmTokenUpdateRequest
import com.example.senior_on.data.remote.dto.HomeLocationResponse
import com.example.senior_on.data.remote.dto.FamilyMemberResponse
import com.example.senior_on.data.remote.dto.HomeButtonCreateRequest
import com.example.senior_on.data.remote.dto.HomeButtonCreateResponse
import com.example.senior_on.data.remote.dto.HomeButtonResponse
import com.example.senior_on.data.remote.dto.HomeButtonSaveRequest
import com.example.senior_on.data.remote.dto.HomeButtonUpdateRequest
import com.example.senior_on.data.remote.dto.HomeFontSizeUpdateRequest
import com.example.senior_on.data.remote.dto.HomeResponse
import com.example.senior_on.data.remote.dto.MusicCardResponse
import com.example.senior_on.data.remote.dto.SeniorHomeResponse
import com.example.senior_on.data.remote.dto.SeniorProfileResponse
import com.example.senior_on.data.remote.dto.SeniorProfileUpdateRequest
import com.example.senior_on.data.remote.dto.SeniorProfileUpdateResponse
import com.example.senior_on.data.remote.dto.TodayHospitalListResponse
import com.example.senior_on.data.remote.dto.TodayScheduleResponse
import com.example.senior_on.data.remote.dto.WeatherResponse
import com.example.senior_on.data.source.device.DeviceDataSource
import com.example.senior_on.data.source.home.HomeDataSource
import com.example.senior_on.domain.model.display.DisplayDeviceConnectionStatus
import com.example.senior_on.domain.model.display.DisplayHomeButton
import com.example.senior_on.domain.model.display.InitialSeniorHomeGridButtons
import com.example.senior_on.domain.model.display.SeniorFontSize
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.domain.model.parent.ParentInfo
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DisplayRepositoryImplTest {
    @Test
    fun getOverviewMapsOnlineConnectionStatusFromHomeResponse() = runBlocking {
        val repository = DisplayRepositoryImpl(
            homeDataSource = FakeHomeDataSource(
                homeResponse = homeResponse(
                    connection = ConnectionResponse(
                        connected = true,
                        battery = 72,
                        device_name = "Galaxy S24",
                        connection_status = "ONLINE",
                    )
                )
            ),
            deviceDataSource = FakeDeviceDataSource(),
        )

        val device = repository.getOverview(currentParentInfo = null).device

        assertEquals("Galaxy S24", device?.name)
        assertEquals(DisplayDeviceConnectionStatus.Online, device?.connectionStatus)
    }

    @Test
    fun getOverviewMapsOfflineConnectionStatusFromHomeResponse() = runBlocking {
        val repository = DisplayRepositoryImpl(
            homeDataSource = FakeHomeDataSource(
                homeResponse = homeResponse(
                    connection = ConnectionResponse(
                        connected = false,
                        battery = 51,
                        device_name = "Galaxy S24",
                        connection_status = "OFFLINE",
                    )
                )
            ),
            deviceDataSource = FakeDeviceDataSource(),
        )

        val device = repository.getOverview(currentParentInfo = null).device

        assertEquals("Galaxy S24", device?.name)
        assertEquals(DisplayDeviceConnectionStatus.Offline, device?.connectionStatus)
    }

    @Test
    fun getOverviewMapsDisconnectedConnectionStatusToAbsentDevice() = runBlocking {
        val repository = DisplayRepositoryImpl(
            homeDataSource = FakeHomeDataSource(
                homeResponse = homeResponse(
                    connection = ConnectionResponse(
                        connected = false,
                        battery = 51,
                        device_name = "stale-device",
                        connection_status = "DISCONNECTED",
                    )
                )
            ),
            deviceDataSource = FakeDeviceDataSource(),
        )

        assertNull(repository.getOverview(currentParentInfo = null).device)
    }

    @Test
    fun getOverviewFallsBackToConnectedWhenConnectionStatusIsMissing() = runBlocking {
        val repository = DisplayRepositoryImpl(
            homeDataSource = FakeHomeDataSource(
                homeResponse = homeResponse(
                    connection = ConnectionResponse(
                        connected = false,
                        battery = 51,
                        device_name = "Galaxy S24",
                    )
                )
            ),
            deviceDataSource = FakeDeviceDataSource(),
        )

        assertEquals(
            DisplayDeviceConnectionStatus.Offline,
            repository.getOverview(currentParentInfo = null).device?.connectionStatus,
        )
    }

    @Test
    fun allSupportedMusicAppsUseBackendEnumValuesAndMapBackToButtons() = runBlocking {
        val musicCases = listOf(
            SeniorHomeButtonType.Melon to "MELON",
            SeniorHomeButtonType.Genie to "GENIE",
            SeniorHomeButtonType.YouTubeMusic to "YOUTUBE_MUSIC",
            SeniorHomeButtonType.Spotify to "SPOTIFY",
            SeniorHomeButtonType.Flo to "FLO",
            SeniorHomeButtonType.Vibe to "VIBE",
            SeniorHomeButtonType.Bugs to "BUGS",
            SeniorHomeButtonType.SamsungMusic to "SAMSUNG_MUSIC",
            SeniorHomeButtonType.KakaoMusic to "KAKAO_MUSIC",
        )

        musicCases.forEach { (button, apiValue) ->
            val savingHomeDataSource = FakeHomeDataSource()
            val savingRepository = DisplayRepositoryImpl(
                homeDataSource = savingHomeDataSource,
                deviceDataSource = FakeDeviceDataSource(),
            )

            savingRepository.saveButtons(
                buttons = listOf(button) + InitialSeniorHomeGridButtons,
                customButtonLabels = emptyMap(),
            )

            assertEquals(
                apiValue,
                savingHomeDataSource.savedButtonRequests.single().musicApp,
            )

            val loadingRepository = DisplayRepositoryImpl(
                homeDataSource = FakeHomeDataSource(
                    homeResponse = HomeResponse(
                        connection = null,
                        buttons = emptyList(),
                        user_name = null,
                        senior_profile = null,
                        font_size = "MEDIUM",
                        music_card = MusicCardResponse(
                            enabled = true,
                            icon = null,
                            music_app = apiValue,
                            app_name = button.name,
                            action_type = "APP",
                            action_value = apiValue.lowercase(),
                            package_name = null,
                        ),
                        today_schedule = null,
                    )
                ),
                deviceDataSource = FakeDeviceDataSource(),
            )

            assertEquals(
                button,
                loadingRepository
                    .getOverview(currentParentInfo = null)
                    .screenConfiguration
                    .buttons
                    .first(),
            )
        }
    }

    @Test
    fun offlineDeviceWithConnectionHistoryIsKept() = runBlocking {
        val lastConnectedAt = "2026-08-07T18:05:57.484865"
        val repository = DisplayRepositoryImpl(
            homeDataSource = FakeHomeDataSource(
                deviceResponse = DeviceDetailResponse(
                    deviceName = null,
                    connected = false,
                    connectionStatus = "OFFLINE",
                    batteryLevel = null,
                    networkConnected = false,
                    lastConnectedAt = lastConnectedAt,
                    lastLocationUpdatedAt = null,
                )
            ),
            deviceDataSource = FakeDeviceDataSource(),
        )

        val device = repository.getDevice()

        assertEquals("시니어폰", device?.name)
        assertEquals(
            DisplayDeviceConnectionStatus.Offline,
            device?.connectionStatus,
        )
        assertEquals(lastConnectedAt, device?.lastConnectedAtLabel)
    }

    @Test
    fun deviceWithoutIdentityOrConnectionHistoryIsAbsent() = runBlocking {
        val repository = DisplayRepositoryImpl(
            homeDataSource = FakeHomeDataSource(),
            deviceDataSource = FakeDeviceDataSource(),
        )

        assertNull(repository.getDevice())
    }

    @Test
    fun disconnectedOverviewStillUsesSeniorProfileFromServer() = runBlocking {
        val homeDataSource = FakeHomeDataSource(
            homeResponse = HomeResponse(
                connection = null,
                buttons = emptyList(),
                user_name = null,
                senior_profile = SeniorProfileResponse(
                    senior_id = 77L,
                    name = "김영희",
                    relation = "MOTHER",
                    birth = "1960-01-02",
                    age = 66,
                    address = "서울시 강남구",
                    phone = "010-1234-5678",
                    detail_address = "101동 202호",
                ),
                font_size = "LARGE",
                music_card = null,
                today_schedule = null,
            )
        )
        val repository = DisplayRepositoryImpl(
            homeDataSource = homeDataSource,
            deviceDataSource = FakeDeviceDataSource(),
        )
        val staleLocalParentInfo = ParentInfo(
            seniorId = 1L,
            name = "기존 정보",
            relationshipLabel = "어머니",
            birthDate = LocalDate.of(1960, 1, 2),
            phoneNumber = "010-0000-0000",
            address = "",
            addressDetail = "",
        )

        val overview = repository.getOverview(staleLocalParentInfo)
        val parentInfo = overview.parentInfo

        assertNull(overview.device)
        assertEquals(77L, parentInfo?.seniorId)
        assertEquals("김영희", parentInfo?.name)
        assertEquals("서울시 강남구", parentInfo?.address)
        assertEquals("101동 202호", parentInfo?.addressDetail)
    }

    @Test
    fun emptyServerProfileDoesNotReuseStaleParentInfo() = runBlocking {
        val repository = DisplayRepositoryImpl(
            homeDataSource = FakeHomeDataSource(
                homeResponse = HomeResponse(
                    connection = null,
                    buttons = emptyList(),
                    user_name = null,
                    senior_profile = SeniorProfileResponse(
                        senior_id = null,
                        name = null,
                        relation = null,
                        birth = null,
                        age = null,
                        address = null,
                        phone = null,
                        detail_address = null,
                    ),
                    font_size = "MEDIUM",
                    music_card = null,
                    today_schedule = null,
                )
            ),
            deviceDataSource = FakeDeviceDataSource(),
        )
        val staleParentInfo = ParentInfo(
            seniorId = 1L,
            name = "이전에 표시된 시니어",
            relationshipLabel = "어머니",
            birthDate = LocalDate.of(1960, 1, 2),
            phoneNumber = "010-0000-0000",
            address = "서울시",
            addressDetail = "101동",
        )

        val parentInfo = repository.getOverview(staleParentInfo).parentInfo

        assertNull(parentInfo)
    }

    @Test
    fun getOverviewMapsTodayScheduleFromHomeResponse() = runBlocking {
        val homeDataSource = FakeHomeDataSource(
            homeResponse = HomeResponse(
                connection = null,
                buttons = emptyList(),
                user_name = null,
                senior_profile = null,
                font_size = "LARGE",
                music_card = null,
                today_schedule = TodayScheduleResponse(
                    title = "병원 일정",
                    description = "연세세브란스병원",
                    schedule_count = 1,
                    display_type = "SINGLE",
                    schedule_id = 7L,
                    scheduled_time = "15:00",
                ),
            )
        )
        val repository = DisplayRepositoryImpl(
            homeDataSource = homeDataSource,
            deviceDataSource = FakeDeviceDataSource(),
        )

        val schedule = repository.getOverview(null).todaySchedule

        assertEquals("병원 일정", schedule?.title)
        assertEquals("연세세브란스병원", schedule?.description)
        assertEquals(1, schedule?.count)
        assertEquals("15:00", schedule?.scheduledTime)
    }

    @Test
    fun getWeatherUsesCoordinatesAndMapsWeatherResponse() = runBlocking {
        val homeDataSource = FakeHomeDataSource(
            weatherResponse = WeatherResponse(
                temperature = 24,
                weatherStatus = "CLEAR",
                weatherText = "맑음",
                observedAt = "2026-07-29T17:00:00",
            )
        )
        val repository = DisplayRepositoryImpl(
            homeDataSource = homeDataSource,
            deviceDataSource = FakeDeviceDataSource(),
        )

        val weather = repository.getWeather(
            latitude = 37.5665,
            longitude = 126.9780,
        )

        assertEquals(listOf(37.5665 to 126.9780), homeDataSource.weatherRequests)
        assertEquals(24, weather.temperatureCelsius)
        assertEquals("CLEAR", weather.status)
        assertEquals("맑음", weather.description)
    }

    @Test
    fun primaryCurrentUserCanEditScreen() {
        val members = listOf(
            familyMember(name = "보조 담당자", managerType = "SUB", isMe = false),
            familyMember(name = "현재 사용자", managerType = "PRIMARY", isMe = true),
        )

        assertTrue(members.canCurrentUserEditScreen())
    }

    @Test
    fun subCurrentUserCannotEditScreen() {
        val members = listOf(
            familyMember(name = "주 담당자", managerType = "PRIMARY", isMe = false),
            familyMember(name = "현재 사용자", managerType = "SUB", isMe = true),
        )

        assertFalse(members.canCurrentUserEditScreen())
    }

    @Test
    fun missingCurrentUserCannotEditScreen() {
        val members = listOf(
            familyMember(name = "주 담당자", managerType = "PRIMARY", isMe = false),
        )

        assertFalse(members.canCurrentUserEditScreen())
    }

    @Test
    fun saveButtonsUsesOneFullPutRequestWithNamesPackagesAndOneBasedOrder() =
        runBlocking {
            val homeDataSource = FakeHomeDataSource()
            val repository = DisplayRepositoryImpl(
                homeDataSource = homeDataSource,
                deviceDataSource = FakeDeviceDataSource(),
            )

            repository.saveButtons(
                buttons = listOf(
                    SeniorHomeButtonType.Melon,
                    SeniorHomeButtonType.Schedule,
                    SeniorHomeButtonType.Call,
                    SeniorHomeButtonType.Emergency,
                    SeniorHomeButtonType.Message,
                    SeniorHomeButtonType.Camera,
                    SeniorHomeButtonType.YouTube,
                ),
                customButtonLabels = mapOf(
                    SeniorHomeButtonType.Call to "엄마 전화",
                ),
            )

            val request = homeDataSource.savedButtonRequests.single()
            assertEquals("MELON", request.musicApp)
            assertEquals((1..8).toList(), request.buttons.map { it.buttonOrder })
            assertEquals(
                listOf(
                    "엄마 전화",
                    "메시지",
                    "카메라",
                    "유튜브",
                    "말벗",
                    "복약",
                    "사진",
                    "긴급알림",
                ),
                request.buttons.map { it.buttonName },
            )
            assertEquals(
                listOf(
                    "DEFAULT",
                    "DEFAULT",
                    "DEFAULT",
                    "APP",
                    "DEFAULT",
                    "DEFAULT",
                    "DEFAULT",
                    "DEFAULT",
                ),
                request.buttons.map { it.actionType },
            )
            assertEquals(
                listOf(
                    "PHONE",
                    "MESSAGE",
                    "CAMERA",
                    "YOUTUBE",
                    "COMPANION",
                    "MEDICATION",
                    "PHOTO",
                    "EMERGENCY",
                ),
                request.buttons.map { it.actionValue },
            )
            assertEquals(
                listOf(
                    null,
                    null,
                    null,
                    "com.google.android.youtube",
                    null,
                    null,
                    null,
                    null,
                ),
                request.buttons.map { it.packageName },
            )
            assertEquals(0, homeDataSource.buttonOptionsRequestCount)
            assertEquals(0, homeDataSource.legacyButtonMutationCount)
        }

    @Test
    fun initialGridButtonsUseRequestedOrderNamesAndTargets() = runBlocking {
        val homeDataSource = FakeHomeDataSource()
        val repository = DisplayRepositoryImpl(
            homeDataSource = homeDataSource,
            deviceDataSource = FakeDeviceDataSource(),
        )

        repository.saveButtons(
            buttons = InitialSeniorHomeGridButtons,
            customButtonLabels = emptyMap(),
        )

        val request = homeDataSource.savedButtonRequests.single()
        assertEquals(null, request.musicApp)
        assertEquals((1..10).toList(), request.buttons.map { it.buttonOrder })
        assertEquals(
            listOf(
                "전화",
                "메시지",
                "카메라",
                "사진",
                "유튜브",
                "말벗",
                "복약",
                "긴급알림",
                "카카오톡",
                "네이버",
            ),
            request.buttons.map { it.buttonName },
        )
        assertEquals(
            listOf(
                "PHONE",
                "MESSAGE",
                "CAMERA",
                "PHOTO",
                "YOUTUBE",
                "COMPANION",
                "MEDICATION",
                "EMERGENCY",
                "KAKAO_TALK",
                "NAVER",
            ),
            request.buttons.map { it.actionValue },
        )
        assertEquals(
            listOf(
                null,
                null,
                null,
                null,
                "com.google.android.youtube",
                null,
                null,
                null,
                "com.kakao.talk",
                "com.nhn.android.search",
            ),
            request.buttons.map { it.packageName },
        )
        assertEquals(
            8,
            request.buttons.single {
                it.actionValue == "EMERGENCY"
            }.buttonOrder,
        )
    }

    @Test
    fun getOverviewKeepsBackendInitialButtonsAndEmergencyAtOrderEight() =
        runBlocking {
            val initialButtonNames = listOf(
                "전화",
                "메시지",
                "카메라",
                "사진",
                "유튜브",
                "말벗",
                "복약",
                "긴급알림",
                "카카오톡",
                "네이버",
            )
            val homeDataSource = FakeHomeDataSource(
                homeResponse = HomeResponse(
                    connection = null,
                    buttons = initialButtonNames.mapIndexed { index, name ->
                        HomeButtonResponse(
                            icon = null,
                            button_id = index.toLong(),
                            button_order = index + 1,
                            button_name = name,
                            action_type = null,
                            action_value = null,
                        )
                    },
                    user_name = null,
                    senior_profile = null,
                    font_size = "LARGE",
                    music_card = null,
                    today_schedule = null,
                )
            )
            val repository = DisplayRepositoryImpl(
                homeDataSource = homeDataSource,
                deviceDataSource = FakeDeviceDataSource(),
            )

            val gridButtons = repository
                .getOverview(currentParentInfo = null)
                .screenConfiguration
                .buttons
                .filterNot { it == SeniorHomeButtonType.Schedule }

            assertEquals(InitialSeniorHomeGridButtons, gridButtons)
            assertEquals(SeniorHomeButtonType.Emergency, gridButtons[7])
        }

    @Test
    fun saveButtonsAllowsTwelveGeneralButtonsWithScheduleAndMusicExcluded() =
        runBlocking {
            val homeDataSource = FakeHomeDataSource()
            val repository = DisplayRepositoryImpl(
                homeDataSource = homeDataSource,
                deviceDataSource = FakeDeviceDataSource(),
            )
            val optionalButtons = listOf(
                SeniorHomeButtonType.Call,
                SeniorHomeButtonType.Message,
                SeniorHomeButtonType.Calendar,
                SeniorHomeButtonType.Alarm,
                SeniorHomeButtonType.Memo,
                SeniorHomeButtonType.Recorder,
                SeniorHomeButtonType.Calculator,
                SeniorHomeButtonType.Settings,
            )

            repository.saveButtons(
                buttons = listOf(
                    SeniorHomeButtonType.Melon,
                    SeniorHomeButtonType.Schedule,
                ) + optionalButtons,
                customButtonLabels = emptyMap(),
            )

            val request = homeDataSource.savedButtonRequests.single()
            assertEquals("MELON", request.musicApp)
            assertEquals(12, request.buttons.size)
            assertFalse(request.buttons.any { it.actionValue == "SCHEDULE" })
            assertEquals(
                8,
                request.buttons.single { it.actionValue == "EMERGENCY" }.buttonOrder,
            )
        }

    @Test
    fun saveButtonsRejectsMoreThanEighteenGeneralButtons() = runBlocking {
        val homeDataSource = FakeHomeDataSource()
        val repository = DisplayRepositoryImpl(
            homeDataSource = homeDataSource,
            deviceDataSource = FakeDeviceDataSource(),
        )
        val optionalButtons = listOf(
            SeniorHomeButtonType.Call,
            SeniorHomeButtonType.Message,
            SeniorHomeButtonType.Calendar,
            SeniorHomeButtonType.Alarm,
            SeniorHomeButtonType.Memo,
            SeniorHomeButtonType.Recorder,
            SeniorHomeButtonType.Calculator,
            SeniorHomeButtonType.Settings,
            SeniorHomeButtonType.Flashlight,
            SeniorHomeButtonType.KakaoTalk,
            SeniorHomeButtonType.NaverBand,
            SeniorHomeButtonType.NaverCafe,
            SeniorHomeButtonType.Line,
            SeniorHomeButtonType.YouTube,
            SeniorHomeButtonType.Naver,
        )

        val result = runCatching {
            repository.saveButtons(
                buttons = listOf(
                    SeniorHomeButtonType.Spotify,
                    SeniorHomeButtonType.Schedule,
                ) + optionalButtons,
                customButtonLabels = emptyMap(),
            )
        }

        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertTrue(homeDataSource.savedButtonRequests.isEmpty())
    }

    @Test
    fun saveButtonsPersistsAUserSelectedPackageWithoutCatalogMetadata() = runBlocking {
        val homeDataSource = FakeHomeDataSource()
        val repository = DisplayRepositoryImpl(
            homeDataSource = homeDataSource,
            deviceDataSource = FakeDeviceDataSource(),
        )
        val importedApp = DisplayHomeButton(
            name = "유튜브",
            actionType = "APP",
            actionValue = "com.google.android.youtube",
            packageName = "com.google.android.youtube",
        )
        val defaultButtons = listOf(
            "전화" to "PHONE",
            "메시지" to "MESSAGE",
            "카메라" to "CAMERA",
            "달력" to "CALENDAR",
        ).map { (name, actionValue) ->
            DisplayHomeButton(
                name = name,
                actionType = "DEFAULT",
                actionValue = actionValue,
            )
        }

        repository.saveButtons(defaultButtons + importedApp)

        val request = homeDataSource.savedButtonRequests.single()
        assertEquals(
            "com.google.android.youtube",
            request.buttons.single { it.buttonName == "유튜브" }.packageName,
        )
        assertNull(request.buttons.single { it.buttonName == "전화" }.packageName)
    }

    @Test
    fun getOverviewMapsSavedPackagesAndPreservesInternalButtonOrder() = runBlocking {
        val homeDataSource = FakeHomeDataSource(
            homeResponse = HomeResponse(
                connection = null,
                buttons = listOf(
                    HomeButtonResponse(
                        icon = null,
                        button_id = 1L,
                        button_order = 1,
                        button_name = "말벗",
                        action_type = "APP",
                        action_value = "COMPANION",
                    ),
                    HomeButtonResponse(
                        icon = null,
                        button_id = 2L,
                        button_order = 2,
                        button_name = "가족톡",
                        action_type = "APP",
                        action_value = "com.kakao.talk",
                    ),
                    HomeButtonResponse(
                        icon = null,
                        button_id = 3L,
                        button_order = 3,
                        button_name = "복약",
                        action_type = "APP",
                        action_value = "MEDICATION",
                    ),
                    HomeButtonResponse(
                        icon = null,
                        button_id = 4L,
                        button_order = 4,
                        button_name = "긴급알림",
                        action_type = "APP",
                        action_value = "EMERGENCY",
                    ),
                ),
                user_name = "담당자",
                senior_profile = null,
                font_size = "MEDIUM",
                music_card = null,
                today_schedule = null,
            )
        )
        val repository = DisplayRepositoryImpl(
            homeDataSource = homeDataSource,
            deviceDataSource = FakeDeviceDataSource(),
        )

        val overview = repository.getOverview(currentParentInfo = null)

        assertEquals(
            listOf(
                SeniorHomeButtonType.Schedule,
                SeniorHomeButtonType.ChatBuddy,
                SeniorHomeButtonType.KakaoTalk,
                SeniorHomeButtonType.Medication,
                SeniorHomeButtonType.Photo,
                SeniorHomeButtonType.Emergency,
            ),
            overview.screenConfiguration.buttons,
        )
        assertEquals(
            "가족톡",
            overview.screenConfiguration
                .customButtonLabels[SeniorHomeButtonType.KakaoTalk],
        )
        assertTrue(overview.hasSavedButtonConfiguration)
        assertEquals(1, homeDataSource.buttonOptionsRequestCount)
    }

    @Test
    fun getOverviewLoadsBackendDefaultButtonOptionsForEditing() = runBlocking {
        val homeDataSource = FakeHomeDataSource(
            buttonOptionsResponse = listOf(
                ButtonOptionResponse(
                    icon = null,
                    option_id = 10L,
                    button_name = "전화",
                    action_type = "DEFAULT",
                    action_value = "PHONE",
                ),
            ),
        )
        val repository = DisplayRepositoryImpl(
            homeDataSource = homeDataSource,
            deviceDataSource = FakeDeviceDataSource(),
        )

        val overview = repository.getOverview(currentParentInfo = null)

        assertEquals(1, homeDataSource.buttonOptionsRequestCount)
        val phoneOption = overview.availableButtonOptions.first {
            it.actionValue == "PHONE"
        }
        assertEquals(10L, phoneOption.optionId)
        assertEquals(SeniorHomeButtonType.Call, phoneOption.type)
    }

    @Test
    fun emptyBackendOptionsFallBackToDefaultIntentButtons() = runBlocking {
        val repository = DisplayRepositoryImpl(
            homeDataSource = FakeHomeDataSource(
                buttonOptionsResponse = emptyList(),
            ),
            deviceDataSource = FakeDeviceDataSource(),
        )

        val overview = repository.getOverview(currentParentInfo = null)

        assertEquals(
            listOf(
                "PHONE",
                "MESSAGE",
                "CAMERA",
                "PHOTO",
                "MEMO",
                "ALARM",
                "CALCULATOR",
                "SETTINGS",
                "VOICE_MEMO",
                "TIMER",
                "PLAY_STORE",
                "INTERNET",
            ),
            overview.availableButtonOptions.map(DisplayHomeButton::actionValue),
        )
    }

    @Test
    fun emptyServerButtonsAreReportedAsNotInitialized() = runBlocking {
        val repository = DisplayRepositoryImpl(
            homeDataSource = FakeHomeDataSource(),
            deviceDataSource = FakeDeviceDataSource(),
        )

        val overview = repository.getOverview(currentParentInfo = null)

        assertFalse(overview.hasSavedButtonConfiguration)
    }

    @Test
    fun normalFontSizeUsesMediumApiValue() = runBlocking {
        val homeDataSource = FakeHomeDataSource()
        val repository = DisplayRepositoryImpl(
            homeDataSource = homeDataSource,
            deviceDataSource = FakeDeviceDataSource(),
        )

        repository.updateFontSize(SeniorFontSize.Normal)

        assertEquals("MEDIUM", homeDataSource.fontSizeRequests.single().font_size)
    }

    @Test
    fun customRelationshipUsesOtherAndCustomRelation() = runBlocking {
        val homeDataSource = FakeHomeDataSource()
        val repository = DisplayRepositoryImpl(
            homeDataSource = homeDataSource,
            deviceDataSource = FakeDeviceDataSource(),
        )

        repository.updateSeniorProfile(
            ParentInfo(
                seniorId = 7L,
                name = "김영희",
                relationshipLabel = "이모",
                birthDate = LocalDate.of(1960, 1, 2),
                phoneNumber = "010-1234-5678",
                address = "서울시 강남구",
                addressDetail = "101동",
            )
        )

        val request = homeDataSource.profileRequests.single()
        assertEquals("OTHER", request.relation)
        assertEquals("이모", request.customRelation)
        assertEquals("101동", request.detailAddress)
    }
}

private fun familyMember(
    name: String,
    managerType: String,
    isMe: Boolean,
) = FamilyMemberResponse(
    usersId = 1L,
    name = name,
    role = "CHILD",
    canBecomePrimary = false,
    managerType = managerType,
    me = isMe,
    profileImageUrl = null,
)

private fun homeResponse(connection: ConnectionResponse?) = HomeResponse(
    connection = connection,
    buttons = emptyList(),
    user_name = null,
    senior_profile = null,
    font_size = "LARGE",
    music_card = null,
    today_schedule = null,
)

private class FakeHomeDataSource(
    private val homeResponse: HomeResponse = HomeResponse(
        connection = null,
        buttons = emptyList(),
        user_name = null,
        senior_profile = null,
        font_size = "LARGE",
        music_card = null,
        today_schedule = null,
    ),
    private val weatherResponse: WeatherResponse =
        WeatherResponse(null, null, null, null),
    private val deviceResponse: DeviceDetailResponse =
        DeviceDetailResponse(null, false, null, null, false, null, null),
    private val buttonOptionsResponse: List<ButtonOptionResponse>? = null,
) : HomeDataSource {
    val savedButtonRequests = mutableListOf<HomeButtonSaveRequest>()
    val fontSizeRequests = mutableListOf<HomeFontSizeUpdateRequest>()
    val profileRequests = mutableListOf<SeniorProfileUpdateRequest>()
    val weatherRequests = mutableListOf<Pair<Double, Double>>()
    var buttonOptionsRequestCount = 0
        private set
    var legacyButtonMutationCount = 0
        private set

    override suspend fun getHome() = homeResponse

    override suspend fun getWeather(
        latitude: Double,
        longitude: Double,
    ): WeatherResponse {
        weatherRequests += latitude to longitude
        return weatherResponse
    }

    override suspend fun getSeniorHome() =
        SeniorHomeResponse(emptyList(), "LARGE", null, null)

    override suspend fun getTodayHospitals(): List<TodayHospitalListResponse> =
        emptyList()

    override suspend fun getDevice() = deviceResponse

    override suspend fun getButtonOptions(): List<ButtonOptionResponse> {
        buttonOptionsRequestCount += 1
        return buttonOptionsResponse
            ?: error("버튼 옵션 응답이 설정되지 않았습니다.")
    }

    override suspend fun saveButtons(request: HomeButtonSaveRequest) {
        savedButtonRequests += request
    }

    override suspend fun addButton(
        request: HomeButtonCreateRequest,
    ): HomeButtonCreateResponse {
        legacyButtonMutationCount += 1
        error("Legacy button API must not be called")
    }

    override suspend fun updateButtons(request: HomeButtonUpdateRequest) {
        legacyButtonMutationCount += 1
        error("Legacy button API must not be called")
    }

    override suspend fun deleteButton(buttonId: Long) {
        legacyButtonMutationCount += 1
        error("Legacy button API must not be called")
    }

    override suspend fun updateFontSize(request: HomeFontSizeUpdateRequest) {
        fontSizeRequests += request
    }

    override suspend fun updateSeniorProfile(
        request: SeniorProfileUpdateRequest,
    ): SeniorProfileUpdateResponse {
        profileRequests += request
        return SeniorProfileUpdateResponse(
            seniorId = 7L,
            name = request.name,
            relation = request.relation,
            customRelation = request.customRelation,
            birth = request.birth,
            phoneNumber = request.phoneNumber,
            address = request.address,
            detailAddress = request.detailAddress,
        )
    }
}

private class FakeDeviceDataSource : DeviceDataSource {
    override suspend fun updateStatus(request: DeviceStatusUpdateRequest) = true
    override suspend fun disconnect() = Unit
    override suspend fun updateFcmToken(request: FcmTokenUpdateRequest) = Unit
    override suspend fun getLatestLocation(): DeviceLocationResponse = error("Not used")
    override suspend fun updateLocation(request: DeviceLocationUpdateRequest) = Unit
    override suspend fun getHomeLocation(): HomeLocationResponse = error("Not used")
}
