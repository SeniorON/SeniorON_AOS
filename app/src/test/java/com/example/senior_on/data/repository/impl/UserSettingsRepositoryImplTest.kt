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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
        assertFalse(settings.isDefaultProfileImage)
        assertEquals(1, source.accountRequestCount)
        assertEquals(1, source.profileImageRequestCount)
    }

    @Test
    fun `default profile image flag comes from profile image API`() = runBlocking {
        val source = FakeUserSettingsDataSource(
            profileImageResponse = ProfileImageResponse(
                profileImageUrl = null,
                isDefaultProfileImage = true,
            )
        )
        val repository = UserSettingsRepositoryImpl(source)

        val settings = repository.getSettings()

        assertEquals(null, settings.profileImageUrl)
        assertTrue(settings.isDefaultProfileImage)
    }

    @Test
    fun `missing default flag falls back to whether profile image URL exists`() = runBlocking {
        val source = FakeUserSettingsDataSource(
            profileImageResponse = ProfileImageResponse(
                profileImageUrl = null,
                isDefaultProfileImage = null,
            )
        )
        val repository = UserSettingsRepositoryImpl(source)

        assertTrue(repository.getSettings().isDefaultProfileImage)
    }

    @Test
    fun `profile image reset result uses server default image response`() = runBlocking {
        val source = FakeUserSettingsDataSource(
            resetProfileImageResponse = ProfileImageResponse(
                profileImageUrl = null,
                isDefaultProfileImage = true,
            )
        )
        val repository = UserSettingsRepositoryImpl(source)

        val resetImage = repository.resetProfileImage()

        assertEquals(null, resetImage.profileImageUrl)
        assertTrue(resetImage.isDefaultProfileImage)
        assertEquals(1, source.profileImageResetRequestCount)
    }

    @Test
    fun `current name is read from the account API`() = runBlocking {
        val source = FakeUserSettingsDataSource()
        val repository = UserSettingsRepositoryImpl(source)

        assertEquals("김민지", repository.getName())
        assertEquals(1, source.accountRequestCount)
    }
}

private class FakeUserSettingsDataSource(
    private val profileImageResponse: ProfileImageResponse = ProfileImageResponse(
        profileImageUrl = "https://example.com/profile.png",
        isDefaultProfileImage = false,
    ),
    private val resetProfileImageResponse: ProfileImageResponse = ProfileImageResponse(
        profileImageUrl = null,
        isDefaultProfileImage = true,
    ),
) : UserSettingsDataSource {
    var accountRequestCount = 0
    var profileImageRequestCount = 0
    var profileImageResetRequestCount = 0

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
        return profileImageResponse
    }

    override suspend fun resetProfileImage(): ProfileImageResponse {
        profileImageResetRequestCount += 1
        return resetProfileImageResponse
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
