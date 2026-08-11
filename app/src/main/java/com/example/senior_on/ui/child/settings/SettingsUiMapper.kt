package com.example.senior_on.ui.child.settings

import com.example.senior_on.domain.model.auth.AppUserMode
import com.example.senior_on.domain.model.auth.AppUserProfile
import com.example.senior_on.domain.model.parent.ParentInfo
import com.example.senior_on.ui.common.seniorinfo.SeniorRelationship
import com.example.senior_on.ui.common.seniorinfo.parseBirthDate
import java.time.format.DateTimeFormatter

private val SettingsBirthDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

internal fun AppUserProfile.toSettingsProfileUiState(): SettingsProfileUiState {
    return SettingsProfileUiState(
        name = name,
        accountTypeLabel = when (mode) {
            AppUserMode.Child -> "보호자 계정"
            AppUserMode.Senior -> "시니어 계정"
        },
        email = email,
    )
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

internal fun ConnectedSeniorDeviceUiState.toParentInfo(
    currentParentInfo: ParentInfo,
): ParentInfo {
    val parsedBirthDate = requireNotNull(parseBirthDate(birthDate)) {
        "A valid birth date is required before saving connected senior information"
    }

    return currentParentInfo.copy(
        name = name.trim(),
        relationshipLabel = relationshipLabel.trim(),
        birthDate = parsedBirthDate,
        phoneNumber = phoneNumber,
        address = address.trim(),
        addressDetail = addressDetail.trim(),
    )
}
