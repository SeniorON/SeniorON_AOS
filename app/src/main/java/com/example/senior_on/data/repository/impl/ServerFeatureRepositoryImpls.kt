package com.example.senior_on.data.repository.impl

import com.example.senior_on.core.time.koreaToday
import com.example.senior_on.data.remote.dto.*
import com.example.senior_on.data.source.device.DeviceDataSource
import com.example.senior_on.data.source.device.DeviceIdentifierDataSource
import com.example.senior_on.data.source.device.LocalDeviceStatusDataSource
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

class HomeServerRepositoryImpl(
    private val source: HomeDataSource
) : HomeServerRepository {
    override suspend fun getHome(): HomeSnapshot = source.getHome().let {
        HomeSnapshot(
            userName = it.user_name.orEmpty(),
            fontSize = it.font_size.orEmpty(),
            connected = it.connection?.connected == true,
            battery = it.connection?.battery,
            buttons = it.buttons.orEmpty().map(HomeButtonResponse::toDomain),
            seniorAddress = it.senior_profile?.address,
            seniorId = it.senior_profile?.senior_id,
            seniorName = it.senior_profile?.name,
            seniorPhoneNumber = it.senior_profile?.phone,
        )
    }
    override suspend fun getSeniorHome() = source.getSeniorHome().let {
        SeniorHomeSnapshot(
            buttons = it.buttons.orEmpty()
                .sortedBy { button -> button.button_order }
                .map(HomeButtonResponse::toDomain),
            fontSize = it.font_size.orEmpty(),
            musicCard = it.music_card?.let { card ->
                ServerMusicCard(
                    enabled = card.enabled == true,
                    icon = card.icon,
                    musicApp = card.music_app,
                    appName = card.app_name,
                    actionType = card.action_type,
                    actionValue = card.action_value,
                    packageName = card.package_name,
                )
            },
            todaySchedule = it.today_schedule?.let { schedule ->
                ServerTodaySchedule(
                    title = schedule.title,
                    description = schedule.description,
                    count = schedule.schedule_count ?: 0,
                    displayType = schedule.display_type,
                    scheduleId = schedule.schedule_id,
                    scheduledTime = schedule.scheduled_time,
                )
            },
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
    override suspend fun saveButtons(
        musicApp: String?,
        buttons: List<ServerButton>,
    ) =
        source.saveButtons(
            HomeButtonSaveRequest(
                musicApp = musicApp,
                buttons = buttons.map { button ->
                    ButtonRequest(
                        buttonOrder = button.order,
                        buttonName = button.name,
                        actionType = requireNotNull(button.actionType) {
                            "액션 타입이 없는 버튼은 저장할 수 없습니다."
                        },
                        actionValue = requireNotNull(button.actionValue) {
                            "액션 값이 없는 버튼은 저장할 수 없습니다."
                        },
                        packageName = button.packageName,
                    )
                },
            )
        )
    override suspend fun addButton(optionId: Long) =
        source.addButton(HomeButtonCreateRequest(optionId)).let {
            ServerButton(it.buttonId ?: 0, optionId, it.buttonOrder ?: 0, it.buttonName.orEmpty(), it.icon, null, null)
        }
    override suspend fun updateButtons(buttons: List<Pair<Long, Int>>) =
        source.updateButtons(
            HomeButtonUpdateRequest(
                buttons.map { (buttonId, buttonOrder) ->
                    HomeButtonUpdateItemRequest(
                        button_id = buttonId,
                        button_order = buttonOrder,
                        button_name = null,
                        icon = null,
                    )
                }
            )
        )
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
    private val source: RemoteFamilySource,
    private val elapsedTimeMillis: () -> Long = { System.nanoTime() / 1_000_000L },
) : FamilyServerRepository {
    private val photoUploadMutex = Mutex()
    private val pendingPhotoUploads = mutableMapOf<String, PendingFamilyPhotoUpload>()

    override suspend fun hasFamily(): Boolean = try {
        source.getMembers().isNotEmpty()
    } catch (exception: HttpException) {
        if (exception.code() == 404) false else throw exception
    }

    override suspend fun join(code: String) = source.join(
        FamilyJoinRequest(normalizeFamilyCodeForRequest(code))
    ).let {
        FamilyCodeInfo(it.familyId, it.familyCode.orEmpty())
    }
    override suspend fun createCode() = source.createCode().let {
        FamilyCodeInfo(it.familyId, it.familyCode.orEmpty())
    }
    override suspend fun getCode() = source.getCode().let {
        FamilyCodeInfo(null, it.familyCode.orEmpty(), it.familyMemberCount)
    }
    override suspend fun getHome() = source.getHome().let { response ->
        ServerFamilyHome(
            members = response.members.orEmpty().map(
                FamilyMemberResponse::toServerFamilyMember,
            ),
            recentPhotos = response.recentPhotos.orEmpty().map(
                FamilyPhotoItemResponse::toServerFamilyPhoto,
            ),
        )
    }
    override suspend fun getMembers() = source.getMembers().map(
        FamilyMemberResponse::toServerFamilyMember,
    )
    override suspend fun changePrimaryManager(userId: Long) {
        source.changePrimaryManager(FamilyPrimaryManagerUpdateRequest(userId))
    }
    override suspend fun deleteMember(userId: Long) = source.deleteMember(userId)
    override suspend fun getPhotoAlbums() = source.getAlbums().map { album ->
        ServerFamilyPhotoAlbum(
            uploaderId = album.uploaderUserId.requirePositiveFamilyId("uploaderUserId"),
            uploaderName = album.uploaderName.orEmpty(),
            latestPhotoUrl = album.latestPhotoUrl.orEmpty(),
            photoCount = album.photoCount ?: 0L,
            hasNewPhotos = album.hasNewPhotos == true,
        )
    }
    override suspend fun getPhotos(
        uploaderId: Long?,
        cursorAt: String?,
        cursorId: Long?,
        size: Int?,
    ) = source.getPhotos(uploaderId, cursorAt, cursorId, size).let { response ->
        ServerFamilyPhotoPage(
            photos = response.photos.orEmpty().map(
                FamilyPhotoItemResponse::toServerFamilyPhoto,
            ),
            totalCount = response.totalCount ?: 0,
            nextCursor = response.nextCursor?.let { cursor ->
                val createdAt = cursor.createdAt
                val photoId = cursor.familyPhotoId
                if (createdAt.isNullOrBlank() || photoId == null) {
                    null
                } else {
                    ServerFamilyPhotoCursor(
                        createdAt = createdAt,
                        photoId = photoId,
                    )
                }
            },
            hasNext = response.hasNext == true,
        )
    }
    override suspend fun uploadPhoto(
        photo: PreparedFamilyPhoto,
        description: String,
        idempotencyKey: String,
    ): ServerFamilyPhoto = photoUploadMutex.withLock {
        val fileSize = photo.file.length()
        require(fileSize in 1..MAX_FAMILY_PHOTO_BYTES) {
            "가족 사진은 10MB 이하의 파일이어야 합니다."
        }
        require(photo.mimeType in FAMILY_PHOTO_CONTENT_TYPES) {
            "가족 사진은 JPG, PNG, WEBP 형식이어야 합니다."
        }

        val normalizedDescription = description.trim().takeIf(String::isNotEmpty)
        require(normalizedDescription == null || normalizedDescription.length <= MAX_PHOTO_DESCRIPTION_LENGTH) {
            "사진 설명은 30자 이하여야 합니다."
        }

        val pendingUpload = getOrCreatePendingPhotoUpload(
            idempotencyKey = idempotencyKey,
            contentType = photo.mimeType,
            fileSize = fileSize,
            description = normalizedDescription,
        )
        val uploaded = if (pendingUpload.storageUploadCompleted) {
            pendingUpload
        } else {
            try {
                source.uploadPhotoToStorage(
                    uploadUrl = pendingUpload.uploadUrl,
                    image = photo.file.asRequestBody(pendingUpload.contentType.toMediaType()),
                )
            } catch (exception: HttpException) {
                if (exception.code() in 400..499) {
                    pendingPhotoUploads.remove(idempotencyKey)
                }
                throw exception
            }

            pendingUpload.copy(storageUploadCompleted = true).also {
                pendingPhotoUploads[idempotencyKey] = it
            }
        }

        try {
            source.completePhotoUpload(
                idempotencyKey = idempotencyKey,
                request = FamilyPhotoUploadCompleteRequest(
                    imageKey = uploaded.imageKey,
                    description = uploaded.description,
                ),
            ).toServerFamilyPhoto().also {
                pendingPhotoUploads.remove(idempotencyKey)
            }
        } catch (exception: HttpException) {
            if (exception.code() in PHOTO_COMPLETION_RESTART_HTTP_CODES) {
                pendingPhotoUploads.remove(idempotencyKey)
            }
            throw exception
        }
    }

    private suspend fun getOrCreatePendingPhotoUpload(
        idempotencyKey: String,
        contentType: String,
        fileSize: Long,
        description: String?,
    ): PendingFamilyPhotoUpload {
        val now = elapsedTimeMillis()
        discardStalePendingPhotoUploads(now)
        pendingPhotoUploads[idempotencyKey]?.let { pending ->
            require(pending.contentType == contentType && pending.fileSize == fileSize) {
                "동일한 업로드 세션에는 같은 사진 파일을 사용해야 합니다."
            }
            return pending
        }

        val response = source.createPhotoUploadUrl(
            FamilyPhotoUploadUrlRequest(
                contentType = contentType,
                fileSize = fileSize,
            )
        )
        val imageKey = response.imageKey.requireNotBlankResponseField("imageKey")
        val uploadUrl = response.uploadUrl.requireNotBlankResponseField("uploadUrl")
        val expiresInSeconds = requireNotNull(response.expiresInSeconds) {
            "사진 업로드 URL 응답에 expiresInSeconds가 없습니다."
        }
        require(expiresInSeconds > 0) {
            "사진 업로드 URL 만료 시간이 올바르지 않습니다."
        }

        return PendingFamilyPhotoUpload(
            imageKey = imageKey,
            uploadUrl = uploadUrl,
            contentType = contentType,
            fileSize = fileSize,
            description = description,
            createdAtMillis = now,
            expiresAtMillis = now + expiresInSeconds * MILLIS_PER_SECOND,
        ).also { pending ->
            pendingPhotoUploads[idempotencyKey] = pending
            trimPendingPhotoUploads(idempotencyKey)
        }
    }

    private fun discardStalePendingPhotoUploads(nowMillis: Long) {
        pendingPhotoUploads.entries.removeAll { (_, pending) ->
            pending.isStale(nowMillis) ||
                (!pending.storageUploadCompleted && pending.isExpired(nowMillis))
        }
    }

    private fun trimPendingPhotoUploads(currentIdempotencyKey: String) {
        while (pendingPhotoUploads.size > MAX_PENDING_PHOTO_UPLOADS) {
            val oldestKey = pendingPhotoUploads
                .filterKeys { key -> key != currentIdempotencyKey }
                .minByOrNull { (_, pending) -> pending.createdAtMillis }
                ?.key
                ?: return
            pendingPhotoUploads.remove(oldestKey)
        }
    }
    override suspend fun getPhoto(photoId: Long) =
        source.getPhoto(photoId).toServerFamilyPhoto()
    override suspend fun markPhotoViewed(photoId: Long) = source.markViewed(photoId)
    override suspend fun deletePhoto(photoId: Long) = source.deletePhoto(photoId)
}

private data class PendingFamilyPhotoUpload(
    val imageKey: String,
    val uploadUrl: String,
    val contentType: String,
    val fileSize: Long,
    val description: String?,
    val createdAtMillis: Long,
    val expiresAtMillis: Long,
    val storageUploadCompleted: Boolean = false,
) {
    fun isExpired(nowMillis: Long): Boolean =
        nowMillis >= expiresAtMillis - UPLOAD_URL_EXPIRY_MARGIN_MILLIS

    fun isStale(nowMillis: Long): Boolean =
        nowMillis - createdAtMillis >= PENDING_UPLOAD_RETENTION_MILLIS
}

private fun String?.requireNotBlankResponseField(fieldName: String): String =
    requireNotNull(this?.takeIf(String::isNotBlank)) {
        "사진 업로드 URL 응답에 $fieldName 값이 없습니다."
    }

private const val MAX_FAMILY_PHOTO_BYTES = 10L * 1024L * 1024L
private const val MAX_PHOTO_DESCRIPTION_LENGTH = 30
private const val MILLIS_PER_SECOND = 1_000L
private const val UPLOAD_URL_EXPIRY_MARGIN_MILLIS = 5_000L
private const val PENDING_UPLOAD_RETENTION_MILLIS = 24L * 60L * 60L * 1_000L
private const val MAX_PENDING_PHOTO_UPLOADS = 8
private val FAMILY_PHOTO_CONTENT_TYPES = setOf(
    "image/jpeg",
    "image/png",
    "image/webp",
)
private val PHOTO_COMPLETION_RESTART_HTTP_CODES = setOf(400, 403, 404)

private fun FamilyMemberResponse.toServerFamilyMember() = ServerFamilyMember(
    id = usersId.requirePositiveFamilyId("usersId"),
    name = name.orEmpty(),
    role = role.orEmpty(),
    managerType = managerType.orEmpty(),
    canBecomePrimary = canBecomePrimary == true,
    isMe = me == true,
    profileImageUrl = profileImageUrl,
)

private fun FamilyPhotoItemResponse.toServerFamilyPhoto() = ServerFamilyPhoto(
    id = familyPhotoId.requirePositiveFamilyId("familyPhotoId"),
    imageUrl = imageUrl.orEmpty(),
    uploaderId = uploaderUserId.requirePositiveFamilyId("uploaderUserId"),
    uploaderName = uploaderName.orEmpty(),
    description = description.orEmpty(),
    createdAt = createdAt.orEmpty(),
    canDelete = canDelete == true,
    isNew = newPhoto == true,
)

private fun Long?.requirePositiveFamilyId(fieldName: String): Long {
    val id = requireNotNull(this) { "Family response is missing $fieldName" }
    require(id > 0L) { "Family response has invalid $fieldName: $id" }
    return id
}

class HospitalRepositoryImpl(
    private val source: HospitalDataSource
) : HospitalRepository {
    override suspend fun getMonthly(parentId: Long, year: Int, month: Int) =
        source.getMonthly(parentId, year, month).map(HospitalListResponse::toDomain)

    override suspend fun getDaily(parentId: Long, date: String) =
        source.getDaily(parentId, date.trim()).map(HospitalDetailResponse::toDomain)

    override suspend fun getUpcoming(parentId: Long): List<HospitalUpcomingGroup> =
        source.getUpcoming(parentId).map { response ->
            val groupDate = response.scheduleDate.orEmpty()
            HospitalUpcomingGroup(
                date = groupDate,
                appointments = response.schedules.orEmpty().map { schedule ->
                    val appointment = schedule.toDomain()
                    if (appointment.date.isBlank()) {
                        appointment.copy(date = groupDate)
                    } else {
                        appointment
                    }
                },
            )
        }

    override suspend fun create(parentId: Long, appointment: HospitalAppointment) =
        source.create(parentId, appointment.toCreateRequest()).toDomain()

    override suspend fun update(parentId: Long, appointment: HospitalAppointment) =
        source.update(parentId, appointment.id, appointment.toUpdateRequest())

    override suspend fun delete(parentId: Long, hospitalId: Long) =
        source.delete(parentId, hospitalId)
}

class MedicationRepositoryImpl(
    private val source: MedicationDataSource
) : MedicationRepository {
    override suspend fun getMedications(parentId: Long): List<MedicationInfo> =
        source.getMedications(parentId)
            .groupBy { response ->
                response.medicationGroupId
                    ?.takeIf(String::isNotBlank)
                    ?: response.medicationId?.toString().orEmpty()
            }
            .map { (groupId, responses) ->
                val first = responses.first()
                val times = first.medicineTimes
                    ?.map(String::trim)
                    ?.filter(String::isNotEmpty)
                    ?.takeIf(List<*>::isNotEmpty)
                    ?: responses.mapNotNull { it.medicineTime }
                        .map(String::trim)
                        .filter(String::isNotEmpty)
                        .distinct()
                val days = first.medicineDayList
                    ?.map(String::trim)
                    ?.filter(String::isNotEmpty)
                    ?.takeIf(List<*>::isNotEmpty)
                    ?: first.medicineDays
                        ?.split(",")
                        ?.map(String::trim)
                        ?.filter(String::isNotEmpty)
                        ?.takeIf(List<*>::isNotEmpty)
                    ?: responses.flatMap { response ->
                        response.medicineDays.orEmpty()
                            .split(",")
                            .map(String::trim)
                            .filter(String::isNotEmpty)
                    }.distinct()
                MedicationInfo(
                    id = first.medicationId,
                    groupId = groupId,
                    name = first.medicineName.orEmpty(),
                    ingredient = first.ingredientName,
                    times = times,
                    days = days,
                    startDate = first.startDate,
                    repeatType = first.repeatType?.takeIf(String::isNotBlank) ?: "DAILY",
                    repeatInterval = first.repeatInterval?.takeIf { it >= 1 } ?: 1,
                    repeatEndType = first.repeatEndType?.takeIf(String::isNotBlank) ?: "ONGOING",
                    durationWeeks = first.durationWeeks?.takeIf { it >= 1 },
                    endDate = first.endDate?.takeIf(String::isNotBlank),
                    medicationIds = responses
                        .flatMap { response ->
                            response.medicationIds.orEmpty() + listOfNotNull(response.medicationId)
                        }
                        .distinct(),
                )
            }

    override suspend fun create(parentId: Long, medication: MedicationInfo) =
        source.create(parentId, medication.toCreateRequest()).let { response ->
            MedicationInfo(
                id = response.medicationIds?.firstOrNull(),
                groupId = response.medicationGroupId.orEmpty(),
                name = response.medicineName.orEmpty(),
                ingredient = response.ingredientName,
                times = response.medicineTimes.orEmpty(),
                days = response.medicineDays.orEmpty(),
                startDate = response.startDate,
                repeatType = response.repeatType?.takeIf(String::isNotBlank) ?: medication.repeatType,
                repeatInterval = response.repeatInterval?.takeIf { it >= 1 } ?: medication.repeatInterval,
                repeatEndType = response.repeatEndType?.takeIf(String::isNotBlank)
                    ?: medication.repeatEndType,
                durationWeeks = response.durationWeeks?.takeIf { it >= 1 } ?: medication.durationWeeks,
                endDate = response.endDate?.takeIf(String::isNotBlank) ?: medication.endDate,
                medicationIds = response.medicationIds.orEmpty(),
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

    override suspend fun getParentMonthlySchedules(parentId: Long, year: Int, month: Int) =
        source.getParentMonthlySchedules(parentId, year, month)
            .toDomain(requestedYear = year, requestedMonth = month)

    override suspend fun markNearestTaken() = source.checkNearest().toDomain()

    override suspend fun markTaken(medicationLogId: Long) =
        source.check(medicationLogId).toDomain()
}

internal fun MedicationMonthlyScheduleResponse.toDomain(
    requestedYear: Int,
    requestedMonth: Int,
): MedicationMonthlySchedule {
    val requestedYearMonth = YearMonth.of(requestedYear, requestedMonth)
    return MedicationMonthlySchedule(
        year = requestedYear,
        month = requestedMonth,
        scheduledDates = scheduledDates.orEmpty()
            .mapNotNull { value ->
                runCatching { LocalDate.parse(value.trim()) }.getOrNull()
            }
            .filterTo(mutableSetOf()) { date ->
                YearMonth.from(date) == requestedYearMonth
            },
    )
}

class NotificationRepositoryImpl(
    private val source: NotificationDataSource
) : NotificationRepository {
    override suspend fun getNotifications(type: String, cursor: Long?, size: Int?) =
        source.getNotifications(type.trim().uppercase(), cursor, size).let {
            NotificationPage(
                it.totalCount ?: 0L,
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
    override suspend fun getHome() = source.getSettings().let { response ->
        NotificationHome(
            enabledCount = response.enabledCount ?: 0L,
            items = response.items.orEmpty().map { item ->
                NotificationHomeItem(
                    type = item.type.orEmpty(),
                    enabled = item.enabled == true,
                    hasAlert = item.hasAlert == true,
                    occurredAt = item.occurredAt,
                    dateTimeLabel = item.dateTimeLabel,
                    summary = item.summary,
                    senderId = item.senderId,
                    senderName = item.senderName,
                    deviceBattery = item.deviceBattery,
                    address = item.address,
                    linkUrl = item.linkUrl,
                    phase = item.phase,
                    emptyMessage = item.emptyMessage,
                    notificationId = item.notificationId,
                    eventId = item.eventId,
                )
            },
        )
    }
    override suspend fun updateSetting(type: String, enabled: Boolean) =
        source.updateSetting(type.trim().uppercase(), NotificationSettingRequest(enabled)).let {
            NotificationSetting(it.type.orEmpty(), it.enabled == true)
        }
    override suspend fun isParentDeviceOnline() = source.getParentDeviceStatus().online == true
    override suspend fun getInactivitySetting(userId: Long) =
        source.getInactivitySetting(userId).toDomain()
    override suspend fun getMyInactivitySetting() =
        source.getMyInactivitySetting().toDomain()
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
            SafetyEvent(
                it.id,
                "RISK_LINK",
                it.detectedAt,
                null,
                null,
                null,
                battery,
                it.linkUrl,
                when (it.riskLevel?.trim()?.uppercase()) {
                    "낮음", "LOW", "SAFE" -> false
                    "높음", "HIGH", "DANGEROUS" -> true
                    else -> null
                },
            )
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
            id = it.eventId,
            type = it.eventType.orEmpty(),
            occurredAt = it.occurredAt,
            address = it.address,
            latitude = it.latitude,
            longitude = it.longitude,
            deviceBattery = it.deviceBattery,
            linkUrl = it.linkUrl,
            dangerous = it.isDangerous,
            phase = it.phase,
            message = it.message,
            senderName = it.senderName,
            lastSeenAt = it.lastSeenAt,
        )
    }
}

class UserSettingsRepositoryImpl(
    private val source: UserSettingsDataSource
) : UserSettingsRepository {
    override suspend fun getSettings(): UserAccountSettings {
        val account = source.getAccount()
        val profileImage = source.getProfileImage()
        return UserAccountSettings(
            name = account.name.orEmpty(),
            role = account.role.orEmpty(),
            email = account.email.orEmpty(),
            profileImageUrl = profileImage.profileImageUrl,
            isDefaultProfileImage = profileImage.isDefaultProfileImage
                ?: profileImage.profileImageUrl.isNullOrBlank(),
        )
    }

    override suspend fun getName(): String =
        source.getAccount().name.orEmpty()

    override suspend fun updateName(name: String) =
        source.updateName(NameUpdateRequest(name.trim())).name.orEmpty()

    override suspend fun changePassword(current: String, new: String, confirmation: String) =
        source.changePassword(PasswordChangeRequest(current, new, confirmation)).changed == true

    override suspend fun getProfileImageUrl(): String? =
        source.getProfileImage().profileImageUrl

    override suspend fun resetProfileImage(): UserProfileImage =
        source.resetProfileImage().let { response ->
            UserProfileImage(
                profileImageUrl = response.profileImageUrl,
                isDefaultProfileImage = response.isDefaultProfileImage
                    ?: response.profileImageUrl.isNullOrBlank(),
            )
        }

    override suspend fun updateProfileImage(photo: PreparedFamilyPhoto): String? {
        val part = MultipartBody.Part.createFormData(
            "image", photo.displayName, photo.file.asRequestBody(photo.mimeType.toMediaType())
        )
        return source.updateProfileImage(part).profileImageUrl
    }
}

class DeviceRepositoryImpl(
    private val source: DeviceDataSource,
    private val identifierSource: DeviceIdentifierDataSource,
    private val localStatusSource: LocalDeviceStatusDataSource,
) : DeviceRepository {
    override suspend fun updateStatus(): Boolean = source.updateStatus(
        DeviceStatusUpdateRequest(
            deviceIdentifier = identifierSource.getOrCreateIdentifier(),
            deviceName = localStatusSource.getDeviceName(),
            batteryLevel = localStatusSource.getBatteryLevel(),
        )
    )

    override suspend fun updateFcmToken(token: String) = source.updateFcmToken(
        FcmTokenUpdateRequest(
            deviceIdentifier = identifierSource.getOrCreateIdentifier(),
            deviceToken = token,
        )
    )

    override suspend fun disconnect() = source.disconnect()

    override suspend fun getLatestLocation(): DeviceLocation =
        source.getLatestLocation().let { response ->
            DeviceLocation(
                latitude = requireNotNull(response.latitude) {
                    "최근 위치의 위도가 없습니다."
                },
                longitude = requireNotNull(response.longitude) {
                    "최근 위치의 경도가 없습니다."
                },
                lastLocationUpdatedAt = response.lastLocationUpdatedAt,
            )
        }

    override suspend fun updateLocation(latitude: Double, longitude: Double) {
        source.updateLocation(
            DeviceLocationUpdateRequest(
                deviceIdentifier = identifierSource.getOrCreateIdentifier(),
                latitude = latitude,
                longitude = longitude,
            )
        )
    }

    override suspend fun getHomeLocation(): SeniorHomeLocation =
        source.getHomeLocation().let { response ->
            SeniorHomeLocation(
                latitude = requireNotNull(response.latitude) {
                    "등록된 집 좌표의 위도가 없습니다."
                },
                longitude = requireNotNull(response.longitude) {
                    "등록된 집 좌표의 경도가 없습니다."
                },
            )
        }

    override fun getBatteryLevel(): Int = localStatusSource.getBatteryLevel()
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
    MedicationCreateRequest(
        medicineName = name.trim(),
        ingredientName = ingredient?.trim()?.takeIf(String::isNotEmpty),
        medicineTimes = times,
        startDate = startDate?.takeIf(String::isNotBlank) ?: koreaToday().toString(),
        repeatType = repeatType,
        repeatInterval = repeatInterval.coerceAtLeast(1),
        medicineDays = days,
        repeatEndType = repeatEndType,
        durationWeeks = durationWeeks?.takeIf { it >= 1 }
            ?.takeIf { repeatEndType.equals("DURATION", ignoreCase = true) },
        endDate = endDate?.takeIf(String::isNotBlank)
            ?.takeIf {
                repeatEndType.equals("END_DATE", ignoreCase = true) ||
                    repeatEndType.equals("DURATION", ignoreCase = true)
            },
    )

private fun MedicationInfo.toUpdateRequest() =
    MedicationUpdateRequest(
        medicationGroupId = groupId.trim(),
        medicineName = name.trim(),
        ingredientName = ingredient?.trim()?.takeIf(String::isNotEmpty),
        medicineTimes = times,
        startDate = startDate?.takeIf(String::isNotBlank) ?: koreaToday().toString(),
        repeatType = repeatType,
        repeatInterval = repeatInterval.coerceAtLeast(1),
        medicineDays = days,
        repeatEndType = repeatEndType,
        durationWeeks = durationWeeks?.takeIf { it >= 1 }
            ?.takeIf { repeatEndType.equals("DURATION", ignoreCase = true) },
        endDate = endDate?.takeIf(String::isNotBlank)
            ?.takeIf {
                repeatEndType.equals("END_DATE", ignoreCase = true) ||
                    repeatEndType.equals("DURATION", ignoreCase = true)
            },
    )
private fun MedicationScheduleResponse.toDomain() =
    MedicationSchedule(
        logId = medicationLogId ?: 0,
        name = medicineName.orEmpty(),
        plannedTime = plannedTime.orEmpty(),
        taken = isTaken == true || status.equals("TAKEN", ignoreCase = true),
        takenTime = takenTime,
        ingredient = ingredientName,
        plannedDate = plannedDate,
        status = status,
    )

private fun MedicationCheckResponse.toDomain() =
    MedicationSchedule(
        logId = medicationLogId ?: 0L,
        name = "",
        plannedTime = "",
        taken = isTaken == true,
        takenAt = takenAt,
        status = if (isTaken == true) "TAKEN" else null,
    )

private fun InactivitySettingResponse.toDomain() =
    InactivitySetting(usersId ?: 0, thresholdHours ?: 0, isEnabled == true)
