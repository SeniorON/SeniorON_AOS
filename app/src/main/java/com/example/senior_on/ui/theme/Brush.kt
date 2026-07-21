package com.example.senior_on.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object SeniorOnBrushes {
    val FamilyPrimaryBorder = Brush.linearGradient(
        colors = listOf(
            SeniorOnColors.Primary600,
            SeniorOnColors.Primary400
        )
    )

    val Gradient01 = Brush.linearGradient(
        colorStops = arrayOf(
            0.1374f to Color(0xFF406916),
            1f to Color(0xFF82AC57)
        ),
        start = Offset.Zero,
        end = Offset.Infinite
    )

    val Gradient02 = Brush.linearGradient(
        colorStops = arrayOf(
            0.1244f to Color(0xFFF5FBED),
            1f to Color(0xFFE1ECC4)
        ),
        start = Offset.Zero,
        end = Offset.Infinite
    )

    val Gradient03 = Brush.linearGradient(
        colorStops = arrayOf(
            0f to Color(0xFF486B79),
            0.5562f to Color(0xFF769B66)
        ),
        start = Offset.Zero,
        end = Offset.Infinite
    )
}
