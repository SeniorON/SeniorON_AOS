package com.example.senior_on.data.source.auth

import com.example.senior_on.data.remote.dto.UserRole

interface SessionDataSource {
    suspend fun getSavedRole(): UserRole?
}
