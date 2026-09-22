package com.example.senior_on.ui.child.health

import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarSwipeTest {
    @Test fun leftSwipeMovesToNextMonth() {
        assertEquals(1, calendarSwipeMonthDelta(-48f, 48f))
        assertEquals(1, calendarSwipeMonthDelta(-200f, 48f))
    }

    @Test fun rightSwipeMovesToPreviousMonth() {
        assertEquals(-1, calendarSwipeMonthDelta(48f, 48f))
        assertEquals(-1, calendarSwipeMonthDelta(200f, 48f))
    }

    @Test fun shortOrReversedDragDoesNotChangeMonth() {
        assertEquals(0, calendarSwipeMonthDelta(0f, 48f))
        assertEquals(0, calendarSwipeMonthDelta(47f, 48f))
        assertEquals(0, calendarSwipeMonthDelta(-47f, 48f))
    }
}
