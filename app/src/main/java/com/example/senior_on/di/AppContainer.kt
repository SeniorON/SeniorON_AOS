package com.example.senior_on.di

import android.content.Context
import com.example.senior_on.data.local.FamilyPhotoUploadPreparer
import com.example.senior_on.domain.repository.parent.ChatBuddyRepository
import com.example.senior_on.domain.repository.display.DisplayRepository
import com.example.senior_on.domain.repository.family.FamilyRepository
import com.example.senior_on.data.repository.mock.parent.MockChatBuddyRepository
import com.example.senior_on.data.repository.mock.parent.MockCaregiverRelationshipRepository
import com.example.senior_on.data.repository.mock.display.MockDisplayRepository
import com.example.senior_on.data.repository.mock.family.MockFamilyPhotoStore
import com.example.senior_on.data.repository.mock.family.MockFamilyRepository
import com.example.senior_on.data.repository.mock.fixtures.MockFamilyFixtures
import com.example.senior_on.data.repository.mock.fixtures.MockFamilyPhotoFixtures
import com.example.senior_on.data.repository.mock.fixtures.MockSeniorFixtures
import com.example.senior_on.data.repository.mock.fixtures.MockUserFixtures
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
import com.example.senior_on.data.repository.mock.parent.MockParentInfoRepository
import com.example.senior_on.domain.repository.parent.ParentInfoRepository
import com.example.senior_on.domain.repository.parent.CaregiverRelationshipRepository
import com.example.senior_on.domain.model.parent.CaregiverRelationship
import com.example.senior_on.domain.model.parent.SeniorRelationType
import com.example.senior_on.domain.model.auth.AppUserProfile

interface AppContainer {
    fun userProfileFor(userId: String): AppUserProfile
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
    fun caregiverRelationshipRepositoryFor(
        userId: String,
    ): CaregiverRelationshipRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val familyPhotoStore = MockFamilyPhotoStore(
        initialPhotos = MockFamilyPhotoFixtures.initialPhotos()
    )
    private val primaryFamilyRepository: FamilyRepository = MockFamilyRepository(
        initialOverview = MockFamilyFixtures.primaryCaregiverOverview,
        photoStore = familyPhotoStore,
    )
    private val assistantFamilyRepository: FamilyRepository = MockFamilyRepository(
        initialOverview = MockFamilyFixtures.assistantCaregiverOverview,
        photoStore = familyPhotoStore,
    )
    private val primaryCaregiverRelationshipRepository:
        CaregiverRelationshipRepository = MockCaregiverRelationshipRepository(
            activeSeniorId = MockSeniorFixtures.SENIOR_ID,
            initialRelationship = CaregiverRelationship(
                relation = SeniorRelationType.MOTHER,
            )
        )
    private val assistantCaregiverRelationshipRepository:
        CaregiverRelationshipRepository = MockCaregiverRelationshipRepository(
            activeSeniorId = MockSeniorFixtures.SENIOR_ID,
        )

    override val familyRepository: FamilyRepository = primaryFamilyRepository

    override fun userProfileFor(userId: String): AppUserProfile {
        return MockUserFixtures.profileFor(userId)
            ?: MockUserFixtures.primaryCaregiver
    }

    override fun familyRepositoryFor(userId: String): FamilyRepository {
        return when (userId.trim().lowercase()) {
            MockUserFixtures.ASSISTANT_CAREGIVER_USER_ID ->
                assistantFamilyRepository
            else -> primaryFamilyRepository
        }
    }

    override fun caregiverRelationshipRepositoryFor(
        userId: String,
    ): CaregiverRelationshipRepository {
        return when (userId.trim().lowercase()) {
            MockUserFixtures.ASSISTANT_CAREGIVER_USER_ID ->
                assistantCaregiverRelationshipRepository
            else -> primaryCaregiverRelationshipRepository
        }
    }
    override val familyPhotoUploadPreparer = FamilyPhotoUploadPreparer(context)
    override val displayRepository: DisplayRepository = MockDisplayRepository()
    override val parentInfoRepository: ParentInfoRepository = MockParentInfoRepository(
        initialParentInfo = MockSeniorFixtures.mother,
    )
    override val parentScheduleRepository: ParentScheduleRepository =
        MockParentScheduleRepository()
    override val chatBuddyRepository: ChatBuddyRepository = MockChatBuddyRepository()
    override val parentFamilyPhotoRepository: ParentFamilyPhotoRepository =
        MockParentFamilyPhotoRepository(photoStore = familyPhotoStore)
    override val parentMedicationRepository: ParentMedicationRepository =
        MockParentMedicationRepository()
    override val parentEmergencyAlertRepository: ParentEmergencyAlertRepository =
        MockParentEmergencyAlertRepository()
    override val parentLinkSafetyRepository: ParentLinkSafetyRepository =
        MockParentLinkSafetyRepository()
}
