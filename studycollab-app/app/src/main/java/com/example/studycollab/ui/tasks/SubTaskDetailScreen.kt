package com.example.studycollab.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studycollab.ui.chat.StudyGroupViewModel
import com.example.studycollab.utils.UserSession
import com.example.studycollab.data.model.getParticipantId // Required helper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubTaskDetailScreen(
    navController: NavController,
    groupId: String,
    assignmentId: String,
    viewModel: AssignmentViewModel,
    studyViewModel: StudyGroupViewModel
) {
    LaunchedEffect(groupId, assignmentId) {
        viewModel.fetchGroupWork(groupId, assignmentId)
    }

    val work = viewModel.currentGroupWork
    // FIX: Using the nested structure visible in your screenshots
    val currentUserId = UserSession.userId

    // FIX: Use getParticipantId() to avoid JsonElement comparison errors
    val currentGroup = studyViewModel.myGroups.find { it._id.toString().contains(groupId) }
    val isAdmin = currentGroup?.members?.any {
        it.getParticipantId() == currentUserId && it.role == "admin"
    } == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Breakdown") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(onClick = { /* Open Add Task Dialog */ }) {
                    Icon(Icons.Default.Add, "Add Task")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            work?.subTasks?.forEach { task ->
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