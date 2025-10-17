package com.example.smartsight

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ContactList(val name: String, val contacts: MutableList<String>)

// Helper function to fetch contacts from the device
fun fetchContacts(contentResolver: ContentResolver): List<String> {
    val contactsList = mutableListOf<String>()
    val cursor = contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null, null, null, null
    )
    cursor?.use {
        val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        while (it.moveToNext()) {
            val name = it.getString(nameIdx)
            val number = it.getString(numberIdx)
            contactsList.add("$name ($number)")
        }
    }
    return contactsList
}

// More robust helper function to extract the phone number from a "Name (Number)" string
fun parseNumber(contact: String): String {
    val numberPart = contact.substringAfterLast('(', "")
    return numberPart.filter { it.isDigit() }
}

// This function sends SMS directly using a specific SIM card (subscriptionId)
fun sendSMS(context: Context, number: String, message: String, subscriptionId: Int) {
    if (number.isBlank()) return
    try {
        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java).createForSubscriptionId(subscriptionId)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
        }
        smsManager.sendTextMessage(number, null, message, null, null)
        Toast.makeText(context, "SMS sent to $number", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to send SMS to $number", Toast.LENGTH_LONG).show()
        e.printStackTrace()
    }
}

// Fetches the current location and triggers a callback with the Google Maps URL
@SuppressLint("MissingPermission") // Permissions are checked before this is called
fun fetchLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationFetched: (locationUrl: String) -> Unit
) {
    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude
                val gmapLink = "https://maps.google.com/?q=${lat},${lon}"
                onLocationFetched(gmapLink)
            } else {
                Toast.makeText(context, "Could not get location. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to get location: ${it.message}", Toast.LENGTH_SHORT).show()
        }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Location_Display(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }
    var showContactsDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val lists = remember { loadLists(context).toMutableStateList() }
    var contacts by remember { mutableStateOf<List<String>>(emptyList()) }
    val selectedContacts = remember { mutableStateListOf<String>() }
    var viewedList by remember { mutableStateOf<ContactList?>(null) }
    val currentList = viewedList
    var isCreatingList by remember { mutableStateOf(false) }
    var isQuickSendMode by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE
        )
    )
    // Action to share location via SMS to a specific list of contacts
    @SuppressLint("MissingPermission")
    val shareAction: (ContactList) -> Unit = { contactList ->
        if (permissionsState.allPermissionsGranted) {
            if (contactList.contacts.isNotEmpty()) {
                val subscriptionId = SubscriptionManager.getDefaultSmsSubscriptionId()
                if (subscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                    Toast.makeText(context, "Sending message to ${contactList.name}...", Toast.LENGTH_LONG).show()
                    fetchLocation(context, fusedLocationClient) { locationUrl ->
                        val message = "My location: $locationUrl"
                        contactList.contacts.forEach { contactString ->
                            val number = parseNumber(contactString)
                            sendSMS(context, number, message, subscriptionId)
                        }
                    }
                } else {
                    Toast.makeText(context, "Cannot send: Please set a default SIM for SMS.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "${contactList.name} has no contacts.", Toast.LENGTH_SHORT).show()
            }
        } else {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFEFEF))
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF9A7DFF))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.Black, modifier = Modifier.clickable { navController.navigate("DropDown") })
            Text(text = "Location Sharing", fontSize = 20.sp, color = Color.Black)
            Box(modifier = Modifier.size(28.dp).background(Color.Black, shape = RoundedCornerShape(4.dp)))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dynamic Lists
        Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp)) {
            lists.forEach { listObject ->
                Spacer(modifier = Modifier.height(8.dp))
                ListButton(
                    list = listObject,
                    onListClicked = { selectedList -> viewedList = selectedList },
                    onShareClicked = { shareAction(listObject) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = {
                    if (permissionsState.permissions.first { it.permission == Manifest.permission.READ_CONTACTS }.status.isGranted) {
                        contacts = fetchContacts(context.contentResolver)
                        isCreatingList = true
                        showContactsDialog = true
                    } else {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                },
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF9A7DFF))
            ) {
                Text("Create List", color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Contact Selector
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    if (permissionsState.permissions.first { it.permission == Manifest.permission.READ_CONTACTS }.status.isGranted) {
                        contacts = fetchContacts(context.contentResolver)
                        isQuickSendMode = true
                        showContactsDialog = true
                    } else {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF9A7DFF))
            ) {
                Text("Select a Contact", color = Color.Black)
            }
        }
    }

    // --- Dialogs ---

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Create List") },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("Enter list name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newListName.isNotBlank()) {
                        val newList = ContactList(name = newListName, contacts = selectedContacts.toMutableList())
                        lists.add(newList)
                        saveLists(context, lists)
                        newListName = ""
                        selectedContacts.clear()
                    }
                    showDialog = false
                    isCreatingList = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    newListName = ""
                    showDialog = false
                    isCreatingList = false
                }) { Text("Cancel") }
            }
        )
    }

    if (currentList != null) {
        AlertDialog(
            onDismissRequest = { viewedList = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = currentList.name)
                    IconButton(onClick = {
                        lists.remove(currentList)
                        saveLists(context, lists)
                        viewedList = null
                    }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete List")
                    }
                }
            },
            text = {
                LazyColumn {
                    items(currentList.contacts) { contact ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(contact, modifier = Modifier.padding(vertical = 4.dp))
                            IconButton(onClick = {
                                val listToUpdate = lists.find { it.name == currentList.name }
                                listToUpdate?.contacts?.remove(contact)
                                saveLists(context, lists)
                                viewedList = listToUpdate?.copy()
                            }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove Contact")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewedList = null }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    if (permissionsState.permissions.first { it.permission == Manifest.permission.READ_CONTACTS }.status.isGranted) {
                        contacts = fetchContacts(context.contentResolver)
                        isCreatingList = false
                        showContactsDialog = true
                    } else {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                }) { Text("Add Contact") }
            }
        )
    }

    // --- Contacts Dialog with Search ---
    if (showContactsDialog) {
        var searchQuery by remember { mutableStateOf("") }
        val filteredContacts = contacts.filter { it.contains(searchQuery, ignoreCase = true) }

        AlertDialog(
            onDismissRequest = { showContactsDialog = false },
            title = { Text("Select a Contact") },
            text = {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search contacts") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    LazyColumn(modifier = Modifier.height(300.dp).fillMaxWidth()) {
                        items(filteredContacts) { contact ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (selectedContacts.contains(contact)) selectedContacts.remove(contact)
                                        else selectedContacts.add(contact)
                                    }
                                    .padding(8.dp)
                            ) {
                                Checkbox(
                                    checked = selectedContacts.contains(contact),
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) selectedContacts.add(contact)
                                        else selectedContacts.remove(contact)
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = contact)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (isCreatingList) {
                        showContactsDialog = false
                        showDialog = true
                    } else if (isQuickSendMode) {
                        if (permissionsState.allPermissionsGranted) {
                            showContactsDialog = false
                            val subscriptionId = SubscriptionManager.getDefaultSmsSubscriptionId()
                            if (subscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                                Toast.makeText(context, "Sending location to selected contacts...", Toast.LENGTH_SHORT).show()
                                fetchLocation(context, fusedLocationClient) { locationUrl ->
                                    val message = "My location: $locationUrl"
                                    selectedContacts.forEach { contactString ->
                                        val number = parseNumber(contactString)
                                        sendSMS(context, number, message, subscriptionId)
                                    }
                                    selectedContacts.clear()
                                    isQuickSendMode = false
                                }
                            } else {
                                Toast.makeText(context, "No default SIM for SMS found.", Toast.LENGTH_LONG).show()
                                isQuickSendMode = false
                            }
                        } else {
                            permissionsState.launchMultiplePermissionRequest()
                        }
                    } else {
                        val listToUpdate = lists.find { it.name == viewedList?.name }
                        listToUpdate?.contacts?.addAll(selectedContacts)
                        saveLists(context, lists)
                        selectedContacts.clear()
                        showContactsDialog = false
                        viewedList = listToUpdate?.copy()
                    }
                }) {
                    Text("Confirm")
                }
            }
        )
    }
}

@Composable
fun ListButton(list: ContactList, onListClicked: (ContactList) -> Unit, onShareClicked: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(
            onClick = { onListClicked(list) },
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF9A7DFF))
        ) {
            Text(list.name, color = Color.Black)
        }
        Spacer(modifier = Modifier.width(6.dp))
        IconButton(onClick = onShareClicked) {
            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.Black)
        }
    }
}

fun saveLists(context: Context, lists: List<ContactList>) {
    val sharedPreferences = context.getSharedPreferences("SmartSightApp", Context.MODE_PRIVATE)
    val gson = Gson()
    val jsonString = gson.toJson(lists)
    sharedPreferences.edit().putString("contact_lists", jsonString).apply()
}

fun loadLists(context: Context): MutableList<ContactList> {
    val sharedPreferences = context.getSharedPreferences("SmartSightApp", Context.MODE_PRIVATE)
    val gson = Gson()
    val jsonString = sharedPreferences.getString("contact_lists", null)
    val loadedLists: MutableList<ContactList> = if (jsonString.isNullOrEmpty()) {
        mutableListOf()
    } else {
        val type = object : TypeToken<MutableList<ContactList>>() {}.type
        gson.fromJson(jsonString, type)
    }
    if (loadedLists.isEmpty()) {
        loadedLists.add(ContactList(name = "Priority list", contacts = mutableListOf()))
    }
    return loadedLists
}
