package com.example.studycollab.ui.tasks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun TaskListScreen(
    navController: NavController,
    courseId: String,
    viewModel: AssignmentViewModel
) {
    LaunchedEffect(courseId) {
        viewModel.fetchCourseAssignments(courseId)
    }

    Scaffold(
        topBar = { /* TopAppBar with Back Button */ }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            items(viewModel.courseAssignments) { assignment ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    onClick = {
                        // Assuming you have the groupId from the context or pass it in
                        navController.navigate("group_task_details/YOUR_GROUP_ID/${assignment.id}")
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = assignment.title, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Due: ${assignment.dueAt}", style = MaterialTheme.typography.bodySmall)
                        if (assignment.fileUrl != null) {
                            Text(text = "📎 View PDF Instructions", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}