package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.dto.ButtonOptionResponse
import com.example.senior_on.data.remote.dto.DeviceDetailResponse
import com.example.senior_on.data.remote.dto.DeviceStatusUpdateRequest
import com.example.senior_on.data.remote.dto.HomeButtonCreateRequest
import com.example.senior_on.data.remote.dto.HomeButtonCreateResponse
import com.example.senior_on.data.remote.dto.HomeButtonResponse
import com.example.senior_on.data.remote.dto.HomeButtonSaveRequest
import com.example.senior_on.data.remote.dto.HomeButtonUpdateRequest
import com.example.senior_on.data.remote.dto.HomeFontSizeUpdateRequest
import com.example.senior_on.data.remote.dto.HomeResponse
import com.example.senior_on.data.remote.dto.SeniorHomeResponse
import com.example.senior_on.data.remote.dto.SeniorProfileUpdateRequest
import com.example.senior_on.data.remote.dto.SeniorProfileUpdateResponse
import com.example.senior_on.data.remote.dto.TodayHospitalListResponse
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
                    SeniorHomeButtonType.Message,
                    SeniorHomeButtonType.Emergency,
                ),
                customButtonLabels = mapOf(
                    SeniorHomeButtonType.Call to "엄마 전화",
                ),
            )

            val request = homeDataSource.savedButtonRequests.single()
            assertEquals("MELON", request.musicApp)
            assertEquals(listOf(1, 2, 3), request.buttons.map { it.buttonOrder })
            assertEquals(
                listOf("엄마 전화", "메시지", "긴급알림"),
                request.buttons.map { it.buttonName },
            )
            assertEquals(
                listOf(
                    "com.samsung.android.dialer",
                    "com.samsung.android.messaging",
                    "EMERGENCY",
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
        assertEquals((1..8).toList(), request.buttons.map { it.buttonOrder })
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
            ),
            request.buttons.map { it.buttonName },
        )
        assertEquals(
            listOf(
                "com.samsung.android.dialer",
                "com.samsung.android.messaging",
                "com.sec.android.app.camera",
                "com.sec.android.gallery3d",
                "com.google.android.youtube",
                "COMPANION",
                "MEDICATION",
                "EMERGENCY",
            ),
            request.buttons.map { it.packageName },
        )
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
) : HomeDataSource {
    val savedButtonRequests = mutableListOf<HomeButtonSaveRequest>()
    val fontSizeRequests = mutableListOf<HomeFontSizeUpdateRequest>()
    val profileRequests = mutableListOf<SeniorProfileUpdateRequest>()
    var buttonOptionsRequestCount = 0
        private set
    var legacyButtonMutationCount = 0
        private set

    override suspend fun getHome() = homeResponse

    override suspend fun getWeather(
        latitude: Double,
        longitude: Double,
    ) = WeatherResponse(null, null, null, null)

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
