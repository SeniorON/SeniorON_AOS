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
    fun `failed post deletion refresh retries reads without repeating delete`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeSeniorRepository(listOf(managedSenior(3L)))
            val vm = SeniorManagementViewModel(repository, FakeFamilyRepository())
            advanceUntilIdle()
            var deletes = 0
            var saved: List<Long>? = null
            var completed = false
            vm.disconnectRelationship(3L, { false }, { saved = it }, {}, { completed = true }, {
                deletes++
                repository.managedSeniors = emptyList()
                repository.failList = true
            })
            advanceUntilIdle()
            assertEquals(1, deletes)
            assertTrue(vm.uiState.value.disconnectRefreshPending)
            assertTrue(vm.uiState.value.disconnectRefreshError!!.contains("연결 해제는 완료"))
            assertNull(saved)
            assertFalse(completed)
            vm.refreshSeniors()
            advanceUntilIdle()
            assertEquals(1, deletes)
            repository.failList = false
            vm.retryDisconnectRefresh()
            vm.retryDisconnectRefresh()
            advanceUntilIdle()
            assertEquals(1, deletes)
            assertEquals(emptyList<Long>(), saved)
            assertTrue(completed)
            assertFalse(vm.uiState.value.disconnectRefreshPending)
            assertNull(vm.uiState.value.disconnectRefreshError)
        } finally { Dispatchers.resetMain() }
    }

    @Test
    fun `selection save retry never repeats deletion or role check`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val vm = SeniorManagementViewModel(FakeSeniorRepository(), FakeFamilyRepository())
            advanceUntilIdle()
            var deletes = 0
            var roles = 0
            var failSave = true
            var completed = false
            val save: suspend (List<Long>) -> Unit = { if (failSave) error("storage failed") }
            vm.disconnectRelationship(3L, { roles++; false }, save, {}, { completed = true }, { deletes++ })
            advanceUntilIdle()
            assertTrue(vm.uiState.value.disconnectRefreshPending)
            failSave = false
            vm.disconnectRelationship(3L, { roles++; true }, save, {}, {}, { deletes++ })
            advanceUntilIdle()
            assertEquals(1, roles)
            assertEquals(1, deletes)
            assertTrue(completed)
            assertFalse(vm.uiState.value.disconnectRefreshPending)
        } finally { Dispatchers.resetMain() }
    }

    @Test
    fun `remaining seniors keep server order and persistence failure prevents navigation`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeSeniorRepository(listOf(managedSenior(3L), managedSenior(2L), managedSenior(1L)))
            val vm = SeniorManagementViewModel(repository, FakeFamilyRepository())
            advanceUntilIdle()
            var saved: List<Long>? = null
            var completed = false
            vm.disconnectRelationship(3L, { false }, { saved = it }, {}, { completed = true }, {
                repository.managedSeniors = listOf(managedSenior(2L), managedSenior(1L))
            })
            advanceUntilIdle()
            assertEquals(listOf(2L, 1L), saved)
            assertTrue(completed)
            completed = false
            vm.disconnectRelationship(2L, { false }, { error("storage failed") }, {}, { completed = true }, {})
            advanceUntilIdle()
            assertFalse(completed)
            assertFalse(vm.uiState.value.isDisconnecting)
        } finally { Dispatchers.resetMain() }
    }

    @Test
    fun `primary caregiver cannot remove relationship or change selection`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeSeniorRepository()
            val vm = SeniorManagementViewModel(repository, FakeFamilyRepository())
            advanceUntilIdle()
            var requests = 0
            var saves = 0
            var completed = false
            vm.disconnectRelationship(3L, { true }, { saves++ }, {}, { completed = true }, { requests++ })
            advanceUntilIdle()
            assertEquals(0, requests)
            assertEquals(0, saves)
            assertFalse(completed)
            assertFalse(vm.uiState.value.isDisconnecting)
        } finally { Dispatchers.resetMain() }
    }

    @Test
    fun `successful removal reloads list and clears selection when no seniors remain`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeSeniorRepository()
            val vm = SeniorManagementViewModel(repository, FakeFamilyRepository())
            advanceUntilIdle()
            var saved: List<Long>? = null
            var completed = false
            vm.disconnectRelationship(3L, { false }, { saved = it }, {}, { completed = true }, {})
            advanceUntilIdle()
            assertEquals(emptyList<Long>(), saved)
            assertEquals(2, repository.managedListRequestCount)
            assertTrue(completed)
        } finally { Dispatchers.resetMain() }
    }

    @Test
    fun `duplicate clicks ignored and failed request leaves selection unchanged`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val vm = SeniorManagementViewModel(FakeSeniorRepository(), FakeFamilyRepository())
            advanceUntilIdle()
            var requests = 0
            var saves = 0
            var completed = false
            val remove: suspend (Long) -> Unit = { requests++; error("failed") }
            repeat(2) {
                vm.disconnectRelationship(3L, { false }, { saves++ }, {}, { completed = true }, remove)
            }
            advanceUntilIdle()
            assertEquals(1, requests)
            assertEquals(0, saves)
            assertFalse(completed)
            assertFalse(vm.uiState.value.isDisconnecting)
        } finally { Dispatchers.resetMain() }
    }

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
    var failList = false
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
        if (failList) error("network failed")
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
    override suspend fun getCode(seniorId: Long): FamilyCodeInfo = error("unused")
    override suspend fun getHome(seniorId: Long): ServerFamilyHome = error("unused")
    override suspend fun getMembers(seniorId: Long): List<ServerFamilyMember> = error("unused")
    override suspend fun changePrimaryManager(userId: Long, seniorId: Long) = error("unused")
    override suspend fun deleteMember(userId: Long, seniorId: Long) = error("unused")
    override suspend fun getPhotoAlbums(seniorId: Long): List<ServerFamilyPhotoAlbum> = error("unused")
    override suspend fun getPhotos(
        uploaderId: Long?,
        cursorAt: String?,
        cursorId: Long?,
        size: Int?,
        seniorId: Long,
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
