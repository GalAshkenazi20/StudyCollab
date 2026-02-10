package com.example.studycollab.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studycollab.data.model.getParticipantId
import com.example.studycollab.ui.Screen
import com.example.studycollab.utils.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    groupId: String,
    viewModel: StudyGroupViewModel,
    navController: NavController
) {
    // Robust finding logic: compares clean strings to avoid JsonElement mismatches
    val group = viewModel.myGroups.find { g ->
        g._id.toString().filter { it.isLetterOrDigit() }.contains(groupId.filter { it.isLetterOrDigit() })
    }

    val currentUserId = UserSession.userId ?: ""
    val isAdmin = group?.members?.any {
        it.getParticipantId() == currentUserId && it.role == "admin"
    } == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group?.name ?: "Group Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            group?.let { g ->
                Text("Purpose: ${g.purpose}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                // Fixed: providing default empty string for nullable description
                Text(text = g.description ?: "", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(32.dp))

                // --- ACTION BUTTONS ---
                Button(
                    onClick = { navController.navigate(Screen.GroupTasks.createRoute(groupId)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Assignment, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Group Tasks")
                }

                Button(
                    onClick = { navController.navigate(Screen.ChatRoom.createRoute(groupId)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Group Chat")
                }

                Button(
                    onClick = { navController.navigate(Screen.Participants.createRoute(groupId)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.People, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Participants")
                }

                if (isAdmin) {
                    OutlinedButton(
                        onClick = { /* Add Members Logic */ },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Members")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            // Fixed: passing mandatory onComplete lambda
                            viewModel.deleteGroup(groupId = groupId, onComplete = {
                                navController.popBackStack()
                            })
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Delete Group")
                    }
                }
            } ?: Text("Group info not found. Return to list and try again.")
        }
    }
}