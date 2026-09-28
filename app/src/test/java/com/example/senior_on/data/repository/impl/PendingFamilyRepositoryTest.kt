package com.example.senior_on.data.repository.impl

import com.example.senior_on.domain.model.auth.*
import com.example.senior_on.domain.model.server.FamilyCodeInfo
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import java.lang.reflect.Proxy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class PendingFamilyRepositoryTest {
    private var account = 1L
    private var creations = 0
    private var families = emptyList<OnboardingFamilyStatus>()
    private val saved = mutableMapOf<Long, FamilyCodeInfo>()
    private val code = FamilyCodeInfo(10L, "ABCD-1234")
    private val delegate = Proxy.newProxyInstance(
        FamilyServerRepository::class.java.classLoader,
        arrayOf(FamilyServerRepository::class.java),
    ) { _, method, _ ->
        check(method.name == "createCode")
        creations++
        code
    } as FamilyServerRepository
    private val repository = PendingFamilyRepository(
        delegate, { account },
        { OnboardingStatus(false, CareManagerType.None, null, false, false, false, families = families) },
        { saved[it] }, { id, value -> if (value == null) saved.remove(id) else saved[id] = value; Unit },
    )
    private fun family(senior: Long? = null, role: CareManagerType = CareManagerType.Primary) =
        OnboardingFamilyStatus(10L, role, null, senior, null, false, false)

    @Test fun viewingDoesNotCreateFamily() = runTest {
        assertNull(repository.getPendingCode())
        assertEquals(0, creations)
    }

    @Test fun createdCodeIsSavedAndReusedAcrossEntryPoints() = runTest {
        assertEquals(code, repository.createCode())
        families = listOf(family())
        assertEquals(code, saved[account])
        assertEquals(code, repository.getPendingCode())
        assertEquals(code, repository.createCode())
        assertEquals(1, creations)
    }

    @Test fun missingCachedCodeDoesNotCreateDuplicateFamily() = runTest {
        families = listOf(family())
        assertTrue(runCatching { repository.getPendingCode() }.isFailure)
        assertTrue(runCatching { repository.createCode() }.isFailure)
        assertEquals(0, creations)
    }

    @Test fun registeredFamilyCodeIsNotOfferedForAnotherSenior() = runTest {
        saved[account] = code
        families = listOf(family(senior = 3L))
        assertNull(repository.getPendingCode())
        assertNull(saved[account])
        assertEquals(0, creations)
    }

    @Test fun secondaryManagerDoesNotSeePendingCode() = runTest {
        saved[account] = code
        families = listOf(family(role = CareManagerType.Sub))
        assertNull(repository.getPendingCode())
    }

    @Test fun otherAccountCannotReuseCachedCode() = runTest {
        saved[1L] = code
        account = 2L
        families = listOf(family())
        assertTrue(runCatching { repository.getPendingCode() }.isFailure)
        assertEquals(code, saved[1L])
        assertEquals(0, creations)
    }
}
