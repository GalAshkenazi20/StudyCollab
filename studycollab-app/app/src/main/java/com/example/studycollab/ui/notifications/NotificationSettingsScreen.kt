package com.example.studycollab.ui.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotificationSettingsScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Smart Notification Preferences", style = MaterialTheme.typography.headlineMedium)
        Text("Manage your academic and group alerts.", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(24.dp))

        NotificationToggle("Group Updates", "Notify when groups are created or tasks assigned")
        NotificationToggle("Deadlines", "Reminders for upcoming assignments and tests")
        NotificationToggle("Schedule Changes", "Updates on lecture rooms or cancellations")
        NotificationToggle("Peer Activity", "Notifications for chat messages and sub-task progress")
    }
}

@Composable
fun NotificationToggle(title: String, description: String) {
    var isEnabled by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
    }
}