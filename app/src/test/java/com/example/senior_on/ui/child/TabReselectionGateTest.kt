package com.example.senior_on.ui.child

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TabReselectionGateTest {
    @Test fun entryDoesNotReplayPreviousTabsTap() {
        val gate = TabReselectionGate(4)
        assertFalse(gate.consume(4))
        assertTrue(gate.consume(5))
        assertFalse(gate.consume(5))
    }

    @Test fun ignoredBusyOrMainTapDoesNotRunLaterOnRecomposition() {
        val gate = TabReselectionGate(0)
        assertTrue(gate.consume(1)) // Handler ignores the tap while busy or already at main.
        assertFalse(gate.consume(1))
        assertTrue(gate.consume(2))
    }

    @Test fun recreatedRouteDoesNotReplayConsumedTap() {
        assertTrue(TabReselectionGate(0).consume(1))
        assertFalse(TabReselectionGate(1).consume(1))
    }
}
