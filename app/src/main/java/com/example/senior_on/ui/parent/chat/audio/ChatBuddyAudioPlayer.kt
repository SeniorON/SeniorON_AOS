package com.example.senior_on.ui.parent.chat.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatBuddyAudioPlayer(context: Context) {
    private val audioDirectory = File(context.cacheDir, AUDIO_DIRECTORY).apply { mkdirs() }
    private var mediaPlayer: MediaPlayer? = null
    private var audioFile: File? = null

    suspend fun play(
        bytes: ByteArray,
        format: String?,
        contentType: String?,
        onCompleted: () -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        release()
        val file = File(
            audioDirectory,
            "response_${System.currentTimeMillis()}.${(format ?: contentType).extension()}",
        )
        try {
            withContext(Dispatchers.IO) { file.writeBytes(bytes) }
            audioFile = file

            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(file.absolutePath)
                setOnPreparedListener { it.start() }
                setOnCompletionListener {
                    release()
                    onCompleted()
                }
                setOnErrorListener { _, what, extra ->
                    release()
                    onError(IllegalStateException("음성 재생 오류($what/$extra)"))
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (exception: Exception) {
            release()
            onError(exception)
        }
    }

    fun release() {
        val player = mediaPlayer
        mediaPlayer = null
        runCatching { player?.stop() }
        runCatching { player?.reset() }
        runCatching { player?.release() }
        audioFile?.delete()
        audioFile = null
    }

    private fun String?.extension(): String = when (this?.lowercase()) {
        "mp3", "mpeg", "audio/mp3", "audio/mpeg" -> "mp3"
        "wav", "wave", "audio/wav", "audio/wave" -> "wav"
        "aac", "audio/aac" -> "aac"
        else -> "m4a"
    }

    companion object {
        private const val AUDIO_DIRECTORY = "chat_buddy_audio"
    }
}
