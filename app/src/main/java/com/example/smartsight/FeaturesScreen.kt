package com.example.smartsight

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun FeaturesScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFEFEF)), // Light gray background
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
                modifier = Modifier.clickable {
                    navController.navigate("DropDown")
                }
            )

            Text(
                text = "Features",
                fontSize = 20.sp,
                color = Color.Black
            )

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.Black, shape = RoundedCornerShape(4.dp))
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SOS Button - Center top
        FeatureButton(
            text = "SOS",
            icon = Icons.Default.Warning, // Emergency warning icon
            onClick = { /* SOS logic here */ }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Second Row - Navigation and Object Detection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FeatureButton(
                text = "Navigation",
                icon = Icons.Default.LocationOn,
                onClick = { /* Navigation logic here */ }
            )
            FeatureButton(
                text = "Object Detection",
                icon = Icons.Default.Search,
                onClick = { navController.navigate("objectDetection") }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Third Row - Document Reading and Location Sharing
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FeatureButton(
                text = "Document Reading",
                icon = Icons.Default.Description, // Document icon
                onClick = { navController.navigate("documentReading") }
            )
            FeatureButton(
                text = "Location Sharing",
                icon = Icons.Default.Share,
                onClick = { navController.navigate("Location") }
            )
        }
    }
}

@Composable
fun FeatureButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(120.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF9A7DFF), // Purple background
            contentColor = Color.White          // White text
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = text, color = Color.White)
        }
    }
}
