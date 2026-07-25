package com.example.senior_on.data.repository

import com.example.senior_on.domain.model.ParentLinkSafetyResult
import com.example.senior_on.domain.model.ParentLinkSafetyVerdict
import kotlinx.coroutines.delay

class MockParentLinkSafetyRepository : ParentLinkSafetyRepository {
    override suspend fun inspectLink(url: String): ParentLinkSafetyResult {
        delay(3_000)
        return ParentLinkSafetyResult(
            url = url,
            verdict = if ("fake-bank" in url) {
                ParentLinkSafetyVerdict.Dangerous
            } else {
                ParentLinkSafetyVerdict.Safe
            }
        )
    }
}
