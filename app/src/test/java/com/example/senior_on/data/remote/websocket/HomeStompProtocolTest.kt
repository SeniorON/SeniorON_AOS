package com.example.senior_on.data.remote.websocket

import org.junit.Assert.*
import org.junit.Test

class HomeStompProtocolTest {
    @Test fun parsesSplitFramesAndHeartbeats() {
        val decoder = HomeStompDecoder()
        assertTrue(decoder.append("\n\r\nCONNECTED\nversion:1.2\n".toByteArray()).isEmpty())
        val frames = decoder.append("\n\u0000\nMESSAGE\ndestination:/topic/senior/1/home\n\nHOME_UPDATED\u0000".toByteArray())
        assertEquals(listOf("CONNECTED", "MESSAGE"), frames.map { it.command })
        assertEquals("HOME_UPDATED", frames.last().body)
    }

    @Test fun contentLengthUsesUtf8BytesAndSupportsEmbeddedNull() {
        val decoder = HomeStompDecoder()
        val body = "한\u0000글".toByteArray()
        val frame = "MESSAGE\r\ncontent-length:${body.size}\r\nx:test\\cvalue\\nnext\r\n\r\n".toByteArray() + body + byteArrayOf(0)
        assertTrue(decoder.append(frame.copyOfRange(0, frame.size - 2)).isEmpty())
        val result = decoder.append(frame.takeLast(2).toByteArray()).single()
        assertEquals("한\u0000글", result.body)
        assertEquals("test:value\nnext", result.headers["x"])
    }

    @Test fun duplicateHeadersUseFirstValue() {
        val frame = HomeStompDecoder().append("MESSAGE\nx:first\nx:second\n\nhello\u0000".toByteArray()).single()
        assertEquals("first", frame.headers["x"])
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsOversizedInput() {
        HomeStompDecoder().append(ByteArray(65_537))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidLength() {
        HomeStompDecoder().append("MESSAGE\ncontent-length:-1\n\nx\u0000".toByteArray())
    }
}
