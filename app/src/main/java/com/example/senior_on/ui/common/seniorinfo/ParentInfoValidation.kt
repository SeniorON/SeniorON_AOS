package com.example.senior_on.ui.common.seniorinfo

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
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

internal fun formatPhoneFieldValue(value: TextFieldValue, previous: TextFieldValue? = null): TextFieldValue {
    val inputDigits = value.text.filter(Char::isDigit)
    if (previous != null && inputDigits.length > RequiredPhoneNumberLength) return previous
    val digits = inputDigits.take(RequiredPhoneNumberLength)
    if (digits == value.text) return value
    fun offset(index: Int) = value.text.take(index).count(Char::isDigit).coerceAtMost(digits.length)
    fun range(range: TextRange) = TextRange(offset(range.start), offset(range.end))
    return value.copy(text = digits, selection = range(value.selection), composition = value.composition?.let(::range))
}

// Keep separators out of the editable text, so backspace always removes a digit.
internal val PhoneVisualTransformation = VisualTransformation { input ->
    val formatted = formatPhoneNumber(input.text)
    TransformedText(AnnotatedString(formatted), object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int =
            offset + (if (offset >= 3 && input.length > 3) 1 else 0) +
                (if (offset >= 7 && input.length > 7) 1 else 0)

        override fun transformedToOriginal(offset: Int): Int = formatted.take(offset).count(Char::isDigit)
    })
}

internal fun formatPhoneNumber(value: String): String {
    val digits = value.filter(Char::isDigit).take(RequiredPhoneNumberLength)

    return when {
        digits.length <= 3 -> digits
        digits.length <= 7 -> "${digits.substring(0, 3)}-${digits.substring(3)}"
        else -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
    }
}
