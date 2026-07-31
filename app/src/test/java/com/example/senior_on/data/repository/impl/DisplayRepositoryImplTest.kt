package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.dto.ButtonOptionResponse
import com.example.senior_on.data.remote.dto.DeviceDetailResponse
import com.example.senior_on.data.remote.dto.DeviceStatusUpdateRequest
import com.example.senior_on.data.remote.dto.FamilyMemberResponse
import com.example.senior_on.data.remote.dto.HomeButtonCreateRequest
import com.example.senior_on.data.remote.dto.HomeButtonCreateResponse
import com.example.senior_on.data.remote.dto.HomeButtonResponse
import com.example.senior_on.data.remote.dto.HomeButtonSaveRequest
import com.example.senior_on.data.remote.dto.HomeButtonUpdateRequest
import com.example.senior_on.data.remote.dto.HomeFontSizeUpdateRequest
import com.example.senior_on.data.remote.dto.HomeResponse
import com.example.senior_on.data.remote.dto.SeniorHomeResponse
import com.example.senior_on.data.remote.dto.SeniorProfileResponse
import com.example.senior_on.data.remote.dto.SeniorProfileUpdateRequest
import com.example.senior_on.data.remote.dto.SeniorProfileUpdateResponse
import com.example.senior_on.data.remote.dto.TodayHospitalListResponse
import com.example.senior_on.data.remote.dto.TodayScheduleResponse
import com.example.senior_on.data.remote.dto.WeatherResponse
import com.example.senior_on.data.source.device.DeviceDataSource
import com.example.senior_on.data.source.home.HomeDataSource
import com.example.senior_on.domain.model.display.InitialSeniorHomeGridButtons
import com.example.senior_on.domain.model.display.SeniorFontSize
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.domain.model.parent.ParentInfo
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DisplayRepositoryImplTest {
    @Test
    fun getOverviewUsesSeniorIdFromHomeSeniorProfile() = runBlocking {
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

        val parentInfo = repository
            .getOverview(staleLocalParentInfo)
            .parentInfo

        assertEquals(77L, parentInfo?.seniorId)
        assertEquals("김영희", parentInfo?.name)
        assertEquals("서울시 강남구", parentInfo?.address)
        assertEquals("101동 202호", parentInfo?.addressDetail)
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
    fun saveButtonsAllowsEighteenGeneralButtonsWithScheduleAndMusicExcluded() =
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
                SeniorHomeButtonType.Flashlight,
                SeniorHomeButtonType.Camera,
                SeniorHomeButtonType.KakaoTalk,
                SeniorHomeButtonType.NaverBand,
                SeniorHomeButtonType.NaverCafe,
                SeniorHomeButtonType.Line,
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
            assertEquals(18, request.buttons.size)
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
            SeniorHomeButtonType.Camera,
            SeniorHomeButtonType.KakaoTalk,
            SeniorHomeButtonType.NaverBand,
            SeniorHomeButtonType.NaverCafe,
            SeniorHomeButtonType.Line,
            SeniorHomeButtonType.YouTube,
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
        assertEquals(0, homeDataSource.buttonOptionsRequestCount)
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

    override suspend fun getDevice() =
        DeviceDetailResponse(null, false, null, null, false, null, null)

    override suspend fun getButtonOptions(): List<ButtonOptionResponse> {
        buttonOptionsRequestCount += 1
        error("화면 탭에서는 버튼 옵션 API를 호출하면 안 됩니다.")
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
    override suspend fun updateStatus(request: DeviceStatusUpdateRequest) = Unit
    override suspend fun disconnect() = Unit
}
