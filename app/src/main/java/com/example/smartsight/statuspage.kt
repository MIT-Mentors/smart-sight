package com.example.smartsight



import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController

import kotlinx.coroutines.delay



    // Launcher for BLUETOOTH_CONNECT permission request









@Composable
 fun AppScreen(onProceedToNextUi: () -> Unit,navController: NavController) {
    val context = LocalContext.current

    // UI States for different statuses
    var btConnected by remember { mutableStateOf(isActualBluetoothDeviceConnected(context)) }
    var netConnected by remember { mutableStateOf(isInternetConnected(context)) }
    var batteryPct by remember { mutableIntStateOf(batteryPercent(context)) }
    var goNext by remember { mutableStateOf(false) } // Controls navigation to NextUi

    // Periodically poll for status updates
    LaunchedEffect(Unit) {
        while (true) {
            val currentBtStatus = isActualBluetoothDeviceConnected(context)
            val currentNetStatus = isInternetConnected(context)
            val currentBatteryStatus = batteryPercent(context)

            // Update states if changed
            if (btConnected != currentBtStatus) btConnected = currentBtStatus
            if (netConnected != currentNetStatus) netConnected = currentNetStatus
            if (batteryPct != currentBatteryStatus) batteryPct = currentBatteryStatus

            // Update navigation flag
            val shouldGoNext = currentBtStatus && currentNetStatus
            if (goNext != shouldGoNext) {
                goNext = shouldGoNext
                if (shouldGoNext) onProceedToNextUi() // Callback when ready to proceed
            }
            delay(1500) // Polling interval: 1.5 seconds
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        if (goNext) {
            NextUi() // Show next screen if conditions are met
        } else {
            StatusUi(btConnected, netConnected, batteryPct) // Show status screen
        }
    }
}

@Composable
fun StatusUi(btConnected: Boolean, netConnected: Boolean, batteryPct: Int) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Bluetooth Status
        Image(
            painter = painterResource(if (btConnected) R.drawable.outline_bluetooth_connected_24
            else R.drawable.baseline_bluetooth_disabled_24),
            contentDescription = "Bluetooth Status",
            modifier = Modifier.size(84.dp)
        )
        Text(
            text = if (btConnected) "Device Connected" else "Device Not Connected",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )

        // Internet Status
        Image(
            painter = painterResource(if (netConnected) R.drawable.outline_android_wifi_3_bar_24 else R.drawable.outline_android_wifi_3_bar_off_24),
            contentDescription = "Internet Status",
            modifier = Modifier.size(84.dp)
        )
        Text(
            text = if (netConnected) "Internet Connected" else "Internet Not Connected",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )

        // Battery Status
        Image(
            painter = painterResource(R.drawable.bt),
            contentDescription = "Battery Status",
            modifier = Modifier.size(84.dp)
        )
        Text(
            text = "Battery $batteryPct%",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
 fun NextUi() {
    // Placeholder for the screen shown after checks pass
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("All Set!", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Bluetooth & Internet are connected.")
        }
    }
}

/* ---------- Helper Functions ---------- */

// Checks if a Bluetooth device is actively connected.
// Requires BLUETOOTH_CONNECT permission on Android 12+.
@SuppressLint("MissingPermission")
 fun isActualBluetoothDeviceConnected(context: Context): Boolean {
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
    val bluetoothAdapter = bluetoothManager?.adapter
    if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return false

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
    }
    val relevantProfiles = listOf(BluetoothProfile.A2DP, BluetoothProfile.HEADSET, BluetoothProfile.GATT)
    for (profile in relevantProfiles) {
        try {
            if (bluetoothManager.getConnectedDevices(profile).isNotEmpty()) return true
        } catch (e: SecurityException) { return false }
    }
    return false
}

// Checks for active internet connection.
// Requires ACCESS_NETWORK_STATE permission.
@SuppressLint("MissingPermission")
 fun isInternetConnected(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager? ?: return false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    } else {
        @Suppress("DEPRECATION")
        val activeNetworkInfo = cm.activeNetworkInfo
        @Suppress("DEPRECATION")
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}

// Gets current battery percentage.
 fun batteryPercent(context: Context): Int {
    val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).coerceIn(0, 100)
}



