package com.example.senior_on.data.repository

import com.example.senior_on.domain.model.ParentEmergencyAlertReceipt

interface ParentEmergencyAlertRepository {
    suspend fun sendEmergencyAlert(): ParentEmergencyAlertReceipt
}
