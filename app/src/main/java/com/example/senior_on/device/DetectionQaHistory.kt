package com.example.senior_on.device

import android.content.Context
import com.example.senior_on.location.tracking.OutingTrackingStateStore
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

internal data class QaHistoryEntry(val time: Long, val message: String)

/** Bounded, device-local QA journal. Never consumed by detection. No coordinates or addresses. */
internal object DetectionQaHistory {
    private const val Lifetime = 48L * 60 * 60 * 1000
    private val archiveExecutor = java.util.concurrent.Executors.newSingleThreadExecutor()
    private fun accountHash(context: Context): String? {
        if (!DetectionQa.enabled) return null
        val account = OutingTrackingStateStore(context).accountKey() ?: return null
        return MessageDigest.getInstance("SHA-256").digest(account.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
    private fun archiveFile(context: Context, hash: String) = java.io.File(
        java.io.File(context.noBackupFilesDir, "detection_qa_history"), "$hash.jsonl")
    private fun append(file: java.io.File, entries: List<QaHistoryEntry>) {
        file.parentFile?.mkdirs()
        file.appendText(entries.joinToString("") {
            JSONObject().put("time", java.time.Instant.ofEpochMilli(it.time).toString())
                .put("message", it.message).toString() + "\n"
        }, Charsets.UTF_8)
    }

    /** Call on IO dispatcher. FIFO barrier includes all previously queued archive writes. */
    fun export(context: Context, destination: android.net.Uri) {
        val hash = checkNotNull(accountHash(context))
        val seed = read(context).reversed()
        archiveExecutor.submit {
            val file = archiveFile(context, hash)
            if (!file.exists()) append(file, seed)
            checkNotNull(context.contentResolver.openOutputStream(destination, "wt")).use { output ->
                file.inputStream().use { it.copyTo(output) }
            }
        }.get()
    }
    private fun preferences(context: Context): android.content.SharedPreferences? {
        if (!DetectionQa.enabled) return null
        val hash = accountHash(context) ?: return null
        return context.getSharedPreferences("detection_qa_history_$hash", Context.MODE_PRIVATE)
    }

    private fun decode(raw: String, now: Long): List<QaHistoryEntry> {
        val array = JSONArray(raw)
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            QaHistoryEntry(item.getLong("time"), item.getString("message"))
        }.filter { it.time >= now - Lifetime }.takeLast(200)
    }

    @Synchronized fun read(context: Context): List<QaHistoryEntry> = runCatching {
        val prefs = preferences(context) ?: return emptyList()
        decode(prefs.getString("entries", "[]") ?: "[]", System.currentTimeMillis()).reversed()
    }.getOrDefault(emptyList())

    @Synchronized fun record(context: Context, message: String) {
        // Diagnostic failures must not change the production flow or retry outcome.
        runCatching {
            val prefs = preferences(context) ?: return
            val now = System.currentTimeMillis()
            val previous = runCatching { decode(prefs.getString("entries", "[]") ?: "[]", now) }.getOrDefault(emptyList())
            val entries = (previous + QaHistoryEntry(now, message.take(800))).takeLast(200)
            val hash = accountHash(context) ?: return
            val app = context.applicationContext
            archiveExecutor.execute {
                runCatching {
                    val file = archiveFile(app, hash)
                    if (!file.exists()) append(file, previous)
                    append(file, listOf(QaHistoryEntry(now, message.take(800))))
                }.onFailure {
                    com.example.senior_on.logging.AppLog.w("SeniorOnDetectionQA", "QA_HISTORY_ARCHIVE_WRITE_FAILED")
                }
            }
            val array = JSONArray()
            entries.forEach { array.put(JSONObject().put("time", it.time).put("message", it.message)) }
            prefs.edit().putString("entries", array.toString()).apply()
        }
    }

    @Synchronized fun clear(context: Context) {
        runCatching { preferences(context)?.edit()?.remove("entries")?.apply() }
    }
}
