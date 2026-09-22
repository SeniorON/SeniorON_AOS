package com.example.senior_on.data.repository.impl

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SelectedSeniorPersistenceTest {
    @get:Rule val folder = TemporaryFolder(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir)

    @Test fun selectionSurvivesStoreRecreationAndIsAccountScoped() = runBlocking {
        val file = File(folder.root, "selected.preferences_pb")
        val firstJob = SupervisorJob()
        val first = SelectedSeniorRepositoryImpl(PreferenceDataStoreFactory.create(scope = CoroutineScope(firstJob + Dispatchers.IO), produceFile = { file }))
        first.select("caregiver-a", 42)
        first.select("caregiver-b", 77)
        firstJob.cancelAndJoin()
        val secondJob = SupervisorJob()
        try {
            val restored = SelectedSeniorRepositoryImpl(PreferenceDataStoreFactory.create(scope = CoroutineScope(secondJob + Dispatchers.IO), produceFile = { file }))
            assertEquals(42L, restored.observe("caregiver-a").first())
            assertEquals(77L, restored.observe("caregiver-b").first())
            assertNull(restored.observe("caregiver-c").first())
            restored.select("caregiver-a", null)
            assertNull(restored.observe("caregiver-a").first())
            assertEquals(77L, restored.observe("caregiver-b").first())
        } finally { secondJob.cancelAndJoin() }
    }
}
