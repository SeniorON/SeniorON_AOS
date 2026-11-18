package com.example.senior_on.ui.child.display

import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

/** The transparent cutout follows the actual header even after a resize or font change. */
@Composable
internal fun SeniorAddTutorial(anchor: Rect, onAdd: () -> Unit, onSkip: () -> Unit) {
    Dialog(onDismissRequest = onSkip, properties = DialogProperties(
        usePlatformDefaultWidth = false, dismissOnClickOutside = false,
    )) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect { window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND) }
        var origin by remember { mutableStateOf<Offset?>(null) }
        Box(Modifier.fillMaxSize().onGloballyPositioned { origin = it.positionOnScreen() }) {
            val local = origin?.let { anchor.translate(-it) } ?: return@Box
            val density = LocalDensity.current
            val target = local.inflate(with(density) { 8.dp.toPx() })
            Canvas(Modifier.matchParentSize()) {
                val mask = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addRect(Rect(Offset.Zero, size))
                    addRoundRect(RoundRect(target, androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())))
                }
                drawPath(mask, SeniorOnColors.Black.copy(alpha = 0.76f))
            }
            with(density) {
                Box(Modifier.absoluteOffset(target.left.toDp(), target.top.toDp())
                    .size(target.width.toDp(), target.height.toDp())
                    .semantics { contentDescription = "시니어 추가" }
                    .clickable(role = Role.Button, onClick = onAdd))
                Column(
                    Modifier.fillMaxWidth().padding(top = target.bottom.toDp().coerceAtLeast(0.dp) + 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("시니어를 연결하려면 여기를 눌러 주세요.",
                        color = SeniorOnColors.White, style = SeniorOnTextStyles.BodyMSemiBold)
                    TextButton(onClick = onSkip) {
                        Text("나중에 하기", color = SeniorOnColors.White)
                    }
                }
            }
        }
    }
}
