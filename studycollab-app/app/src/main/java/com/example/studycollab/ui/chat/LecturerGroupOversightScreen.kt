package com.example.studycollab.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.studycollab.data.model.Course
import com.example.studycollab.data.model.StudyGroup
import com.example.studycollab.data.remote.ApiClient
import com.example.studycollab.ui.courses.LecturerCourseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturerGroupOversightScreen(
    navController: NavController,
    lecturerViewModel: LecturerCourseViewModel = viewModel()
) {
    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var groups by remember { mutableStateOf<List<StudyGroup>>(emptyList()) }
    var isLoadingGroups by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        lecturerViewModel.fetchLecturerCourses()
    }

    LaunchedEffect(selectedCourse) {
        selectedCourse?.let { course ->
            isLoadingGroups = true
            try {
                val response = ApiClient.apiService.getGroupsByCourse(course.id)
                if (response.isSuccessful) {
                    groups = response.body() ?: emptyList()
                }
            } catch (e: Exception) { e.printStackTrace() }
            finally { isLoadingGroups = false }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Group Oversight", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("View study groups in your courses", color = Color.Gray)

        Spacer(Modifier.height(16.dp))

        Text("Select Course:", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (selectedCourse == null) {
                items(lecturerViewModel.lecturerCourses) { course ->
                    Card(
                        onClick = { selectedCourse = course },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MenuBook, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text(course.name, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            } else {
                item {
                    Card(
                        onClick = { selectedCourse = null; groups = emptyList() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowBack, null)
                            Spacer(Modifier.width(8.dp))
                            Text("${selectedCourse!!.name} — tap to change", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }

                if (isLoadingGroups) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (groups.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No study groups in this course yet", color = Color.Gray)
                        }
                    }
                } else {
                    item {
                        Text("${groups.size} groups found", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                    }
                    items(groups) { group ->
                        GroupOversightCard(group)
                    }
                }
            }
        }
    }
}

@Composable
fun GroupOversightCard(group: StudyGroup) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(group.name ?: "Unnamed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(8.dp))

            val admin = group.members?.find { it.role == "admin" }
            if (admin != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp), tint = Color(0xFFFFC107))
                    Spacer(Modifier.width(4.dp))
                    Text("Admin: ${admin.userId?.toString() ?: "Unknown"}", style = MaterialTheme.typography.bodySmall)
                }
            }

            Text(
                "${group.members?.size ?: 0} members",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            if (group.purpose != null) {
                Spacer(Modifier.height(4.dp))
                Text("Purpose: ${group.purpose}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}