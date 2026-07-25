package com.example.senior_on.domain.model

import java.time.LocalDate
import java.time.Period

data class ParentInfo(
    val name: String,
    val relationshipLabel: String,
    val birthDate: LocalDate,
    val phoneNumber: String,
    val address: String,
    val addressDetail: String = "",
    val addressLatitude: Double? = null,
    val addressLongitude: Double? = null,
) {
    val fullAddress: String
        get() = listOf(address, addressDetail)
            .filter(String::isNotBlank)
            .joinToString(" ")

    fun age(today: LocalDate = LocalDate.now()): Int =
        Period.between(birthDate, today).years.coerceAtLeast(0)
}
