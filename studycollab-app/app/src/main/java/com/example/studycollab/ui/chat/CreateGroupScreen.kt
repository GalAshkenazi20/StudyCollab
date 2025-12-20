package com.example.studycollab.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CreateGroupScreen(
    onBackClick: () -> Unit
) {
    val viewModel: StudyGroupViewModel = viewModel()

    // Form State
    var groupName by remember { mutableStateOf("") }
    var courseId by remember { mutableStateOf("") }
    var creatorId by remember { mutableStateOf("") } // In a real app, this comes from the logged-in user
    var purpose by remember { mutableStateOf("general") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Create New Study Group", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        // Input Fields
        OutlinedTextField(
            value = groupName,
            onValueChange = { groupName = it },
            label = { Text("Group Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = courseId,
            onValueChange = { courseId = it },
            label = { Text("Course ID (e.g. CS-101)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = creatorId,
            onValueChange = { creatorId = it },
            label = { Text("Creator ID (Paste User ID here)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = purpose,
            onValueChange = { purpose = it },
            label = { Text("Purpose (general, exam, project)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Feedback Messages
        if (viewModel.successMessage != null) {
            Text(viewModel.successMessage!!, color = Color.Green)
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (viewModel.errorMessage != null) {
            Text(viewModel.errorMessage!!, color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Submit Button
        Button(
            onClick = { viewModel.createGroup(groupName, courseId, creatorId, purpose) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Create Group")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onBackClick) {
            Text("Back to Dashboard")
        }
    }
}