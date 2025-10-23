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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.Manifest
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ObjectDetectionScreen(
    navController: NavController,
    viewModel: SharedViewModel = viewModel()
){

    // collect the state from the ViewModel.
    val responseText by viewModel.responseText.collectAsState()
    val imageBitmap by viewModel.imageBitmap.collectAsState()
    val sosPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE
        )
    )
    LaunchedEffect(Unit) {
        viewModel.sosPermissionRequest.collect { permissionsToRequest ->
            // When the ViewModel sends an event, launch the permission dialog
            sosPermissionsState.launchMultiplePermissionRequest()
        }
    }

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
            // Use the imageBitmap from the ViewModel
            if (imageBitmap != null) {
                Image(bitmap = imageBitmap!!, contentDescription = "Object Detection Image")
            } else {
                Text("No image yet", color = Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Detect Button
        Button(
            onClick = {
                viewModel.sendMessage("capture")
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
            Text(text = responseText, color = Color.DarkGray, fontSize = 16.sp)
        }
    }
}