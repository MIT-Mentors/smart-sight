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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import android.content.Context
import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.util.*

/**
 * Runs ML Kit Image Labeling on the given Bitmap and executes a callback with the result.
 *
 * @param bitmap The image to process.
 * @param onResult Callback function to update the ViewModel's response text.
 * @param context Android Context.
 * @param speak Function to trigger TextToSpeech.
 */
fun runObjectLabeling(
    bitmap: Bitmap,
    onResult: (String) -> Unit,
    context: Context,
    speak: (String) -> Unit
) {
    // 1. Prepare InputImage for ML Kit
    val image = InputImage.fromBitmap(bitmap, 0)
    // 2. Set options: Using a confidence threshold (0.7f or 70%)
    val options = ImageLabelerOptions.Builder()
        .setConfidenceThreshold(0.7f)
        .build()

    val labeler = ImageLabeling.getClient(options)

    // 3. Process the image
    labeler.process(image)
        .addOnSuccessListener { resultLabels ->
            val result = if (resultLabels.isEmpty()) {
                "No highly confident objects detected."
            } else {
                "Detected objects are " + resultLabels.joinToString(", ") {
                    // Example: "cat (95%)"
                    "${it.text} (${(it.confidence * 100).toInt()}%)"
                }
            }
            onResult(result)
            speak(result)
        }
        .addOnFailureListener { e ->
            val errorMsg = "Detection failed: ${e.message}"
            onResult(errorMsg)
            speak(errorMsg)
        }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ObjectDetectionScreen(
    navController: NavController,
    viewModel: SharedViewModel = viewModel()
){
    // Collect state from the ViewModel
    val responseText by viewModel.responseText.collectAsState()
    val imageBitmap by viewModel.imageBitmap.collectAsState()

    // Permission state for SOS feature
    val sosPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE
        )
    )

    // LaunchedEffect to request SOS permissions when the ViewModel emits an event
    LaunchedEffect(Unit) {
        viewModel.sosPermissionRequest.collect {
            // When the ViewModel sends an event, launch the permission dialog
            sosPermissionsState.launchMultiplePermissionRequest()
        }
    }

    val context = LocalContext.current
    var tts: TextToSpeech? by remember { mutableStateOf(null) }

    // Manages the TextToSpeech lifecycle (initialization and shutdown)
    DisposableEffect(context) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    // Function to speak the given text
    val speak: (String) -> Unit = { text ->
        if (text.isNotEmpty()) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    // --- INTEGRATION: TRIGGER ML KIT ON NEW IMAGE ---
    // This effect triggers when a new image is received from the ViewModel
    LaunchedEffect(imageBitmap) {
        imageBitmap?.let { composeBitmap ->
            // 1. Convert Compose ImageBitmap to Android Bitmap for ML Kit
            val androidBitmap = composeBitmap.asAndroidBitmap()

            // 2. Run the object labeling and speak the result
            runObjectLabeling(
                bitmap = androidBitmap,
                onResult = { resultText ->
                    // 3. Update the ViewModel's state with the result text
                    viewModel.updateResponseText(resultText)
                },
                context = context,
                speak = speak
            )
        }
    }
    // ------------------------------------------------

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
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap!!,
                    contentDescription = "Object Detection Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text("No image yet", color = Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Detect Button
        Button(
            onClick = {
                // Send "capture" message to ESP32 via ViewModel
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

        // Response text (updated by ML Kit callback via ViewModel)
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