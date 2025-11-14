package com.example.smartsight

// --- Android + ML Kit imports ---
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

// --- JUnit imports ---
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

/**
 * Instrumented test class for validating ML Kit Object Detection.
 *
 * This test runs directly on an Android device or emulator
 * (since ML Kit requires Android context, bitmaps, and Play Services).
 */
@RunWith(AndroidJUnit4::class)
class ObjectDetectionImageTest {

    // Context used to access resources and drawables within the Android test environment.
    private val context = ApplicationProvider.getApplicationContext<Context>()

    /**
     * Loads a drawable image by name, processes it through ML Kit's
     * Image Labeler, and returns the detection result string.
     *
     * @param drawableName The base name of the drawable (e.g. "clock" for R.drawable.clock)
     * @return Result text returned by the runObjectLabeling() function
     */
    private fun detectObjectsFromDrawable(drawableName: String): String {
        // Get the drawable resource ID dynamically by its name
        val resId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)

        // Decode the drawable into a Bitmap
        val inputStream = context.resources.openRawResource(resId)
        val bitmap = BitmapFactory.decodeStream(inputStream)

        // Create an InputImage for ML Kit from the Bitmap
        val image = InputImage.fromBitmap(bitmap, 0)

        // Configure ML Kit labeler with a minimum confidence threshold
        val labeler = ImageLabeling.getClient(
            ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.6f)
                .build()
        )

        // Used to wait for ML Kit’s async process to complete before returning
        var resultText = ""
        val latch = CountDownLatch(1)

        // Call the same detection function used in your main app
        runObjectLabeling(
            bitmap = bitmap,
            onResult = {
                // Callback called after ML Kit finishes detection
                resultText = it
                Log.d("MLKIT_TEST", "Detected: $it")
                latch.countDown()  // Release the latch so the test can continue
            },
            context = context,
            // No real TTS needed here — just log it instead
            speak = { Log.d("MLKIT_TEST", "Speak: $it") },
            labeler = labeler // Passing the labeler explicitly
        )

        // Block test execution until detection completes
        latch.await()
        return resultText
    }

    /**
     * Test 1 — Validate that ML Kit correctly detects a CLOCK.
     *
     * Steps:
     * 1. Loads the clock image from drawable.
     * 2. Runs object labeling.
     * 3. Asserts that the detected result contains the word "clock".
     */
    @Test
    fun detectClockImage_Success() {
        val result = detectObjectsFromDrawable("clock")
        Log.d("MLKIT_TEST", "Clock test result: $result")

        // Assertion: we expect ML Kit to identify it as a clock
        assertTrue(
            "Expected ML Kit to detect clock, but got: $result",
            result.contains("clock", ignoreCase = true)
        )
    }

    /**
     * Test 2 — Validate that ML Kit misidentifies a DOG image.
     *
     * Here, we deliberately use a DOG image.
     * and assert that the result does NOT contain "dog".
     *
     * This simulates a “failure” case or incorrect classification scenario.
     */
    @Test
    fun detectNonTableImage_FailureOrMismatch() {
        val result = detectObjectsFromDrawable("dog") // <- Place dog.jpg in drawable folder
        Log.d("MLKIT_TEST", "Non-dog test result: $result")

        // Assertion: ensure ML Kit does NOT say "table"
        assertFalse(
            "Expected ML Kit to misidentify or detect differently, but got: $result",
            result.contains("dog", ignoreCase = true)
        )
    }
}
