package com.example.studycollab.ui.courses

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChecklistRtl
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.studycollab.data.model.Course

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturerCoursesScreen(
    navController: NavController,
    viewModel: LecturerCourseViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.fetchLecturerCourses()
    }

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
        if (viewModel.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(viewModel.lecturerCourses) { course ->
                    LecturerCourseCard(
                        course = course,
                        onManageMaterials = {
                            navController.navigate("materials_management/${course.id}/${course.name}")
                        },
                        onManageSyllabus = {
                            navController.navigate("syllabus_management/${course.id}/${course.name}")
                        },
                        onAssignmentPublished = { title, fileUri ->
                            viewModel.uploadAssignment(course.id, title, "", fileUri)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LecturerCourseCard(
    course: Course,
    onManageMaterials: () -> Unit,
    onManageSyllabus: () -> Unit,
    onAssignmentPublished: (title: String, fileUri: Uri) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = course.name, style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onManageSyllabus,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ChecklistRtl, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Manage Syllabus")
            }
        }
    }

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