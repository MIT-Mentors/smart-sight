package com.example.smartsight



import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu     // Example navigation icon
import androidx.compose.material3.* // For M3 components
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.text.style.TextAlign

// Make sure you have the Material 3 dependency in your app/build.gradle.kts (or build.gradle)
// implementation("androidx.compose.material3:material3:1.2.1") // Or latest stable/alpha version
// And the Compose BOM
// implementation(platform("androidx.compose:compose-bom:2024.09.00"))



@OptIn(ExperimentalMaterial3Api::class) // Opt-in for experimental Material 3 APIs
@Composable
fun NavigationScreen(navController: NavController) {
    var startLocation by remember { mutableStateOf("") }
    var stopLocation by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {

                    Text("Navigation", fontWeight = FontWeight.Bold,textAlign = TextAlign.Center)
                        },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("DropDown") }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF9A7DFF), // Example purple color
                    titleContentColor = Color.Black,
                    actionIconContentColor = Color.Black, // Color for action icons
                    navigationIconContentColor = Color.Black // Color for navigation icon
                )
            )
        }
    ) { innerPadding -> // Content padding provided by Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Apply the padding to your main content
                .background(Color(0xFFF0F0F0)) // Background for the content area
                .padding(16.dp) // Additional padding for content inside the Column
        ) {
            // Input Fields
            OutlinedTextField(
                value = startLocation,
                onValueChange = { startLocation = it },
                label = { Text("Start Location") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = stopLocation,
                onValueChange = { stopLocation = it },
                label = { Text("Stop Location") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { /* TODO: Handle Start clicked */ }) {
                    Text("Start")
                }
                Button(onClick = { /* TODO: Handle Stop clicked */ }) {
                    Text("Stop")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Info Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFF9A7DFF), RoundedCornerShape(12.dp)), // Lighter purple
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Distance - 50KM", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Icon(
                        painter = painterResource(R.drawable.baseline_arrow_forward_24), // Your existing arrow
                        contentDescription = "Direction",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Right turn in 100M", fontSize = 16.sp)
                }
            }
        }
    }
}


