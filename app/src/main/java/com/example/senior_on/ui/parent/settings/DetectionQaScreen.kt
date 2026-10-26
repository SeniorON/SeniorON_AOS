package com.example.senior_on.ui.parent.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.senior_on.device.*
import com.example.senior_on.location.tracking.*
import com.example.senior_on.ui.parent.component.ParentSettingsScaffold
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import com.example.senior_on.data.source.auth.PersistedSessionStore
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
internal fun DetectionQaScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    if (!DetectionQa.enabled) return
    val context = LocalContext.current.applicationContext
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val store = remember(context) { ParentInactivityStateStore(context) }
    val diagnostic by DetectionQaStatus.state.collectAsStateWithLifecycle()
    var queueError by remember { mutableStateOf<String?>(null) }
    val queueFlow = remember(context) {
        InactivityOutbox(context).observe().catch {
            queueError = "대기 이벤트를 읽지 못했어요. 화면을 다시 열어 주세요."
            emit(null)
        }
    }
    val storedEvent by queueFlow.collectAsStateWithLifecycle(initialValue = null)
    val session = remember(context) { PersistedSessionStore(context) }.getSession()
    val owner = session?.let { "${it.role}:${it.usersId}:${it.userId}" }
    val event = storedEvent?.takeIf { it.account == owner }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var activity by remember { mutableStateOf(store.snapshot()) }
    var online by remember { mutableStateOf(context.isOutingNetworkAvailable()) }
    var history by remember { mutableStateOf(emptyList<QaHistoryEntry>()) }
    val historyScope = rememberCoroutineScope()
    var exportStatus by remember { mutableStateOf("") }
    val exportHistory = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri ->
        if (uri != null) historyScope.launch {
            exportStatus = "전체 이력 저장 중"
            try {
                withContext(Dispatchers.IO) { DetectionQaHistory.export(context, uri) }
                exportStatus = "전체 이력 파일 저장 완료"
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                exportStatus = "파일 저장에 실패했어요. 다시 시도해 주세요."
            }
        }
    }
    var distancePreviewEnabled by remember { mutableStateOf(false) }
    var previewDistance by remember { mutableStateOf<Float?>(null) }
    var previewAt by remember { mutableStateOf<Long?>(null) }
    var previewStatus by remember { mutableStateOf("꺼짐") }

    // Display-only sampling: never writes tracking state or sends a location/event.
    LaunchedEffect(context, lifecycle, distancePreviewEnabled) {
        previewDistance = null
        previewAt = null
        if (!distancePreviewEnabled) {
            previewStatus = "꺼짐"
            return@LaunchedEffect
        }
        val container = (context as com.example.senior_on.SeniorOnApplication).appContainer
        val trackingStore = OutingTrackingStateStore(context)
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                previewStatus = "현재 위치 확인 중"
                try {
                    val account = trackingStore.accountKey()
                    check(account != null && com.example.senior_on.data.local.ParentConnectionGate.isReady())
                    check(PersistedSessionStore(context).getSession()?.role == com.example.senior_on.data.remote.dto.UserRole.PARENT)
                    check(com.example.senior_on.data.local.AccessTokenStore.getBearerToken() != null)
                    check(container.parentSharingGuard?.state?.value?.locationEnabled == true)
                    val home = trackingStore.getHomeLocation()
                    if (home == null) {
                        previewDistance = null
                        previewAt = null
                        previewStatus = "저장된 자택 위치가 없어요"
                    } else {
                        val current = kotlinx.coroutines.withTimeoutOrNull(10_000L) {
                            container.locationRepository.getFreshLocation()
                        }
                        check(current != null)
                        check(trackingStore.accountKey() == account &&
                            com.example.senior_on.data.local.ParentConnectionGate.isReady() &&
                            container.parentSharingGuard?.state?.value?.locationEnabled == true &&
                            trackingStore.getHomeLocation() == home)
                        previewDistance = distanceMeters(home.latitude, home.longitude, current.latitude, current.longitude)
                        previewAt = System.currentTimeMillis()
                        previewStatus = "표시 전용 · 외출/귀가 판정에 사용하지 않음"
                    }
                } catch (cancelled: kotlinx.coroutines.CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    previewDistance = null
                    previewAt = null
                    previewStatus = "조회 불가 · 로그인, 연결, 위치 공유·권한 및 GPS를 확인해 주세요"
                }
                delay(10_000L)
            }
        }
    }

    LaunchedEffect(context, lifecycle) {
        // Restore only the existing detection observation, separately from the optional preview.
        val saved = withContext(Dispatchers.IO) { runCatching { OutingTrackingStateStore(context).snapshot() }.getOrNull() }
        if (saved != null && saved.latitude != null && saved.longitude != null) {
            DetectionQaStatus.update { current ->
                if (current.locationCheckedAt != null) current else current.copy(
                    distanceMeters = distanceMeters(saved.home.latitude, saved.home.longitude, saved.latitude, saved.longitude),
                    locationCheckedAt = saved.confirmedAt, outingState = saved.state.name,
                    outingResult = "저장된 마지막 상태 — 새 검사 대기",
                )
            }
        }
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                now = System.currentTimeMillis()
                activity = store.snapshot()
                online = context.isOutingNetworkAvailable()
                history = withContext(Dispatchers.IO) { DetectionQaHistory.read(context) }
                delay(1_000)
            }
        }
    }

    ParentSettingsScaffold("감지 QA", onBackClick, modifier) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("테스트 모드 · 서버 알림 전송 안 함", style = SeniorOnTextStyles.BodyLSemiBold, color = SeniorOnColors.Primary600)
            Text("표시만 1초마다 갱신해요. 터치·스크롤하면 활동 시각이 갱신돼요. 가만히 두고 확인해 주세요.", style = SeniorOnTextStyles.BodyMMedium)
            QaCard("현재 거리 보기 · QA 전용") {
                Switch(checked = distancePreviewEnabled, onCheckedChange = { distancePreviewEnabled = it })
                QaLine("자택에서 현재 거리", previewDistance?.let { "${it.roundToInt()}m" } ?: "미확인")
                QaLine("조회 시각", qaTime(previewAt))
                QaLine("상태", previewStatus)
                Text("켜면 이 화면을 보는 동안 조회 완료 후 10초 간격으로 위치를 확인해요. 걸어간 누적 거리가 아닌 자택과의 직선거리이며 오차가 있어요. 판정 상태·활동 시각·대기 이벤트를 변경하거나 서버에 전송하지 않아요. GPS 사용에 따른 배터리 소모는 늘 수 있어요.", style = SeniorOnTextStyles.BodyMMedium)
            }
            QaCard("무활동") {
                QaLine("기준", "2분 · ${if (activity.enabled) "설정 켜짐" else "설정 꺼짐"}")
                QaLine("마지막 활동", qaTime(activity.lastActiveAtMillis))
                QaLine("활동 없이 경과", activity.lastActiveAtMillis?.let { qaDuration(now - it) } ?: "기록 없음")
                QaLine("자동 검사", "포그라운드 30초 / 백그라운드 15분")
                QaLine("다음 포그라운드 검사", diagnostic.nextForegroundCheckAt?.let { qaDuration(it - now) } ?: "검사 중 또는 중지")
                QaLine("최근 검사", qaTime(diagnostic.inactivityCheckedAt))
                QaLine("검사 결과", diagnostic.inactivityResult)
                QaLine("최근 전송 시도", diagnostic.inactivitySend)
            }
            QaCard("무활동 대기 이벤트 상세") {
                if (queueError != null) {
                    Text(queueError.orEmpty(), style = SeniorOnTextStyles.BodyMMedium)
                } else if (event == null) {
                    Text("저장된 이벤트 없음 · 활동 재개 또는 설정 변경으로 제거될 수 있어요.", style = SeniorOnTextStyles.BodyMMedium)
                } else {
                    QaLine("이벤트 상태", qaInactivityEventStatus(event, activity.lastActiveAtMillis, now))
                    QaLine("이벤트 ID", event.clientEventId.ifBlank { "이전 기록 · 다음 검사에서 ID 생성" })
                    QaLine("최초 로컬 감지 / 대기 등록", qaTime(event.queuedAt))
                    QaLine("감지 기준이 된 마지막 입력", qaTime(event.lastActive))
                    QaLine("감지 당시 무활동 시간", qaDuration(event.queuedAt - event.lastActive))
                    QaLine("이벤트 생성 후 경과", qaDuration(now - event.queuedAt))
                    QaLine("이벤트 전송 단계 진입", "${event.attempts}회")
                    QaLine("마지막 전송 시도", qaTime(event.lastAttemptAt))
                    QaLine("완료 / 만료 처리 시각", qaTime(event.completedAt))
                    QaLine("대기 만료 예정", qaTime(event.queuedAt + java.util.concurrent.TimeUnit.HOURS.toMillis(48)))
                    Text("감지 시각과 ID는 재검사·재시도 때 바뀌지 않아요. 횟수는 전송 단계 진입 기준이며 설정 조회 실패는 제외해요. 진입 직후 활동이 재개되면 전송은 취소될 수 있어요. 이 화면은 기록을 읽기만 해요.", style = SeniorOnTextStyles.BodyMMedium)
                }
            }
            QaCard("외출·귀가") {
                QaLine("인터넷", if (online) "온라인" else "오프라인 · 판정 보류")
                QaLine("거리 기준", "외출 150m 이상 / 귀가 100m 이내")
                QaLine("마지막 확인 거리", diagnostic.distanceMeters?.let { "${it.roundToInt()}m" } ?: "미확인")
                QaLine("위치 확인 시각", qaTime(diagnostic.locationCheckedAt))
                QaLine("확인 후 경과", diagnostic.locationCheckedAt?.let { qaDuration(now - it) } ?: "미확인")
                QaLine("마지막 판정 상태", when (diagnostic.outingState) { "Home" -> "집 안"; "Outing" -> "집 밖"; else -> "미확인" })
                QaLine("검사 결과", diagnostic.outingResult)
                QaLine("최근 전송 시도", diagnostic.outingSend)
                Text("거리는 실시간 위치가 아니에요. 오프라인·조회 실패 시에도 마지막 확인값이며, 위치 오차가 있을 수 있어요. 귀가는 기존처럼 5분 간격으로 재확인해요.", style = SeniorOnTextStyles.BodyMMedium)
            }
            QaCard("외출·귀가 상세 이력 · 최신순") {
                Text("화면에는 현재 계정의 최근 48시간·최대 200건을 표시해요. 전체 이력은 별도 파일에 계속 누적하며 아래 버튼으로 저장할 수 있어요. 앱 데이터 삭제/삭제 후 재설치 시 내부 파일도 사라져요. 좌표·주소는 저장하지 않아요. 새 판정의 RECONNECTION은 실제 오프라인 관측 후 온라인에서 위치를 확인한 경우예요. 업데이트 전 이력·대기 이벤트는 기존 값을 유지해요.", style = SeniorOnTextStyles.BodyMMedium)
                TextButton(onClick = { exportHistory.launch("outing-qa-${System.currentTimeMillis()}.txt") }) {
                    Text("전체 이력 파일로 저장")
                }
                if (exportStatus.isNotEmpty()) Text(exportStatus, style = SeniorOnTextStyles.BodyMMedium)
                TextButton(onClick = { DetectionQaHistory.clear(context); history = emptyList() }) {
                    Text("화면의 최근 이력만 지우기 · 전체 파일 유지")
                }
                if (history.isEmpty()) Text("기록 없음 · 업데이트 이후 발생한 검사부터 표시돼요.", style = SeniorOnTextStyles.BodyMMedium)
                history.forEach { entry -> QaLine(qaTime(entry.time), entry.message) }
            }
        }
    }
}

@Composable private fun QaCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().background(SeniorOnColors.White).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = SeniorOnTextStyles.HeadingS)
        content()
    }
}

@Composable private fun QaLine(label: String, value: String) {
    Column {
        Text(label, style = SeniorOnTextStyles.BodyMMedium, color = SeniorOnColors.Gray500)
        Text(value, style = SeniorOnTextStyles.BodyLSemiBold, color = SeniorOnColors.Gray800)
    }
}

internal fun qaDuration(millis: Long): String {
    val seconds = millis.coerceAtLeast(0) / 1000
    return "${seconds / 60}분 ${seconds % 60}초"
}

internal fun qaInactivityEventStatus(event: PendingInactivity, lastActive: Long?, now: Long): String = when {
    event.status == "sent" -> "모의 전송 완료"
    event.status == "expired" -> "만료 처리 완료"
    event.lastActive != lastActive -> "활동 재개 · 무효화 대상 (다음 검사에서 정리)"
    event.isExpired(now) -> "대기 기한 경과 · 다음 검사에서 만료 처리"
    else -> "대기 중 · 최신 설정·활동 확인 후 재시도"
}

private fun qaTime(millis: Long?): String = millis?.let {
    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MM-dd HH:mm:ss"))
} ?: "기록 없음"
