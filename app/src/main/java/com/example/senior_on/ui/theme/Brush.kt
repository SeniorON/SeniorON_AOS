package com.example.senior_on.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object SeniorOnBrushes {
    val Gradient01 = Brush.linearGradient(
        colorStops = arrayOf(
            0.1944f to Color(0xFF648B3B),
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
}
