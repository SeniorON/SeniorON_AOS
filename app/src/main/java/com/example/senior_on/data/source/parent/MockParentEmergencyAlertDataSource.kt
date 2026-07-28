package com.example.senior_on.data.source.parent

import com.example.senior_on.domain.model.parent.ParentEmergencyAlertReceipt
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.delay

class MockParentEmergencyAlertDataSource : ParentEmergencyAlertDataSource {
    override suspend fun sendEmergencyAlert(): ParentEmergencyAlertReceipt {
        delay(450)
        return ParentEmergencyAlertReceipt(
            alertId = UUID.randomUUID().toString(),
            sentAt = Instant.now()
        )
    }
}
