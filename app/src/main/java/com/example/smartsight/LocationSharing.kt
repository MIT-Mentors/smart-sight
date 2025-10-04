package com.example.smartsight

import android.Manifest
import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.navigation.NavController
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.material.icons.filled.Delete
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts



data class ContactList(val name: String, val contacts: MutableList<String>)


// Helper function to fetch contacts
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
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Permission was granted by the user, now we can fetch contacts
                // Note: We might need to trigger the action again
            } else {
                // Permission was denied
            }
        }
    )

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
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color.Black,
                modifier = Modifier.clickable {
                    navController.navigate("DropDown")
                }
            )

            Text(
                text = "Location Sharing",
                fontSize = 20.sp,
                color = Color.Black
            )

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.Black, shape = RoundedCornerShape(4.dp))
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dynamic Lists
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
        ) {
            lists.forEach { listObject ->
                Spacer(modifier = Modifier.height(8.dp))
                // Pass the list and the function to run onClick
                ListButton(
                    list = listObject,
                    onListClicked = { selectedList ->
                        viewedList = selectedList
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Create list button
            OutlinedButton(
                onClick = {
                    val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                        contacts = fetchContacts(context.contentResolver)

                        isCreatingList = true
                        showContactsDialog = true // This now opens the contacts dialog
                    }else {
                        // Permission has not been granted, launch the request
                        launcher.launch(Manifest.permission.READ_CONTACTS)
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    val permission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_CONTACTS
                    )
                    if (permission == PermissionChecker.PERMISSION_GRANTED) {
                        contacts = fetchContacts(context.contentResolver)
                        showContactsDialog = true
                    } else {
                        launcher.launch(Manifest.permission.READ_CONTACTS)
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF9A7DFF))
            ) {
                Text("Select a Contact",color = Color.Black)
            }
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(onClick = { }) {
                Icon(Icons.Default.Share, contentDescription = "Share",tint = Color.Black,)
            }
        }
    }

    // Create List Dialog
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
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    newListName = ""
                    showDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog to show contacts in a list
    if (currentList != null) {
        AlertDialog(
            onDismissRequest = { viewedList = null },
            // Use the safe copy for the title
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = currentList.name)
                    IconButton(onClick = {
                        // 1. Remove the list from our main list
                        lists.remove(currentList)

                        // 2. Save the changes to storage
                        saveLists(context, lists)

                        // 3. Close the dialog
                        viewedList = null
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete List"
                        )
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
                                // 1. Find the list to update in the main 'lists' state
                                val listToUpdate = lists.find { it.name == currentList.name }

                                // 2. Remove the specific contact from that list
                                listToUpdate?.contacts?.remove(contact)

                                // 3. Save the updated lists to storage
                                saveLists(context, lists)

                                // 4. Refresh the dialog's view with the updated list
                                viewedList = listToUpdate?.copy()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove Contact"
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewedList = null }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val permission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_CONTACTS
                    )
                    if (permission == PermissionChecker.PERMISSION_GRANTED) {
                        contacts = fetchContacts(context.contentResolver)
                        isCreatingList = false
                        contacts = fetchContacts(context.contentResolver)
                        showContactsDialog = true
                    } else {
                        launcher.launch(Manifest.permission.READ_CONTACTS)
                    }
                     })
                {
                    Text("Add Contact")
                }
            }
        )
    }

    // Contacts Dialog
    if (showContactsDialog) {
        AlertDialog(
            onDismissRequest = { showContactsDialog = false },
            title = { Text("Select a Contact") },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth()
                ) {
                    items(contacts) { contact ->
                        // Row to hold checkbox and text
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // This is the logic to select/deselect
                                    if (selectedContacts.contains(contact)) {
                                        selectedContacts.remove(contact)
                                    } else {
                                        selectedContacts.add(contact)
                                    }
                                }
                                .padding(8.dp)
                        ) {
                            Checkbox(
                                // Checked if the contact is in our list
                                checked = selectedContacts.contains(contact),
                                onCheckedChange = { isChecked ->
                                    // This logic also selects/deselects
                                    if (isChecked) {
                                        selectedContacts.add(contact)
                                    } else {
                                        selectedContacts.remove(contact)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = contact)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (isCreatingList) {
                        // If CREATING a new list, ask for a name.
                        showContactsDialog = false
                        showDialog = true
                    } else {
                        // If ADDING to an existing list...
                        // 1. Find the list we are editing.
                        val listToUpdate = lists.find { it.name == viewedList?.name }

                        // 2. Add the newly selected contacts to it.
                        listToUpdate?.contacts?.addAll(selectedContacts)

                        // 3. Save the changes.
                        saveLists(context, lists)

                        // 4. Clear selections and close the dialog.
                        selectedContacts.clear()
                        showContactsDialog = false

                        // 5. Refresh the view dialog.
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
fun ListButton(list: ContactList, onListClicked: (ContactList) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(
            onClick = { onListClicked(list) },
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF9A7DFF))
        ) {
            Text(list.name, color = Color.Black)
        }
        Spacer(modifier = Modifier.width(6.dp))
        IconButton(onClick = { }) {
            Icon(Icons.Default.Share, contentDescription = "Share",tint = Color.Black,)
        }
    }
}

// Function to save the lists
fun saveLists(context: Context, lists: List<ContactList>) {
    // 1. Get a reference to SharedPreferences
    val sharedPreferences = context.getSharedPreferences("SmartSightApp", Context.MODE_PRIVATE)

    // 2. Create a Gson object
    val gson = Gson()

    // 3. Convert our list of ContactList objects into a JSON string
    val jsonString = gson.toJson(lists)

    // 4. Save the string
    sharedPreferences.edit().putString("contact_lists", jsonString).apply()
}


// Function to load the lists
fun loadLists(context: Context): MutableList<ContactList> {
    val sharedPreferences = context.getSharedPreferences("SmartSightApp", Context.MODE_PRIVATE)
    val gson = Gson()

    val jsonString = sharedPreferences.getString("contact_lists", null)

    // Start with an empty list or the loaded list
    val loadedLists: MutableList<ContactList> = if (jsonString.isNullOrEmpty()) {
        mutableListOf()
    } else {
        val type = object : TypeToken<MutableList<ContactList>>() {}.type
        gson.fromJson(jsonString, type)
    }

    // If, after loading, the list is empty, add the default one
    if (loadedLists.isEmpty()) {
        loadedLists.add(ContactList(name = "Priority list", contacts = mutableListOf()))
    }

    return loadedLists
}
