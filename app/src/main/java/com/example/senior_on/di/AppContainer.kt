package com.example.senior_on.di

import android.content.Context
import com.example.senior_on.data.local.FamilyPhotoUploadPreparer
import com.example.senior_on.domain.repository.parent.ChatBuddyRepository
import com.example.senior_on.domain.repository.display.DisplayRepository
import com.example.senior_on.domain.repository.family.FamilyRepository
import com.example.senior_on.data.repository.mock.parent.MockChatBuddyRepository
import com.example.senior_on.data.repository.mock.display.MockDisplayRepository
import com.example.senior_on.data.repository.mock.family.MockFamilyRepository
import com.example.senior_on.data.repository.mock.family.MockFamilyFixtures
import com.example.senior_on.data.repository.mock.parent.MockParentFamilyPhotoRepository
import com.example.senior_on.data.repository.mock.parent.MockParentEmergencyAlertRepository
import com.example.senior_on.data.repository.mock.parent.MockParentMedicationRepository
import com.example.senior_on.data.repository.mock.parent.MockParentLinkSafetyRepository
import com.example.senior_on.data.repository.mock.parent.MockParentScheduleRepository
import com.example.senior_on.domain.repository.parent.ParentFamilyPhotoRepository
import com.example.senior_on.domain.repository.parent.ParentEmergencyAlertRepository
import com.example.senior_on.domain.repository.parent.ParentMedicationRepository
import com.example.senior_on.domain.repository.parent.ParentLinkSafetyRepository
import com.example.senior_on.domain.repository.parent.ParentScheduleRepository
import com.example.senior_on.data.repository.mock.parent.MockParentInfoFixtures
import com.example.senior_on.data.repository.mock.parent.MockParentInfoRepository
import com.example.senior_on.domain.repository.parent.ParentInfoRepository

interface AppContainer {
    val familyRepository: FamilyRepository
    fun familyRepositoryFor(userId: String): FamilyRepository
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
    private val primaryFamilyRepository: FamilyRepository = MockFamilyRepository(
        initialOverview = MockFamilyFixtures.primaryCaregiverOverview
    )
    private val assistantFamilyRepository: FamilyRepository = MockFamilyRepository(
        initialOverview = MockFamilyFixtures.assistantCaregiverOverview
    )

    override val familyRepository: FamilyRepository = primaryFamilyRepository

    override fun familyRepositoryFor(userId: String): FamilyRepository {
        return when (userId.trim().lowercase()) {
            "child01" -> assistantFamilyRepository
            else -> primaryFamilyRepository
        }
    }
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
