package com.example.senior_on.domain.model

import java.time.Instant

data class ParentEmergencyAlertReceipt(
    val alertId: String,
    val sentAt: Instant
)
