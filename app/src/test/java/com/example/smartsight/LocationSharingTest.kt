package com.example.smartsight

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.location.Location
import android.os.Build
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import io.mockk.*
import org.junit.*
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for the LocationSharing utility functions.
 *
 * These tests focus on non-UI logic such as:
 * - Parsing phone numbers
 * - Reading and saving contact lists
 * - Sending SMS messages
 * - Fetching location and handling success/failure gracefully
 *
 * All Android components are mocked using MockK to allow
 * JVM-only testing (no device or emulator needed).
 */
@Config(sdk = [Build.VERSION_CODES.S])
class LocationSharingTest {

    private lateinit var context: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    @Before
    fun setup() {
        // Prepare mock context and shared preferences before each test
        context = mockk(relaxed = true)
        mockPrefs = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)

        // Mock SharedPreferences read/write chain
        every { context.getSharedPreferences(any(), any()) } returns mockPrefs
        every { mockPrefs.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.apply() } just runs
    }

    /**
     * parseNumber() should correctly extract numeric digits from a formatted string.
     */
    @Test
    fun `parseNumber extracts digits correctly`() {
        val input = "John (+91 98765 43210)"
        val result = parseNumber(input)
        assertEquals("919876543210", result)
    }

    /**
     * parseNumber() should handle empty strings gracefully and return an empty result.
     */
    @Test
    fun `parseNumber handles empty input gracefully`() {
        val input = ""
        val result = parseNumber(input)
        assertEquals("", result)
    }

    /**
     * parseNumber() should return empty when there are no parentheses or digits to extract.
     */
    @Test
    fun `parseNumber handles no parentheses`() {
        val input = "Alice 12345"
        val result = parseNumber(input)
        assertEquals("", result)
    }

    /**
     * fetchContacts() should query the content resolver and return formatted contact names.
     */
    @Test
    fun `fetchContacts returns formatted list`() {
        val resolver = mockk<ContentResolver>()
        val cursor = mockk<Cursor>(relaxed = true)

        // Mock the query() call to return our fake cursor
        every {
            resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null
            )
        } returns cursor

        // Simulate one contact record in the cursor
        every { cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME) } returns 0
        every { cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER) } returns 1
        every { cursor.moveToNext() } returnsMany listOf(true, false)
        every { cursor.getString(0) } returns "John"
        every { cursor.getString(1) } returns "12345"
        every { cursor.close() } just runs

        val result = fetchContacts(resolver)

        // Verify formatted contact output
        assertEquals(listOf("John (12345)"), result)
    }


    /**
     * saveLists() should correctly serialize contact lists to JSON and save to SharedPreferences.
     */
    @Test
    fun `saveLists stores json in SharedPreferences`() {
        val lists = listOf(ContactList("Friends", mutableListOf("12345")))
        saveLists(context, lists)
        verify { mockEditor.putString(eq("contact_lists"), match { it.contains("Friends") }) }
        verify { mockEditor.apply() }
    }

    /**
     * loadLists() should return a default 'Priority list' if no data is found in SharedPreferences.
     */
    @Test
    fun `loadLists returns empty priority list if prefs empty`() {
        every { mockPrefs.getString("contact_lists", null) } returns null
        val result = loadLists(context)
        assertTrue(result.any { it.name == "Priority list" })
    }

    /**
     * loadLists() should correctly parse stored JSON data into ContactList objects.
     */
    @Test
    fun `loadLists deserializes stored json`() {
        val json = Gson().toJson(listOf(ContactList("Team", mutableListOf("11111"))))
        every { mockPrefs.getString("contact_lists", null) } returns json

        val result = loadLists(context)
        assertEquals("Team", result.first().name)
    }


    /**
     * sendSMS() should do nothing (no Toast or SMS) when the phone number is blank.
     */
    @Test
    fun `sendSMS should not send when number blank`() {
        mockkStatic(Toast::class)
        every {
            Toast.makeText(
                any<Context>(),
                ofType(CharSequence::class),
                any<Int>()
            )
        } returns mockk(relaxed = true)

        sendSMS(context, "", "Hello", 1)

        // Verify that Toast.makeText() was never called
        verify(exactly = 0) {
            Toast.makeText(any<Context>(), ofType(CharSequence::class), any<Int>())
        }
    }

    /**
     * sendSMS() should catch exceptions and display a 'Failed' toast message.
     */
    @Test
    fun `sendSMS shows failure toast on exception`() {
        mockkStatic(Toast::class)
        val smsManager = mockk<SmsManager>()
        mockkStatic(SmsManager::class)

        // Mock SmsManager throwing an exception during creation
        every { context.getSystemService(SmsManager::class.java) } returns smsManager
        every { smsManager.createForSubscriptionId(any()) } throws RuntimeException("SIM error")

        every {
            Toast.makeText(
                any<Context>(),
                ofType(CharSequence::class),
                any<Int>()
            )
        } returns mockk(relaxed = true)

        sendSMS(context, "9876543210", "Test", 1)

        // Verify that a 'Failed' Toast was displayed
        verify {
            Toast.makeText(
                context,
                match<CharSequence> { it.contains("Failed") },
                Toast.LENGTH_LONG
            )
        }
    }

    /**
     * fetchLocation() should invoke the callback with a valid Google Maps URL
     * when location data is successfully retrieved.
     */
    @Test
    fun `fetchLocation calls callback with google maps link`() {
        val client = mockk<FusedLocationProviderClient>()
        val task = mockk<Task<Location>>(relaxed = true)
        val location = mockk<Location>()

        // Simulate successful location retrieval
        every { location.latitude } returns 12.345
        every { location.longitude } returns 67.890
        every { client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null) } returns task

        every { task.addOnSuccessListener(any()) } answers {
            val listener = args[0] as OnSuccessListener<Location>
            listener.onSuccess(location)
            task
        }

        var capturedUrl: String? = null
        fetchLocation(context, client) { capturedUrl = it }

        // Verify correct map link is passed
        assertNotNull(capturedUrl)
        assertTrue(capturedUrl!!.startsWith("https://maps.google.com"))
    }

    /**
     * fetchLocation() should show a toast when location data is null.
     */
    @Test
    fun `fetchLocation handles null location gracefully`() {
        val client = mockk<FusedLocationProviderClient>()
        val task = mockk<Task<Location>>(relaxed = true)

        // Simulate success but null location
        every { client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null) } returns task
        every { task.addOnSuccessListener(any()) } answers {
            val listener = args[0] as OnSuccessListener<Location>
            listener.onSuccess(null)
            task
        }

        mockkStatic(Toast::class)
        every {
            Toast.makeText(any<Context>(), ofType(CharSequence::class), any<Int>())
        } returns mockk(relaxed = true)

        fetchLocation(context, client) {}

        // Verify "Could not get location" toast displayed
        verify {
            Toast.makeText(
                context,
                match<CharSequence> { it.contains("Could not get location") },
                Toast.LENGTH_SHORT
            )
        }
    }

    /**
     * fetchLocation() should show a toast when a failure occurs in location retrieval.
     */
    @Test
    fun `fetchLocation shows failure toast on error`() {
        val client = mockk<FusedLocationProviderClient>()
        val task = mockk<Task<Location>>(relaxed = true)

        every { client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null) } returns task

        // Simulate a failure callback
        every { task.addOnFailureListener(any()) } answers {
            val listener = args[0] as OnFailureListener
            listener.onFailure(RuntimeException("Location failed"))
            task
        }

        mockkStatic(Toast::class)
        every {
            Toast.makeText(any<Context>(), ofType(CharSequence::class), any<Int>())
        } returns mockk(relaxed = true)

        fetchLocation(context, client) {}

        // Verify toast message for failure
        verify {
            Toast.makeText(
                context,
                match<CharSequence> { it.contains("Failed to get location") },
                Toast.LENGTH_SHORT
            )
        }
    }
}
