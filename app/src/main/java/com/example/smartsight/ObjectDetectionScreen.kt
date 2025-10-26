package com.example.smartsight

import android.content.Context
import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.*
import java.util.*

// Function to run ML Kit Image Labeling on the received Bitmap
fun runObjectLabeling(
    bitmap: Bitmap,
    responseText: MutableState<String>,
    context: Context,
    speak: (String) -> Unit
) {
    // 1. Prepare InputImage for ML Kit
    val image = InputImage.fromBitmap(bitmap, 0)
    // 2. Set options: Using a higher confidence threshold (0.7f or 70%) to improve accuracy/relevance
    val options = ImageLabelerOptions.Builder()
        .setConfidenceThreshold(0.7f)
        .build()

    val labeler = ImageLabeling.getClient(options)
    // 3. Process the image

    labeler.process(image)
        .addOnSuccessListener { resultLabels ->
            val result = if (resultLabels.isEmpty()) {
                "No highly confident objects detected."
            } else {
                "Detected objects are " + resultLabels.joinToString(", ") {
                    "${it.text} (${(it.confidence * 100).toInt()}%)"
                }
            }
            responseText.value = result
            speak(result)
        }
        .addOnFailureListener { e ->
            val errorMsg = "Detection failed: ${e.message}"
            responseText.value = errorMsg
            speak(errorMsg)
        }
}

@Composable
fun ObjectDetectionScreen(
    navController: NavController,
    espIp: String = stringResource(id = R.string.ESP_IP)
) {
    val responseText = remember { mutableStateOf("Response will appear here") }
    val imageBitmap = remember { mutableStateOf<ImageBitmap?>(null) }
    val context = LocalContext.current
    var tts: TextToSpeech? by remember { mutableStateOf(null) }

    // Manages the TextToSpeech lifecycle, creating it and shutting it down safely.
    DisposableEffect(context) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    val speak: (String) -> Unit = { text ->
        if (text.isNotEmpty()) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    // Start WebSocket client, passing the ML Kit detection function as a callback
    val wsClient = remember {
        ESPWebSocketClient(
            espIp,
            responseText,
            imageBitmap,
            onBitmapReady = { bmp, response ->
                runObjectLabeling(bmp, response, context, speak)
            }
        )
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF9A7DFF))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color.Black,
                modifier = Modifier.clickable { navController.navigate("DropDown") }
            )
            Text(text = "Object Detection", fontSize = 20.sp, color = Color.Black)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.Black, shape = RoundedCornerShape(4.dp))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            if (imageBitmap.value != null) {
                Image(
                    bitmap = imageBitmap.value!!,
                    contentDescription = "Object Detection Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text("No image yet", color = Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Use a coroutine on a background thread for networking to avoid freezing the UI.
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        withContext(Dispatchers.Main) { responseText.value = "Connecting..." }

                        if (!wsClient.isOpen()) {
                            wsClient.reconnect()
                        }

                        // Wait for connection with a 5-second timeout to prevent an infinite loop.
                        var timeout = 0
                        while (!wsClient.isOpen() && timeout < 50) {
                            delay(100)
                            timeout++
                        }
                        // Final check: Only proceed if the connection was successful.
                        if (wsClient.isOpen()) {
                                    wsClient.sendMessage("capture")
                                    withContext(Dispatchers.Main) {
                                        responseText.value = "Requesting image..."
                                        speak("Requesting image from ESP32 camera")
                                    }
                                }
                        else {
                            withContext(Dispatchers.Main) {
                                responseText.value = "Connection failed. Please try again."
                                speak("Connection failed. Please try again.")
                                    }
                                }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            responseText.value = "Error: ${e.message}"
                            speak("Error: ${e.message}")
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9A7DFF),
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(50.dp)
        ) {
            Text("Detect", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(100.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(text = responseText.value, color = Color.DarkGray, fontSize = 16.sp)
        }
    }
}
