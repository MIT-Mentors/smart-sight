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

@Composable // List of button to navigate to different screens
fun DropDownScreen(navController: NavController){
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
            var sliderPosition by remember { mutableStateOf(0.5f) }
            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF9A7DFF), // Color of the draggable thumb
                    activeTrackColor = Color(0xFF9A7DFF), // Color of the track to the left of the thumb
                    inactiveTrackColor = Color.Gray, // Color of the track to the right of the thumb
                    activeTickColor = Color.Green, // Color of the ticks to the left of the thumb (if used)
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