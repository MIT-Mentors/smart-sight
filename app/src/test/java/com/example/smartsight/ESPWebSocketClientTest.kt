package com.example.smartsight

import androidx.compose.ui.graphics.ImageBitmap
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.nio.ByteBuffer
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for the ESPWebSocketClient class.
 *
 * These tests validate the non-UI behavior of the WebSocket client, ensuring
 * it correctly manages its connection state, safely handles invalid messages,
 * and remains stable even when the connection is not established.
 */
class ESPWebSocketClientTest {

    // --- Mocked callbacks passed into ESPWebSocketClient
    private val onConnectionChange: (String) -> Unit = mockk(relaxed = true)
    private val onTextResponse: (String) -> Unit = mockk(relaxed = true)
    private val onImageReceived: (ImageBitmap) -> Unit = mockk(relaxed = true)
    private val onError: (String) -> Unit = mockk(relaxed = true)

    // Instance under test
    private lateinit var client: ESPWebSocketClient

    @Before
    fun setup() {
        // Create a new instance of the client before each test
        // with mocked callback functions to avoid side effects.
        client = ESPWebSocketClient(
            espIp = "127.0.0.1",
            onConnectionChange = onConnectionChange,
            onTextResponse = onTextResponse,
            onImageReceived = onImageReceived,
            onError = onError
        )
    }

    /**
     * Test that a new client starts in a disconnected state.
     * This ensures that the internal "connected" flag defaults to false.
     */
    @Test
    fun `client starts disconnected`() {
        assertFalse(client.isConnected())
    }

    /**
     * Test that sendMessage() does not crash or throw when the client
     * is not connected. This simulates a safe no-op when there is
     * no active WebSocket connection.
     */
    @Test
    fun `sendMessage doesn't crash when not connected`() {
        client.sendMessage("test")
        assertFalse(client.isConnected())
    }

    /**
     * Test that calling close() sets the connected flag to false.
     * This ensures proper state cleanup when the connection is closed.
     */
    @Test
    fun `close sets connected to false`() {
        client.close()
        assertFalse(client.isConnected())
    }

    /**
     * Test that isConnected() correctly reflects the internal state.
     * Here we manually toggle the private "connected" field to true
     * using reflection, and then verify that isConnected() reports true.
     */
    @Test
    fun `isConnected reflects true when manually toggled`() {
        val field = client.javaClass.getDeclaredField("connected")
        field.isAccessible = true
        field.set(client, true)
        assertTrue(client.isConnected())
    }

    /**
     * Test that onMessage(ByteBuffer) handles invalid or corrupted image data gracefully.
     *
     * The method decodes incoming binary WebSocket data into an image.
     * This test provides invalid bytes that cannot form a valid image, and verifies
     * that the function handles it without throwing exceptions or crashing.
     *
     * Reflection is used here to invoke the protected "onMessage(ByteBuffer)" method,
     * which normally would only be called internally by the WebSocketClient library.
     */
    @Test
    fun `onMessage ByteBuffer handles image decode errors gracefully`() {
        val invalidBytes = ByteArray(10) { 0 } // Simulate an invalid/corrupt image payload

        // Access and invoke the private "onMessage(ByteBuffer)" method using reflection
        val method = client.javaClass.getDeclaredMethod("onMessage", ByteBuffer::class.java)
        method.isAccessible = true

        // The call should not throw any exceptions even with invalid data
        method.invoke(client, ByteBuffer.wrap(invalidBytes))
    }
}
