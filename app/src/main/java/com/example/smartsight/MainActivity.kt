package com.example.smartsight



import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartsight.ui.theme.SmartSightTheme // Assuming you have a theme

class MainActivity : ComponentActivity() {

    // Launcher for BLUETOOTH_CONNECT permission request
    private val requestBluetoothConnectPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("Permissions", "BLUETOOTH_CONNECT permission granted.")
                // You might want to trigger a re-check of bluetooth status here if needed,
                // but the polling in AppScreen should pick it up.
            } else {
                Log.w("Permissions", "BLUETOOTH_CONNECT permission denied.")
                // Handle the case where the user denies the permission if critical
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request BLUETOOTH_CONNECT on Android 12+ if not already granted at app start
        requestBluetoothPermissionIfNeeded()

        setContent {
            SmartSightTheme { // Apply your app's theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "status_screen") {
                        composable("status_screen") {
                            // Call the AppScreen from statuspage.kt
                            AppScreen(navController = navController)
                        }
                        composable("features") {
                            // Replace with your actual FeaturesScreen composable
                            FeaturesScreen(navController = navController)
                        }
                        // Add other destinations as needed
                        composable("documentReading") { DocumentReadingScreen(navController) }
                        composable("objectDetection") { Surface(modifier = Modifier.fillMaxSize(), color = Color.White){ObjectDetectionScreen(navController) }}
                        composable(route = "Location"){ Location_Display(navController)}
                        composable(route = "DropDown"){ DropDownScreen(navController)}
                        composable(route = "AboutApp"){ About_Screen(navController)}
                        composable("navigation") { NavigationScreen(navController) }
                    }
                }
            }
        }
    }

    private fun requestBluetoothPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.i("Permissions", "BLUETOOTH_CONNECT already granted.")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_CONNECT) -> {
                    Log.w("Permissions", "BLUETOOTH_CONNECT rationale should be shown.")
                    // Optionally show a dialog explaining why you need the permission
                    // then launch the permission request. For now, just request.
                    requestBluetoothConnectPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                }
                else -> {
                    // Directly request the permission
                    requestBluetoothConnectPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                }
            }
        }
    }
}

// Example FeaturesScreen (should be in its own file or this file)


/*  @Composable
fun SmartSightApp() {
  val navController = rememberNavController()

  NavHost(navController = navController, startDestination = "features")
  {
      composable("statuspage") { MaterialTheme { AppScreen( navController)} }
      composable("features") { FeaturesScreen(navController) }
      composable("documentReading") { DocumentReadingScreen(navController) }
      composable("objectDetection") { ObjectDetectionScreen(navController) }
      composable(route = "Location"){ Location_Display(navController)}
      composable(route = "DropDown"){ DropDownScreen(navController)}
      composable(route = "AboutApp"){ About_Screen(navController)}
      composable("navigation") { NavigationScreen(navController) }
  }
}
*/


