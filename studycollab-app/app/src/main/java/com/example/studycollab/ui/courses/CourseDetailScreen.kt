package com.example.studycollab.ui.courses

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.studycollab.ui.chat.StudyGroupViewModel
import com.example.studycollab.utils.mouseWheelScroll // Ensure this helper is imported

@Composable
fun CourseDetailScreen(
    navController: NavController,
    courseName: String,
    courseCode: String,
    studyViewModel: StudyGroupViewModel
) {
    val course = studyViewModel.myCourses.find { it.code == courseCode }
    val scrollState = rememberScrollState()
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    // Removed local Scaffold/TopAppBar - now handled by AppNavigation
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(initialOffsetY = { 30 })
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .mouseWheelScroll(scrollState) // FIX: Mouse wheel support
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            course?.let { c ->
                // Modern Header with Bold Typography
                Text(
                    text = courseCode,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = courseName,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                )

                c.lecturerName?.let { name ->
                    Text(
                        text = "Lecturer: $name",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Modern Progress Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        val progress = if (c.totalLectures > 0) c.completedLectures.toFloat() / c.totalLectures else 0f
                        Text(
                            text = "Course Progress: ${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                        Text(
                            text = "${c.completedLectures} / ${c.totalLectures} lectures completed",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(text = "Syllabus Status", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                c.topics.forEach { topic ->
                    Surface(
                        modifier = Modifier.padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (topic.isCompleted) MaterialTheme.colorScheme.surfaceDim else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = topic.isCompleted, onCheckedChange = null, enabled = false)
                            Text(
                                text = topic.title,
                                style = if (topic.isCompleted) {
                                    MaterialTheme.typography.bodyLarge.copy(
                                        textDecoration = TextDecoration.LineThrough,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                } else {
                                    MaterialTheme.typography.bodyLarge
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ModernActionButton("Assignments & Tasks", Icons.Default.Assignment) {
                        navController.navigate("student_assignments/${c.id}/${courseName}")
                    }
                    ModernActionButton("Course Materials", Icons.Default.Folder) {
                        navController.navigate("student_materials/${c.id}/${courseName}")
                    }
                    Button(
                        onClick = { navController.navigate("chat/${c.id}") },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Open Course Forum", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ModernActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}