package com.example.senior_on.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import com.example.senior_on.domain.model.family.PreparedFamilyPhoto
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

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

    suspend fun prepareProfileImage(photoUri: String): PreparedFamilyPhoto =
        withContext(Dispatchers.IO) {
            val uri = photoUri.toUri()
            val uploadDirectory = File(appContext.cacheDir, UploadDirectory).apply {
                mkdirs()
            }
            val uploadFile = File.createTempFile(
                "profile_upload_",
                ".jpg",
                uploadDirectory,
            )

            try {
                val decodedBitmap = decodeProfileBitmap(uri)
                val jpegBitmap = Bitmap.createBitmap(
                    decodedBitmap.width,
                    decodedBitmap.height,
                    Bitmap.Config.ARGB_8888,
                )
                Canvas(jpegBitmap).apply {
                    drawColor(Color.WHITE)
                    drawBitmap(decodedBitmap, 0f, 0f, null)
                }
                if (jpegBitmap !== decodedBitmap) {
                    decodedBitmap.recycle()
                }

                try {
                    uploadFile.outputStream().use { output ->
                        check(
                            jpegBitmap.compress(
                                Bitmap.CompressFormat.JPEG,
                                ProfileJpegQuality,
                                output,
                            )
                        ) {
                            "프로필 이미지를 변환하지 못했습니다."
                        }
                    }
                } finally {
                    jpegBitmap.recycle()
                }

                check(uploadFile.length() in 1..MaxUploadBytes) {
                    "프로필 이미지 용량을 줄이지 못했습니다."
                }
                PreparedFamilyPhoto(
                    file = uploadFile,
                    mimeType = "image/jpeg",
                    displayName = "profile_${UUID.randomUUID()}.jpg",
                )
            } catch (exception: Exception) {
                uploadFile.delete()
                throw exception
            }
        }

    suspend fun prepareDrawable(
        @DrawableRes drawableResId: Int,
        displayName: String,
    ): PreparedFamilyPhoto = withContext(Dispatchers.IO) {
        val drawable = requireNotNull(ContextCompat.getDrawable(appContext, drawableResId)) {
            "기본 프로필 이미지를 불러오지 못했습니다."
        }
        val uploadDirectory = File(appContext.cacheDir, UploadDirectory).apply {
            mkdirs()
        }
        val uploadFile = File.createTempFile(
            "profile_default_",
            ".png",
            uploadDirectory,
        )

        try {
            val iconSize = calculateDefaultProfileIconSize(DefaultProfileImageSize)
            val iconBitmap = drawable.toBitmap(
                width = iconSize,
                height = iconSize,
                config = Bitmap.Config.ARGB_8888,
            )
            val bitmap = Bitmap.createBitmap(
                DefaultProfileImageSize,
                DefaultProfileImageSize,
                Bitmap.Config.ARGB_8888,
            )
            Canvas(bitmap).drawBitmap(
                iconBitmap,
                (DefaultProfileImageSize - iconSize) / 2f,
                (DefaultProfileImageSize - iconSize) / 2f,
                null,
            )
            iconBitmap.recycle()
            try {
                uploadFile.outputStream().use { output ->
                    check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                        "기본 프로필 이미지를 변환하지 못했습니다."
                    }
                }
            } finally {
                bitmap.recycle()
            }
            PreparedFamilyPhoto(
                file = uploadFile,
                mimeType = "image/png",
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

    private fun decodeProfileBitmap(uri: Uri): Bitmap {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(appContext.contentResolver, uri)
            return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val width = info.size.width
                val height = info.size.height
                val longestSide = maxOf(width, height)
                if (longestSide > MaxProfileImageDimension) {
                    val scale = MaxProfileImageDimension.toFloat() / longestSide
                    decoder.setTargetSize(
                        (width * scale).toInt().coerceAtLeast(1),
                        (height * scale).toInt().coerceAtLeast(1),
                    )
                }
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        }

        val resolver = appContext.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        } ?: throw IOException("선택한 프로필 이미지를 열 수 없습니다.")
        check(bounds.outWidth > 0 && bounds.outHeight > 0) {
            "지원하지 않는 프로필 이미지 형식입니다."
        }

        var sampleSize = 1
        while (
            bounds.outWidth / sampleSize > MaxProfileImageDimension ||
            bounds.outHeight / sampleSize > MaxProfileImageDimension
        ) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return resolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, options)
        } ?: throw IOException("선택한 프로필 이미지를 변환할 수 없습니다.")
    }

    private companion object {
        const val UploadDirectory = "family_uploads"
        const val DefaultImageExtension = "jpg"
        const val DefaultBufferSize = 8 * 1024
        const val MaxUploadBytes = 10L * 1024L * 1024L
        const val MaxDisplayNameLength = 120
        const val MaxProfileImageDimension = 1600
        const val ProfileJpegQuality = 88
        const val DefaultProfileImageSize = 256
    }
}

internal fun calculateDefaultProfileIconSize(canvasSize: Int): Int =
    (canvasSize * DefaultProfileIconSizeDp / DefaultProfileContainerSizeDp)
        .roundToInt()

private const val DefaultProfileIconSizeDp = 64f
private const val DefaultProfileContainerSizeDp = 98f

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
