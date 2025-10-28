package com.example.smartsight

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer

class ESPWebSocketClient(
    espIp: String,
    // Use callbacks to send data out to the ViewModel
    private val onConnectionChange: (String) -> Unit,
    private val onTextResponse: (String) -> Unit,
    private val onImageReceived: (ImageBitmap) -> Unit,
    private val onError: (String) -> Unit
) {
    private var connected = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private val wsClient: WebSocketClient

    init {
        val wsUri = URI("ws://$espIp:8888/")
        wsClient = object : WebSocketClient(wsUri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                connected = true
                Log.i("ESPWebSocketClient", "Connected to ESP32 at $espIp")
                updateUi { onConnectionChange("Connected to ESP32!") }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                connected = false
                Log.w("ESPWebSocketClient", "Disconnected: $reason (code=$code)")
                updateUi { onConnectionChange("Disconnected from ESP32") }
            }

            override fun onError(ex: Exception?) {
                connected = false
                Log.e("ESPWebSocketClient", "WebSocket error", ex)
                updateUi { onError("WebSocket error: ${ex?.message}") }
            }

            override fun onMessage(message: String?) {
                Log.d("ESPWebSocketClient", "Received text: $message")
                updateUi { onTextResponse(message ?: "Empty text message") }
            }

            override fun onMessage(bytes: ByteBuffer) {
                try {
                    val byteArray = ByteArray(bytes.remaining())
                    bytes.get(byteArray)

                    val bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    if (bmp != null) {
                        Log.i("ESPWebSocketClient", "Received image (${byteArray.size} bytes)")
                        updateUi {
                            onImageReceived(bmp.asImageBitmap())
                            onTextResponse("Image received!") // Also update text
                        }
                    } else {
                        Log.w("ESPWebSocketClient", "Failed to decode image (${byteArray.size} bytes)")
                        updateUi { onTextResponse("Failed to decode image") }
                    }
                } catch (e: Exception) {
                    Log.e("ESPWebSocketClient", "Error decoding image", e)
                    updateUi { onError("Image decode error: ${e.message}") }
                }
            }
        }

        // Start connecting immediately
        Log.i("ESPWebSocketClient", "Attempting to connect...")
        updateUi { onConnectionChange("Connecting to ESP32...") }
        wsClient.connect()
    }

    fun isConnected(): Boolean = connected

    fun sendMessage(message: String) {
        if (connected) {
            wsClient.send(message)
            Log.d("ESPWebSocketClient", "Sent message: $message")
        } else {
            Log.w("ESPWebSocketClient", "Cannot send, WebSocket not connected yet!")
            updateUi { onError("WebSocket not connected yet!") }
        }
    }

    // Call this when the ViewModel is cleared
    fun close() {
        connected = false
        wsClient.close()
        Log.i("ESPWebSocketClient", "WebSocket connection closed.")
    }

    private fun updateUi(action: () -> Unit) {
        mainHandler.post(action)
    }
}