package com.example.senior_on.ui.onboarding

import com.example.senior_on.ui.common.account.cappedAccountKeyboardLift
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountKeyboardLiftTest {
    @Test fun followsKeyboardWhenThereIsEnoughRoom() {
        assertEquals(250, cappedAccountKeyboardLift(250, 900, 100, 100, 300))
    }

    @Test fun stopsBelowInputsOnSmallScreen() {
        assertEquals(100, cappedAccountKeyboardLift(300, 600, 100, 100, 300))
    }

    @Test fun neverMovesUpWhenContentAlreadyFillsScreen() {
        assertEquals(0, cappedAccountKeyboardLift(300, 450, 100, 100, 300))
    }

    @Test fun returnsToBottomWhenKeyboardCloses() {
        assertEquals(0, cappedAccountKeyboardLift(0, 900, 100, 100, 300))
    }
}
