package com.example.smartsight

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer

class ESPWebSocketClient(
    espIp: String,
    private val responseText: MutableState<String>,
    private val imageBitmap: MutableState<ImageBitmap?>,
    private val onBitmapReady: (Bitmap, MutableState<String>) -> Unit
) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val webSocketClient: WebSocketClient

    init {
        val wsUri = URI("ws://$espIp:8888/")
        webSocketClient = object : WebSocketClient(wsUri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.i("ESPWebSocketClient", "Connected to ESP32 at $espIp")
                updateUi { responseText.value = "Connected to ESP32!" }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.w("ESPWebSocketClient", "Disconnected: $reason (code=$code)")
                updateUi { responseText.value = "Disconnected from ESP32" }
            }

            override fun onError(ex: Exception?) {
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
                            responseText.value = "Image received. Analyzing objects..."
                            onBitmapReady(bmp, responseText)
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
        // Attempt to connect when the object is created.
        webSocketClient.connect()
    }


   //Checks if the WebSocket connection is currently open.
     // This is the function that fixes the "Unresolved reference" error.

    fun isOpen(): Boolean {
        return webSocketClient.isOpen
    }


     //Checks if the WebSocket connection is currently closed.

    fun isClosed(): Boolean {
        return webSocketClient.isClosed
    }


     // Tells the WebSocket client to attempt to reconnect.
     // This will be used in the Button's onClick logic.

    fun reconnect() {
        webSocketClient.reconnect()
    }


    // Sends a message to the ESP32 to request an image.
     // It now uses the new isOpen() function for a more reliable check.

    fun sendMessage(message: String) {
        if (isOpen()) {
            webSocketClient.send(message)
            Log.d("ESPWebSocketClient", "Sent message: $message")
        } else {
            Log.w("ESPWebSocketClient", "Attempted to send message, but WebSocket is not connected.")
            // The UI will handle showing a "connecting..." message, so no need to update it here.
        }
    }

    private fun updateUi(action: () -> Unit) {
        // Run updates safely on the main (UI) thread
        mainHandler.post { action() }
    }
}
