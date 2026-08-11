package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.dto.NameUpdateRequest
import com.example.senior_on.data.remote.dto.NameUpdateResponse
import com.example.senior_on.data.remote.dto.PasswordChangeRequest
import com.example.senior_on.data.remote.dto.PasswordChangeResponse
import com.example.senior_on.data.remote.dto.ProfileImageResponse
import com.example.senior_on.data.remote.dto.ProfileImageUpdateResponse
import com.example.senior_on.data.remote.dto.UserAccountResponse
import com.example.senior_on.data.source.settings.UserSettingsDataSource
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import org.junit.Assert.assertEquals
import org.junit.Test

class UserSettingsRepositoryImplTest {
    @Test
    fun `settings profile comes from account and profile image APIs`() = runBlocking {
        val source = FakeUserSettingsDataSource()
        val repository = UserSettingsRepositoryImpl(source)

        val settings = repository.getSettings()

        assertEquals("김민지", settings.name)
        assertEquals("CHILD", settings.role)
        assertEquals("caregiver@example.com", settings.email)
        assertEquals("https://example.com/profile.png", settings.profileImageUrl)
        assertEquals(1, source.accountRequestCount)
        assertEquals(1, source.profileImageRequestCount)
    }

    @Test
    fun `current name is read from the account API`() = runBlocking {
        val source = FakeUserSettingsDataSource()
        val repository = UserSettingsRepositoryImpl(source)

        assertEquals("김민지", repository.getName())
        assertEquals(1, source.accountRequestCount)
    }
}

private class FakeUserSettingsDataSource : UserSettingsDataSource {
    var accountRequestCount = 0
    var profileImageRequestCount = 0

    override suspend fun getAccount(): UserAccountResponse {
        accountRequestCount += 1
        return UserAccountResponse(
            name = "김민지",
            role = "CHILD",
            email = "caregiver@example.com",
        )
    }

    override suspend fun getProfileImage(): ProfileImageResponse {
        profileImageRequestCount += 1
        return ProfileImageResponse(
            profileImageUrl = "https://example.com/profile.png",
        )
    }

    override suspend fun updateName(request: NameUpdateRequest): NameUpdateResponse =
        error("Not used in this test")

    override suspend fun changePassword(
        request: PasswordChangeRequest,
    ): PasswordChangeResponse = error("Not used in this test")

    override suspend fun updateProfileImage(
        image: MultipartBody.Part,
    ): ProfileImageUpdateResponse = error("Not used in this test")
}
