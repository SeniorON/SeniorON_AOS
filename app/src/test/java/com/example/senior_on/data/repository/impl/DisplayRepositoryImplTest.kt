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
import com.example.senior_on.data.remote.dto.HomeButtonResponse
import com.example.senior_on.data.remote.dto.HomeButtonSaveRequest
import com.example.senior_on.data.remote.dto.HomeFontSizeUpdateRequest
import com.example.senior_on.data.remote.dto.HomeResponse
import com.example.senior_on.data.remote.dto.MusicCardResponse
import com.example.senior_on.data.remote.dto.SeniorHomeResponse
import com.example.senior_on.data.remote.dto.SeniorProfileResponse
import com.example.senior_on.data.remote.dto.SeniorProfileUpdateRequest
import com.example.senior_on.data.remote.dto.SeniorProfileUpdateResponse
import com.example.senior_on.data.remote.dto.TodayHospitalListResponse
import com.example.senior_on.data.remote.dto.TodayScheduleResponse
import com.example.senior_on.data.source.device.DeviceDataSource
import com.example.senior_on.data.source.home.HomeDataSource
import com.example.senior_on.domain.model.display.DisplayDeviceConnectionStatus
import com.example.senior_on.domain.model.display.DisplayHomeButton
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

private const val TEST_SENIOR_ID = 77L

class DisplayRepositoryImplTest {
    @Test fun sharingFlagsAreSeniorScopedAndDoNotChangeConnectionOrOsPermissions() = runBlocking {
        val repository = DisplayRepositoryImpl(
            homeDataSource = FakeHomeDataSource(deviceResponse = deviceDetailResponse(connectionStatus = "ONLINE")
                .copy(locationPermissionGranted = true, lastLocationUpdatedAt = "2026-09-24")),
            deviceDataSource = FakeDeviceDataSource(),
            permissionsLoader = { id ->
                assertEquals(TEST_SENIOR_ID, id)
                com.example.senior_on.data.remote.api.SeniorPermissionSettings(id, false, true)
            },
        )
        val device = repository.getDevice(TEST_SENIOR_ID)!!
        assertEquals(DisplayDeviceConnectionStatus.Online, device.connectionStatus)
        assertEquals(true, device.locationPermissionGranted)
        assertEquals(false, device.locationSharingEnabled)
        assertEquals(true, device.inactivitySharingEnabled)
        assertNull(device.lastLocationUpdatedAtLabel)
    }

    @Test fun sharingReadFailureIsUnknownNotDisconnected() = runBlocking {
        val repository = DisplayRepositoryImpl(
            homeDataSource = FakeHomeDataSource(deviceResponse = deviceDetailResponse(connectionStatus = "OFFLINE")),
            deviceDataSource = FakeDeviceDataSource(),
            permissionsLoader = { error("network failure") },
        )
        val device = repository.getDevice(TEST_SENIOR_ID)!!
        assertEquals(DisplayDeviceConnectionStatus.Offline, device.connectionStatus)
        assertNull(device.locationSharingEnabled)
        assertNull(device.inactivitySharingEnabled)
    }

    @Test
    fun getOverviewForwardsSelectedSeniorIdToHomeRequests() = runBlocking {
        val homeDataSource = FakeHomeDataSource(
            buttonOptionsResponse = emptyList(),
        )
        val repository = DisplayRepositoryImpl(
            homeDataSource = homeDataSource,
            deviceDataSource = FakeDeviceDataSource(),
        )

        repository.getOverview(
            seniorId = TEST_SENIOR_ID,
            currentParentInfo = null,
        )

        assertEquals(listOf(TEST_SENIOR_ID), homeDataSource.homeSeniorIds)
        assertEquals(listOf(TEST_SENIOR_ID), homeDataSource.deviceSeniorIds)
        assertEquals(listOf(TEST_SENIOR_ID), homeDataSource.buttonOptionsSeniorIds)
    }

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
                ),
                deviceFailure = IllegalStateException("device detail unavailable"),
            ),
            deviceDataSource = FakeDeviceDataSource(),
        )

        val device = repository.getOverview(
            seniorId = TEST_SENIOR_ID,
            currentParentInfo = null,
        ).device

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
                ),
                deviceFailure = IllegalStateException("device detail unavailable"),
            ),
            deviceDataSource = FakeDeviceDataSource(),
        )

        val device = repository.getOverview(
            seniorId = TEST_SENIOR_ID,
            currentParentInfo = null,
        ).device

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

        assertNull(
            repository.getOverview(
                seniorId = TEST_SENIOR_ID,
                currentParentInfo = null,
            ).device
        )
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
                ),
                deviceFailure = IllegalStateException("device detail unavailable"),
            ),
            deviceDataSource = FakeDeviceDataSource(),
        )

        assertEquals(
            DisplayDeviceConnectionStatus.Offline,
            repository.getOverview(
                seniorId = TEST_SENIOR_ID,
                currentParentInfo = null,
            ).device?.connectionStatus,
        )
    }

    @Test
    fun getOverviewUsesLoginExpiredStatusFromDeviceDetail() = runBlocking {
        val homeDataSource = FakeHomeDataSource(
            homeResponse = homeResponse(
                connection = ConnectionResponse(
                    connected = false,
                    battery = 51,
                    device_name = "Galaxy S24",
                    connection_status = "OFFLINE",
                )
            ),
            deviceResponse = deviceDetailResponse(
                connectionStatus = "LOGIN_EXPIRED",
                lastConnectedAt = "2026-09-15T21:54:00",
            ),
        )
        val repository = DisplayRepositoryImpl(
            homeDataSource = homeDataSource,
            deviceDataSource = FakeDeviceDataSource(),
        )

        val device = repository.getOverview(
            seniorId = TEST_SENIOR_ID,
            currentParentInfo = null,
        ).device

        assertEquals("Galaxy S24", device?.name)
        assertEquals(DisplayDeviceConnectionStatus.LoginExpired, device?.connectionStatus)
        assertEquals(listOf(TEST_SENIOR_ID), homeDataSource.deviceSeniorIds)
    }

    @Test
    fun getOverviewKeepsSuccessfulDisconnectedDeviceDetailAuthoritative() = runBlocking {
        val repository = DisplayRepositoryImpl(
            homeDataSource = FakeHomeDataSource(
                homeResponse = homeResponse(
                    connection = ConnectionResponse(
                        connected = true,
                        battery = 72,
                        device_name = "stale-device",
                        connection_status = "ONLINE",
                    )
                ),
                deviceResponse = deviceDetailResponse(
                    deviceName = "stale-device",
                    connectionStatus = "DISCONNECTED",
                    connected = false,
                ),
            ),
            deviceDataSource = FakeDeviceDataSource(),
        )

        assertNull(
            repository.getOverview(
                seniorId = TEST_SENIOR_ID,
                currentParentInfo = null,
            ).device
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
                seniorId = TEST_SENIOR_ID,
                buttons = listOf(
                    button,
                    SeniorHomeButtonType.Call,
                    SeniorHomeButtonType.Message,
                    SeniorHomeButtonType.Camera,
                    SeniorHomeButtonType.YouTube,
                    SeniorHomeButtonType.ChatBuddy,
                    SeniorHomeButtonType.Medication,
                    SeniorHomeButtonType.Photo,
                    SeniorHomeButtonType.Emergency,
                ),
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
                    .getOverview(
                        seniorId = TEST_SENIOR_ID,
                        currentParentInfo = null,
                    )
                    .screenConfiguration
                    .buttons
                    .first(),
            )
        }
    }

    @Test
    fun offlineDeviceWithConnectionHistoryIsKept() = runBlocking {
        val lastConnectedAt = "2026-08-07T18:05:57.484865"
        val homeDataSource = FakeHomeDataSource(
            deviceResponse = DeviceDetailResponse(
                deviceName = null,
                connected = false,
                connectionStatus = "OFFLINE",
                batteryLevel = null,
                charging = null,
                deviceStatusSharingEnabled = null,
                networkConnected = false,
                defaultHomeEnabled = null,
                locationPermissionGranted = null,
                gpsEnabled = null,
                notificationPermissionGranted = null,
                appExecutionMaintained = null,
                lastConnectedAt = lastConnectedAt,
                lastLocationUpdatedAt = null,
            )
        )
        val repository = DisplayRepositoryImpl(
            homeDataSource = homeDataSource,
            deviceDataSource = FakeDeviceDataSource(),
        )

        val device = repository.getDevice(TEST_SENIOR_ID)

        assertEquals("시니어폰", device?.name)
        assertEquals(
            DisplayDeviceConnectionStatus.Offline,
            device?.connectionStatus,
        )
        assertEquals(lastConnectedAt, device?.lastConnectedAtLabel)
        assertEquals(listOf(TEST_SENIOR_ID), homeDataSource.deviceSeniorIds)
    }

    @Test
    fun deviceDetailMapsEveryBackendStatusField() = runBlocking {
        val response = DeviceDetailResponse(
            deviceName = "Galaxy S24",
            connected = true,
            connectionStatus = "ONLINE",
            batteryLevel = 72,
            charging = true,
            deviceStatusSharingEnabled = true,
            networkConnected = true,
            defaultHomeEnabled = true,
            locationPermissionGranted = true,
            gpsEnabled = false,
            notificationPermissionGranted = true,
            appExecutionMaintained = false,
            lastConnectedAt = "2026-09-15T21:54:00",
            lastLocationUpdatedAt = "2026-09-15T21:53:00",
        )
        val repository = DisplayRepositoryImpl(
            homeDataSource = FakeHomeDataSource(deviceResponse = response),
            deviceDataSource = FakeDeviceDataSource(),
        )

        val device = repository.getDevice(TEST_SENIOR_ID)

        assertEquals(DisplayDeviceConnectionStatus.Online, device?.connectionStatus)
        assertEquals(72, device?.batteryLevelPercent)
        assertEquals(true, device?.charging)
        assertEquals(true, device?.deviceStatusSharingEnabled)
        assertEquals(true, device?.networkConnected)
        assertEquals(true, device?.defaultHomeEnabled)
        assertEquals(true, device?.locationPermissionGranted)
        assertEquals(false, device?.gpsEnabled)
        assertEquals(true, device?.notificationPermissionGranted)
        assertEquals(false, device?.appExecutionMaintained)
        assertEquals(response.lastConnectedAt, device?.lastConnectedAtLabel)
        assertEquals(response.lastLocationUpdatedAt, device?.lastLocationUpdatedAtLabel)
    }

    @Test
    fun loginExpiredDeviceKeepsIdentityAndUsesDedicatedStatus() = runBlocking {
        val repository = DisplayRepositoryImpl(
            homeDataSource = FakeHomeDataSource(
                deviceResponse = DeviceDetailResponse(
                    deviceName = "Galaxy S24",
                    connected = false,
                    connectionStatus = " login_expired ",
                    batteryLevel = null,
                    charging = null,
                    deviceStatusSharingEnabled = null,
                    networkConnected = null,
                    defaultHomeEnabled = null,
                    locationPermissionGranted = null,
                    gpsEnabled = null,
                    notificationPermissionGranted = null,
                    appExecutionMaintained = null,
                    lastConnectedAt = "2026-09-15T21:54:00",
                    lastLocationUpdatedAt = "2026-09-15T21:53:00",
                )
            ),
            deviceDataSource = FakeDeviceDataSource(),
        )

        val device = repository.getDevice(TEST_SENIOR_ID)

        assertEquals("Galaxy S24", device?.name)
        assertEquals(DisplayDeviceConnectionStatus.LoginExpired, device?.connectionStatus)
        assertNull(device?.batteryLevelPercent)
        assertNull(device?.networkConnected)
    }

    @Test
    fun disconnectedStatusWinsOverStaleDeviceFields() = runBlocking {
        val repository = DisplayRepositoryImpl(
            homeDataSource = FakeHomeDataSource(
                deviceResponse = DeviceDetailResponse(
                    deviceName = "stale-device",
                    connected = true,
                    connectionStatus = "DISCONNECTED",
                    batteryLevel = 90,
                    charging = true,
                    deviceStatusSharingEnabled = true,
                    networkConnected = true,
                    defaultHomeEnabled = true,
                    locationPermissionGranted = true,
                    gpsEnabled = true,
                    notificationPermissionGranted = true,
                    appExecutionMaintained = true,
                    lastConnectedAt = "2026-09-15T21:54:00",
                    lastLocationUpdatedAt = "2026-09-15T21:53:00",
                )
            ),
            deviceDataSource = FakeDeviceDataSource(),
        )

        assertNull(repository.getDevice(TEST_SENIOR_ID))
    }

    @Test
    fun deviceWithoutIdentityOrConnectionHistoryIsAbsent() = runBlocking {
        val repository = DisplayRepositoryImpl(
            homeDataSource = FakeHomeDataSource(),
            deviceDataSource = FakeDeviceDataSource(),
        )

        assertNull(repository.getDevice(TEST_SENIOR_ID))
    }

    @Test
    fun disconnectDeviceForwardsSelectedSeniorId() = runBlocking {
        val deviceDataSource = FakeDeviceDataSource()
        val repository = DisplayRepositoryImpl(
            homeDataSource = FakeHomeDataSource(),
            deviceDataSource = deviceDataSource,
        )

        repository.disconnectDevice(TEST_SENIOR_ID)

        assertEquals(listOf(TEST_SENIOR_ID), deviceDataSource.disconnectedSeniorIds)
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

        val overview = repository.getOverview(
            seniorId = TEST_SENIOR_ID,
            currentParentInfo = staleLocalParentInfo,
        )
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

        val parentInfo = repository.getOverview(
            seniorId = TEST_SENIOR_ID,
            currentParentInfo = staleParentInfo,
        ).parentInfo

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

        val schedule = repository.getOverview(
            seniorId = TEST_SENIOR_ID,
            currentParentInfo = null,
        ).todaySchedule

        assertEquals("병원 일정", schedule?.title)
        assertEquals("연세세브란스병원", schedule?.description)
        assertEquals(1, schedule?.count)
        assertEquals("15:00", schedule?.scheduledTime)
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
                seniorId = TEST_SENIOR_ID,
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
                    "설정",
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
                    "SETTINGS",
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
            assertEquals(
                listOf(TEST_SENIOR_ID),
                homeDataSource.savedButtonSeniorIds,
            )
        }

    @Test
    fun getOverviewKeepsBackendButtonItemsAndOrder() =
        runBlocking {
            val serverButtons = listOf(
                HomeButtonResponse(
                    icon = null,
                    button_id = 1L,
                    button_order = 1,
                    button_name = "전화",
                    action_type = "DEFAULT",
                    action_value = "PHONE",
                ),
                HomeButtonResponse(
                    icon = null,
                    button_id = 2L,
                    button_order = 2,
                    button_name = "달력",
                    action_type = "DEFAULT",
                    action_value = "CALENDAR",
                ),
                HomeButtonResponse(
                    icon = null,
                    button_id = 3L,
                    button_order = 3,
                    button_name = "긴급알림",
                    action_type = "DEFAULT",
                    action_value = "EMERGENCY",
                ),
            )
            val homeDataSource = FakeHomeDataSource(
                homeResponse = HomeResponse(
                    connection = null,
                    buttons = serverButtons,
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

            val buttonItems = repository
                .getOverview(
                    seniorId = TEST_SENIOR_ID,
                    currentParentInfo = null,
                )
                .configuredButtonItems
                .filterNot { it.isDefaultAction("SCHEDULE") }

            assertEquals(
                listOf("PHONE", "CALENDAR", "EMERGENCY"),
                buttonItems.map(DisplayHomeButton::actionValue),
            )
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
                SeniorHomeButtonType.Flashlight,
            )

            repository.saveButtons(
                seniorId = TEST_SENIOR_ID,
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
            SeniorHomeButtonType.Flashlight,
            SeniorHomeButtonType.KakaoTalk,
            SeniorHomeButtonType.NaverBand,
            SeniorHomeButtonType.NaverCafe,
            SeniorHomeButtonType.Line,
            SeniorHomeButtonType.YouTube,
            SeniorHomeButtonType.Naver,
            SeniorHomeButtonType.KakaoMap,
            SeniorHomeButtonType.Daum,
        )

        val result = runCatching {
            repository.saveButtons(
                seniorId = TEST_SENIOR_ID,
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

        repository.saveButtons(
            seniorId = TEST_SENIOR_ID,
            buttons = defaultButtons + importedApp,
        )

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

        val overview = repository.getOverview(
            seniorId = TEST_SENIOR_ID,
            currentParentInfo = null,
        )

        assertEquals(
            listOf(
                SeniorHomeButtonType.Schedule,
                SeniorHomeButtonType.ChatBuddy,
                SeniorHomeButtonType.KakaoTalk,
                SeniorHomeButtonType.Medication,
                SeniorHomeButtonType.Settings,
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

        val overview = repository.getOverview(
            seniorId = TEST_SENIOR_ID,
            currentParentInfo = null,
        )

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

        val overview = repository.getOverview(
            seniorId = TEST_SENIOR_ID,
            currentParentInfo = null,
        )

        assertEquals(
            listOf(
                "PHONE",
                "MESSAGE",
                "CAMERA",
                "PHOTO",
                "CALENDAR",
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

        val overview = repository.getOverview(
            seniorId = TEST_SENIOR_ID,
            currentParentInfo = null,
        )

        assertFalse(overview.hasSavedButtonConfiguration)
    }

    @Test
    fun normalFontSizeUsesMediumApiValue() = runBlocking {
        val homeDataSource = FakeHomeDataSource()
        val repository = DisplayRepositoryImpl(
            homeDataSource = homeDataSource,
            deviceDataSource = FakeDeviceDataSource(),
        )

        repository.updateFontSize(
            seniorId = TEST_SENIOR_ID,
            fontSize = SeniorFontSize.Normal,
        )

        assertEquals("MEDIUM", homeDataSource.fontSizeRequests.single().font_size)
        assertEquals(listOf(TEST_SENIOR_ID), homeDataSource.fontSizeSeniorIds)
    }

    @Test
    fun customRelationshipUsesOtherAndCustomRelation() = runBlocking {
        val homeDataSource = FakeHomeDataSource()
        val repository = DisplayRepositoryImpl(
            homeDataSource = homeDataSource,
            deviceDataSource = FakeDeviceDataSource(),
        )

        val savedParentInfo = repository.updateSeniorProfile(
            ParentInfo(
                seniorId = 7L,
                name = "김영희",
                relationshipLabel = "이모",
                birthDate = LocalDate.of(1960, 1, 2),
                phoneNumber = "010-1234-5678",
                address = "서울시 강남구",
                addressDetail = "101동",
                addressLatitude = 37.5172,
                addressLongitude = 127.0473,
            )
        )

        val request = homeDataSource.profileRequests.single()
        assertEquals(listOf(7L), homeDataSource.profileSeniorIds)
        assertEquals("OTHER", request.relation)
        assertEquals("이모", request.customRelation)
        assertEquals("101동", request.detailAddress)
        assertEquals(37.5172, request.latitude)
        assertEquals(127.0473, request.longitude)
        assertEquals(37.5172, savedParentInfo.addressLatitude)
        assertEquals(127.0473, savedParentInfo.addressLongitude)
    }

    @Test
    fun todayHospitalRequestForwardsSelectedSeniorId() = runBlocking {
        val homeDataSource = FakeHomeDataSource()
        val repository = HomeServerRepositoryImpl(homeDataSource)

        repository.getTodayHospitalSchedules(TEST_SENIOR_ID)

        assertEquals(listOf(TEST_SENIOR_ID), homeDataSource.todayHospitalSeniorIds)
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

private fun deviceDetailResponse(
    deviceName: String? = "Galaxy S24",
    connected: Boolean? = false,
    connectionStatus: String?,
    lastConnectedAt: String? = null,
) = DeviceDetailResponse(
    deviceName = deviceName,
    connected = connected,
    connectionStatus = connectionStatus,
    batteryLevel = null,
    charging = null,
    deviceStatusSharingEnabled = null,
    networkConnected = null,
    defaultHomeEnabled = null,
    locationPermissionGranted = null,
    gpsEnabled = null,
    notificationPermissionGranted = null,
    appExecutionMaintained = null,
    lastConnectedAt = lastConnectedAt,
    lastLocationUpdatedAt = null,
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
    private val deviceResponse: DeviceDetailResponse =
        DeviceDetailResponse(
            deviceName = null,
            connected = false,
            connectionStatus = "DISCONNECTED",
            batteryLevel = null,
            charging = null,
            deviceStatusSharingEnabled = null,
            networkConnected = null,
            defaultHomeEnabled = null,
            locationPermissionGranted = null,
            gpsEnabled = null,
            notificationPermissionGranted = null,
            appExecutionMaintained = null,
            lastConnectedAt = null,
            lastLocationUpdatedAt = null,
        ),
    private val deviceFailure: RuntimeException? = null,
    private val buttonOptionsResponse: List<ButtonOptionResponse>? = null,
) : HomeDataSource {
    val savedButtonRequests = mutableListOf<HomeButtonSaveRequest>()
    val savedButtonSeniorIds = mutableListOf<Long>()
    val fontSizeRequests = mutableListOf<HomeFontSizeUpdateRequest>()
    val fontSizeSeniorIds = mutableListOf<Long>()
    val profileRequests = mutableListOf<SeniorProfileUpdateRequest>()
    val profileSeniorIds = mutableListOf<Long>()
    val homeSeniorIds = mutableListOf<Long>()
    val todayHospitalSeniorIds = mutableListOf<Long>()
    val deviceSeniorIds = mutableListOf<Long>()
    val buttonOptionsSeniorIds = mutableListOf<Long>()
    var buttonOptionsRequestCount = 0
        private set

    override suspend fun getHome(seniorId: Long): HomeResponse {
        homeSeniorIds += seniorId
        return homeResponse
    }

    override suspend fun getSeniorHome() =
        SeniorHomeResponse(emptyList(), "LARGE", null, null)

    override suspend fun getTodayHospitals(
        seniorId: Long,
    ): List<TodayHospitalListResponse> {
        todayHospitalSeniorIds += seniorId
        return emptyList()
    }

    override suspend fun getDevice(seniorId: Long): DeviceDetailResponse {
        deviceSeniorIds += seniorId
        deviceFailure?.let { throw it }
        return deviceResponse
    }

    override suspend fun getButtonOptions(seniorId: Long): List<ButtonOptionResponse> {
        buttonOptionsSeniorIds += seniorId
        buttonOptionsRequestCount += 1
        return buttonOptionsResponse
            ?: error("버튼 옵션 응답이 설정되지 않았습니다.")
    }

    override suspend fun saveButtons(request: HomeButtonSaveRequest) {
        savedButtonSeniorIds += request.seniorId
        savedButtonRequests += request
    }

    override suspend fun updateFontSize(request: HomeFontSizeUpdateRequest) {
        fontSizeSeniorIds += request.seniorId
        fontSizeRequests += request
    }

    override suspend fun updateSeniorProfile(
        request: SeniorProfileUpdateRequest,
    ): SeniorProfileUpdateResponse {
        profileSeniorIds += request.seniorId
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
            latitude = request.latitude,
            longitude = request.longitude,
        )
    }
}

private class FakeDeviceDataSource : DeviceDataSource {
    val disconnectedSeniorIds = mutableListOf<Long>()

    override suspend fun updateStatus(request: DeviceStatusUpdateRequest) = true
    override suspend fun disconnect(seniorId: Long) {
        disconnectedSeniorIds += seniorId
    }
    override suspend fun updateFcmToken(request: FcmTokenUpdateRequest) = Unit
    override suspend fun getLatestLocation(
        seniorId: Long,
    ): DeviceLocationResponse = error("Not used")
    override suspend fun updateLocation(request: DeviceLocationUpdateRequest) = Unit
    override suspend fun getHomeLocation(): HomeLocationResponse = error("Not used")
}
