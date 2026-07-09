package com.example.senior_on.ui.senior_info

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import java.time.LocalDate

internal const val RequiredPhoneNumberLength = 11

internal fun parseBirthDate(value: String): LocalDate? {
    val dateParts = value.split(".")
    if (dateParts.size != 3) {
        return null
    }

    return runCatching {
        LocalDate.of(
            dateParts[0].toInt(),
            dateParts[1].toInt(),
            dateParts[2].toInt()
        )
    }.getOrNull()?.takeUnless { it.isAfter(LocalDate.now()) }
}

internal fun LocalDate.toBirthDateString(): String {
    return "${year.toString().padStart(4, '0')}." +
        "${monthValue.toString().padStart(2, '0')}." +
        dayOfMonth.toString().padStart(2, '0')
}

internal fun formatPhoneFieldValue(value: TextFieldValue): TextFieldValue {
    val formattedPhoneNumber = formatPhoneNumber(value.text)

    return TextFieldValue(
        text = formattedPhoneNumber,
        selection = TextRange(formattedPhoneNumber.length)
    )
}

internal fun formatPhoneNumber(value: String): String {
    val digits = value.filter(Char::isDigit).take(RequiredPhoneNumberLength)

    return when {
        digits.length <= 3 -> digits
        digits.length <= 7 -> "${digits.substring(0, 3)}-${digits.substring(3)}"
        else -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
    }
}
