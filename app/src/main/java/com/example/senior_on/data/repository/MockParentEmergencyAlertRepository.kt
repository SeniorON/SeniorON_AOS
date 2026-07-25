package com.example.senior_on.data.repository

import com.example.senior_on.domain.model.ParentEmergencyAlertReceipt
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.delay

class MockParentEmergencyAlertRepository : ParentEmergencyAlertRepository {
    override suspend fun sendEmergencyAlert(): ParentEmergencyAlertReceipt {
        delay(450)
        return ParentEmergencyAlertReceipt(
            alertId = UUID.randomUUID().toString(),
            sentAt = Instant.now()
        )
    }
}
