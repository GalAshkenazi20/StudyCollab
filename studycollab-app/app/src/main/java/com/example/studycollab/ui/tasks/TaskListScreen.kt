package com.example.studycollab.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studycollab.data.model.getParticipantId
import com.example.studycollab.ui.Screen
import com.example.studycollab.ui.chat.StudyGroupViewModel
import com.example.studycollab.utils.UserSession

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

    LaunchedEffect(courseId, groupId) {
        viewModel.fetchCourseAssignments(courseId)
        viewModel.fetchGroupWork(groupId, "")
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
                                dueDate = task.status,
                                onClick = {
                                    // Navigate to task details if needed
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
                    viewModel.addSubTask(
                        workId = viewModel.currentGroupWork?.id ?: "",
                        title = taskTitle,
                        assignedToId = "",
                        groupId = groupId,
                        assignmentId = viewModel.currentGroupWork?.assignmentId ?: "000000000000000000000000"
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
    dueDate: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Deadline: $dueDate",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}