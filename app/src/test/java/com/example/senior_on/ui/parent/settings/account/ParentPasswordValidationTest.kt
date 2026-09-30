package com.example.senior_on.ui.parent.settings.account

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ParentPasswordValidationTest {
    @Test fun reportsEachInvalidCondition() {
        assertEquals("현재 비밀번호를 입력해 주세요.", parentPasswordValidationError("", "", ""))
        assertEquals("새 비밀번호를 입력해 주세요.", parentPasswordValidationError("old", "", ""))
        assertEquals("새 비밀번호 확인을 입력해 주세요.", parentPasswordValidationError("old", "Newpass1", ""))
        assertEquals("새 비밀번호는 영문과 숫자를 포함해 8자 이상 입력해 주세요.",
            parentPasswordValidationError("old", "short", "short"))
        assertEquals("현재 비밀번호와 다른 새 비밀번호를 입력해 주세요.",
            parentPasswordValidationError("Newpass1", "Newpass1", "Newpass1"))
        assertEquals("새 비밀번호와 확인 입력이 일치하지 않아요.",
            parentPasswordValidationError("old", "Newpass1", "Newpass2"))
    }

    @Test fun leavesActualCurrentPasswordVerificationToServer() {
        assertNull(parentPasswordValidationError("unverified", "Newpass1", "Newpass1"))
    }
}
