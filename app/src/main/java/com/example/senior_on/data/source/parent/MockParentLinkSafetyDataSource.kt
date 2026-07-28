package com.example.senior_on.data.source.parent

import com.example.senior_on.domain.model.parent.ParentLinkSafetyResult
import com.example.senior_on.domain.model.parent.ParentLinkSafetyVerdict
import kotlinx.coroutines.delay

class MockParentLinkSafetyDataSource : ParentLinkSafetyDataSource {
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
