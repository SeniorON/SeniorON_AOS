package com.example.senior_on.device

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Read-only diagnostic projection. Never drives detection or persists coordinates. */
data class DetectionQaSnapshot(
    val inactivityCheckedAt: Long? = null,
    val inactivityResult: String = "검사 대기",
    val nextForegroundCheckAt: Long? = null,
    val outingState: String = "미확인",
    val outingResult: String = "검사 대기",
    val distanceMeters: Float? = null,
    val locationCheckedAt: Long? = null,
    val inactivitySend: String = "전송 시도 없음",
    val outingSend: String = "전송 시도 없음",
)

object DetectionQaStatus {
    private val mutable = MutableStateFlow(DetectionQaSnapshot())
    val state = mutable.asStateFlow()
    fun update(transform: (DetectionQaSnapshot) -> DetectionQaSnapshot) {
        if (DetectionQa.enabled) mutable.update(transform)
    }
    fun inactivity(result: String) = update {
        it.copy(inactivityCheckedAt = System.currentTimeMillis(), inactivityResult = result)
    }
}
