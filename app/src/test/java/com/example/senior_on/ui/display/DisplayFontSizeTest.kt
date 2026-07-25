package com.example.senior_on.ui.display

import com.example.senior_on.domain.model.SeniorFontSize
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayFontSizeTest {
    @Test
    fun seniorFontSizesUseHeadingTokens() {
        assertEquals(
            SeniorOnTextStyles.HeadingXXXL,
            SeniorFontSize.Large.seniorHomeButtonTextStyle,
        )
        assertEquals(
            SeniorOnTextStyles.HeadingXXL,
            SeniorFontSize.Normal.seniorHomeButtonTextStyle,
        )
        assertEquals(
            SeniorOnTextStyles.HeadingXL,
            SeniorFontSize.Small.seniorHomeButtonTextStyle,
        )
    }
}
