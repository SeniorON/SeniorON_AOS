package com.example.senior_on.data.local

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.example.senior_on.domain.model.family.PreparedFamilyPhoto
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FamilyPhotoUploadPreparer(context: Context) {
    private val appContext = context.applicationContext

    suspend fun prepare(photoUri: String): PreparedFamilyPhoto =
        withContext(Dispatchers.IO) {
            val uri = photoUri.toUri()
            val resolver = appContext.contentResolver
            val extensionMimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(
                    MimeTypeMap.getFileExtensionFromUrl(photoUri)
                        .lowercase(Locale.ROOT),
                )
            val mimeType = resolveFamilyPhotoMimeType(
                reportedMimeType = resolver.getType(uri),
                extensionMimeType = extensionMimeType,
            )

            val extension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(mimeType)
                ?: DefaultImageExtension
            val displayName = queryDisplayName(uri)
                ?.substringAfterLast('/')
                ?.take(MaxDisplayNameLength)
                ?: "family_photo_${UUID.randomUUID()}.$extension"
            val uploadDirectory = File(appContext.cacheDir, UploadDirectory).apply {
                mkdirs()
            }
            val uploadFile = File.createTempFile(
                "family_upload_",
                ".$extension",
                uploadDirectory,
            )

            try {
                resolver.openInputStream(uri)?.use { input ->
                    uploadFile.outputStream().use { output ->
                        val buffer = ByteArray(DefaultBufferSize)
                        var totalBytes = 0L
                        while (true) {
                            val readBytes = input.read(buffer)
                            if (readBytes < 0) break
                            totalBytes += readBytes
                            if (totalBytes > MaxUploadBytes) {
                                throw IOException("Family photo exceeds upload size limit")
                            }
                            output.write(buffer, 0, readBytes)
                        }
                    }
                } ?: throw IOException("Unable to open selected family photo")

                PreparedFamilyPhoto(
                    file = uploadFile,
                    mimeType = mimeType,
                    displayName = displayName,
                )
            } catch (exception: Exception) {
                uploadFile.delete()
                throw exception
            }
        }

    suspend fun deleteOwnedSource(photoUri: String) = withContext(Dispatchers.IO) {
        val uri = photoUri.toUri()
        if (uri.authority == "${appContext.packageName}.fileprovider") {
            appContext.contentResolver.delete(uri, null, null)
        }
    }

    private fun queryDisplayName(uri: Uri): String? =
        appContext.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (columnIndex < 0) null else cursor.getString(columnIndex)
        }

    private companion object {
        const val UploadDirectory = "family_uploads"
        const val DefaultImageExtension = "jpg"
        const val DefaultBufferSize = 8 * 1024
        const val MaxUploadBytes = 10L * 1024L * 1024L
        const val MaxDisplayNameLength = 120
    }
}

private val AllowedFamilyPhotoMimeTypes = setOf(
    "image/jpeg",
    "image/png",
    "image/webp",
)

internal fun resolveFamilyPhotoMimeType(
    reportedMimeType: String?,
    extensionMimeType: String?,
): String {
    val mimeType = reportedMimeType
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?: extensionMimeType
            ?.trim()
            ?.takeIf(String::isNotEmpty)
        ?: throw IllegalArgumentException("Unable to determine family photo MIME type")
    val normalizedMimeType = mimeType.lowercase(Locale.ROOT)
    require(normalizedMimeType in AllowedFamilyPhotoMimeTypes) {
        "Only JPEG, PNG, and WebP images can be uploaded"
    }
    return normalizedMimeType
}
