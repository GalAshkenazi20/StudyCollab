package com.example.studycollab.ui.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studycollab.data.model.NotificationPreferences
import com.example.studycollab.data.remote.ApiClient
import com.example.studycollab.utils.UserSession
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen() {
    var prefs by remember { mutableStateOf(NotificationPreferences()) }
    var isLoading by remember { mutableStateOf(true) }
    var reminderExpanded by remember { mutableStateOf(false) }
    val userId = UserSession.userId
    val scope = rememberCoroutineScope()

    // Load preferences
    LaunchedEffect(userId) {
        userId?.let {
            try {
                val response = ApiClient.apiService.getNotificationPreferences(it)
                if (response.isSuccessful) {
                    prefs = response.body() ?: NotificationPreferences()
                }
            } catch (e: Exception) { e.printStackTrace() }
            finally { isLoading = false }
        } ?: run { isLoading = false }
    }

    // Save helper
    fun savePrefs(updated: NotificationPreferences) {
        prefs = updated
        scope.launch {
            userId?.let {
                try { ApiClient.apiService.updateNotificationPreferences(it, updated) }
                catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Notification Preferences", style = MaterialTheme.typography.headlineMedium)
        Text("Manage your academic and group alerts.", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            NotifToggle("Group Updates", "Groups created, tasks assigned", prefs.groupUpdates) {
                savePrefs(prefs.copy(groupUpdates = it))
            }

            NotifToggle("Deadlines", "Assignment deadline reminders", prefs.deadlines) {
                savePrefs(prefs.copy(deadlines = it))
            }

            NotifToggle("Schedule Changes", "Lecture changes, cancellations", prefs.scheduleChanges) {
                savePrefs(prefs.copy(scheduleChanges = it))
            }

            NotifToggle("Peer Activity", "Messages, task progress", prefs.peerActivity) {
                savePrefs(prefs.copy(peerActivity = it))
            }

            NotifToggle("Office Hours", "Booking confirmations", prefs.officeHours) {
                savePrefs(prefs.copy(officeHours = it))
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))

            // Reminder timing picker
            Text("Remind me before deadlines:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            val options = listOf(1, 3, 6, 12, 24, 48)
            ExposedDropdownMenuBox(
                expanded = reminderExpanded,
                onExpandedChange = { reminderExpanded = !reminderExpanded }
            ) {
                OutlinedTextField(
                    value = "${prefs.deadlineReminderHours} hours before",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reminderExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = reminderExpanded,
                    onDismissRequest = { reminderExpanded = false }
                ) {
                    options.forEach { hours ->
                        DropdownMenuItem(
                            text = { Text("$hours hours before") },
                            onClick = {
                                savePrefs(prefs.copy(deadlineReminderHours = hours))
                                reminderExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotifToggle(title: String, desc: String, isChecked: Boolean, onChanged: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(desc, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = isChecked, onCheckedChange = onChanged)
    }
}