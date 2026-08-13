package com.example.senior_on.domain.repository.server

import com.example.senior_on.domain.model.family.PreparedFamilyPhoto
import com.example.senior_on.domain.model.server.*

interface HomeServerRepository {
    suspend fun getHome(): HomeSnapshot
    suspend fun getSeniorHome(): SeniorHomeSnapshot
    suspend fun getTodayHospitalSchedules(): List<TodayHospitalSchedule>
    suspend fun getWeather(latitude: Double, longitude: Double): WeatherInfo
    suspend fun getDevice(): DeviceInfo
    suspend fun getButtonOptions(): List<ServerButton>
    suspend fun saveButtons(
        musicApp: String?,
        buttons: List<ServerButton>,
    )
    suspend fun addButton(optionId: Long): ServerButton
    suspend fun updateButtons(buttons: List<Pair<Long, Int>>)
    suspend fun deleteButton(buttonId: Long)
    suspend fun updateFontSize(fontSize: String)
    suspend fun updateSeniorProfile(
        name: String, relation: String, customRelation: String?, birth: String,
        phoneNumber: String, address: String?, detailAddress: String?
    ): SeniorProfileUpdate
}

interface FamilyServerRepository {
    suspend fun hasFamily(): Boolean
    suspend fun join(code: String): FamilyCodeInfo
    suspend fun createCode(): FamilyCodeInfo
    suspend fun getCode(): FamilyCodeInfo
    suspend fun getHome(): ServerFamilyHome
    suspend fun getMembers(): List<ServerFamilyMember>
    suspend fun changePrimaryManager(userId: Long)
    suspend fun deleteMember(userId: Long)
    suspend fun getPhotoAlbums(): List<ServerFamilyPhotoAlbum>
    suspend fun getPhotos(
        uploaderId: Long? = null,
        cursorAt: String? = null,
        cursorId: Long? = null,
        size: Int? = null,
    ): ServerFamilyPhotoPage
    suspend fun uploadPhoto(
        photo: PreparedFamilyPhoto,
        description: String,
        idempotencyKey: String,
    ): ServerFamilyPhoto
    suspend fun getPhoto(photoId: Long): ServerFamilyPhoto
    suspend fun markPhotoViewed(photoId: Long)
    suspend fun deletePhoto(photoId: Long)
}

interface HospitalRepository {
    suspend fun getMonthly(parentId: Long, year: Int, month: Int): List<HospitalAppointment>
    suspend fun getDaily(parentId: Long, date: String): List<HospitalAppointment>
    suspend fun getUpcoming(parentId: Long): List<HospitalUpcomingGroup>
    suspend fun create(parentId: Long, appointment: HospitalAppointment): HospitalAppointment
    suspend fun update(parentId: Long, appointment: HospitalAppointment)
    suspend fun delete(parentId: Long, hospitalId: Long)
}

interface MedicationRepository {
    suspend fun getMedications(parentId: Long): List<MedicationInfo>
    suspend fun create(parentId: Long, medication: MedicationInfo): MedicationInfo
    suspend fun update(parentId: Long, medication: MedicationInfo)
    suspend fun delete(parentId: Long, groupId: String)
    suspend fun getMySchedules(date: String): List<MedicationSchedule>
    suspend fun getParentSchedules(parentId: Long, date: String): List<MedicationSchedule>
    suspend fun getParentMonthlySchedules(
        parentId: Long,
        year: Int,
        month: Int,
    ): MedicationMonthlySchedule
    suspend fun markNearestTaken(): MedicationSchedule
    suspend fun markTaken(medicationLogId: Long): MedicationSchedule
}

interface NotificationRepository {
    suspend fun getNotifications(type: String, cursor: Long? = null, size: Int? = null): NotificationPage
    suspend fun markRead(id: Long)
    suspend fun delete(id: Long)
    suspend fun getHome(): NotificationHome
    suspend fun updateSetting(type: String, enabled: Boolean): NotificationSetting
    suspend fun isParentDeviceOnline(): Boolean
    suspend fun getInactivitySetting(userId: Long): InactivitySetting
    suspend fun getMyInactivitySetting(): InactivitySetting
    suspend fun updateInactivitySetting(userId: Long, thresholdHours: Int): InactivitySetting
}

interface EventRepository {
    suspend fun createSos(latitude: Double, longitude: Double, battery: Int?): SafetyEvent
    suspend fun createRiskLink(url: String, battery: Int?): SafetyEvent
    suspend fun createOutingReturn(phase: String, latitude: Double, longitude: Double, battery: Int): SafetyEvent
    suspend fun createInactivity(latitude: Double, longitude: Double, battery: Int, lastSeenAt: String): SafetyEvent
    suspend fun getDetail(eventId: Long): SafetyEvent
}

interface UserSettingsRepository {
    suspend fun getSettings(): UserAccountSettings
    suspend fun getName(): String
    suspend fun updateName(name: String): String
    suspend fun changePassword(current: String, new: String, confirmation: String): Boolean
    suspend fun getProfileImageUrl(): String?
    suspend fun resetProfileImage(): UserProfileImage
    suspend fun updateProfileImage(photo: PreparedFamilyPhoto): String?
}

interface DeviceRepository {
    /**
     * @return `false` when the server reports that this device was explicitly disconnected.
     */
    suspend fun updateStatus(): Boolean
    suspend fun updateFcmToken(token: String)
    suspend fun disconnect()
    suspend fun getLatestLocation(): DeviceLocation
    suspend fun updateLocation(latitude: Double, longitude: Double)
    suspend fun getHomeLocation(): SeniorHomeLocation
    fun getBatteryLevel(): Int
}
