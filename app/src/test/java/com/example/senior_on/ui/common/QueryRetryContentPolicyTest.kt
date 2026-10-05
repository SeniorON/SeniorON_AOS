package com.example.senior_on.ui.common

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QueryRetryContentPolicyTest {
    @Test fun failedRefreshHidesCachedContent() {
        assertTrue(shouldReplaceQueryContent(true, "조회 실패", false))
    }

    @Test fun retryDoesNotExposeCachedContentBeforeSuccess() {
        assertTrue(shouldReplaceQueryContent(true, null, true))
        assertFalse(shouldReplaceQueryContent(true, null, false))
    }

    @Test fun initialLoadIsNotEmptySuccess() {
        assertTrue(shouldReplaceQueryContent(false, null, false))
    }
}
