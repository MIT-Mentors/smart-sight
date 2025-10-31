package com.example.smartsight

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun About_Screen(navController: NavController) {
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
                text = "About",
                fontSize = 20.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.Black, shape = RoundedCornerShape(4.dp))
            )
        }

        // Scrollable Content
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            Text(
                text = "About Smart Sight",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4B0082),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Smart Sight is an assistive Android application designed to empower visually challenged individuals through real-time visual and situational awareness. " +
                        "The app connects with a wearable Smart Sight glass that includes a camera and an SOS button. Together, they provide essential features such as Object Detection, Document Reading, Location Sharing, and SOS Emergency Assistance. " +
                        "A future version will also include a Navigation feature for guided movement.",
                fontSize = 16.sp,
                textAlign = TextAlign.Left,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            DividerTitle("Startup Status Screen")
            Text(
                text = buildAnnotatedString {
                    append("When Smart Sight is opened, the initial screen displays key system statuses:\n\n")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("• Bluetooth Status") }
                    append(" – Ensures the audio device is paired.\n")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("• Internet Status") }
                    append(" – Verifies active network connection.\n")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("• Battery Percentage") }
                    append(" – Shows current battery level of the device.\n\nOnce all essential modules are ready, the home screen appears.")
                },
                fontSize = 16.sp,
                color = Color.Black,
                textAlign = TextAlign.Left,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            DividerTitle("Home Screen")
            Text(
                text = "The Home screen presents five core feature buttons in a simple, high-contrast layout for easy access:\n\n" +
                        "1. SOS\n2. Navigation (coming soon)\n3. Object Detection\n4. Document Reading\n5. Location Sharing\n",
                fontSize = 16.sp,
                textAlign = TextAlign.Left,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle("1. Object Detection")
            Text(
                text = buildAnnotatedString {
                    append("This feature allows the user to recognize objects in front of them through the Smart Sight wearable.\n\n")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("How it works:\n") }
                    append("• Ensure both the mobile and Smart Sight wearable are connected to the same Wi-Fi network.\n")
                    append("• Once connected, a message “Connected to ESP” appears in the text box.\n")
                    append("• Tap the “Detect” button.\n• The camera on the glasses captures an image, which is displayed on the app screen.\n")
                    append("• Detected objects are then shown in the text box and announced through voice output.\n\n")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Example Scenario:\n") }
                    append("If a chair and a bottle are in front of the user, the app displays “Chair, Bottle” and speaks the same aloud.")
                },
                fontSize = 16.sp,
                color = Color.Black,
                textAlign = TextAlign.Left,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle("2. Document Reading")
            Text(
                text = buildAnnotatedString {
                    append("This feature helps in reading printed or displayed text aloud for visually challenged users.\n\n")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Current Implementation:\n") }
                    append("• The app currently contains hard-coded lines of text for testing.\n")
                    append("• When the Document Reading button is pressed, the stored text appears on the screen.\n")
                    append("• Press the “Read Aloud” button to hear the text as voice output.\n\n")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Future Update:\n") }
                    append("The next version will allow users to capture documents through the camera and listen to recognized text in real-time.")
                },
                fontSize = 16.sp,
                color = Color.Black,
                textAlign = TextAlign.Left,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle("3. Location Sharing")
            Text(
                text = buildAnnotatedString {
                    append("This feature lets users share their live location with trusted contacts or groups.\n\n")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Interface Buttons:\n") }
                    append("• Create List – add multiple contacts and assign a name for easy reference.\n")
                    append("• Priority List – shows predefined contact groups for quick sharing.\n")
                    append("• Select a Contact – choose a single person to share your location with.\n\n")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Steps to Use:\n") }
                    append("1. Go to Location Sharing.\n2. Use Create List to add people you want to share your location with.\n")
                    append("3. To share with a specific person, choose Select a Contact.\n4. Once contacts or lists are selected, tap the Share (≻) button.\n")
                    append("5. Your current GPS location is then sent automatically to the selected recipients.\n\n")
                    append("Note: GPS must be turned ON on the mobile phone for location sharing to function correctly.")
                },
                fontSize = 16.sp,
                color = Color.Black,
                textAlign = TextAlign.Left,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle("4. SOS (Emergency Alert)")
            Text(
                text = buildAnnotatedString {
                    append("This safety-critical feature is activated through a physical button on the Smart Sight wearable glass.\n\n")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("How it works:\n") }
                    append("• Press the physical SOS button on the wearable device.\n")
                    append("• The app instantly retrieves the current GPS location from the connected mobile phone.\n")
                    append("• That location is shared with all contacts in the Priority List created under the Location Sharing feature.\n")
                    append("• Voice confirmation ensures that the SOS message has been sent successfully.\n\n")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Important: ") }
                    append("Ensure the GPS service is ON before using the SOS feature.")
                },
                fontSize = 16.sp,
                color = Color.Black,
                textAlign = TextAlign.Left,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle("5. Navigation (Coming Soon)")
            Text(
                text = "The Navigation module will offer voice-guided path instructions and obstacle-aware routing in future updates, enhancing safe movement in both indoor and outdoor environments.",
                fontSize = 16.sp,
                color = Color.Black,
                textAlign = TextAlign.Left,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            DividerTitle("Technical Requirements")
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("• Wi-Fi Connection: ") }
                    append("For Object Detection (mobile and wearable device communication)\n")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("• GPS: ") }
                    append("For Location Sharing and SOS\n")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("• Internet: ") }
                    append("For text-to-speech output and cloud updates\n")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("• Bluetooth: ") }
                    append("For certain wearable functions (future support)")
                },
                fontSize = 16.sp,
                textAlign = TextAlign.Left,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            DividerTitle("Privacy & Accessibility")
            Text(
                text = "Smart Sight prioritizes privacy and ease of use:\n" +
                        "• Camera feeds are processed within the local network; no external upload.\n" +
                        "• Large buttons, clear icons, and audio feedback for full accessibility.\n" +
                        "• No data shared without user consent.",
                fontSize = 16.sp,
                color = Color.Black,
                textAlign = TextAlign.Left,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            DividerTitle("Credits")
            Text(
                text = "Developed by the Smart Sight Team\n" +
                        "-- Contributors --\n" +
                        "Pragash Durai\nSanjay Kumar M\nSanjay P\nTarunrajan M\n\n" +
                        "Contact Email: feedback@smartsight.app\nVersion: v1.0 (Prototype)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
        }
    }
}

// Helper Composables
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF4B0082),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun DividerTitle(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF6A0DAD),
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
