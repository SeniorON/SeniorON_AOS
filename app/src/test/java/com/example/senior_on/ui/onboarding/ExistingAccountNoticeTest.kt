package com.example.senior_on.ui.onboarding

import com.example.senior_on.ui.onboarding.viewmodel.ExistingAccountNotice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExistingAccountNoticeTest {
    @Test fun reportsOnlyTheLoginMethodConfirmedByTheServer() {
        assertEquals(ExistingAccountNotice.General, ExistingAccountNotice.fromCode("USER409_6"))
        for (code in listOf("USER409_4", "USER409_5")) {
            assertEquals(ExistingAccountNotice.Social, ExistingAccountNotice.fromCode(code))
        }
        assertEquals(ExistingAccountNotice.Unspecified, ExistingAccountNotice.fromCode("USER409"))
    }

    @Test fun verificationAndLoginIdConflictsAreNotDuplicateAccountNotices() {
        for (code in listOf("USER409_2", "USER409_1", "EMAIL429_1", null)) {
            assertNull(ExistingAccountNotice.fromCode(code))
        }
    }
}
