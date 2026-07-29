package com.example.senior_on.domain.repository.server

import com.example.senior_on.domain.model.family.PreparedFamilyPhoto
import com.example.senior_on.domain.model.server.*

interface HomeServerRepository {
    suspend fun getHome(): HomeSnapshot
    suspend fun getWeather(latitude: Double, longitude: Double): WeatherInfo
    suspend fun getDevice(): DeviceInfo
    suspend fun getButtonOptions(): List<ServerButton>
    suspend fun saveButtons(musicApp: String?, buttons: List<ServerButton>)
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
    suspend fun join(code: String): FamilyCodeInfo
    suspend fun createCode(): FamilyCodeInfo
    suspend fun getCode(): FamilyCodeInfo
    suspend fun getHome(): ServerFamilyHome
    suspend fun getMembers(): List<ServerFamilyMember>
    suspend fun changePrimaryManager(userId: Long)
    suspend fun deleteMember(userId: Long)
    suspend fun getPhotos(uploaderId: Long? = null, cursorAt: String? = null, cursorId: Long? = null, size: Int? = null): List<ServerFamilyPhoto>
    suspend fun uploadPhoto(photo: PreparedFamilyPhoto, description: String): ServerFamilyPhoto
    suspend fun markPhotoViewed(photoId: Long)
    suspend fun deletePhoto(photoId: Long)
}

interface HospitalRepository {
    suspend fun getMonthly(parentId: Long, year: Int, month: Int): List<HospitalAppointment>
    suspend fun getDaily(parentId: Long, date: String): List<HospitalAppointment>
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
    suspend fun markTaken(logId: Long): MedicationSchedule
}

interface NotificationRepository {
    suspend fun getNotifications(type: String, cursor: Long? = null, size: Int? = null): NotificationPage
    suspend fun markRead(id: Long)
    suspend fun delete(id: Long)
    suspend fun getSettings(): List<NotificationSetting>
    suspend fun updateSetting(type: String, enabled: Boolean): NotificationSetting
    suspend fun isParentDeviceOnline(): Boolean
    suspend fun getInactivitySetting(userId: Long): InactivitySetting
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
    suspend fun updateName(name: String): String
    suspend fun changePassword(current: String, new: String, confirmation: String): Boolean
    suspend fun updateProfileImage(photo: PreparedFamilyPhoto): String?
}

interface DeviceRepository {
    suspend fun updateStatus(identifier: String, name: String, batteryLevel: Int)
    suspend fun disconnect()
}
