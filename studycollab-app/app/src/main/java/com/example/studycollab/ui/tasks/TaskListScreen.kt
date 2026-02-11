package com.example.studycollab.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studycollab.data.model.getParticipantId
import com.example.studycollab.ui.chat.StudyGroupViewModel
import com.example.studycollab.utils.UserSession
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    navController: NavController,
    courseId: String,
    groupId: String,
    viewModel: AssignmentViewModel,
    studyViewModel: StudyGroupViewModel
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    // Placeholder ID for tasks not tied to a specific lecturer assignment
    val defaultAssignmentId = "000000000000000000000000"

    // Sync state on entry
    LaunchedEffect(courseId, groupId) {
        viewModel.fetchCourseAssignments(courseId)
        viewModel.fetchGroupWork(groupId, defaultAssignmentId)
    }

    val currentUserId = UserSession.userId ?: ""
    val group = studyViewModel.myGroups.find { g ->
        g._id.toString().filter { it.isLetterOrDigit() } == groupId.filter { it.isLetterOrDigit() }
    }
    val isAdmin = group?.members?.any {
        it.getParticipantId() == currentUserId && it.role == "admin"
    } == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Group Tasks") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create New Task")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val tasks = viewModel.currentGroupWork?.subTasks ?: emptyList()

                if (tasks.isEmpty()) {
                    Text(
                        text = "No tasks created yet.\nAdmins can create group tasks using the + button.",
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(tasks) { task ->
                            TaskAssignmentCard(
                                title = task.title,
                                status = task.status,
                                onClick = {
                                    // Pass the subTaskId to navigate to the detailed view
                                    navController.navigate("group_task_details/$groupId/${task.id}")
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showCreateDialog) {
            AddTaskDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { taskTitle ->
                    // FIXED: Passing all 5 parameters for correct DB saving
                    viewModel.addSubTask(
                        workId = viewModel.currentGroupWork?.id ?: "",
                        title = taskTitle,
                        assignedToId = "",
                        groupId = groupId,
                        assignmentId = defaultAssignmentId
                    )
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
fun TaskAssignmentCard(
    title: String,
    status: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (status == "completed")
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        textDecoration = if (status == "completed") TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (status == "completed") Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Status Label Mapping
                val statusText = when (status) {
                    "pending_approval" -> "Waiting for Admin Approval"
                    "completed" -> "Completed"
                    else -> "In Progress"
                }

                val statusColor = when (status) {
                    "completed" -> Color(0xFF4CAF50) // Green
                    "pending_approval" -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.secondary
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = statusColor
                )
            }

            if (status == "completed") {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Done",
                    tint = Color(0xFF4CAF50)
                )
            }
        }
    }
}