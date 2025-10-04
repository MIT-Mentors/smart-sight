package com.example.smartsight

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FeaturesScreen(navController: NavController) {
    val context = LocalContext.current
    // Set up location client and permissions, just like in the other screen
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE
        )
    )

    // Define the SOS action logic
    @SuppressLint("MissingPermission")
    val sosAction = {
        // First, check if all necessary permissions have been granted
        if (permissionsState.allPermissionsGranted) {
            // Load all contact lists
            val lists = loadLists(context)
            // Find the "Priority list", which is created by default if it doesn't exist
            val priorityList = lists.firstOrNull() { it.name == "Priority list" }

            if (priorityList != null && priorityList.contacts.isNotEmpty()) {
                // Get the default SIM ID for sending SMS
                val subscriptionId = SubscriptionManager.getDefaultSmsSubscriptionId()
                if (subscriptionId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                    Toast.makeText(context, "Cannot send SOS: Please set a default SIM for SMS.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Sending SOS to Priority List...", Toast.LENGTH_LONG).show()
                    // Fetch the location
                    fetchLocation(context, fusedLocationClient) { locationUrl ->
                        val message = "SOS! My location: $locationUrl"
                        // Loop through every contact in the priority list and send the SMS
                        priorityList.contacts.forEach { contactString ->
                            val number = parseNumber(contactString)
                            sendSMS(context, number, message, subscriptionId)
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Priority list is empty or not found.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // If permissions are not granted, request them from the user
            permissionsState.launchMultiplePermissionRequest()
        }
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