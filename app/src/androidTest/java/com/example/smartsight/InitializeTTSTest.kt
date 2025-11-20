package com.example.smartsight

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InitializeTTSTest {

    @Test
    fun testTTSInitialization() {
        // Get a real context, since this is an instrumented test.
        val context = ApplicationProvider.getApplicationContext<Context>()
        var tts: TextToSpeech? = null

        // A lock object to synchronize the test thread and the callback thread.
        val lock = Object()

        // Call the asynchronous function.
        initializeTextToSpeech(context) { instance ->
            // This callback runs on a different thread upon success.
            synchronized(lock) {
                tts = instance
                lock.notify() // Wake up the waiting test thread.
            }
        }

        // Pause the main test thread and wait for the callback to finish.
        synchronized(lock) {
            lock.wait(4000) // Wait for a maximum of 4 seconds.
        }

        // Assert that the TTS instance was successfully assigned in the callback.
        assertNotNull("TTS must be initialized", tts)

        tts?.shutdown()
    }
}

