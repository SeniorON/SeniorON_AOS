package com.example.senior_on.ui.child.display

import com.example.senior_on.domain.model.display.SeniorFontSize
import com.example.senior_on.ui.parent.home.homeButtonTextStyle
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayFontSizeTest {
    @Test
    fun seniorHomeFontSizesUseHeadingTokens() {
        assertEquals(
            SeniorOnTextStyles.HeadingXXXL,
            SeniorFontSize.Large.homeButtonTextStyle,
        )
        assertEquals(
            SeniorOnTextStyles.HeadingXL,
            SeniorFontSize.Normal.homeButtonTextStyle,
        )
        assertEquals(
            SeniorOnTextStyles.HeadingL,
            SeniorFontSize.Small.homeButtonTextStyle,
        )
    }
}
