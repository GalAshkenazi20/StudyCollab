package com.example.studycollab.ui.courses

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturerCoursesScreen(navController: NavController) {
    // This would typically come from a ViewModel fetching courses taught by the lecturer
    val mockCourses = listOf("Computer Science 101", "Data Compression", "Software Engineering")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Courses") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(mockCourses) { courseName ->
                // FIXED: Changed parameter names to match the refactored LecturerCourseCard
                LecturerCourseCard(
                    courseName = courseName,
                    onManageMaterials = {
                        Log.d("LecturerCourses", "Navigating to materials for $courseName")
                        // navController.navigate("materials/$courseName")
                    },
                    onAssignmentPublished = { title, fileUri ->
                        // This block runs when the lecturer clicks 'Publish' in the dialog
                        Log.d("LecturerCourses", "New Assignment: $title, File: $fileUri")

                        // NEXT STEP: Call your ViewModel here to upload the PDF to Node.js
                        // viewModel.uploadAssignment(courseName, title, fileUri)
                    }
                )
            }
        }
    }
}

@Composable
fun LecturerCourseCard(
    courseName: String,
    onManageMaterials: () -> Unit,
    onAssignmentPublished: (title: String, fileUri: android.net.Uri) -> Unit
) {
    // State to control the visibility of the creation dialog
    var showCreateDialog by remember { mutableStateOf(false) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = courseName, style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Clicking this now opens the enforced file-upload dialog
                OutlinedButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Assignment")
                }

                Button(
                    onClick = onManageMaterials,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Materials")
                }
            }
        }
    }

    // Logic to show the dialog and handle the mandatory file upload
    if (showCreateDialog) {
        CreateAssignmentDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, uri ->
                if (uri != null) {
                    onAssignmentPublished(title, uri)
                }
                showCreateDialog = false
            }
        )
    }
}