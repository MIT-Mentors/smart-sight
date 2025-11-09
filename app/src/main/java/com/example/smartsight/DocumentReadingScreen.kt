package com.example.smartsight

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.*


 fun initializeTextToSpeech(
    context: Context,
    onInitialized: (TextToSpeech) -> Unit
) {
    lateinit var textToSpeech: TextToSpeech //

    textToSpeech = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {

            val result = textToSpeech.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The specified language is not supported!")
            }
            onInitialized(textToSpeech)
        } else {
            Log.e("TTS", "TTS Initialization failed!")
        }
    }
}

@Composable
fun DocumentReadingScreen(navController: NavController) {
    val context = LocalContext.current
    val scriptToRead = stringResource(id = R.string.speechScripts)
    var textToSpeech: TextToSpeech? by remember { mutableStateOf(null) }

    DisposableEffect(Unit) {
        initializeTextToSpeech(context) { textToSpeechInstance ->
            textToSpeech  = textToSpeechInstance
        }

        onDispose {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

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
            Text(text = "Document Reading", fontSize = 20.sp, color = Color.Black)
            Box(modifier = Modifier.size(28.dp).background(Color.Black, shape = RoundedCornerShape(4.dp)))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Display the text in a scrollable Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 32.dp)
                .background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(
                text = scriptToRead,
                color = Color.DarkGray,
                fontSize = 16.sp,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // "Read Aloud" button
        Button(
            onClick = {
                textToSpeech?.speak(scriptToRead, TextToSpeech.QUEUE_FLUSH, null, "")
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
            Text("Read Aloud", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

