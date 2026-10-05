package com.example.senior_on.ui.child.family.viewmodel

import com.example.senior_on.domain.model.auth.RemoteRequestException
import com.example.senior_on.domain.model.family.PreparedFamilyPhoto
import com.example.senior_on.domain.model.server.FamilyCodeInfo
import com.example.senior_on.domain.model.server.ServerConnectedSenior
import com.example.senior_on.domain.model.server.ServerFamilyHome
import com.example.senior_on.domain.model.server.ServerFamilyMember
import com.example.senior_on.domain.model.server.ServerFamilyPhoto
import com.example.senior_on.domain.model.server.ServerFamilyPhotoAlbum
import com.example.senior_on.domain.model.server.ServerFamilyPhotoPage
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SeniorConnectionErrorMessageTest {
    @Test
    fun `backend connection error codes map to the matching guidance`() {
        assertEquals(
            "시니어 코드를 확인해 주세요.",
            remoteError("FAMILY400").connectionErrorMessage(),
        )
        assertEquals(
            "현재 가족의 시니어 코드는 연결할 수 없어요.",
            remoteError("PHOTO_GROUP400").connectionErrorMessage(),
        )
        assertEquals(
            "주 담당자만 시니어를 연결할 수 있어요.",
            remoteError("PHOTO_GROUP403").connectionErrorMessage(),
        )
        assertEquals(
            "이미 연결된 시니어예요.",
            remoteError("PHOTO_GROUP409").connectionErrorMessage(),
        )
    }

    @Test
    fun `backend disconnection error codes map to the matching guidance`() {
        assertEquals(
            "주 담당자만 연결을 해제할 수 있어요.",
            remoteError("PHOTO_GROUP403").disconnectionErrorMessage(),
        )
        assertEquals(
            "이미 해제되었거나 찾을 수 없는 연결이에요.",
            remoteError("PHOTO_GROUP404").disconnectionErrorMessage(),
        )
    }

    @Test
    fun `시니어 전환 후 이전 연결 응답을 반영하지 않는다`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val connectStarted = CompletableDeferred<Unit>()
            val releaseConnect = CompletableDeferred<Unit>()
            val repository = SwitchingSeniorRepository(
                connectStarted = connectStarted,
                releaseConnect = releaseConnect,
            )
            val viewModel = SeniorConnectionViewModel(repository)
            var oldSeniorSuccessCalled = false

            viewModel.loadConnections(FIRST_SENIOR_ID)
            advanceUntilIdle()
            viewModel.connect(FIRST_SENIOR_ID, "FIRST-001") {
                oldSeniorSuccessCalled = true
            }
            runCurrent()
            connectStarted.await()

            viewModel.loadConnections(SECOND_SENIOR_ID)
            runCurrent()
            releaseConnect.complete(Unit)
            advanceUntilIdle()

            assertEquals(SECOND_SENIOR_ID, viewModel.uiState.value.seniorId)
            assertEquals(
                listOf(SECOND_CONNECTION),
                viewModel.uiState.value.connectedSeniors,
            )
            assertFalse(oldSeniorSuccessCalled)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `시니어 전환 후 이전 연결 해제 콜백을 실행하지 않는다`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val disconnectStarted = CompletableDeferred<Unit>()
            val releaseDisconnect = CompletableDeferred<Unit>()
            val repository = SwitchingSeniorRepository(
                disconnectStarted = disconnectStarted,
                releaseDisconnect = releaseDisconnect,
            )
            val viewModel = SeniorConnectionViewModel(repository)
            var oldSeniorSuccessCalled = false

            viewModel.loadConnections(FIRST_SENIOR_ID)
            advanceUntilIdle()
            viewModel.disconnect(
                seniorId = FIRST_SENIOR_ID,
                photoGroupId = FIRST_CONNECTION.photoGroupId,
                onSuccess = { oldSeniorSuccessCalled = true },
            )
            runCurrent()
            disconnectStarted.await()

            viewModel.loadConnections(SECOND_SENIOR_ID)
            runCurrent()
            releaseDisconnect.complete(Unit)
            advanceUntilIdle()

            assertEquals(SECOND_SENIOR_ID, viewModel.uiState.value.seniorId)
            assertEquals(
                listOf(SECOND_CONNECTION),
                viewModel.uiState.value.connectedSeniors,
            )
            assertFalse(oldSeniorSuccessCalled)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun remoteError(code: String) = RemoteRequestException(
        status = if (code.endsWith("403")) 403 else 400,
        code = code,
        retryAfterSeconds = null,
        message = "backend error",
        cause = IOException("backend error"),
    )

    companion object {
        const val FIRST_SENIOR_ID = 11L
        const val SECOND_SENIOR_ID = 22L

        val FIRST_CONNECTION = ServerConnectedSenior(
            photoGroupId = 101L,
            seniorId = FIRST_SENIOR_ID,
            name = "시니어 A",
            relationshipLabel = "어머니",
            connectedAt = "2026-10-02T18:00:00",
        )
        val SECOND_CONNECTION = ServerConnectedSenior(
            photoGroupId = 202L,
            seniorId = SECOND_SENIOR_ID,
            name = "시니어 B",
            relationshipLabel = "아버지",
            connectedAt = "2026-10-02T18:00:00",
        )
    }
}

private class SwitchingSeniorRepository(
    private val connectStarted: CompletableDeferred<Unit>? = null,
    private val releaseConnect: CompletableDeferred<Unit>? = null,
    private val disconnectStarted: CompletableDeferred<Unit>? = null,
    private val releaseDisconnect: CompletableDeferred<Unit>? = null,
) : FamilyServerRepository {
    override suspend fun getConnectedSeniors(seniorId: Long) = when (seniorId) {
        11L -> listOf(SeniorConnectionErrorMessageTest.FIRST_CONNECTION)
        22L -> listOf(SeniorConnectionErrorMessageTest.SECOND_CONNECTION)
        else -> emptyList()
    }

    override suspend fun connectPhotoGroup(seniorId: Long, seniorCode: String) {
        if (seniorId == 11L) {
            connectStarted?.complete(Unit)
            withContext(NonCancellable) { releaseConnect?.await() }
        }
    }

    override suspend fun disconnectPhotoGroup(seniorId: Long, photoGroupId: Long) {
        if (seniorId == 11L) {
            disconnectStarted?.complete(Unit)
            withContext(NonCancellable) { releaseDisconnect?.await() }
        }
    }

    override suspend fun hasFamily() = error("Not used")
    override suspend fun join(code: String) = error("Not used")
    override suspend fun createCode() = error("Not used")
    override suspend fun getCode(seniorId: Long): FamilyCodeInfo = error("Not used")
    override suspend fun getHome(seniorId: Long): ServerFamilyHome = error("Not used")
    override suspend fun getMembers(seniorId: Long): List<ServerFamilyMember> = error("Not used")
    override suspend fun changePrimaryManager(userId: Long, seniorId: Long) = error("Not used")
    override suspend fun deleteMember(userId: Long, seniorId: Long) = error("Not used")
    override suspend fun getPhotoAlbums(seniorId: Long): List<ServerFamilyPhotoAlbum> = error("Not used")
    override suspend fun getPhotos(
        uploaderId: Long?,
        cursorAt: String?,
        cursorId: Long?,
        size: Int?,
        seniorId: Long,
    ): ServerFamilyPhotoPage = error("Not used")

    override suspend fun uploadPhoto(
        photo: PreparedFamilyPhoto,
        description: String,
        idempotencyKey: String,
    ): ServerFamilyPhoto = error("Not used")

    override suspend fun getPhoto(photoId: Long): ServerFamilyPhoto = error("Not used")
    override suspend fun markPhotoViewed(photoId: Long, seniorId: Long) = error("Not used")
    override suspend fun deletePhoto(photoId: Long) = error("Not used")
}
