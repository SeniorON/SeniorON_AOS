package com.example.senior_on.data.repository

import com.example.senior_on.domain.model.ParentLinkSafetyResult

interface ParentLinkSafetyRepository {
    suspend fun inspectLink(url: String): ParentLinkSafetyResult
}
