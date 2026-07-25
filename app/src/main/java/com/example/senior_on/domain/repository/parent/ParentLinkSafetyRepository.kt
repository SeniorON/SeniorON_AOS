package com.example.senior_on.domain.repository.parent

import com.example.senior_on.domain.model.parent.ParentLinkSafetyResult

interface ParentLinkSafetyRepository {
    suspend fun inspectLink(url: String): ParentLinkSafetyResult
}
