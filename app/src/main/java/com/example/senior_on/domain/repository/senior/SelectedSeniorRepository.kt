package com.example.senior_on.domain.repository.senior

import kotlinx.coroutines.flow.Flow

interface SelectedSeniorRepository {
    fun observe(accountId: String): Flow<Long?>
    suspend fun select(accountId: String, seniorId: Long?)
}
