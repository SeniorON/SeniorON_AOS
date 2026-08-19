package com.example.senior_on.ui.parent.chat.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class ChatBuddyAudioRecorder(context: Context) {
    private val appContext = context.applicationContext
    private val audioDirectory = File(context.cacheDir, AUDIO_DIRECTORY).apply { mkdirs() }
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun start() {
        check(recorder == null) { "이미 음성을 녹음하고 있어요." }

        val file = File(audioDirectory, "companion_${System.currentTimeMillis()}.m4a")
        val newRecorder = createRecorder()
        try {
            newRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(AUDIO_BIT_RATE)
                setAudioSamplingRate(AUDIO_SAMPLE_RATE)
                setMaxDuration(MAX_DURATION_MILLIS)
                setMaxFileSize(MAX_FILE_SIZE_BYTES)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recorder = newRecorder
            outputFile = file
        } catch (exception: Exception) {
            runCatching { newRecorder.release() }
            file.delete()
            throw exception
        }
    }

    fun stop(): File {
        val activeRecorder = recorder ?: error("진행 중인 녹음이 없어요.")
        val file = outputFile ?: error("녹음 파일을 찾을 수 없어요.")
        recorder = null
        outputFile = null

        try {
            activeRecorder.stop()
        } catch (exception: RuntimeException) {
            file.delete()
            throw IllegalStateException("녹음 시간이 너무 짧아요. 다시 말씀해 주세요.", exception)
        } finally {
            runCatching { activeRecorder.release() }
        }

        check(file.exists() && file.length() > 0L) { "녹음된 음성을 찾을 수 없어요." }
        return file
    }

    fun cancel() {
        val activeRecorder = recorder
        recorder = null
        runCatching { activeRecorder?.stop() }
        runCatching { activeRecorder?.release() }
        outputFile?.delete()
        outputFile = null
    }

    @Suppress("DEPRECATION")
    private fun createRecorder(): MediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(appContext)
    } else {
        MediaRecorder()
    }

    companion object {
        private const val AUDIO_DIRECTORY = "chat_buddy_audio"
        private const val AUDIO_BIT_RATE = 128_000
        private const val AUDIO_SAMPLE_RATE = 44_100
        private const val MAX_DURATION_MILLIS = 60_000
        private const val MAX_FILE_SIZE_BYTES = 9_500_000L
    }
}
