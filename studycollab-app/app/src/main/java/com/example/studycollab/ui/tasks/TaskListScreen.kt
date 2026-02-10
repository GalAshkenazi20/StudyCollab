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

    // Re-fetch assignments and initialize group work context on entry
    LaunchedEffect(courseId, groupId) {
        viewModel.fetchCourseAssignments(courseId)
        // We fetch group work here to see if a workId already exists for this group
        // This prevents the "Save but no show" issue by syncing the work state early
        viewModel.fetchGroupWork(groupId, "")
    }

    // Determine Admin status for UI controls
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
                title = { Text("Group Tasks") }, // Fixed: English headline
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
            } else if (viewModel.courseAssignments.isEmpty()) {
                // Informative empty state
                Text(
                    text = "No assignments found for this course.\nAdmins can create group tasks using the + button.",
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
                    items(viewModel.courseAssignments) { assignment ->
                        TaskAssignmentCard(
                            title = assignment.title,
                            dueDate = assignment.dueAt.toString(),
                            onClick = {
                                // Navigate to the specific sub-task breakdown
                                navController.navigate(
                                    Screen.GroupTaskDetails.createRoute(groupId, assignment.id)
                                )
                            }
                        )
                    }
                }
            }
        }

        if (showCreateDialog) {
            AddTaskDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { taskTitle ->
                    // Pass the context so the backend can link the first task
                    viewModel.addSubTask(
                        workId = viewModel.currentGroupWork?.id ?: "",
                        title = taskTitle,
                        assignedToId = "",
                        groupId = groupId,      // Mandatory for first-time creation
                        assignmentId = "000000000000000000000000"        // Can be empty for general group tasks
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