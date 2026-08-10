package com.example.senior_on.data.source.parent

import com.example.senior_on.data.remote.dto.RiskLinkRequest
import com.example.senior_on.data.source.device.LocalDeviceStatusDataSource
import com.example.senior_on.data.source.event.EventDataSource
import com.example.senior_on.domain.model.parent.ParentLinkSafetyResult
import com.example.senior_on.domain.model.parent.ParentLinkSafetyVerdict

class RemoteParentLinkSafetyDataSource(
    private val eventDataSource: EventDataSource,
    private val deviceStatusDataSource: LocalDeviceStatusDataSource,
) : ParentLinkSafetyDataSource {
    override suspend fun inspectLink(url: String): ParentLinkSafetyResult {
        val normalizedUrl = url.trim()
        val response = eventDataSource.createRiskLink(
            RiskLinkRequest(
                linkUrl = normalizedUrl,
                deviceBattery = deviceStatusDataSource.getBatteryLevel(),
            )
        )
        return ParentLinkSafetyResult(
            url = response.linkUrl?.takeIf(String::isNotBlank) ?: normalizedUrl,
            verdict = response.riskLevel.toParentLinkSafetyVerdict(),
        )
    }
}

internal fun String?.toParentLinkSafetyVerdict(): ParentLinkSafetyVerdict =
    when (this?.trim()?.uppercase()) {
        "낮음", "LOW", "SAFE" -> ParentLinkSafetyVerdict.Safe
        "높음", "HIGH", "DANGEROUS" -> ParentLinkSafetyVerdict.Dangerous
        else -> ParentLinkSafetyVerdict.Unknown
    }
