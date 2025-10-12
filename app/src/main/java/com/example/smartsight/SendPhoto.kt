package com.example.smartsight

import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.asImageBitmap
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer

class ESPWebSocketClient(
    espIp: String,
    private val responseText: MutableState<String>,
    private val imageBitmap: MutableState<androidx.compose.ui.graphics.ImageBitmap?>
) {
    private var connected = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private val wsClient: WebSocketClient
    //Websocket initialization
    init {
        val wsUri = URI("ws://$espIp:8888/")
        wsClient = object : WebSocketClient(wsUri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                connected = true
                Log.i("ESPWebSocketClient", "Connected to ESP32 at $espIp")
                updateUi { responseText.value = "Connected to ESP32!" }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                connected = false
                Log.w("ESPWebSocketClient", "Disconnected: $reason (code=$code)")
                updateUi { responseText.value = "Disconnected from ESP32" }
            }

            override fun onError(ex: Exception?) {
                connected = false
                Log.e("ESPWebSocketClient", "WebSocket error", ex)
                updateUi { responseText.value = "WebSocket error: ${ex?.message}" }
            }

            override fun onMessage(message: String?) {
                Log.d("ESPWebSocketClient", "Received text: $message")
                updateUi { responseText.value = message ?: "Empty text message" }
            }

            override fun onMessage(bytes: ByteBuffer) {
                try {
                    val byteArray = ByteArray(bytes.remaining())
                    bytes.get(byteArray)

                    val bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    if (bmp != null) {
                        Log.i("ESPWebSocketClient", "Received image (${byteArray.size} bytes)")
                        updateUi {
                            imageBitmap.value = bmp.asImageBitmap()
                            responseText.value = "Image received!"
                        }
                    } else {
                        Log.w("ESPWebSocketClient", "Failed to decode image (${byteArray.size} bytes)")
                        updateUi { responseText.value = "Failed to decode image" }
                    }
                } catch (e: Exception) {
                    Log.e("ESPWebSocketClient", "Error decoding image", e)
                    updateUi { responseText.value = "Image decode error: ${e.message}" }
                }
            }
        }
        wsClient.connect()
    }

    fun isConnected(): Boolean = connected
    // Send message to ESP to request an image
    fun sendMessage(message: String) {
        if (connected) {
            wsClient.send(message)
            Log.d("ESPWebSocketClient", "Sent message: $message")
        } else {
            updateUi { responseText.value = "WebSocket not connected yet!" }
        }
    }

    private fun updateUi(action: () -> Unit) {
        // Run updates safely on the main (UI) thread
        mainHandler.post { action() }
    }
}
