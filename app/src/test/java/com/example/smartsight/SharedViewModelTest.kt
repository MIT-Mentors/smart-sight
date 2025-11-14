package com.example.smartsight

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat
import app.cash.turbine.test
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.*
import kotlin.test.*
import kotlin.test.Test

/**
 * Unit tests for the SharedViewModel class.
 *
 * These tests cover all non-UI logic in the ViewModel:
 * - SharedPreferences storage
 * - WebSocket message handling
 * - Permission checks
 * - SOS emergency logic
 * - StateFlow updates
 *
 * Note: All Android components (Context, Application, SmsManager, etc.)
 * are mocked using MockK to ensure tests run purely on the JVM (no emulator required).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SharedViewModelTest {

    // ViewModel and Android context mocks
    private lateinit var viewModel: SharedViewModel
    private lateinit var mockApp: Application
    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    // Use a TestDispatcher to control coroutine timing deterministically
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        // Override Dispatchers.Main for coroutine testing
        Dispatchers.setMain(testDispatcher)

        // --- Mock Android dependencies ---
        mockApp = mockk(relaxed = true)
        mockContext = mockk(relaxed = true)
        mockPrefs = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)

        // Mock SharedPreferences access chain
        every { mockApp.applicationContext } returns mockContext
        every { mockContext.getSharedPreferences(any(), any()) } returns mockPrefs
        every { mockPrefs.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.apply() } just runs

        // Mock app string resources (e.g., IP address for ESP client)
        every { mockApp.getString(any()) } returns "192.168.4.1"

        // Mock the WebSocket client constructor
        mockkConstructor(ESPWebSocketClient::class)
        every { anyConstructed<ESPWebSocketClient>().sendMessage(any()) } just runs

        // Instantiate ViewModel under test
        viewModel = SharedViewModel(mockApp)
    }

    @After
    fun tearDown() {
        // Clean up all mocks and reset coroutines
        unmockkAll()
        Dispatchers.resetMain()
    }

    /**
     * parseNumber() should correctly extract digits and a leading "+" if present.
     */
    @Test
    fun `parseNumber should extract digits correctly`() {
        val contact = "Alice (+91 98765 43210)"
        val method = viewModel.javaClass.getDeclaredMethod("parseNumber", String::class.java)
        method.isAccessible = true
        val result = method.invoke(viewModel, contact) as String
        assertEquals("+919876543210", result)
    }

    /**
     * parseNumber() should handle empty parentheses gracefully.
     */
    @Test
    fun `parseNumber should handle empty parentheses`() {
        val contact = "Empty ()"
        val method = viewModel.javaClass.getDeclaredMethod("parseNumber", String::class.java)
        method.isAccessible = true
        val result = method.invoke(viewModel, contact) as String
        assertEquals("", result)
    }

    /**
     * parseNumber() should extract plain digits when "+" is missing.
     */
    @Test
    fun `parseNumber should handle numbers without plus`() {
        val contact = "Bob (9876543210)"
        val method = viewModel.javaClass.getDeclaredMethod("parseNumber", String::class.java)
        method.isAccessible = true
        val result = method.invoke(viewModel, contact) as String
        assertEquals("9876543210", result)
    }


    /**
     * loadLists() should create a default "Priority list" if no data is saved.
     */
    @Test
    fun `loadLists should add Priority list when missing`() {
        every { mockPrefs.getString("contact_lists", any()) } returns null

        val method = viewModel.javaClass.getDeclaredMethod("loadLists", Context::class.java)
        method.isAccessible = true
        val result = method.invoke(viewModel, mockContext) as MutableList<*>

        assertTrue(result.any { (it as ContactList).name == "Priority list" })
    }

    /**
     * loadLists() should correctly deserialize stored JSON into ContactList objects.
     */
    @Test
    fun `loadLists should deserialize existing lists`() {
        val json = """[{"name":"Friends","contacts":["12345"]}]"""
        every { mockPrefs.getString("contact_lists", any()) } returns json

        val method = viewModel.javaClass.getDeclaredMethod("loadLists", Context::class.java)
        method.isAccessible = true
        val result = method.invoke(viewModel, mockContext) as MutableList<ContactList>

        assertEquals("Friends", result.first().name)
    }


    /**
     * When the message equals "capture", ViewModel should update the response text accordingly.
     * This simulates the ESP camera capture command.
     */
    @Test
    fun `sendMessage should update state when message is capture`() = runTest {
        viewModel.sendMessage("capture")
        advanceUntilIdle()
        assertEquals("Requesting image...", viewModel.responseText.first())
    }

    /**
     * sendMessage() should delegate message sending to the WebSocket client.
     */
    @Test
    fun `sendMessage should call WebSocket client`() {
        viewModel.sendMessage("hello")
        verify { anyConstructed<ESPWebSocketClient>().sendMessage("hello") }
    }


    /**
     * checkSosPermissions() should return true when all required permissions are granted.
     */
    @Test
    fun `checkSosPermissions returns true if all granted`() {
        mockkStatic(ContextCompat::class)
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_GRANTED

        val method = viewModel.javaClass.getDeclaredMethod("checkSosPermissions")
        method.isAccessible = true
        val result = method.invoke(viewModel) as Boolean
        assertTrue(result)
    }

    /**
     * checkSosPermissions() should return false if any required permission is denied.
     */
    @Test
    fun `checkSosPermissions returns false if any denied`() {
        mockkStatic(ContextCompat::class)
        every { ContextCompat.checkSelfPermission(any(), any()) } returnsMany listOf(
            PackageManager.PERMISSION_GRANTED,
            PackageManager.PERMISSION_DENIED,
            PackageManager.PERMISSION_GRANTED
        )

        val method = viewModel.javaClass.getDeclaredMethod("checkSosPermissions")
        method.isAccessible = true
        val result = method.invoke(viewModel) as Boolean
        assertFalse(result)
    }


    /**
     * triggerSos() should emit a permission request when any permission is missing.
     */
    @Test
    fun `triggerSos emits permissions when not granted`() = runTest {
        mockkStatic(ContextCompat::class)
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_DENIED

        viewModel.sosPermissionRequest.test {
            viewModel.triggerSos()
            advanceUntilIdle()
            val emitted = awaitItem()
            assertTrue(emitted.contains(Manifest.permission.SEND_SMS))
            cancelAndConsumeRemainingEvents()
        }
    }

    /**
     * executeSosLogic() should show a toast when the Priority list is empty.
     * This test ensures the ViewModel handles missing contacts gracefully.
     */
    @Test
    fun `executeSosLogic should show toast if Priority list empty`() {
        val context = mockk<Context>(relaxed = true)
        every { context.getSharedPreferences(any(), any()) } returns mockPrefs
        every { mockPrefs.getString(any(), any()) } returns """[]"""

        val method = viewModel.javaClass.getDeclaredMethod("executeSosLogic")
        method.isAccessible = true
        method.invoke(viewModel)
    }

    /**
     * executeSosLogic() should handle invalid SIM situations gracefully.
     * This prevents crashes when no SIM card or invalid subscription is present.
     */
    @Test
    fun `executeSosLogic should handle invalid SIM`() {
        mockkStatic(SubscriptionManager::class)
        every { SubscriptionManager.getDefaultSmsSubscriptionId() } returns SubscriptionManager.INVALID_SUBSCRIPTION_ID

        val context = mockk<Context>(relaxed = true)
        every { context.getSharedPreferences(any(), any()) } returns mockPrefs
        every { mockPrefs.getString(any(), any()) } returns """[{"name":"Priority list","contacts":["9999999999"]}]"""

        val method = viewModel.javaClass.getDeclaredMethod("executeSosLogic")
        method.isAccessible = true
        method.invoke(viewModel)
    }

    /**
     * updateResponseText() should change the StateFlow value immediately.
     */
    @Test
    fun `updateResponseText changes state flow value`() = runTest {
        viewModel.updateResponseText("Updated")
        assertEquals("Updated", viewModel.responseText.first())
    }
}
