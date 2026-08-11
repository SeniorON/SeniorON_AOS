package com.example.senior_on.ui.child.settings

import com.example.senior_on.domain.model.parent.ParentInfo
import com.example.senior_on.ui.common.seniorinfo.SeniorRelationship
import java.time.format.DateTimeFormatter

private val SettingsBirthDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

internal fun String?.toSettingsAccountTypeLabel(): String = when (this?.trim()?.uppercase()) {
    "CHILD" -> "보호자 계정"
    "PARENT" -> "시니어 계정"
    else -> "계정"
}

internal fun ParentInfo.toConnectedSeniorDeviceUiState(
    deviceName: String,
    relationshipLabel: String,
): ConnectedSeniorDeviceUiState {
    val relationship = SeniorRelationship.entries.firstOrNull { candidate ->
        candidate != SeniorRelationship.Custom && candidate.label == relationshipLabel
    } ?: SeniorRelationship.Custom

    return ConnectedSeniorDeviceUiState(
        deviceName = deviceName,
        name = name,
        relationship = relationship,
        customRelationship = relationshipLabel.takeIf {
            relationship == SeniorRelationship.Custom
        }.orEmpty(),
        birthDate = birthDate.format(SettingsBirthDateFormatter),
        phoneNumber = phoneNumber,
        address = address,
        addressDetail = addressDetail,
    )
}
