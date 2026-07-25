package com.example.senior_on.domain.model.parent

import java.time.Instant

data class ParentEmergencyAlertReceipt(
    val alertId: String,
    val sentAt: Instant
)
