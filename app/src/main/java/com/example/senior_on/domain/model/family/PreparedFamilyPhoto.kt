package com.example.senior_on.domain.model.family

import java.io.File

data class PreparedFamilyPhoto(
    val file: File,
    val mimeType: String,
    val displayName: String
)
