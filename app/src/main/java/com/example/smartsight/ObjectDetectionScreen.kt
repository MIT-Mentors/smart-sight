package com.example.smartsight

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectDetectionScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        CenterAlignedTopAppBar(
            title = { Text("Object Detection", color = Color.White) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_media_previous),
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color(0xFF9C27B0)
            )
        )

        // Body Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Sample Image
            Image(
                painter = painterResource(id = R.drawable.sample_object), // Add image in drawable
                contentDescription = "Sample Object",
                modifier = Modifier
                    .size(300.dp)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Response Box (empty for now)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Response will appear here", color = Color.Gray)
            }
        }
    }
}
