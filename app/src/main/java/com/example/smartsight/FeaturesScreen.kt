package com.example.smartsight

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FeaturesScreen(
    navController: NavController,
    viewModel: SharedViewModel = viewModel()
){
        // Required Permissions
        val permissionsState = rememberMultiplePermissionsState(
            permissions = listOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_PHONE_STATE
            )
        )

        // Listen for permission request "events" from the ViewModel
        LaunchedEffect(Unit) {
            viewModel.sosPermissionRequest.collect { permissionsToRequest ->
                // When the ViewModel sends an event, launch the permission dialog
                permissionsState.launchMultiplePermissionRequest()
            }
        }
        val sosAction = {
            viewModel.triggerSos()
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFEFEF)),
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
            Text(text = "Features", fontSize = 20.sp, color = Color.Black)
            Box(modifier = Modifier.size(28.dp).background(Color.Black, shape = RoundedCornerShape(4.dp)))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SOS Button
        FeatureButton(
            text = "SOS",
            icon = Icons.Default.Warning,
            onClick = { sosAction() } // Hook up the SOS logic here
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
                onClick = { navController.navigate("navigation")  }
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
                icon = Icons.Default.Description,
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

// Reusable Button UI
@Composable
fun FeatureButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(120.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF9A7DFF),
            contentColor = Color.White
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = text, color = Color.White, textAlign = TextAlign.Center)
        }
    }
}