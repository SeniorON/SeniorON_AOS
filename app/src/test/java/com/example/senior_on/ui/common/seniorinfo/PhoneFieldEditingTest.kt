package com.example.senior_on.ui.common.seniorinfo

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneFieldEditingTest {
    @Test
    fun movingCursorInMiddleRetainsSelection() {
        val value = TextFieldValue("01012345678", TextRange(5, 3))
        assertEquals(value, formatPhoneFieldValue(value))
    }

    @Test
    fun initialNumberWithSeparatorsAndExcessDigitsMapsBothSelectionEnds() {
        val value = formatPhoneFieldValue(TextFieldValue("010-1234-56789", TextRange(14, 5)))
        assertEquals("01012345678", value.text)
        assertEquals(TextRange(11, 4), value.selection)
    }

    @Test
    fun insertingDigitIntoFullNumberPreservesNumberAndCursor() {
        val previous = TextFieldValue("01012345678", TextRange(5))
        val proposed = TextFieldValue("010129345678", TextRange(6))
        assertEquals(previous, formatPhoneFieldValue(proposed, previous))
    }

    @Test
    fun replacingSelectedDigitInFullNumberKeepsSuffix() {
        val previous = TextFieldValue("01012345678", TextRange(5, 6))
        val proposed = TextFieldValue("01012945678", TextRange(6))
        assertEquals(proposed, formatPhoneFieldValue(proposed, previous))
    }

    @Test
    fun displayedHyphensPreserveCursorOffsetsAtEveryLength() {
        val digits = "01012345678"
        for (length in 0..digits.length) {
            val input = digits.take(length)
            val display = PhoneVisualTransformation.filter(AnnotatedString(input))
            assertEquals(formatPhoneNumber(input), display.text.text)
            for (position in 0..length) {
                val visual = display.offsetMapping.originalToTransformed(position)
                assertEquals(position, display.offsetMapping.transformedToOriginal(visual))
            }
        }
    }
}
