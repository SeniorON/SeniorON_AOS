package com.example.senior_on.device

import android.content.Context
import com.example.senior_on.domain.model.server.SafetyEvent
import com.example.senior_on.domain.repository.server.EventRepository
import java.io.IOException

fun detectionEventRepository(context: Context, real: EventRepository, config: DetectionConfig): EventRepository {
    if (!config.qaEnabled) return real
    return DetectionEventRepository(real) { type, summary ->
        val mode = if (config.simulateSendFailure) "fail" else "success"
        DetectionQa.trace("WOULD_SEND type=$type $summary result=$mode")
        if (type == "OUTING_RETURN") DetectionQaHistory.record(context,
            "외출·귀가 모의 전송 ${if (mode == "fail") "실패" else "성공"} · $summary · 실제 서버 호출 없음")
        DetectionQaStatus.update {
            val result = if (mode == "fail") "모의 전송 실패 — 재시도 대상" else "모의 전송 성공 (서버 호출 안 함)"
            if (type == "INACTIVITY") it.copy(inactivitySend = result) else it.copy(outingSend = result)
        }
        if (mode == "fail") throw IOException("Simulated detection send failure")
    }
}

/** Only detection creates are intercepted; SOS and reads continue using the real repository. */
internal class DetectionEventRepository(
    private val real: EventRepository,
    private val send: (String, String) -> Unit,
) : EventRepository by real {
    override suspend fun createOutingReturn(
        phase: String, latitude: Double, longitude: Double, battery: Int,
        detectionType: String, clientEventId: String, detectedAt: String,
    ): SafetyEvent {
        send("OUTING_RETURN", "phase=$phase detectionType=$detectionType clientEventId=$clientEventId detectedAt=$detectedAt")
        return result("OUTING_RETURN", phase)
    }

    override suspend fun createInactivity(
        latitude: Double?, longitude: Double?, battery: Int, lastSeenAt: String,
        clientEventId: String,
    ): SafetyEvent {
        send("INACTIVITY", "clientEventId=$clientEventId hasLocation=${latitude != null && longitude != null}")
        return result("INACTIVITY", null)
    }

    private fun result(type: String, phase: String?) = SafetyEvent(
        id = null, type = type, occurredAt = null, address = null,
        latitude = null, longitude = null, deviceBattery = null, phase = phase,
    )
}
