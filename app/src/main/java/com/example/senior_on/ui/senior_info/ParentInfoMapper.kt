package com.example.senior_on.ui.senior_info

import com.example.senior_on.domain.model.ParentInfo
import java.time.format.DateTimeFormatter

private val ParentInfoBirthDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

internal fun ParentInfoInputState.toParentInfo(): ParentInfo {
    val parsedBirthDate = requireNotNull(parseBirthDate(birthDate)) {
        "A valid birth date is required before saving parent information"
    }
    val resolvedRelationship = when (relationship) {
        SeniorRelationship.Custom -> customRelationship
        else -> relationship.label
    }

    return ParentInfo(
        name = name.trim(),
        relationshipLabel = resolvedRelationship.trim(),
        birthDate = parsedBirthDate,
        phoneNumber = phoneNumber,
        address = address.trim(),
        addressDetail = addressDetail.trim(),
        addressLatitude = addressLatitude,
        addressLongitude = addressLongitude,
    )
}

internal fun ParentInfo.toInputState(): ParentInfoInputState {
    val relationship = SeniorRelationship.entries.firstOrNull { candidate ->
        candidate != SeniorRelationship.Custom && candidate.label == relationshipLabel
    } ?: SeniorRelationship.Custom

    return ParentInfoInputState(
        name = name,
        relationship = relationship,
        customRelationship = relationshipLabel.takeIf {
            relationship == SeniorRelationship.Custom
        }.orEmpty(),
        birthDate = birthDate.format(ParentInfoBirthDateFormatter),
        phoneNumber = phoneNumber,
        address = address,
        addressDetail = addressDetail,
        addressLatitude = addressLatitude,
        addressLongitude = addressLongitude,
    )
}
