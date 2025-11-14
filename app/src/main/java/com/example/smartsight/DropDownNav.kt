package com.example.smartsight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import kotlin.math.roundToInt

@Composable // List of buttons to navigate to different screens
fun DropDownScreen(navController: NavController){
    //  Get Context and AudioManager
    val context = LocalContext.current
    val audioManager = remember {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    //  Get max volume for the "Music" stream
    val maxVolume = remember {
        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }

    // Create a state for the current volume
    var currentVolume by remember {
        mutableStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
    }

    // Create a state for the slider's position (a float between 0.0 and 1.0)
    var sliderPosition by remember(currentVolume) {
        // This recalculates the position when currentVolume changes
        mutableStateOf(currentVolume.toFloat() / maxVolume.toFloat())
    }

    // Listen for system volume changes (physical buttons)
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == "android.media.VOLUME_CHANGED_ACTION") {
                    // Update our volume state when the system volume changes
                    val newVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    currentVolume = newVolume
                }
            }
        }

        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        context.registerReceiver(receiver, filter)

        // Unregister the receiver when the composable is disposed
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFEFEFEF)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        // Home Button
        Button(onClick = {
            navController.navigate("features")},
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF9A7DFF))
        ){
            Text(text = "Home", color = Color.Black)

        }
        Spacer(modifier = Modifier.height(20.dp))
        //  Battery Status
        Button(onClick = {/* ToDo: Implement SOS logic here */},
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF9A7DFF))){
            Text(text = "Battery - 100%",color = Color.Black)

        }
        Spacer(modifier = Modifier.height(20.dp))

        // Slider for Volume Control
        Column(
            modifier = Modifier
                .background(Color(0xFFE6E6FA))
                .padding(12.dp)
        ) {
            Text("Volume", modifier = Modifier.padding(start = 10.dp),color = Color.Black)
            Slider(
                value = sliderPosition, // Use our derived slider position
                onValueChange = { newPosition ->

                    sliderPosition = newPosition

                    // Convert the float position (0.0 to 1.0) back to an Int volume
                    val newVolume = (newPosition * maxVolume).roundToInt()

                    // Set the system volume
                    // The '0' flag means we don't want to show the system UI
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)

                    // Update our current volume state
                    currentVolume = newVolume
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF9A7DFF),
                    activeTrackColor = Color(0xFF9A7DFF),
                    inactiveTrackColor = Color.Gray,
                    activeTickColor = Color.Green,
                    inactiveTickColor = Color.LightGray)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        // About Screen
        Button(onClick = {
            navController.navigate("AboutApp") },
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF9A7DFF))){
            Text(text = "About",color = Color.Black)

        }
    }}