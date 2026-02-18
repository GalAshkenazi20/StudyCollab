package com.example.studycollab.ui.courses

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studycollab.ui.chat.StudyGroupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    navController: NavController,
    courseName: String,
    courseCode: String,
    studyViewModel: StudyGroupViewModel // Shared instance passed from Navigation
) {
    // Finding the specific course from the list already loaded in the shared ViewModel
    val course = studyViewModel.myCourses.find { it.code == courseCode }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(courseName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            course?.let { c ->
                // --- Progress Tracking Section (FR-8.1) ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val progress = if (c.totalLectures > 0) c.completedLectures.toFloat() / c.totalLectures else 0f
                        Text(
                            text = "Course Progress: ${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Updated to use the correct lambda syntax for progress
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "${c.completedLectures} out of ${c.totalLectures} lectures passed",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Syllabus Checklist ---
                Text(text = "Syllabus Status", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))

                c.topics.forEach { topic ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = topic.isCompleted, onCheckedChange = null, enabled = false)
                        Text(
                            text = topic.title,
                            style = if (topic.isCompleted) {
                                MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough)
                            } else {
                                MaterialTheme.typography.bodyLarge
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // --- Action Buttons ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        //added
                        onClick = {
                            navController.navigate("student_assignments/${c.id}/${courseName}")
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Assignments & Tasks")
                    }

                    Button(
                        //added
                        onClick = {
                            navController.navigate("student_materials/${c.id}/${courseName}")
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Course Materials")
                    }

                    Button(
                        onClick = {
                            navController.navigate("chat/${c.id}")
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Course Forum")
                    }
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // If this message appears, check if studyViewModel.fetchMyCourses(userId) was actually successful
                Text("Course data not found. Try refreshing the list.")
            }
        }
    }
}