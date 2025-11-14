package com.example.smartsight

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Data class must be defined here if it's not in a common file
data class ContactList(val name: String, val contacts: MutableList<String>)

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    // Existing State
    private val _responseText = MutableStateFlow("Initializing...")
    val responseText: StateFlow<String> = _responseText.asStateFlow()

    private val _imageBitmap = MutableStateFlow<ImageBitmap?>(null)
    val imageBitmap: StateFlow<ImageBitmap?> = _imageBitmap.asStateFlow()

    // WebSocket Client (assuming ESPWebSocketClient is in a separate file)
    private val wsClient: ESPWebSocketClient

    private val _sosPermissionRequest = MutableSharedFlow<List<String>>()
    val sosPermissionRequest = _sosPermissionRequest.asSharedFlow()

    // The ViewModel will own the location client
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    // List of permissions required for the SOS feature
    private val sosPermissions = listOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_PHONE_STATE
    )

    init {
        val espIp = application.getString(com.example.smartsight.R.string.ESP_IP)
        wsClient = ESPWebSocketClient(
            espIp = espIp,
            onConnectionChange = { _responseText.value = it },
            onTextResponse = { text ->
                // CHECK FOR THE "SOS button" MESSAGE (Trigger point)
                if (text == "SOS button") {
                    triggerSos()
                } else {
                    _responseText.value = text
                }
            },
            onImageReceived = { _imageBitmap.value = it },
            onError = { _responseText.value = it }
        )
    }

    fun sendMessage(message: String) {
        if (message == "capture") {
            _responseText.value = "Requesting image..."
        }
        wsClient.sendMessage(message)
    }

    fun updateResponseText(newText: String) {
        _responseText.value = newText
    }

    fun triggerSos() {
        if (checkSosPermissions()) {
            executeSosLogic()
        } else {
            viewModelScope.launch {
                _sosPermissionRequest.emit(sosPermissions)
            }
        }
    }

    private fun checkSosPermissions(): Boolean {
        val context = getApplication<Application>().applicationContext
        return sosPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    private fun executeSosLogic() {
        val context = getApplication<Application>().applicationContext

        // 1. Load all contact lists (uses loadLists implementation below)
        val lists = loadLists(context)
        // 2. Find the "Priority list"
        val priorityList = lists.firstOrNull() { it.name == "Priority list" }

        if (priorityList != null && priorityList.contacts.isNotEmpty()) {
            val subscriptionId = SubscriptionManager.getDefaultSmsSubscriptionId()
            if (subscriptionId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                Toast.makeText(context, "Cannot send SOS: Please set a default SIM for SMS.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Sending SOS to Priority List...", Toast.LENGTH_LONG).show()

                // 3. Fetch the location
                fetchLocation(context, fusedLocationClient) { locationUrl ->
                    val message = "SOS! My location: $locationUrl"

                    // 4. Loop and send SMS
                    priorityList.contacts.forEach { contactString ->
                        val number = parseNumber(contactString)
                        sendSMS(context, number, message, subscriptionId)
                    }
                }
            }
        } else {
            Toast.makeText(context, "Priority list is empty or not found. Please add contacts in Location Sharing.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        wsClient.close()
    }

    /**
     * Loads the contact lists from SharedPreferences, which is the source of truth.
     */
    private fun loadLists(context: Context): MutableList<ContactList> {
        val sharedPreferences = context.getSharedPreferences("SmartSightApp", Context.MODE_PRIVATE)
        val gson = Gson()
        val jsonString = sharedPreferences.getString("contact_lists", null)
        val loadedLists: MutableList<ContactList> = if (jsonString.isNullOrEmpty()) {
            mutableListOf()
        } else {
            // Use TypeToken to correctly deserialize the list of objects
            val type = object : TypeToken<MutableList<ContactList>>() {}.type
            gson.fromJson(jsonString, type)
        }
        // Ensure "Priority list" exists in the returned list for lookup
        if (loadedLists.none { it.name == "Priority list" }) {
            loadedLists.add(ContactList(name = "Priority list", contacts = mutableListOf()))
        }
        return loadedLists
    }

    /**
     * Implementation location fetching using FusedLocationProviderClient.
     */
    @SuppressLint("MissingPermission")
    private fun fetchLocation(context: Context, client: FusedLocationProviderClient, onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val cancellationTokenSource = CancellationTokenSource()

            val locationTask = client.getCurrentLocation(
                // Use the correct LocationRequest method for modern Android
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            )

            locationTask.addOnSuccessListener { location: Location? ->
                val locationUrl = if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    // Create the Google Maps link with coordinates
                    "https://maps.google.com/?q=${lat},${lon}"
                } else {
                    "Location not available."
                }
                onResult(locationUrl)
            }.addOnFailureListener { e ->
                Log.e("SOS", "Failed to get location: ${e.message}", e)
                onResult("Failed to get location.")
            }
        }
    }

    /**
     * Extracts the phone number from the contact string
     */
    private fun parseNumber(contact: String): String {
        // Logic must match what is used when contacts are saved in Location_Display.kt
        val numberPart = contact.substringAfterLast('(')
        val cleanNumber = numberPart.substringBeforeLast(')').filter { it.isDigit() || it == '+' }
        return cleanNumber
    }

    /**
     * Sends an SMS using SmsManager.
     */
    @SuppressLint("MissingPermission")
    private fun sendSMS(context: Context, number: String, message: String, subscriptionId: Int) {
        if (number.isBlank()) return

        try {
            // Use the subscription-aware SmsManager
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java).createForSubscriptionId(subscriptionId)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
            }

            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(number, null, parts, null, null)

            Log.i("SOS_SMS", "Message sent successfully to $number")
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "SOS sent to $number", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("SOS_SMS", "Failed to send message to $number: ${e.message}", e)
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "Failed to send SOS to $number", Toast.LENGTH_LONG).show()
            }
        }
    }
}