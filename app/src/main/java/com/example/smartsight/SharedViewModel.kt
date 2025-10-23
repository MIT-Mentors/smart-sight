package com.example.smartsight

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.telephony.SubscriptionManager
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    // Existing State
    private val _responseText = MutableStateFlow("Initializing...")
    val responseText: StateFlow<String> = _responseText.asStateFlow()

    private val _imageBitmap = MutableStateFlow<ImageBitmap?>(null)
    val imageBitmap: StateFlow<ImageBitmap?> = _imageBitmap.asStateFlow()

    // WebSocket Client
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
        val espIp = application.getString(R.string.ESP_IP)
        wsClient = ESPWebSocketClient(
            espIp = espIp,
            onConnectionChange = { _responseText.value = it },
            onTextResponse = { text ->
                // CHECK FOR THE "SOS button" MESSAGE
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

        // Load all contact lists
        val lists = loadLists(context)
        // Find the "Priority list"
        val priorityList = lists.firstOrNull() { it.name == "Priority list" }

        if (priorityList != null && priorityList.contacts.isNotEmpty()) {
            // Get the default SIM ID
            val subscriptionId = SubscriptionManager.getDefaultSmsSubscriptionId()
            if (subscriptionId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                Toast.makeText(context, "Cannot send SOS: Please set a default SIM for SMS.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Sending SOS to Priority List...", Toast.LENGTH_LONG).show()
                // Fetch the location
                fetchLocation(context, fusedLocationClient) { locationUrl ->
                    val message = "SOS! My location: $locationUrl"
                    // Loop and send SMS
                    priorityList.contacts.forEach { contactString ->
                        val number = parseNumber(contactString)
                        sendSMS(context, number, message, subscriptionId)
                    }
                }
            }
        } else {
            Toast.makeText(context, "Priority list is empty or not found.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        wsClient.close()
    }
}