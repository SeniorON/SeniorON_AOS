package com.example.senior_on.data.remote.websocket

import com.google.gson.JsonParser
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

/** A null seniorId is a local reconnect signal, never a server payload. */
data class NotificationHomeSignal(val seniorId: Long?, val eventId: Long? = null, val reason: String? = null)

internal fun NotificationHomeSignal.matches(selectedSeniorId: Long, detailEventId: Long? = null): Boolean =
    seniorId == null || (seniorId == selectedSeniorId && (detailEventId == null || eventId == detailEventId))

internal fun parseNotificationHomeSignal(body: String): NotificationHomeSignal? = runCatching {
    val json = JsonParser.parseString(body).asJsonObject
    if (json["action"]?.asString != "NOTIFICATION_HOME_UPDATED") return null
    val seniorId = json["seniorId"].asLong.takeIf { it > 0 } ?: return null
    val eventId = json["eventId"].asLong.takeIf { it > 0 } ?: return null
    val reason = json["reason"].asString
    if (reason !in setOf("CREATED", "ADDRESS_UPDATED")) return null
    NotificationHomeSignal(seniorId, eventId, reason)
}.getOrNull()

class NotificationStompSource(
    private val socketFactory: WebSocket.Factory,
    private val url: String,
    private val bearerToken: () -> String?,
    private val log: (String) -> Unit = {},
) {
    fun observe(): Flow<NotificationHomeSignal> = flow {
        var retryDelay = 1_000L
        while (currentCoroutineContext().isActive && bearerToken() != null) {
            val started = System.nanoTime()
            try {
                connection().collect { emit(it) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: IOException) {
                log("Notification socket disconnected")
            }
            if (bearerToken() == null) break
            if (System.nanoTime() - started > 30_000_000_000L) retryDelay = 1_000
            delay(retryDelay)
            retryDelay = (retryDelay * 2).coerceAtMost(30_000)
        }
    }

    private fun connection(): Flow<NotificationHomeSignal> = callbackFlow {
        val token = bearerToken() ?: run { close(); return@callbackFlow }
        val decoder = HomeStompDecoder()
        var subscribed = false
        val timeout = launch { delay(20_000); close(IOException("STOMP connect timeout")) }
        val socket = socketFactory.newWebSocket(
            Request.Builder().url(url).header("Authorization", token).build(),
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    if (!webSocket.send("CONNECT\naccept-version:1.2\nhost:${url.toHttpUrl().host}\nheart-beat:0,0\n\n\u0000")) {
                        close(IOException("STOMP connect failed"))
                    }
                }
                override fun onMessage(webSocket: WebSocket, text: String) = receive(webSocket, text.toByteArray(Charsets.UTF_8))
                override fun onMessage(webSocket: WebSocket, bytes: ByteString) = receive(webSocket, bytes.toByteArray())
                private fun receive(socket: WebSocket, bytes: ByteArray) {
                    if (!this@callbackFlow.isActive) return
                    try {
                        for (frame in decoder.append(bytes)) when (frame.command) {
                            "CONNECTED" -> if (!subscribed) {
                                require(frame.headers["version"] == "1.2")
                                check(socket.send("SUBSCRIBE\nid:notification-home\ndestination:/user/queue/notification-home\nack:auto\n\n\u0000"))
                                subscribed = true
                                timeout.cancel()
                                log("Notification subscription queued")
                                trySend(NotificationHomeSignal(null))
                            }
                            // Spring user destinations may be rewritten to a session-specific queue.
                            "MESSAGE" -> if (subscribed && frame.headers["subscription"] == "notification-home") {
                                parseNotificationHomeSignal(frame.body)?.let {
                                    if (trySend(it).isFailure) close(IOException("Notification queue overflow"))
                                }
                            }
                            "ERROR" -> close(IOException("STOMP rejected"))
                        }
                    } catch (_: IllegalArgumentException) { close(IOException("Invalid STOMP frame"))
                    } catch (_: IllegalStateException) { close(IOException("Invalid STOMP session")) }
                }
                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    webSocket.close(code, null)
                    close()
                }
                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) { close() }
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    response?.body?.close()
                    close(IOException("Notification socket failed"))
                }
            },
        )
        awaitClose { timeout.cancel(); socket.cancel() }
    }
}
