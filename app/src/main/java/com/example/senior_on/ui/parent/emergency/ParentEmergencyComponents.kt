package com.example.senior_on.ui.parent.emergency

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SeniorOnColors

@Composable
internal fun ParentEmergencyIcon(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(80.dp)
            .drawWithCache {
                val borderWidth = 0.8.dp.toPx()
                val gradientBorder = Brush.linearGradient(
                    colors = listOf(
                        Color.White,
                        Color.Transparent
                    ),
                    start = Offset(size.width, 0f),
                    end = Offset(0f, size.height)
                )

                onDrawBehind {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.20f)
                    )
                    drawCircle(
                        brush = gradientBorder,
                        radius = (size.minDimension - borderWidth) / 2f,
                        style = Stroke(width = borderWidth)
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_parent_emergency),
            contentDescription = null,
            modifier = Modifier.size(
                width = 51.dp,
                height = 40.dp
            ),
            tint = SeniorOnColors.White
        )
    }
}
