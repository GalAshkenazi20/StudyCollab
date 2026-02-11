package com.example.studycollab.ui.scheduler

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studycollab.utils.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentOfficeHoursScreen(navController: NavController) {
    // Mock data - replace with ViewModel call later
    val availableSlots = remember { mutableStateListOf(
        SlotInfo("1", "Monday", "10:00", "11:00", false),
        SlotInfo("2", "Wednesday", "14:00", "15:00", false)
    ) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Office Hours") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text(
                text = "Select an available slot to meet with your lecturer.",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(availableSlots) { slot ->
                    BookingCard(slot) {
                        // Logic to call API: PATCH /api/office-hours/book/{slotId}
                        availableSlots.remove(slot)
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(slot: SlotInfo, onBook: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.EventAvailable, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${slot.day}: ${slot.start} - ${slot.end}", style = MaterialTheme.typography.titleMedium)
                Text(text = "Available", color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelSmall)
            }
            Button(onClick = onBook) {
                Text("Book")
            }
        }
    }
}

data class SlotInfo(val id: String, val day: String, val start: String, val end: String, val isBooked: Boolean)