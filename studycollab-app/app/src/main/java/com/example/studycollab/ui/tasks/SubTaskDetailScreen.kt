package com.example.studycollab.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studycollab.data.model.getParticipantId
import com.example.studycollab.ui.chat.StudyGroupViewModel
import com.example.studycollab.utils.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubTaskDetailScreen(
    navController: NavController,
    groupId: String,
    subTaskId: String,
    viewModel: AssignmentViewModel,
    studyViewModel: StudyGroupViewModel
) {
    val work = viewModel.currentGroupWork
    val task = work?.subTasks?.find { it.id == subTaskId }
    val currentUserId = UserSession.userId ?: ""

    val group = studyViewModel.myGroups.find { g ->
        g._id.toString().filter { it.isLetterOrDigit() }.contains(groupId.filter { it.isLetterOrDigit() })
    }
    val isAdmin = group?.members?.any { it.getParticipantId() == currentUserId && it.role == "admin" } == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (task == null) {
                Text(
                    text = "Task details not found.",
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            } else {
                Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                    Text(text = task.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))

                    val statusLabel = when(task.status) {
                        "pending_approval" -> "Pending Approval"
                        "completed" -> "Completed"
                        else -> "To Do"
                    }
                    Text(text = statusLabel, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)

                    Spacer(modifier = Modifier.height(24.dp))

                    // FIXED: This card now pulls the actual name from the populated User object
                    if (task.status != "todo") {
                        val displayName = task.completedBy?.profile?.fullName ?: "Loading name..."

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Marked as done by:", style = MaterialTheme.typography.labelLarge)
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (task.status == "todo") {
                        Button(
                            onClick = { viewModel.completeTask(work!!.id, task.id!!) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Mark as Finished")
                        }
                    }

                    if (isAdmin) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (task.status == "pending_approval") {
                                Button(
                                    onClick = { viewModel.approveTask(work!!.id, task.id!!) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                ) {
                                    Text("Approve")
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.deleteSubTask(work!!.id, task.id!!)
                                    navController.popBackStack()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Delete Task")
                            }
                        }
                    }
                }
            }
        }
    }
}