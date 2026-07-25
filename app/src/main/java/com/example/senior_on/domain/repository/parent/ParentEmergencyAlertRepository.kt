package com.example.senior_on.domain.repository.parent

import com.example.senior_on.domain.model.parent.ParentEmergencyAlertReceipt

interface ParentEmergencyAlertRepository {
    suspend fun sendEmergencyAlert(): ParentEmergencyAlertReceipt
}
