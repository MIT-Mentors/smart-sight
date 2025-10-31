package com.example.smartsight

import androidx.compose.ui.graphics.ImageBitmap
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ESPWebSocketClientTest {

    private lateinit var client: ESPWebSocketClient
    private var connectionMessage = ""
    private var textResponse = ""
    private var imageReceived = false
    private var errorMessage = ""

    @Before
    fun setup() {
        client = ESPWebSocketClient(
            espIp = "192.168.4.1",
            onConnectionChange = { connectionMessage = it },
            onTextResponse = { textResponse = it },
            onImageReceived = { _: ImageBitmap -> imageReceived = true },
            onError = { errorMessage = it }
        )
    }

    @Test
    fun testConnectionStatus_isFalseInitially() {
        assertFalse(client.isConnected())
    }

    @Test
    fun testSendMessage_withoutConnection_showsError() {
        client.sendMessage("test")
        assertTrue(errorMessage.contains("WebSocket not connected"))
    }
}
