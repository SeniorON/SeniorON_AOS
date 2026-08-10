package com.example.senior_on.data.repository.impl

import java.util.Locale

internal fun normalizeFamilyCodeForRequest(value: String): String {
    val characters = value
        .trim()
        .uppercase(Locale.ROOT)
        .filter { it in 'A'..'Z' || it in '0'..'9' }

    return if (characters.length == FamilyCodeCharacterCount) {
        characters.take(FamilyCodeGroupLength) + "-" +
            characters.drop(FamilyCodeGroupLength)
    } else {
        value.trim().uppercase(Locale.ROOT)
    }
}

private const val FamilyCodeCharacterCount = 8
private const val FamilyCodeGroupLength = 4
