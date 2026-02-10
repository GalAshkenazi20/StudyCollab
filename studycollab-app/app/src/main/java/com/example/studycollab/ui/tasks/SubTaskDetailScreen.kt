package com.example.studycollab.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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
    assignmentId: String, // Ensure this is not empty from Navigation
    viewModel: AssignmentViewModel,
    studyViewModel: StudyGroupViewModel
) {
    var showAddTaskDialog by remember { mutableStateOf(false) }

    LaunchedEffect(groupId, assignmentId) {
        viewModel.fetchGroupWork(groupId, assignmentId)
    }

    val work = viewModel.currentGroupWork
    val currentUserId = UserSession.userId ?: ""
    val group = studyViewModel.myGroups.find { g ->
        g._id.toString().filter { it.isLetterOrDigit() }.contains(groupId.filter { it.isLetterOrDigit() })
    }

    val isAdmin = group?.members?.any {
        it.getParticipantId() == currentUserId && it.role == "admin"
    } == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Breakdown") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(onClick = { showAddTaskDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (work == null || work.subTasks.isEmpty()) {
                Text(
                    text = "No sub-tasks defined yet.\nAdmin can add them with the + button.",
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            } else {
                Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                    work.subTasks.forEach { task ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = task.status == "completed",
                                onCheckedChange = {
                                    if (task.status == "todo") viewModel.completeTask(work.id, task.id!!)
                                },
                                enabled = task.status == "todo"
                            )
                            Text(
                                text = task.title,
                                modifier = Modifier.weight(1f),
                                style = if (task.status == "completed") MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough) else MaterialTheme.typography.bodyLarge
                            )
                            if (isAdmin && task.status == "pending_approval") {
                                Button(onClick = { viewModel.approveTask(work.id, task.id!!) }) {
                                    Text("Approve")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddTaskDialog) {
            AddTaskDialog(
                onDismiss = { showAddTaskDialog = false },
                onConfirm = { taskTitle ->
                    // FIXED: Passing all 5 parameters explicitly
                    viewModel.addSubTask(
                        workId = work?.id ?: "",
                        title = taskTitle,
                        assignedToId = "",
                        groupId = groupId,
                        assignmentId = assignmentId // Ensuring this is the nav-arg ID
                    )
                    showAddTaskDialog = false
                }
            )
        }
    }
}