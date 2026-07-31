package com.example.senior_on.di

import android.content.Context
import com.example.senior_on.data.local.FamilyPhotoUploadPreparer
import com.example.senior_on.data.repository.impl.AccountRecoveryRepositoryImpl
import com.example.senior_on.data.repository.impl.AuthRepositoryImpl
import com.example.senior_on.data.repository.impl.CaregiverRelationshipRepositoryImpl
import com.example.senior_on.data.repository.impl.ChatBuddyRepositoryImpl
import com.example.senior_on.data.repository.impl.DisplayRepositoryImpl
import com.example.senior_on.data.repository.impl.FamilyRepositoryImpl
import com.example.senior_on.data.repository.impl.HospitalSpecialtyRepositoryImpl
import com.example.senior_on.data.repository.impl.ParentFamilyPhotoRepositoryImpl
import com.example.senior_on.data.repository.impl.ParentInfoRepositoryImpl
import com.example.senior_on.data.repository.impl.ParentLinkSafetyRepositoryImpl
import com.example.senior_on.data.repository.impl.ParentMedicationRepositoryImpl
import com.example.senior_on.data.repository.impl.SessionRepositoryImpl
import com.example.senior_on.data.repository.impl.SeniorRepositoryImpl
import com.example.senior_on.data.repository.impl.SocialAuthRepositoryImpl
import com.example.senior_on.data.repository.impl.DeviceRepositoryImpl
import com.example.senior_on.data.repository.impl.DeviceRegistrationRepositoryImpl
import com.example.senior_on.data.repository.impl.EventRepositoryImpl
import com.example.senior_on.data.repository.impl.FamilyServerRepositoryImpl
import com.example.senior_on.data.repository.impl.HomeServerRepositoryImpl
import com.example.senior_on.data.repository.impl.HospitalRepositoryImpl
import com.example.senior_on.data.repository.impl.MedicationRepositoryImpl
import com.example.senior_on.data.repository.impl.NotificationRepositoryImpl
import com.example.senior_on.data.repository.impl.LocationRepositoryImpl
import com.example.senior_on.data.repository.impl.UserSettingsRepositoryImpl
import com.example.senior_on.data.source.auth.AccountRecoveryDataSource
import com.example.senior_on.data.source.auth.AuthDataSource
import com.example.senior_on.data.source.auth.MockAuthDataSource
import com.example.senior_on.data.source.auth.MockSessionDataSource
import com.example.senior_on.data.source.auth.SocialAuthDataSource
import com.example.senior_on.data.source.family.MockFamilyDataSource
import com.example.senior_on.data.source.family.MockFamilyPhotoStore
import com.example.senior_on.data.source.health.MockHospitalSpecialtyDataSource
import com.example.senior_on.data.source.mock.fixtures.MockFamilyFixtures
import com.example.senior_on.data.source.mock.fixtures.MockFamilyPhotoFixtures
import com.example.senior_on.data.source.mock.fixtures.MockSeniorFixtures
import com.example.senior_on.data.source.mock.fixtures.MockUserFixtures
import com.example.senior_on.data.source.parent.MockCaregiverRelationshipDataSource
import com.example.senior_on.data.source.parent.MockChatBuddyDataSource
import com.example.senior_on.data.source.parent.MockParentFamilyPhotoDataSource
import com.example.senior_on.data.source.parent.MockParentInfoDataSource
import com.example.senior_on.data.source.parent.MockParentLinkSafetyDataSource
import com.example.senior_on.data.source.parent.MockParentMedicationDataSource
import com.example.senior_on.data.source.senior.SeniorDataSource
import com.example.senior_on.data.source.device.DeviceDataSource
import com.example.senior_on.data.source.device.AndroidDeviceStatusDataSource
import com.example.senior_on.data.source.device.FcmTokenStore
import com.example.senior_on.data.source.device.FirebaseFcmTokenDataSource
import com.example.senior_on.data.source.device.LocalDeviceIdentifierDataSource
import com.example.senior_on.data.source.event.EventDataSource
import com.example.senior_on.data.source.family.RemoteFamilySource
import com.example.senior_on.data.source.health.HospitalDataSource
import com.example.senior_on.data.source.home.HomeDataSource
import com.example.senior_on.data.source.medication.MedicationDataSource
import com.example.senior_on.data.source.location.AndroidLocationDataSource
import com.example.senior_on.data.source.notification.NotificationDataSource
import com.example.senior_on.data.source.settings.UserSettingsDataSource
import com.example.senior_on.domain.model.auth.AppUserProfile
import com.example.senior_on.domain.model.parent.CaregiverRelationship
import com.example.senior_on.domain.model.parent.SeniorRelationType
import com.example.senior_on.domain.repository.auth.AccountRecoveryRepository
import com.example.senior_on.domain.repository.auth.AuthRepository
import com.example.senior_on.domain.repository.auth.SessionRepository
import com.example.senior_on.domain.repository.auth.SocialAuthRepository
import com.example.senior_on.domain.repository.display.DisplayRepository
import com.example.senior_on.domain.repository.family.FamilyRepository
import com.example.senior_on.domain.repository.health.HospitalSpecialtyRepository
import com.example.senior_on.domain.repository.location.LocationRepository
import com.example.senior_on.domain.repository.parent.CaregiverRelationshipRepository
import com.example.senior_on.domain.repository.parent.ChatBuddyRepository
import com.example.senior_on.domain.repository.parent.ParentFamilyPhotoRepository
import com.example.senior_on.domain.repository.parent.ParentInfoRepository
import com.example.senior_on.domain.repository.parent.ParentLinkSafetyRepository
import com.example.senior_on.domain.repository.parent.ParentMedicationRepository
import com.example.senior_on.domain.repository.senior.SeniorRepository
import com.example.senior_on.domain.repository.device.DeviceRegistrationRepository
import com.example.senior_on.domain.repository.server.*
import com.google.android.gms.location.LocationServices

interface AppContainer {
    val authRepository: AuthRepository
    val accountRecoveryRepository: AccountRecoveryRepository
    val socialAuthRepository: SocialAuthRepository
    val seniorRepository: SeniorRepository
    val sessionRepository: SessionRepository
    val deviceRegistrationRepository: DeviceRegistrationRepository
    val homeServerRepository: HomeServerRepository
    val familyServerRepository: FamilyServerRepository
    val hospitalRepository: HospitalRepository
    val medicationRepository: MedicationRepository
    val notificationRepository: NotificationRepository
    val eventRepository: EventRepository
    val userSettingsRepository: UserSettingsRepository
    val deviceRepository: DeviceRepository
    val locationRepository: LocationRepository
    fun userProfileFor(userId: String): AppUserProfile
    val familyRepository: FamilyRepository
    fun familyRepositoryFor(userId: String): FamilyRepository
    val familyPhotoUploadPreparer: FamilyPhotoUploadPreparer
    val chatBuddyRepository: ChatBuddyRepository
    val parentFamilyPhotoRepository: ParentFamilyPhotoRepository
    val parentMedicationRepository: ParentMedicationRepository
    val parentLinkSafetyRepository: ParentLinkSafetyRepository
    val displayRepository: DisplayRepository
    val hospitalSpecialtyRepository: HospitalSpecialtyRepository
    val parentInfoRepository: ParentInfoRepository
    fun caregiverRelationshipRepositoryFor(
        userId: String,
    ): CaregiverRelationshipRepository
}

class DefaultAppContainer(
    context: Context,
    authDataSource: AuthDataSource = MockAuthDataSource(),
    accountRecoveryDataSource: AccountRecoveryDataSource,
    socialAuthDataSource: SocialAuthDataSource,
    seniorDataSource: SeniorDataSource,
    homeDataSource: HomeDataSource,
    remoteFamilySource: RemoteFamilySource,
    hospitalDataSource: HospitalDataSource,
    medicationDataSource: MedicationDataSource,
    notificationDataSource: NotificationDataSource,
    eventDataSource: EventDataSource,
    userSettingsDataSource: UserSettingsDataSource,
    deviceDataSource: DeviceDataSource
) : AppContainer {
    private val deviceIdentifierDataSource = LocalDeviceIdentifierDataSource(context)

    override val authRepository: AuthRepository = AuthRepositoryImpl(authDataSource)
    override val accountRecoveryRepository: AccountRecoveryRepository =
        AccountRecoveryRepositoryImpl(accountRecoveryDataSource)
    override val socialAuthRepository: SocialAuthRepository =
        SocialAuthRepositoryImpl(socialAuthDataSource)
    override val seniorRepository: SeniorRepository =
        SeniorRepositoryImpl(seniorDataSource)
    override val sessionRepository: SessionRepository =
        SessionRepositoryImpl(MockSessionDataSource())
    override val deviceRegistrationRepository: DeviceRegistrationRepository =
        DeviceRegistrationRepositoryImpl(
            fcmTokenDataSource = FirebaseFcmTokenDataSource(FcmTokenStore(context)),
            deviceIdentifierDataSource = deviceIdentifierDataSource
        )
    override val homeServerRepository = HomeServerRepositoryImpl(homeDataSource)
    override val familyServerRepository = FamilyServerRepositoryImpl(remoteFamilySource)
    override val hospitalRepository = HospitalRepositoryImpl(hospitalDataSource)
    override val medicationRepository = MedicationRepositoryImpl(medicationDataSource)
    override val notificationRepository = NotificationRepositoryImpl(notificationDataSource)
    override val eventRepository = EventRepositoryImpl(eventDataSource)
    override val userSettingsRepository = UserSettingsRepositoryImpl(userSettingsDataSource)
    override val deviceRepository = DeviceRepositoryImpl(
        source = deviceDataSource,
        identifierSource = deviceIdentifierDataSource,
        localStatusSource = AndroidDeviceStatusDataSource(context),
    )

    override val locationRepository: LocationRepository = LocationRepositoryImpl(
        AndroidLocationDataSource(
            context = context.applicationContext,
            locationClient = LocationServices.getFusedLocationProviderClient(
               context.applicationContext,
           ),
       ),
    )

    private val familyPhotoStore = MockFamilyPhotoStore(
        initialPhotos = MockFamilyPhotoFixtures.initialPhotos()
    )
    private val primaryFamilyRepository: FamilyRepository = FamilyRepositoryImpl(
        MockFamilyDataSource(
            initialOverview = MockFamilyFixtures.primaryCaregiverOverview,
            photoStore = familyPhotoStore,
        )
    )
    private val assistantFamilyRepository: FamilyRepository = FamilyRepositoryImpl(
        MockFamilyDataSource(
            initialOverview = MockFamilyFixtures.assistantCaregiverOverview,
            photoStore = familyPhotoStore,
        )
    )
    private val primaryCaregiverRelationshipRepository:
        CaregiverRelationshipRepository = CaregiverRelationshipRepositoryImpl(
            MockCaregiverRelationshipDataSource(
                activeSeniorId = MockSeniorFixtures.SENIOR_ID,
                initialRelationship = CaregiverRelationship(
                    relation = SeniorRelationType.MOTHER,
                )
            )
        )
    private val assistantCaregiverRelationshipRepository:
        CaregiverRelationshipRepository = CaregiverRelationshipRepositoryImpl(
            MockCaregiverRelationshipDataSource(
                activeSeniorId = MockSeniorFixtures.SENIOR_ID,
            )
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
    override val displayRepository: DisplayRepository =
        DisplayRepositoryImpl(
            homeDataSource = homeDataSource,
            deviceDataSource = deviceDataSource,
            familyDataSource = remoteFamilySource,
        )
    override val hospitalSpecialtyRepository: HospitalSpecialtyRepository =
        HospitalSpecialtyRepositoryImpl(MockHospitalSpecialtyDataSource)
    override val parentInfoRepository: ParentInfoRepository = ParentInfoRepositoryImpl(
        MockParentInfoDataSource(initialParentInfo = MockSeniorFixtures.mother)
    )
    override val chatBuddyRepository: ChatBuddyRepository =
        ChatBuddyRepositoryImpl(MockChatBuddyDataSource())
    override val parentFamilyPhotoRepository: ParentFamilyPhotoRepository =
        ParentFamilyPhotoRepositoryImpl(
            MockParentFamilyPhotoDataSource(photoStore = familyPhotoStore)
        )
    override val parentMedicationRepository: ParentMedicationRepository =
        ParentMedicationRepositoryImpl(MockParentMedicationDataSource())
    override val parentLinkSafetyRepository: ParentLinkSafetyRepository =
        ParentLinkSafetyRepositoryImpl(MockParentLinkSafetyDataSource())
}
