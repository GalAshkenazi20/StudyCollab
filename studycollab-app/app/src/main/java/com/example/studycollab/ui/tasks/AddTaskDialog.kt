package com.example.studycollab.ui.tasks

import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var taskTitle by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Task") },
        text = {
            TextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                label = { Text("Task Description") },
                singleLine = true,
                placeholder = { Text("e.g., Write Introduction") }
            )
        },
        confirmButton = {
            Button(
                onClick = { if (taskTitle.isNotBlank()) onConfirm(taskTitle) },
                enabled = taskTitle.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}