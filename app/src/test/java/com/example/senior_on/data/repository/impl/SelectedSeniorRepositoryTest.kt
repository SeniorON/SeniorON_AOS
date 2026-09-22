package com.example.senior_on.data.repository.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class SelectedSeniorRepositoryTest {
    @Test fun selectionIsAccountScopedAndSharedAcrossRepositoryInstances() = runTest {
        val store = object : DataStore<Preferences> {
            override val data = MutableStateFlow(emptyPreferences())
            override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
                transform(data.value).also { data.value = it }
        }
        val first = SelectedSeniorRepositoryImpl(store)
        first.select("caregiver-a", 42)
        first.select("caregiver-b", 77)
        val second = SelectedSeniorRepositoryImpl(store)
        assertEquals(42L, second.observe("caregiver-a").first())
        assertEquals(77L, second.observe("caregiver-b").first())
        assertNull(second.observe("caregiver-c").first())
        second.select("caregiver-a", null)
        assertNull(first.observe("caregiver-a").first())
        assertEquals(77L, first.observe("caregiver-b").first())
    }
}
