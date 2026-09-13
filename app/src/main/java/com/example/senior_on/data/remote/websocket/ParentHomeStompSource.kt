package com.example.senior_on.data.remote.websocket

import com.example.senior_on.domain.repository.parent.ParentHomeUpdateEvent
import java.io.IOException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class ParentHomeStompSource(
    private val socketFactory: WebSocket.Factory,
    private val url: String,
    private val log: (String) -> Unit = {},
    private val bearerToken: () -> String?,
) {
    fun observe(seniorUserId: Long): Flow<ParentHomeUpdateEvent> = flow {
        require(seniorUserId > 0)
        var retryDelay = 1_000L
        while (currentCoroutineContext().isActive && bearerToken() != null) {
            val connectionStartedAt = System.nanoTime()
            emit(ParentHomeUpdateEvent.Connecting)
            log("WS connecting /ws")
            try {
                connection(seniorUserId).collect { event ->
                    emit(event)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: IOException) {
                // Transport/server failures do not clear authentication or the existing home.
            }
            emit(ParentHomeUpdateEvent.Disconnected)
            log("Disconnected")
            if (bearerToken() == null) break
            if (System.nanoTime() - connectionStartedAt >= 30_000_000_000L) retryDelay = 1_000L
            log("WS reconnect scheduled in ${retryDelay}ms")
            delay(retryDelay)
            retryDelay = (retryDelay * 2).coerceAtMost(30_000L)
        }
        log("WS observation stopped: no access token")
    }

    private fun connection(seniorUserId: Long): Flow<ParentHomeUpdateEvent> = callbackFlow {
        val token = bearerToken()
        if (token == null) {
            close()
            return@callbackFlow
        }
        val destination = "/topic/senior/$seniorUserId/home"
        val decoder = HomeStompDecoder()
        var subscribed = false
        val connectTimeout = launch {
            delay(20_000)
            log("STOMP CONNECTED timeout (20000ms)")
            close(IOException("STOMP connection timed out"))
        }
        val socket = socketFactory.newWebSocket(
            Request.Builder().url(url).header("Authorization", token).build(),
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    log("WS opened: HTTP ${response.code}")
                    // JWT authentication happens in the HTTP handshake, not STOMP CONNECT headers.
                    if (!webSocket.send(
                            "CONNECT\naccept-version:1.2\nhost:${url.toHttpUrl().host}\nheart-beat:0,0\n\n\u0000"
                        )) {
                        log("STOMP --> CONNECT enqueue failed")
                        close(IOException("Unable to send STOMP CONNECT"))
                    } else {
                        log("STOMP --> CONNECT queued (version=1.2)")
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) =
                    receive(webSocket, text.toByteArray(Charsets.UTF_8))

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) =
                    receive(webSocket, bytes.toByteArray())

                private fun receive(webSocket: WebSocket, bytes: ByteArray) {
                    if (!this@callbackFlow.isActive) return
                    // Never log raw frames: headers, bodies, and close reasons can contain private data.
                    log("WS <-- data (${bytes.size} bytes)")
                    try {
                        for (frame in decoder.append(bytes)) {
                            when (frame.command) {
                                "CONNECTED" -> if (!subscribed) {
                                    log("STOMP <-- CONNECTED")
                                    require(frame.headers["version"] == "1.2")
                                    check(webSocket.send(
                                        "SUBSCRIBE\nid:senior-home\ndestination:$destination\nack:auto\n\n\u0000"
                                    ))
                                    subscribed = true
                                    log("STOMP --> SUBSCRIBE queued (own home topic; no server receipt)")
                                    connectTimeout.cancel()
                                    // Spring's simple broker does not support subscription receipts.
                                    // Reconcile via HTTP after sending SUBSCRIBE on every connection.
                                    trySend(ParentHomeUpdateEvent.Subscribed)
                                }
                                "MESSAGE" -> if (
                                    subscribed && frame.headers["destination"] == destination &&
                                    frame.headers["subscription"] == "senior-home" &&
                                    frame.body.trim() == "HOME_UPDATED"
                                ) {
                                    log("STOMP <-- MESSAGE HOME_UPDATED accepted; requesting home refresh")
                                    trySend(ParentHomeUpdateEvent.HomeUpdated)
                                } else {
                                    log("STOMP <-- MESSAGE ignored (subscription/topic/event mismatch)")
                                }
                                "ERROR" -> {
                                    log("STOMP <-- ERROR (server rejected session; body omitted)")
                                    close(IOException("STOMP server rejected the session"))
                                }
                                else -> log("STOMP <-- unhandled frame (contents omitted)")
                            }
                        }
                    } catch (_: IllegalArgumentException) {
                        log("STOMP invalid frame or version")
                        close(IOException("Invalid STOMP frame"))
                    } catch (_: IllegalStateException) {
                        log("STOMP invalid session or SUBSCRIBE enqueue failed")
                        close(IOException("Invalid STOMP session"))
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    log("WS closing: code=$code")
                    webSocket.close(code, null)
                    close(IOException("WebSocket closed"))
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    log("WS closed: code=$code")
                    close()
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    log("WS failed: HTTP ${response?.code ?: "unavailable"}, type=${t.javaClass.simpleName}")
                    response?.body?.close()
                    close(IOException("Home WebSocket connection failed"))
                }
            },
        )
        awaitClose {
            log("WS cleanup: cancelling transport")
            connectTimeout.cancel()
            // Closing the transport also removes the server subscription. Cancel immediately
            // so leaving the home never leaves an old connection/retry alive.
            socket.cancel()
        }
    }
}
