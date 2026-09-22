package com.example.senior_on.ui.child.display.viewmodel

import com.example.senior_on.domain.model.family.PreparedFamilyPhoto
import com.example.senior_on.domain.model.parent.CaregiverRelationship
import com.example.senior_on.domain.model.parent.SeniorRelationType
import com.example.senior_on.domain.model.senior.ManagedSenior
import com.example.senior_on.domain.model.senior.SeniorInfo
import com.example.senior_on.domain.model.senior.SeniorRegistration
import com.example.senior_on.domain.model.senior.SeniorRelationUpdate
import com.example.senior_on.domain.model.server.FamilyCodeInfo
import com.example.senior_on.domain.model.server.ServerFamilyHome
import com.example.senior_on.domain.model.server.ServerFamilyMember
import com.example.senior_on.domain.model.server.ServerFamilyPhoto
import com.example.senior_on.domain.model.server.ServerFamilyPhotoAlbum
import com.example.senior_on.domain.model.server.ServerFamilyPhotoPage
import com.example.senior_on.domain.repository.senior.SeniorRepository
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SeniorManagementViewModelTest {
    @Test
    fun `initial refresh loads every managed senior`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val seniorRepository = FakeSeniorRepository(
                managedSeniors = listOf(managedSenior(1L), managedSenior(2L)),
            )
            val viewModel = SeniorManagementViewModel(
                seniorRepository = seniorRepository,
                familyRepository = FakeFamilyRepository(),
            )

            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isLoading)
            assertEquals(listOf(1L, 2L), viewModel.uiState.value.managedSeniors.map { it.seniorId })
            assertEquals(1, seniorRepository.managedListRequestCount)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `starting add flow creates a distinct family code`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val familyRepository = FakeFamilyRepository(
                createResult = FamilyCodeInfo(31L, "43TS-6GTE"),
            )
            val viewModel = SeniorManagementViewModel(
                seniorRepository = FakeSeniorRepository(),
                familyRepository = familyRepository,
            )
            advanceUntilIdle()

            viewModel.beginSeniorAddition()
            advanceUntilIdle()

            assertEquals(31L, viewModel.uiState.value.createdFamilyId)
            assertEquals("43TS-6GTE", viewModel.uiState.value.familyCode)
            assertEquals(1, familyRepository.createCodeRequestCount)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `creating senior uses generated family and updates switcher list`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val seniorRepository = FakeSeniorRepository()
            val viewModel = SeniorManagementViewModel(
                seniorRepository = seniorRepository,
                familyRepository = FakeFamilyRepository(),
            )
            advanceUntilIdle()
            viewModel.beginSeniorAddition()
            advanceUntilIdle()
            var completed: ManagedSenior? = null

            viewModel.createSenior(registration()) { completed = it }
            advanceUntilIdle()

            assertEquals(31L, seniorRepository.createdFamilyId)
            assertEquals(7L, completed?.seniorId)
            assertEquals(listOf(7L), viewModel.uiState.value.managedSeniors.map { it.seniorId })
            assertNull(viewModel.uiState.value.createdFamilyId)
            assertNull(viewModel.uiState.value.familyCode)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `a running create rejects duplicate submit`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val gate = CompletableDeferred<Unit>()
            val seniorRepository = FakeSeniorRepository(createGate = gate)
            val viewModel = SeniorManagementViewModel(
                seniorRepository = seniorRepository,
                familyRepository = FakeFamilyRepository(),
            )
            advanceUntilIdle()
            viewModel.beginSeniorAddition()
            advanceUntilIdle()
            var successCount = 0

            viewModel.createSenior(registration()) { successCount += 1 }
            runCurrent()
            assertTrue(viewModel.uiState.value.isSubmitting)
            viewModel.createSenior(registration()) { successCount += 1 }
            gate.complete(Unit)
            advanceUntilIdle()

            assertEquals(1, seniorRepository.createRequestCount)
            assertEquals(1, successCount)
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private class FakeSeniorRepository(
    var managedSeniors: List<ManagedSenior> = emptyList(),
    private val createGate: CompletableDeferred<Unit>? = null,
) : SeniorRepository {
    var managedListRequestCount = 0
    var createRequestCount = 0
    var createdFamilyId: Long? = null

    override suspend fun createSenior(
        familyId: Long,
        accessToken: String?,
        registration: SeniorRegistration,
    ): SeniorInfo {
        createRequestCount += 1
        createdFamilyId = familyId
        createGate?.await()
        return SeniorInfo(
            seniorId = 7L,
            name = registration.name,
            relation = registration.relation,
            customRelation = registration.customRelation,
            birth = registration.birth,
            phoneNumber = registration.phoneNumber,
            address = registration.address,
            detailAddress = registration.detailAddress,
        )
    }

    override suspend fun getManagedSeniors(): List<ManagedSenior> {
        managedListRequestCount += 1
        return managedSeniors
    }

    override suspend fun updateRelation(
        accessToken: String,
        seniorId: Long,
        relationship: CaregiverRelationship,
    ): SeniorRelationUpdate = SeniorRelationUpdate(
        seniorId = seniorId,
        relation = relationship.relation,
        customRelation = relationship.customRelation,
    )
}

private class FakeFamilyRepository(
    private val createResult: FamilyCodeInfo = FamilyCodeInfo(31L, "43TS-6GTE"),
) : FamilyServerRepository {
    var createCodeRequestCount = 0

    override suspend fun createCode(): FamilyCodeInfo {
        createCodeRequestCount += 1
        return createResult
    }

    override suspend fun hasFamily(): Boolean = error("unused")
    override suspend fun join(code: String): FamilyCodeInfo = error("unused")
    override suspend fun getCode(): FamilyCodeInfo = error("unused")
    override suspend fun getHome(): ServerFamilyHome = error("unused")
    override suspend fun getMembers(seniorId: Long?): List<ServerFamilyMember> = error("unused")
    override suspend fun changePrimaryManager(userId: Long) = error("unused")
    override suspend fun deleteMember(userId: Long) = error("unused")
    override suspend fun getPhotoAlbums(seniorId: Long): List<ServerFamilyPhotoAlbum> = error("unused")
    override suspend fun getPhotos(
        uploaderId: Long?,
        cursorAt: String?,
        cursorId: Long?,
        size: Int?,
        seniorId: Long?,
    ): ServerFamilyPhotoPage = error("unused")
    override suspend fun uploadPhoto(
        photo: PreparedFamilyPhoto,
        description: String,
        idempotencyKey: String,
    ): ServerFamilyPhoto = error("unused")
    override suspend fun getPhoto(photoId: Long): ServerFamilyPhoto = error("unused")
    override suspend fun markPhotoViewed(photoId: Long, seniorId: Long) = error("unused")
    override suspend fun deletePhoto(photoId: Long) = error("unused")
}

private fun managedSenior(id: Long): ManagedSenior = ManagedSenior(
    familyId = id + 100L,
    seniorId = id,
    parentUserId = null,
    name = "시니어$id",
    relationship = CaregiverRelationship(SeniorRelationType.MOTHER),
)

private fun registration(): SeniorRegistration = SeniorRegistration(
    name = "박영훈",
    relation = SeniorRelationType.FATHER,
    birth = "1957-08-20",
    phoneNumber = "01012345678",
    address = "서울시 종로구",
    detailAddress = "101호",
)
