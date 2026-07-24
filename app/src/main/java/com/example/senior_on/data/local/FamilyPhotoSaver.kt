package com.example.senior_on.data.local

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.TypedValue
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.example.senior_on.domain.model.FamilyImageSource
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class FamilyPhotoSaver(
    context: Context,
    private val httpClient: OkHttpClient = OkHttpClient()
) {
    private val appContext = context.applicationContext

    suspend fun save(
        imageSource: FamilyImageSource,
        displayName: String
    ): Uri = withContext(Dispatchers.IO) {
        when (imageSource) {
            is FamilyImageSource.Local -> saveLocalImage(
                drawableResId = imageSource.drawableResId,
                displayName = displayName
            )

            is FamilyImageSource.Remote -> saveRemoteImage(
                imageUrl = imageSource.url,
                displayName = displayName
            )

            is FamilyImageSource.Uri -> saveUriImage(
                imageUri = imageSource.value.toUri(),
                displayName = displayName,
            )
        }
    }

    private fun saveUriImage(
        imageUri: Uri,
        displayName: String,
    ): Uri {
        val mimeType = appContext.contentResolver.getType(imageUri)
            ?: mimeTypeFromPath(imageUri.toString())
        return appContext.contentResolver.openInputStream(imageUri)?.use { inputStream ->
            saveToMediaStore(
                inputStream = inputStream,
                mimeType = mimeType,
                displayName = displayName,
            )
        } ?: throw IOException("Unable to open family photo URI")
    }

    private fun saveLocalImage(
        drawableResId: Int,
        displayName: String
    ): Uri {
        val mimeType = localResourceMimeType(drawableResId)
        return appContext.resources.openRawResource(drawableResId).use { inputStream ->
            saveToMediaStore(
                inputStream = inputStream,
                mimeType = mimeType,
                displayName = displayName
            )
        }
    }

    private fun saveRemoteImage(
        imageUrl: String,
        displayName: String
    ): Uri {
        val request = Request.Builder()
            .url(imageUrl)
            .get()
            .build()

        return httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unable to download family photo: ${response.code}")
            }

            val responseBody = response.body
            val mimeType = responseBody.contentType()
                ?.toString()
                ?.substringBefore(';')
                ?: mimeTypeFromPath(imageUrl)

            responseBody.byteStream().use { inputStream ->
                saveToMediaStore(
                    inputStream = inputStream,
                    mimeType = mimeType,
                    displayName = displayName
                )
            }
        }
    }

    private fun saveToMediaStore(
        inputStream: InputStream,
        mimeType: String,
        displayName: String
    ): Uri {
        val resolver = appContext.contentResolver
        val extension = extensionForMimeType(mimeType)
        val fileName = "${displayName.substringBeforeLast('.')}.$extension"
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/SeniorON"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            } else {
                val directory = File(
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                    ),
                    "SeniorON"
                ).apply { mkdirs() }
                put(
                    MediaStore.Images.Media.DATA,
                    File(directory, fileName).absolutePath
                )
            }
        }

        val savedUri = resolver.insert(collection, values)
            ?: throw IOException("Unable to create MediaStore item")

        return try {
            resolver.openOutputStream(savedUri, "w")?.use { outputStream ->
                inputStream.copyTo(outputStream)
            } ?: throw IOException("Unable to open MediaStore output stream")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                resolver.update(
                    savedUri,
                    ContentValues().apply {
                        put(MediaStore.Images.Media.IS_PENDING, 0)
                    },
                    null,
                    null
                )
            }
            savedUri
        } catch (exception: Exception) {
            resolver.delete(savedUri, null, null)
            throw exception
        }
    }

    private fun localResourceMimeType(drawableResId: Int): String {
        val value = TypedValue()
        appContext.resources.getValue(drawableResId, value, true)
        return mimeTypeFromPath(value.string?.toString().orEmpty())
    }

    private fun mimeTypeFromPath(path: String): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(path)
            .lowercase()
        return MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(extension)
            ?: DefaultImageMimeType
    }

    private fun extensionForMimeType(mimeType: String): String =
        MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType)
            ?: DefaultImageExtension

    private companion object {
        const val DefaultImageMimeType = "image/jpeg"
        const val DefaultImageExtension = "jpg"
    }
}
