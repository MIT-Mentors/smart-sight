package com.example.smartsight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ObjectDetectionScreen(navController: NavController) {
    // State to hold the detected objects
    var responseText by remember { mutableStateOf("Response will appear here") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF9A7DFF))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Object Detection",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sample Image (fills half of the screen with no gaps)
        Image(
            painter = painterResource(id = R.drawable.sample_object), // Replace with your actual image name
            contentDescription = "Object Detection Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(horizontal = 0.dp) // Removes extra gaps
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Detect Button
        Button(
            onClick = {
                // Example detected objects response
                responseText = "Detected Objects: Car, Traffic Sign, Tree, Road"
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9A7DFF), // Purple button
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

        // Response Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(100.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = responseText,
                color = Color.DarkGray,
                fontSize = 16.sp
            )
        }
    }
}
