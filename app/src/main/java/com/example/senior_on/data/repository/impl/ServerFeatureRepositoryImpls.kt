package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.dto.*
import com.example.senior_on.data.source.device.DeviceDataSource
import com.example.senior_on.data.source.event.EventDataSource
import com.example.senior_on.data.source.family.RemoteFamilySource
import com.example.senior_on.data.source.health.HospitalDataSource
import com.example.senior_on.data.source.home.HomeDataSource
import com.example.senior_on.data.source.medication.MedicationDataSource
import com.example.senior_on.data.source.notification.NotificationDataSource
import com.example.senior_on.data.source.settings.UserSettingsDataSource
import com.example.senior_on.domain.model.family.PreparedFamilyPhoto
import com.example.senior_on.domain.model.server.*
import com.example.senior_on.domain.repository.server.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate
import java.time.LocalTime

class HomeServerRepositoryImpl(
    private val source: HomeDataSource
) : HomeServerRepository {
    override suspend fun getHome(): HomeSnapshot = source.getHome().let {
        HomeSnapshot(
            userName = it.user_name.orEmpty(),
            fontSize = it.font_size.orEmpty(),
            connected = it.connection?.connected == true,
            battery = it.connection?.battery,
            buttons = it.buttons.orEmpty().map(HomeButtonResponse::toDomain)
        )
    }
    override suspend fun getSeniorHome(): SeniorHomeSnapshot =
        source.getSeniorHome().let { response ->
            SeniorHomeSnapshot(
                fontSize = response.font_size.orEmpty(),
                musicCard = response.music_card
                    ?.takeIf { it.enabled == true }
                    ?.let { music ->
                        ServerButton(
                            id = 0,
                            order = 0,
                            name = music.app_name.orEmpty(),
                            icon = music.icon,
                            actionType = music.action_type,
                            actionValue = music.action_value,
                            packageName = music.package_name,
                        )
                    },
                todaySchedule = response.today_schedule.let { schedule ->
                    SeniorHomeSchedule(
                        count = schedule?.schedule_count ?: 0,
                        displayType = schedule?.display_type.orEmpty(),
                        id = schedule?.schedule_id,
                        title = schedule?.title,
                        description = schedule?.description,
                        scheduledTime = schedule?.scheduled_time,
                    )
                },
                buttons = response.buttons.orEmpty()
                    .sortedBy { it.button_order }
                    .map(HomeButtonResponse::toDomain),
            )
        }
    override suspend fun getTodayHospitalSchedules(): List<TodayHospitalSchedule> =
        source.getTodayHospitals().map { schedule ->
            TodayHospitalSchedule(
                id = schedule.hospitalId ?: 0,
                hospitalName = schedule.hospitalName.orEmpty(),
                department = schedule.department.orEmpty(),
                date = schedule.scheduleDate
                    ?.let(LocalDate::parse)
                    ?: LocalDate.now(),
                time = schedule.scheduleTime
                    ?.let(LocalTime::parse)
                    ?: LocalTime.MIDNIGHT,
                reminderType = schedule.reminderType,
                registeredBy = schedule.registeredBy,
            )
        }.sortedBy(TodayHospitalSchedule::time)
    override suspend fun getWeather(latitude: Double, longitude: Double) =
        source.getWeather(latitude, longitude).let {
            WeatherInfo(it.temperature ?: 0, it.weatherStatus.orEmpty(), it.weatherText.orEmpty(), it.observedAt)
        }
    override suspend fun getDevice() = source.getDevice().let {
        DeviceInfo(
            it.deviceName.orEmpty(), it.connected == true, it.connectionStatus.orEmpty(),
            it.batteryLevel, it.networkConnected == true, it.lastConnectedAt,
            it.lastLocationUpdatedAt
        )
    }
    override suspend fun getButtonOptions() = source.getButtonOptions().map {
        ServerButton(0, it.option_id, 0, it.button_name.orEmpty(), it.icon, it.action_type, it.action_value)
    }
    override suspend fun saveButtons(musicApp: String?, buttons: List<Pair<Long, Int>>) =
        source.saveButtons(HomeButtonSaveRequest(musicApp, buttons.map { ButtonRequest(it.first, it.second) }))
    override suspend fun addButton(optionId: Long) =
        source.addButton(HomeButtonCreateRequest(optionId)).let {
            ServerButton(it.buttonId ?: 0, optionId, it.buttonOrder ?: 0, it.buttonName.orEmpty(), it.icon, null, null)
        }
    override suspend fun updateButtons(buttons: List<Pair<Long, Int>>) =
        source.updateButtons(HomeButtonUpdateRequest(buttons.map { ButtonRequest(it.first, it.second) }))
    override suspend fun deleteButton(buttonId: Long) = source.deleteButton(buttonId)
    override suspend fun updateFontSize(fontSize: String) =
        source.updateFontSize(HomeFontSizeUpdateRequest(fontSize.trim().uppercase()))
    override suspend fun updateSeniorProfile(
        name: String, relation: String, customRelation: String?, birth: String,
        phoneNumber: String, address: String?, detailAddress: String?
    ) = source.updateSeniorProfile(
        SeniorProfileUpdateRequest(
            name.trim(), relation.trim().uppercase(), customRelation?.trim(), birth,
            phoneNumber.trim(), address?.trim(), detailAddress?.trim()
        )
    ).let {
        SeniorProfileUpdate(
            it.seniorId ?: 0, it.name.orEmpty(), it.relation.orEmpty(),
            it.customRelation, it.birth.orEmpty(), it.phoneNumber.orEmpty(),
            it.address, it.detailAddress
        )
    }
}

class FamilyServerRepositoryImpl(
    private val source: RemoteFamilySource
) : FamilyServerRepository {
    override suspend fun join(code: String) = source.join(FamilyJoinRequest(code.trim())).let {
        FamilyCodeInfo(it.familyId, it.familyCode.orEmpty())
    }
    override suspend fun createCode() = source.createCode().let {
        FamilyCodeInfo(it.familyId, it.familyCode.orEmpty())
    }
    override suspend fun getCode() = source.getCode().let {
        FamilyCodeInfo(null, it.familyCode.orEmpty(), it.familyMemberCount)
    }
    override suspend fun getHome() = source.getHome().let {
        ServerFamilyHome(it.members.orEmpty().map(FamilyMemberResponse::toDomain), it.recentPhotos.orEmpty().map(FamilyPhotoItemResponse::toDomain))
    }
    override suspend fun getMembers() = source.getMembers().map(FamilyMemberResponse::toDomain)
    override suspend fun changePrimaryManager(userId: Long) {
        source.changePrimaryManager(FamilyPrimaryManagerUpdateRequest(userId))
    }
    override suspend fun deleteMember(userId: Long) = source.deleteMember(userId)
    override suspend fun getPhotos(uploaderId: Long?, cursorAt: String?, cursorId: Long?, size: Int?) =
        source.getPhotos(uploaderId, cursorAt, cursorId, size).photos.orEmpty().map(FamilyPhotoItemResponse::toDomain)
    override suspend fun uploadPhoto(photo: PreparedFamilyPhoto, description: String): ServerFamilyPhoto {
        val body = photo.file.asRequestBody(photo.mimeType.toMediaType())
        val part = MultipartBody.Part.createFormData("image", photo.displayName, body)
        val descriptionBody = description.trim().toRequestBody("text/plain".toMediaType())
        return source.uploadPhoto(part, descriptionBody).toDomain()
    }
    override suspend fun markPhotoViewed(photoId: Long) = source.markViewed(photoId)
    override suspend fun deletePhoto(photoId: Long) = source.deletePhoto(photoId)
}

class HospitalRepositoryImpl(
    private val source: HospitalDataSource
) : HospitalRepository {
    override suspend fun getMonthly(parentId: Long, year: Int, month: Int) =
        source.getMonthly(parentId, year, month).map(HospitalListResponse::toDomain)
    override suspend fun getDaily(parentId: Long, date: String) =
        source.getDaily(parentId, date.trim()).map(HospitalDetailResponse::toDomain)
    override suspend fun create(parentId: Long, appointment: HospitalAppointment) =
        source.create(parentId, appointment.toCreateRequest()).toDomain()
    override suspend fun update(parentId: Long, appointment: HospitalAppointment) =
        source.update(parentId, appointment.id, appointment.toUpdateRequest())
    override suspend fun delete(parentId: Long, hospitalId: Long) = source.delete(parentId, hospitalId)
}

class MedicationRepositoryImpl(
    private val source: MedicationDataSource
) : MedicationRepository {
    override suspend fun getMedications(parentId: Long) = source.getMedications(parentId).map {
        MedicationInfo(
            it.medicationId, it.medicationGroupId.orEmpty(), it.medicineName.orEmpty(),
            it.ingredientName, listOfNotNull(it.medicineTime), it.medicineDays.orEmpty()
                .split(",").map(String::trim).filter(String::isNotEmpty)
        )
    }
    override suspend fun create(parentId: Long, medication: MedicationInfo) =
        source.create(parentId, medication.toCreateRequest()).let {
            MedicationInfo(
                it.medicationIds?.firstOrNull(), it.medicationGroupId.orEmpty(),
                it.medicineName.orEmpty(), it.ingredientName,
                it.medicineTimes.orEmpty(), it.medicineDays.orEmpty()
            )
        }
    override suspend fun update(parentId: Long, medication: MedicationInfo) {
        source.update(parentId, medication.toUpdateRequest())
    }
    override suspend fun delete(parentId: Long, groupId: String) {
        source.delete(parentId, groupId.trim())
    }
    override suspend fun getMySchedules(date: String) =
        source.getMySchedules(date.trim()).map(MedicationScheduleResponse::toDomain)
    override suspend fun getParentSchedules(parentId: Long, date: String) =
        source.getParentSchedules(parentId, date.trim()).map(MedicationScheduleResponse::toDomain)
    override suspend fun markTaken(logId: Long) = source.check(logId).let {
        MedicationSchedule(it.medicationLogId ?: logId, "", "", it.isTaken == true, it.takenAt)
    }
}

class NotificationRepositoryImpl(
    private val source: NotificationDataSource
) : NotificationRepository {
    override suspend fun getNotifications(type: String, cursor: Long?, size: Int?) =
        source.getNotifications(type.trim().uppercase(), cursor, size).let {
            NotificationPage(
                it.totalCount ?: 0,
                it.items.orEmpty().map { item ->
                    AppNotification(
                        item.notificationId ?: 0, item.eventId, item.title.orEmpty(),
                        item.summary.orEmpty(), item.occurredAt.orEmpty(), item.read == true
                    )
                },
                it.nextCursor
            )
        }
    override suspend fun markRead(id: Long) = source.markRead(id)
    override suspend fun delete(id: Long) = source.delete(id)
    override suspend fun getSettings() = source.getSettings().items.orEmpty().map {
        NotificationSetting(it.type.orEmpty(), it.enabled == true)
    }
    override suspend fun updateSetting(type: String, enabled: Boolean) =
        source.updateSetting(type.trim().uppercase(), NotificationSettingRequest(enabled)).let {
            NotificationSetting(it.type.orEmpty(), it.enabled == true)
        }
    override suspend fun isParentDeviceOnline() = source.getParentDeviceStatus().online == true
    override suspend fun getInactivitySetting(userId: Long) =
        source.getInactivitySetting(userId).toDomain()
    override suspend fun updateInactivitySetting(userId: Long, thresholdHours: Int) =
        source.updateInactivitySetting(userId, InactivitySettingRequest(thresholdHours)).toDomain()
}

class EventRepositoryImpl(
    private val source: EventDataSource
) : EventRepository {
    override suspend fun createSos(latitude: Double, longitude: Double, battery: Int?) =
        source.createSos(SosEventRequest(latitude, longitude, battery)).let {
            SafetyEvent(
                id = it.id,
                type = "SOS",
                occurredAt = null,
                address = it.address,
                latitude = it.latitude,
                longitude = it.longitude,
                deviceBattery = it.deviceBattery,
                receiverCount = it.receiverCount,
                notifiedCount = it.notifiedCount,
            )
        }
    override suspend fun createRiskLink(url: String, battery: Int?) =
        source.createRiskLink(RiskLinkRequest(url.trim(), battery)).let {
            SafetyEvent(it.id, "RISK_LINK", it.detectedAt, null, null, null, battery, it.linkUrl, it.riskLevel != "SAFE")
        }
    override suspend fun createOutingReturn(
        phase: String, latitude: Double, longitude: Double, battery: Int
    ) = source.createOutingReturn(
        OutingReturnRequest(phase.trim().uppercase(), latitude, longitude, battery)
    ).let {
        SafetyEvent(it.id, "OUTING_RETURN", it.occurredAt, it.address, it.latitude, it.longitude, it.deviceBattery, phase = it.phase)
    }
    override suspend fun createInactivity(
        latitude: Double, longitude: Double, battery: Int, lastSeenAt: String
    ) = source.createInactivity(InactivityRequest(latitude, longitude, battery, lastSeenAt)).let {
        SafetyEvent(null, "INACTIVITY", it.lastSeenAt, it.address, it.latitude, it.longitude, battery)
    }
    override suspend fun getDetail(eventId: Long) = source.getDetail(eventId).let {
        SafetyEvent(
            it.eventId, it.eventType.orEmpty(), it.occurredAt, it.address,
            it.latitude, it.longitude, it.deviceBattery, it.linkUrl, it.isDangerous, it.phase
        )
    }
}

class UserSettingsRepositoryImpl(
    private val source: UserSettingsDataSource
) : UserSettingsRepository {
    override suspend fun getSettings() =
        UserAccountSettings(source.getName().name.orEmpty(), source.getProfileImage().profileImageUrl)
    override suspend fun updateName(name: String) =
        source.updateName(NameUpdateRequest(name.trim())).name.orEmpty()
    override suspend fun changePassword(current: String, new: String, confirmation: String) =
        source.changePassword(PasswordChangeRequest(current, new, confirmation)).changed == true
    override suspend fun updateProfileImage(photo: PreparedFamilyPhoto): String? {
        val part = MultipartBody.Part.createFormData(
            "image", photo.displayName, photo.file.asRequestBody(photo.mimeType.toMediaType())
        )
        return source.updateProfileImage(part).profileImageUrl
    }
}

class DeviceRepositoryImpl(
    private val source: DeviceDataSource
) : DeviceRepository {
    override suspend fun updateStatus(identifier: String, name: String, batteryLevel: Int) =
        source.updateStatus(DeviceStatusUpdateRequest(identifier.trim(), name.trim(), batteryLevel.coerceIn(0, 100)))
    override suspend fun disconnect() = source.disconnect()
}

private fun HomeButtonResponse.toDomain() = ServerButton(
    button_id ?: 0,
    null,
    button_order ?: 0,
    button_name.orEmpty(),
    icon,
    action_type,
    action_value,
    package_name,
)
private fun FamilyMemberResponse.toDomain() = ServerFamilyMember(
    usersId ?: 0, name.orEmpty(), role.orEmpty(), managerType.orEmpty(), me == true, profileImageUrl
)
private fun FamilyPhotoItemResponse.toDomain() = ServerFamilyPhoto(
    familyPhotoId ?: 0, imageUrl.orEmpty(), uploaderUserId ?: 0, uploaderName.orEmpty(),
    description.orEmpty(), createdAt.orEmpty(), canDelete == true, newPhoto == true
)
private fun HospitalListResponse.toDomain() = HospitalAppointment(
    hospitalId ?: 0, hospitalName.orEmpty(), department.orEmpty(),
    scheduleDate.orEmpty(), scheduleTime.orEmpty(), reminderType.orEmpty()
)
private fun HospitalDetailResponse.toDomain() = HospitalAppointment(
    hospitalId ?: 0, hospitalName.orEmpty(), department.orEmpty(),
    scheduleDate.orEmpty(), scheduleTime.orEmpty(), reminderType.orEmpty()
)
private fun HospitalCreateResponse.toDomain() = HospitalAppointment(
    hospitalId ?: 0, hospitalName.orEmpty(), department.orEmpty(),
    scheduleDate.orEmpty(), scheduleTime.orEmpty(), reminderType.orEmpty()
)
private fun HospitalAppointment.toCreateRequest() =
    HospitalCreateRequest(hospitalName.trim(), department.trim(), date, time, reminderType)
private fun HospitalAppointment.toUpdateRequest() =
    HospitalUpdateRequest(hospitalName.trim(), department.trim(), date, time, reminderType)
private fun MedicationInfo.toCreateRequest() =
    MedicationCreateRequest(name.trim(), ingredient?.trim(), times, days)
private fun MedicationInfo.toUpdateRequest() =
    MedicationUpdateRequest(groupId.trim(), name.trim(), ingredient?.trim(), days, times)
private fun MedicationScheduleResponse.toDomain() =
    MedicationSchedule(medicationLogId ?: 0, medicineName.orEmpty(), plannedTime.orEmpty(), isTaken == true)
private fun InactivitySettingResponse.toDomain() =
    InactivitySetting(usersId ?: 0, thresholdHours ?: 0, isEnabled == true)
