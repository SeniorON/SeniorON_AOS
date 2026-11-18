package com.example.senior_on.onboarding

import com.example.senior_on.data.remote.dto.SignupResponse
import com.example.senior_on.data.remote.dto.SocialSignupResponse
import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.data.repository.impl.AuthRepositoryImpl
import com.example.senior_on.data.repository.impl.SocialAuthRepositoryImpl
import com.example.senior_on.data.source.auth.AuthDataSource
import com.example.senior_on.data.source.auth.SocialAuthDataSource
import com.example.senior_on.domain.model.auth.*
import java.lang.reflect.Proxy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ChildSignupTutorialTest {
    private inline fun <reified T> signupSource(crossinline reply: () -> Any): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, _ ->
            check(method.name == "signup")
            reply()
        } as T

    @Test fun ordinarySignupMarksOnlyTheReturnedChildId() = runTest {
        val marked = mutableListOf<Long>()
        var response = SignupResponse(81, "Child", "different-login", UserRole.CHILD)
        val repo = AuthRepositoryImpl(signupSource<AuthDataSource> { response }) { marked.add(it) }
        val credentials = SignupCredentials("123", "email", "pw", "pw", "Child", "2000-01-01",
            AppUserMode.Child, true, true, true, false)
        repo.signup(credentials)
        response = response.copy(role = UserRole.PARENT)
        repo.signup(credentials.copy(mode = AppUserMode.Senior))
        assertEquals(listOf(81L), marked)
    }

    @Test fun completedSocialSignupNeedsARealChildAccountId() = runTest {
        val marked = mutableListOf<Long>()
        var response = SocialSignupResponse(null, null, 97, "Child", null, false)
        val repo = SocialAuthRepositoryImpl(signupSource<SocialAuthDataSource> { response }) { marked.add(it) }
        val credentials = SocialSignupCredentials(SocialProvider.Google, "token", "Child", "2000-01-01",
            AppUserMode.Child, true, true, true, false, "fcm", "device")
        repo.signup(credentials)
        response = response.copy(usersId = null)
        repo.signup(credentials)
        response = response.copy(usersId = 98, role = UserRole.PARENT)
        repo.signup(credentials.copy(mode = AppUserMode.Senior))
        assertEquals(listOf(97L), marked)
    }
}
