package com.example.smartsight

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartsight.ui.theme.SmartSightTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartSightTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SmartSightApp()
                }
            }
        }
    }
}

@Composable
fun SmartSightApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "features")
    {
        composable("statuspage") { MaterialTheme { AppScreen( onProceedToNextUi = {},navController)} }
        composable("features") { FeaturesScreen(navController) }
        composable("documentReading") { DocumentReadingScreen(navController) }
        composable("objectDetection") { ObjectDetectionScreen(navController) }
        composable(route = "Location"){ Location_Display(navController)}
        composable(route = "DropDown"){ DropDownScreen(navController)}
        composable(route = "AboutApp"){ About_Screen(navController)}
        composable("navigation") { NavigationScreen(navController) }
    }
}
