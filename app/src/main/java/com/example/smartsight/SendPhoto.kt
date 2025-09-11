package com.example.smartsight

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.asImageBitmap
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer

class ESPWebSocketClient(
    espIp: String,
    val responseText: MutableState<String>,
    val imageBitmap: MutableState<androidx.compose.ui.graphics.ImageBitmap?>
) {
    private var connected = false
    val wsClient: WebSocketClient

    init {
        val wsUri = URI("ws://$espIp:8888/")
        wsClient = object : WebSocketClient(wsUri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                connected = true
                responseText.value = "Connected to ESP32!"
            }
            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                connected = false
                responseText.value = "Disconnected from ESP32"
            }
            override fun onError(ex: Exception?) {
                connected = false
                responseText.value = "WebSocket error: ${ex?.message}"
            }
            override fun onMessage(message: String?) { /* handle text */ }
            override fun onMessage(bytes: java.nio.ByteBuffer) { /* handle image */ }
        }
        wsClient.connect()
    }

    fun isConnected(): Boolean = connected

    fun sendMessage(message: String) {
        if (connected) wsClient.send(message)
        else responseText.value = "WebSocket not connected yet!"
    }
}
