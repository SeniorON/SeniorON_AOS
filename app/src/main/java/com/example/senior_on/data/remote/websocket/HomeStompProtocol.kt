package com.example.senior_on.data.remote.websocket

internal data class HomeStompFrame(
    val command: String,
    val headers: Map<String, String>,
    val body: String,
)

/** Receive-only STOMP 1.2 framing, including split/coalesced frames and byte content-length. */
internal class HomeStompDecoder {
    private var pending = byteArrayOf()

    fun append(bytes: ByteArray): List<HomeStompFrame> {
        require(pending.size + bytes.size <= MAX_FRAME_BYTES) { "STOMP frame too large" }
        pending += bytes
        val frames = mutableListOf<HomeStompFrame>()
        while (pending.isNotEmpty()) {
            val start = pending.indexOfFirst { it != 10.toByte() && it != 13.toByte() }
            if (start == -1) {
                pending = byteArrayOf()
                break
            }
            if (start > 0) pending = pending.copyOfRange(start, pending.size)
            val lines = mutableListOf<String>()
            var cursor = 0
            var bodyStart = -1
            while (cursor < pending.size) {
                val end = (cursor until pending.size).firstOrNull { pending[it] == 10.toByte() }
                    ?: break
                val line = pending.copyOfRange(cursor, end).toString(Charsets.UTF_8).removeSuffix("\r")
                cursor = end + 1
                if (line.isEmpty()) {
                    bodyStart = cursor
                    break
                }
                lines += line
            }
            if (bodyStart < 0) break
            require(lines.isNotEmpty()) { "Missing STOMP command" }
            val headers = linkedMapOf<String, String>()
            for (line in lines.drop(1)) {
                val colon = line.indexOf(':')
                require(colon > 0) { "Invalid STOMP header" }
                val escaped = lines.first() != "CONNECTED"
                val key = line.substring(0, colon).let { if (escaped) unescape(it) else it }
                val value = line.substring(colon + 1).let { if (escaped) unescape(it) else it }
                headers.putIfAbsent(key, value)
            }
            val length = headers["content-length"]?.let {
                requireNotNull(it.toIntOrNull()).also { size -> require(size in 0..MAX_FRAME_BYTES) }
            }
            val bodyEnd = if (length != null) bodyStart + length else {
                (bodyStart until pending.size).firstOrNull { pending[it] == 0.toByte() } ?: break
            }
            if (bodyEnd >= pending.size) break
            require(pending[bodyEnd] == 0.toByte()) { "Missing STOMP terminator" }
            frames += HomeStompFrame(
                command = lines.first(),
                headers = headers,
                body = pending.copyOfRange(bodyStart, bodyEnd).toString(Charsets.UTF_8),
            )
            pending = pending.copyOfRange(bodyEnd + 1, pending.size)
        }
        return frames
    }

    private fun unescape(value: String): String = buildString {
        var index = 0
        while (index < value.length) {
            val char = value[index++]
            if (char != '\\') append(char) else {
                require(index < value.length) { "Invalid STOMP escape" }
                append(when (value[index++]) {
                    'n' -> '\n'
                    'r' -> '\r'
                    'c' -> ':'
                    '\\' -> '\\'
                    else -> error("Invalid STOMP escape")
                })
            }
        }
    }

    private companion object {
        const val MAX_FRAME_BYTES = 64 * 1024
    }
}
