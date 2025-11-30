package com.example.smartsight

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InitializeTTSTest {


    @Test
    fun testTTSInitialization() {

        val context = ApplicationProvider.getApplicationContext<Context>()
        var tts: TextToSpeech? = null

        val lock = Object()


        initializeTextToSpeech(context) { instance ->

            synchronized(lock) {
                tts = instance
                lock.notify()
            }
        }
        synchronized(lock) {
            lock.wait(4000) // Wait for a maximum of 4 seconds.
        }

        Assert.assertNotNull("TTS must be initialized", tts)

        tts?.shutdown()
    }
}
