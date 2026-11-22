package com.example.senior_on.ui.child.display

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle


private val lime = Color(0xffa3e635)
private val brand = Color(0xff6b8536)
private val pill = RoundedCornerShape(50)
private const val pageMillis = 4000

internal enum class TutorialScene(val title: String, val body: String) {
    Start("시니어 등록 시작","화면탭 상단에서 시니어 등록을 시작할 수 있어요."),
    NoCode("시니어 코드가 없나요?","받은 코드가 없다면 ‘아직 없어요’를 선택해요."),
    RequestCode("다음으로 넘어가요","선택 후 ‘다음’을 누르면 코드를 발급받을 수 있어요."),
    Issued("시니어 코드가 발급됐어요","코드를 복사해 가족에게 공유해요. 시니어 폰을 연결할 때도 이 코드를 사용해요."),
    Profile("시니어 정보 입력","필수 정보를 입력하고 ‘저장 후 시작’을 눌러요."),
    Connect("시니어 폰을 연결해 주세요","미연결 카드를 누르면 시니어 폰 연결 방법을 볼 수 있어요."),
    Share("설치 안내 보내기","카카오톡으로 시니어에게 앱 설치 방법을 보낼 수 있어요."),
    Message("시니어 폰: 링크 받기","카카오톡으로 받은 초대 카드에서 ‘설치하기’를 누르면 앱 설치 페이지로 이동해요."),
    EnterCode("시니어 폰: 코드 입력","시니어 폰에서 보호자가 공유한 시니어 코드를 입력해요."),
    Connected("연결 완료!","연결되면 보호자 앱에서 시니어 화면을 편집하고 건강과 알림을 확인할 수 있어요."),
}

private val scenes = TutorialScene.entries
private enum class Ending { Skipped, Completed }

@Composable
internal fun SeniorAddTutorial(onFinish: () -> Unit) {
    var index by rememberSaveable { mutableIntStateOf(0) }
    var ending by rememberSaveable { mutableStateOf<Ending?>(null) }
    Dialog(
        onDismissRequest = { if (ending == null) ending = Ending.Skipped else onFinish() },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xff20221c)), contentAlignment = Alignment.Center) {
            val scene = scenes[index]
            key(index, ending) {
                val requester = remember { BringIntoViewRequester() }
                var target by remember { mutableStateOf(Rect.Zero) }
                var placed by remember { mutableStateOf(false) }
                val anchor = Modifier.bringIntoViewRequester(requester).onGloballyPositioned {
                    target = it.boundsInRoot()
                    placed = true
                }
                LaunchedEffect(placed) { if (placed && ending == null) requester.bringIntoView() }
                val clock = remember { Animatable(0f) }
                val lifecycle = LocalLifecycleOwner.current.lifecycle
                val duration = LocalAccessibilityManager.current?.calculateRecommendedTimeoutMillis(
                    pageMillis.toLong(), containsText = true, containsControls = true
                ) ?: pageMillis.toLong()
                val next = {
                    if (ending == null && scenes[index] == scene) {
                        if (index == scenes.lastIndex) ending = Ending.Completed else index++
                    }
                }
                val ready = !target.isEmpty
                LaunchedEffect(lifecycle, duration, ready) {
                    if (ending != null) clock.animateTo(1f, tween(250))
                    else if (ready) lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                        // Respect indefinite accessibility timeouts: navigation stays manual.
                        val animationMillis = if (duration >= Int.MAX_VALUE) pageMillis else duration.toInt()
                        clock.animateTo(1f, tween(((1f-clock.value) * animationMillis).toInt().coerceAtLeast(1), easing = LinearEasing))
                        if (duration < Int.MAX_VALUE) next()
                    }
                }
                val density = LocalDensity.current
                var origin by remember { mutableStateOf(Offset.Zero) }
                val rect = target.translate(-origin)
                val cx = with(density) { rect.center.x.toDp() }
                val cy = with(density) { rect.center.y.toDp() }
                val hw = with(density) { rect.width.toDp() }
                val hh = with(density) { rect.height.toDp() }
                val below = cy < maxHeight * .52f
                Box(Modifier.fillMaxSize().clipToBounds().onGloballyPositioned { origin = it.boundsInRoot().topLeft }) {
                    // No accessibility or keyboard navigation into the illustrative content.
                    Box(Modifier.fillMaxSize().clearAndSetSemantics { }
                        .focusProperties { onEnter = { cancelFocusChange() } }.focusGroup()
                        .drawWithContent {
                            drawContent()
                            if (ending != null) {
                                drawRect(Color.Black.copy(alpha=.58f * clock.value))
                            } else {
                                val t = clock.value
                                drawRect(Color(0xff0e1208).copy(alpha=.66f * (t/.175f).coerceAtMost(1f)))
                                if (!rect.isEmpty) {
                                    val bounds = rect.inflate(3.dp.toPx())
                                    val scaleTo = (size.width / bounds.width).coerceIn(1f,1.12f)
                                    val zoom = 1f + (scaleTo - 1f) * ((t-.075f)/.275f).coerceIn(0f,1f)
                                    scale(zoom, rect.center) {
                                        val corner = CornerRadius(12.dp.toPx())
                                        clipPath(Path().apply { addRoundRect(RoundRect(bounds,corner)) }) {
                                            this@drawWithContent.drawContent()
                                        }
                                        drawRoundRect(lime,bounds.topLeft,bounds.size,corner,style=Stroke(2.dp.toPx()))
                                        val pulse = (t*2)%1f
                                        val ring = bounds.inflate(6.dp.toPx()*pulse)
                                        drawRoundRect(lime.copy(alpha=.6f*(1-pulse)),ring.topLeft,ring.size,corner,style=Stroke(2.dp.toPx()))
                                    }
                                }
                            }
                        }) { TutorialSceneContent(scene, anchor) }
                    // This sibling takes hit testing away from every field, scroll container and tab.
                    Spacer(Modifier.matchParentSize().pointerInput(Unit) {
                        awaitPointerEventScope { while (true) awaitPointerEvent().changes.forEach { it.consume() } }
                    })
                    if (ending == null && !rect.isEmpty) {
                        Box(Modifier.offset(cx-hw/2,cy-hh/2).size(hw,hh)
                            .semantics { contentDescription = scene.title }.clickable(onClick=next))
                    }
                }
                if (ending == null) {
                    val gap = hh*.56f + 16.dp
                    val edge = cy
                    val caretLeft = (cx-20.dp).coerceIn(8.dp,(maxWidth-48.dp).coerceAtLeast(8.dp))
                    val space = (if (below) maxHeight-edge-gap else edge-gap).coerceAtLeast(1.dp)
                    Box(Modifier.align(if (below) Alignment.BottomCenter else Alignment.TopCenter)
                        .fillMaxWidth().height(space).padding(horizontal=12.dp),
                        contentAlignment = if (below) Alignment.TopCenter else Alignment.BottomCenter) {
                        Column(Modifier.fillMaxWidth().graphicsLayer {
                            val enter = ((clock.value-.175f)/.175f).coerceIn(0f,1f)
                            alpha = enter
                            translationY = (if (below) -8.dp.toPx() else 8.dp.toPx())*(1-enter)
                        }) {
                        val caret = Modifier.offset(x=caretLeft).size(16.dp,8.dp)
                        if (below) TutorialCaret(caret, true)
                        Surface(Modifier.fillMaxWidth().heightIn(max=(space-8.dp).coerceAtLeast(1.dp)),shape=RoundedCornerShape(18.dp),color=Color.White,contentColor=Color(0xff1d1f1a),shadowElevation=6.dp) {
                            Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
                                Text(scene.title,fontSize=16.5.sp,fontWeight=FontWeight.ExtraBold)
                                Spacer(Modifier.height(5.dp))
                                Text(scene.body,fontSize=13.5.sp,lineHeight=21.sp,color=Color(0xff4a4e45))
                                Spacer(Modifier.height(14.dp))
                                Row(verticalAlignment=Alignment.CenterVertically) {
                                    Text("${index+1} / ${scenes.size}",color=brand,fontSize=12.sp,fontWeight=FontWeight.ExtraBold)
                                    Spacer(Modifier.width(8.dp))
                                    scenes.indices.forEach { i ->
                                        Box(Modifier.padding(end=3.dp).size(if(i==index) 18.dp else 5.dp,5.dp).clip(pill)
                                            .background(Color(0xffdfe3d6))) {
                                            if(i==index) Spacer(Modifier.fillMaxHeight().fillMaxWidth(clock.value).background(Color(0xff7cc21a)))
                                        }
                                    }
                                    Spacer(Modifier.weight(1f))
                                    TutorialArrow(false, index>0) { index-- }
                                    Spacer(Modifier.width(6.dp))
                                    TutorialArrow(true, true, next)
                                }
                            }
                        }
                        if (!below) TutorialCaret(caret, false)
                        }
                    }
                    Text("건너뛰기",color=Color.White,fontSize=13.sp,lineHeight=17.sp,fontWeight=FontWeight.Bold,
                        modifier=Modifier.align(Alignment.TopEnd).padding(top=34.dp,end=12.dp).clip(pill)
                            .background(Color.White.copy(alpha=.16f)).border(1.dp,Color.White.copy(alpha=.35f),pill)
                            .clickable(role=Role.Button){ ending=Ending.Skipped }.padding(horizontal=13.dp,vertical=6.dp))
                } else {
                    Surface(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(14.dp)
                        .graphicsLayer { alpha = clock.value; translationY = 10.dp.toPx()*(1-clock.value) },
                        shape=RoundedCornerShape(18.dp),color=Color.White,contentColor=Color.Black) {
                        Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                            Text(if(ending==Ending.Completed) "🎉" else "👋",fontSize=28.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(if(ending==Ending.Completed) "안내를 모두 마쳤어요" else "안내를 건너뛰었어요",fontSize=17.sp,fontWeight=FontWeight.ExtraBold)
                            Text("화면탭의 ‘시니어 추가’에서 등록을 시작할 수 있어요.",Modifier.padding(top=4.dp,bottom=14.dp),
                                color=Color(0xff6b6f66),fontSize=13.sp,textAlign=TextAlign.Center)
                            Button(onClick={ index=0; ending=null },modifier=Modifier.fillMaxWidth(),
                                shape=RoundedCornerShape(10.dp),colors=ButtonDefaults.buttonColors(containerColor=brand)) { Text("처음부터 다시 보기") }
                            TextButton(onClick=onFinish) { Text("화면탭으로",color=brand) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialCaret(modifier: Modifier, up: Boolean) {
    Canvas(modifier) {
        drawPath(Path().apply {
            val base = if (up) size.height else 0f
            moveTo(0f,base); lineTo(size.width/2,size.height-base); lineTo(size.width,base); close()
        }, Color.White)
    }
}

@Composable
private fun TutorialArrow(forward: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Box(Modifier.size(34.dp).alpha(if(enabled) 1f else .35f).clip(pill)
        .background(if(forward) brand else Color(0xfff1f2ec))
        .clickable(enabled=enabled,role=Role.Button,onClick=onClick)
        .semantics { contentDescription = if(forward) "다음 단계" else "이전 단계" },contentAlignment=Alignment.Center) {
        Canvas(Modifier.size(16.dp)) {
            val a = size.width * (if(forward) 9f else 15f)/24
            val b = size.width * (if(forward) 16f else 8f)/24
            drawPath(Path().apply { moveTo(a,size.height*5/24); lineTo(b,size.height/2); lineTo(a,size.height*19/24) },
                if(forward) Color.White else Color(0xff3d4138),style=Stroke(size.width*2.6f/24,cap=StrokeCap.Round,join=StrokeJoin.Round))
        }
    }
}
