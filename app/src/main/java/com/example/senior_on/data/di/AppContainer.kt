package com.example.senior_on.data.di

import android.content.Context
import com.example.senior_on.data.local.FamilyPhotoUploadPreparer
import com.example.senior_on.data.repository.ChatBuddyRepository
import com.example.senior_on.data.repository.DisplayRepository
import com.example.senior_on.data.repository.FamilyRepository
import com.example.senior_on.data.repository.MockChatBuddyRepository
import com.example.senior_on.data.repository.MockDisplayRepository
import com.example.senior_on.data.repository.MockFamilyRepository
import com.example.senior_on.data.repository.MockParentFamilyPhotoRepository
import com.example.senior_on.data.repository.MockParentEmergencyAlertRepository
import com.example.senior_on.data.repository.MockParentMedicationRepository
import com.example.senior_on.data.repository.MockParentLinkSafetyRepository
import com.example.senior_on.data.repository.MockParentScheduleRepository
import com.example.senior_on.data.repository.ParentFamilyPhotoRepository
import com.example.senior_on.data.repository.ParentEmergencyAlertRepository
import com.example.senior_on.data.repository.ParentMedicationRepository
import com.example.senior_on.data.repository.ParentLinkSafetyRepository
import com.example.senior_on.data.repository.ParentScheduleRepository
import com.example.senior_on.data.repository.MockParentInfoFixtures
import com.example.senior_on.data.repository.MockParentInfoRepository
import com.example.senior_on.data.repository.ParentInfoRepository

interface AppContainer {
    val familyRepository: FamilyRepository
    val familyPhotoUploadPreparer: FamilyPhotoUploadPreparer
    val parentScheduleRepository: ParentScheduleRepository
    val chatBuddyRepository: ChatBuddyRepository
    val parentFamilyPhotoRepository: ParentFamilyPhotoRepository
    val parentMedicationRepository: ParentMedicationRepository
    val parentEmergencyAlertRepository: ParentEmergencyAlertRepository
    val parentLinkSafetyRepository: ParentLinkSafetyRepository
    val displayRepository: DisplayRepository
    val parentInfoRepository: ParentInfoRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    override val familyRepository: FamilyRepository = MockFamilyRepository()
    override val familyPhotoUploadPreparer = FamilyPhotoUploadPreparer(context)
    override val displayRepository: DisplayRepository = MockDisplayRepository()
    override val parentInfoRepository: ParentInfoRepository = MockParentInfoRepository(
        initialParentInfo = MockParentInfoFixtures.mother,
    )
    override val parentScheduleRepository: ParentScheduleRepository =
        MockParentScheduleRepository()
    override val chatBuddyRepository: ChatBuddyRepository = MockChatBuddyRepository()
    override val parentFamilyPhotoRepository: ParentFamilyPhotoRepository =
        MockParentFamilyPhotoRepository()
    override val parentMedicationRepository: ParentMedicationRepository =
        MockParentMedicationRepository()
    override val parentEmergencyAlertRepository: ParentEmergencyAlertRepository =
        MockParentEmergencyAlertRepository()
    override val parentLinkSafetyRepository: ParentLinkSafetyRepository =
        MockParentLinkSafetyRepository()
}
