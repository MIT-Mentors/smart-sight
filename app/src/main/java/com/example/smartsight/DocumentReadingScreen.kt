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
fun DocumentReadingScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {

        // Top App Bar - without back button
        CenterAlignedTopAppBar(
            title = { Text("Document Reading", color = Color.White) },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color(0xFF9A7DFF) // Purple
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
            // Sample Image (fits half of the screen width)
            Image(
                painter = painterResource(id = R.drawable.sample_doc), // Add your image in drawable
                contentDescription = "Sample Document",
                modifier = Modifier
                    .fillMaxWidth()      // Fill the available width
                    .weight(1f)           // Take up half the vertical space dynamically
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Response Box
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
