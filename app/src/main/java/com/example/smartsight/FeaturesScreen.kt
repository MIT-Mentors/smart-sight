package com.example.smartsight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun FeaturesScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)), // Light gray background
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF9C27B0)) // Purple header
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "FEATURES",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // First Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FeatureButton("SOS")
            FeatureButton("Navigation")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Second Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Object Detection
            Button(
                onClick = { navController.navigate("objectDetection") },
                modifier = Modifier.size(120.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C27B0), // Purple background
                    contentColor = Color.White          // White text
                )
            ) {
                Text("Object Detection")
            }

            // Document Reading
            Button(
                onClick = { navController.navigate("documentReading") },
                modifier = Modifier.size(120.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C27B0), // Purple background
                    contentColor = Color.White          // White text
                )
            ) {
                Text("Document Reading")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Third Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FeatureButton("Location Sharing")
            FeatureButton("Coming Soon")
        }
    }
}

@Composable
fun FeatureButton(text: String) {
    Button(
        onClick = { /* No action for now */ },
        modifier = Modifier.size(120.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF9C27B0), // Purple background
            contentColor = Color.White          // White text
        )
    ) {
        Text(text = text, color = Color.White)
    }
}
