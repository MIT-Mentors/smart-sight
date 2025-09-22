package com.example.smartsight

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.*

@Composable
fun ObjectDetectionScreen(navController: NavController, espIp: String = "10.186.240.82") {

    val responseText = remember { mutableStateOf("Response will appear here") }
    val imageBitmap = remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    // Start WebSocket client
    val wsClient = remember { ESPWebSocketClient(espIp, responseText, imageBitmap) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Header
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

        // Image preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            if (imageBitmap.value != null) {
                Image(bitmap = imageBitmap.value!!, contentDescription = "Object Detection Image")
            } else {
                Text("No image yet", color = Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Detect Button
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

        // Response text
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
