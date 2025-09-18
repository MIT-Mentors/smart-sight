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
    val lists = remember { mutableStateListOf<String>("Priority list") }

    var showContactsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var contacts by remember { mutableStateOf<List<String>>(emptyList()) }

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
            lists.forEach { listName ->
                Spacer(modifier = Modifier.height(8.dp))
                ListButton(listName)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Create list button
            OutlinedButton(
                onClick = { showDialog = true },
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
                        // TODO: Ask for permission with Accompanist Permissions or ActivityResultLauncher
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
                        lists.add(newListName)
                        newListName = ""
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
                        TextButton(onClick = {
                            // TODO: Handle contact selection
                            showContactsDialog = false
                        }) {
                            Text(contact)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showContactsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun ListButton(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(
            onClick = { },
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF9A7DFF))
        ) {
            Text(text, color = Color.Black)
        }
        Spacer(modifier = Modifier.width(6.dp))
        IconButton(onClick = { }) {
            Icon(Icons.Default.Share, contentDescription = "Share",tint = Color.Black,)
        }
    }
}
