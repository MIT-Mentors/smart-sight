package com.example.smartsight

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color

const val APP_TAG = "SmartSightApp"

@Composable
fun AppScreen(navController: NavController) {
    val context = LocalContext.current

    var btConnected by remember { mutableStateOf(false) }
    var netConnected by remember { mutableStateOf(false) }
    var batteryPct by remember { mutableIntStateOf(0) }
    var conditionsMetForNavigation by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = context) {
        Log.d(APP_TAG, "AppScreen: Initializing states.")
        btConnected = isBluetoothReadyAndDevicePaired(context) // Using revised function
        netConnected = isInternetConnected(context)
        batteryPct = batteryPercent(context)
        conditionsMetForNavigation = btConnected && netConnected
        Log.d(APP_TAG, "AppScreen: Initial states set - BT: $btConnected, Net: $netConnected, Nav: $conditionsMetForNavigation")
    }

    LaunchedEffect(Unit) {
        Log.d(APP_TAG, "AppScreen: Polling_Loop started.")
        while (true) {
            val currentBtStatus = isBluetoothReadyAndDevicePaired(context) // Using revised function
            val currentNetStatus = isInternetConnected(context)
            val currentBatteryStatus = batteryPercent(context)

            if (btConnected != currentBtStatus) {
                btConnected = currentBtStatus
                Log.i(APP_TAG, "AppScreen: Bluetooth status changed to: $currentBtStatus")
            }
            if (netConnected != currentNetStatus) {
                netConnected = currentNetStatus
                Log.i(APP_TAG, "AppScreen: Network status changed to: $currentNetStatus")
            }
            if (batteryPct != currentBatteryStatus) {
                batteryPct = currentBatteryStatus
            }

            val shouldNavigateNow = currentBtStatus && currentNetStatus
            if (conditionsMetForNavigation != shouldNavigateNow) {
                conditionsMetForNavigation = shouldNavigateNow
                Log.i(APP_TAG, "AppScreen: conditionsMetForNavigation changed to: $shouldNavigateNow")
            }
            delay(1500)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        if (conditionsMetForNavigation) {
            LaunchedEffect(Unit) {
                Log.i(APP_TAG, "AppScreen: Nav_Conditions_Met. Attempting to navigate to 'features'.")
                try {
                    navController.navigate("features") {
                        launchSingleTop = true
                    }
                    Log.i(APP_TAG, "AppScreen: Navigation to 'features' initiated successfully.")
                } catch (e: Exception) {
                    Log.e(APP_TAG, "AppScreen: CRITICAL - Exception during navigation to 'features'", e)
                }
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Loading features...")
                }
            }
        } else {
            StatusUi(btConnected, netConnected, batteryPct)
        }
    }
}

@Composable
private fun StatusUi(btConnected: Boolean, netConnected: Boolean, batteryPct: Int) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(
                // Ensure these drawables match your desired icons for "connected" vs "disabled/not connected"
                if (btConnected) R.drawable.outline_bluetooth_connected_24
                else R.drawable.baseline_bluetooth_disabled_24
            ),
            contentDescription = "Bluetooth Status",
            modifier = Modifier.size(84.dp)
        )
        Text(
            text = if (btConnected) "Bluetooth Ready" else "Bluetooth Not Ready", // Text updated
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )

        Image(
            painter = painterResource(
                if (netConnected) R.drawable.outline_android_wifi_3_bar_24
                else R.drawable.outline_android_wifi_3_bar_off_24
            ),
            contentDescription = "Internet Status",
            modifier = Modifier.size(84.dp)
        )
        Text(
            text = if (netConnected) "Internet Connected" else "Internet Not Connected",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )

        Image(
            painter = painterResource(R.drawable.battery),
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


/**
 * Checks if Bluetooth is enabled and if there's at least one bonded (paired) device.
 * This is a common simplified check for "Bluetooth readiness."
 * Requires BLUETOOTH_CONNECT permission on Android 12+ for accessing bondedDevices.
 */
fun isBluetoothReadyAndDevicePaired(context: Context): Boolean {
    Log.d(APP_TAG, "BluetoothCheck: Starting isBluetoothReadyAndDevicePaired")
    try {
        // Get BluetoothAdapter directly if possible, or via BluetoothManager
        val bluetoothAdapter: BluetoothAdapter? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            bluetoothManager?.adapter
        } else {
            @Suppress("DEPRECATION")
            BluetoothAdapter.getDefaultAdapter()
        }

        if (bluetoothAdapter == null) {
            Log.w(APP_TAG, "BluetoothCheck: BluetoothAdapter is null.")
            return false
        }

        if (!bluetoothAdapter.isEnabled) {
            Log.d(APP_TAG, "BluetoothCheck: Bluetooth is disabled.")
            return false
        }
        Log.d(APP_TAG, "BluetoothCheck: Bluetooth is enabled.")

        // Runtime permission check for Android S (API 31) and above is needed to access bondedDevices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(APP_TAG, "BluetoothCheck: BLUETOOTH_CONNECT permission not granted on S+. Cannot check bonded devices.")
                // Depending on requirements, you might return true if adapter is enabled but can't check devices,
                // or false because a full check isn't possible. For "device paired", false is safer.
                return false
            }
            Log.d(APP_TAG, "BluetoothCheck: BLUETOOTH_CONNECT permission is granted on S+ for bonded devices check.")
        }

        // Check for bonded devices. This requires BLUETOOTH (pre-S) or BLUETOOTH_CONNECT (S+)
        val bondedDevices = bluetoothAdapter.bondedDevices
        if (bondedDevices != null && bondedDevices.isNotEmpty()) {
            Log.i(APP_TAG, "BluetoothCheck: At least one device is bonded (paired). Count: ${bondedDevices.size}")
            return true
        } else {
            Log.d(APP_TAG, "BluetoothCheck: No devices are bonded (paired).")
            return false
        }

    } catch (e: SecurityException) {
        // This could happen if permissions are revoked or if there's an OS-level issue
        Log.e(APP_TAG, "BluetoothCheck: SecurityException in isBluetoothReadyAndDevicePaired", e)
        return false
    } catch (e: Throwable) {
        Log.e(APP_TAG, "BluetoothCheck: Unexpected Throwable in isBluetoothReadyAndDevicePaired", e)
        return false
    }
}

// Internet connection
fun isInternetConnected(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        ?: return false

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } else {
        @Suppress("DEPRECATION")
        val activeNetworkInfo = cm.activeNetworkInfo
        @Suppress("DEPRECATION")
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}
// Phone's battery percentage
fun batteryPercent(context: Context): Int {
    val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager?
    return bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.coerceIn(0, 100) ?: 0
}