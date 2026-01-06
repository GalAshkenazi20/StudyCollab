package com.example.studycollab.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studycollab.utils.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    groupId: String,
    viewModel: StudyGroupViewModel,
    navController: NavController
) {
    // TRIGGER: Ensure data is loaded when this screen opens
    LaunchedEffect(groupId) {
        viewModel.loadInitialData()
    }

    val group = viewModel.myGroups.find { it._id == groupId }
    val currentUserId = UserSession.UserSession.userId

    val isAdmin = group?.members?.find { member ->
        val id = if (member.userId.isJsonPrimitive) {
            member.userId.asString
        } else {
            member.userId.asJsonObject.get("_id").asString
        }
        id == currentUserId
    }?.role == "admin"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group?.name ?: "Loading Group...") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (group == null) {
            // Loading State
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Success State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                DashboardButton("👥 View Participants", Icons.Default.Person) {
                     navController.navigate("participants/$groupId")
                }

                DashboardButton("✅ Group Tasks", Icons.Default.CheckCircle) {
                    navController.navigate("group_tasks/$groupId")
                }

                DashboardButton("💬 Group Chat", Icons.AutoMirrored.Filled.Send) {
                    navController.navigate("chat/${group._id}")
                }

                DashboardButton("👨‍🏫 Chat with Lecturer", Icons.Default.Face) {
                    // Logic for lecturer chat
                }

                if (group.purpose == "assignment_submission") {
                    Button(
                        onClick = { /* Submit logic */ },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("📤 Submit Assignment")
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (isAdmin) {
                    var showDeleteDialog by remember { mutableStateOf(false) }

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("Delete Group") },
                            text = { Text("Are you sure? This action cannot be undone.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.deleteGroup(groupId) { success ->
                                        if (success) navController.popBackStack()
                                    }
                                    showDeleteDialog = false
                                }) {
                                    Text("Delete", color = Color.Red)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Delete Group")
                    }
                }
            }
        }
    }
}

// THIS COMPONENT WAS MISSING FROM YOUR FILE
@Composable
fun DashboardButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}