package com.example.smartsight

import android.content.Context
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.*
import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

// Function to run ML Kit Image Labeling on the received Bitmap
fun runObjectLabeling(bitmap: Bitmap, responseText: MutableState<String>, context: Context) {
    // 1. Prepare InputImage for ML Kit
    val image = InputImage.fromBitmap(bitmap, 0)

    // 2. Set options: Using a higher confidence threshold (0.7f or 70%) to improve accuracy/relevance
    val options = ImageLabelerOptions.Builder()
        .setConfidenceThreshold(0.7f) // <--- IMPROVED ACCURACY: Only accept labels 70% sure or higher
        .build()

    val labeler = ImageLabeling.getClient(options)

    // 3. Process the image
    labeler.process(image)
        .addOnSuccessListener { resultLabels ->
            val result = if (resultLabels.isEmpty()) {
                "No highly confident objects/labels detected (confidence < 70%)."
            } else {
                "Detected Objects:\n" + resultLabels.joinToString("\n") {
                    "${it.text} (${(it.confidence * 100).toInt()}%)"
                }
            }
            // Update the response text with the detection results
            responseText.value = result
        }
        .addOnFailureListener { e ->
            responseText.value = "Detection failed: ${e.message}"
        }
}


@Composable
fun ObjectDetectionScreen(navController: NavController, espIp: String = stringResource(id = R.string.ESP_IP)) {

    val responseText = remember { mutableStateOf("Response will appear here") }
    val imageBitmap = remember { mutableStateOf<ImageBitmap?>(null) }

    // Get the context for ML Kit
    val context = LocalContext.current

    // Start WebSocket client, passing the ML Kit detection function as a callback
    val wsClient = remember {
        ESPWebSocketClient(
            espIp,
            responseText,
            imageBitmap,
            // Callback to run detection when a Bitmap is ready
            onBitmapReady = { bmp, response ->
                runObjectLabeling(bmp, response, context)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Header (Unchanged)
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

        // Image preview (Fills the Box and scales)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            if (imageBitmap.value != null) {
                // Image will fill the Box but maintain aspect ratio (ContentScale.Fit)
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

        // Detect Button (Unchanged)
        Button(
            onClick = {
                // Safe send: waits for WebSocket connection
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) { responseText.value = "Waiting for connection..." }
                    // Wait until WebSocket is connected
                    while (!wsClient.isConnected()) {
                        delay(100)
                    }
                    try {
                        wsClient.sendMessage("capture")
                        withContext(Dispatchers.Main) { responseText.value = "Requesting image..." }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            responseText.value = "Error sending capture: ${e.message}"
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

        // Response text (Unchanged)
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