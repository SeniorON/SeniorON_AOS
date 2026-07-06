package com.example.senior_on.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.senior_on.R

val SeniorOnFontFamily = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semi_bold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold)
)

object SeniorOnTextStyles {
    private fun pretendardStyle(
        fontWeight: FontWeight,
        fontSize: Int,
        lineHeight: Float
    ) = TextStyle(
        fontFamily = SeniorOnFontFamily,
        fontWeight = fontWeight,
        fontSize = fontSize.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = 0.sp
    )

    val HeadingXXL = pretendardStyle(FontWeight.Bold, 32, 44.8f)
    val HeadingXL = pretendardStyle(FontWeight.Bold, 30, 42f)
    val HeadingL = pretendardStyle(FontWeight.Bold, 26, 36.4f)
    val HeadingM = pretendardStyle(FontWeight.Bold, 24, 33.6f)
    val HeadingS = pretendardStyle(FontWeight.Bold, 20, 28f)
    val HeadingXS = pretendardStyle(FontWeight.SemiBold, 20, 28f)
    val OnboardingHeading = HeadingXS

    val BodyLBold = pretendardStyle(FontWeight.Bold, 18, 25.2f)
    val BodyLSemiBold = pretendardStyle(FontWeight.SemiBold, 18, 25.2f)
    val BodyLMedium = pretendardStyle(FontWeight.Medium, 18, 25.2f)
    val BodyLRegular = pretendardStyle(FontWeight.Normal, 18, 25.2f)

    val BodyMBold = pretendardStyle(FontWeight.Bold, 16, 22.4f)
    val BodyMSemiBold = pretendardStyle(FontWeight.SemiBold, 16, 22.4f)
    val BodyMMedium = pretendardStyle(FontWeight.Medium, 16, 22.4f)
    val BodyMRegular = pretendardStyle(FontWeight.Normal, 16, 22.4f)

    val BodySSemiBold = pretendardStyle(FontWeight.SemiBold, 14, 19.6f)
    val BodySMedium = pretendardStyle(FontWeight.Medium, 14, 19.6f)
    val BodySRegular = pretendardStyle(FontWeight.Normal, 14, 19.6f)

    val CaptionMedium = pretendardStyle(FontWeight.Medium, 12, 16.8f)
    val CaptionRegular = pretendardStyle(FontWeight.Normal, 12, 16.8f)

    val ButtonL = pretendardStyle(FontWeight.SemiBold, 20, 28f)
    val ButtonM = pretendardStyle(FontWeight.SemiBold, 16, 22.4f)
    val ButtonS = pretendardStyle(FontWeight.Medium, 14, 19.6f)
}
