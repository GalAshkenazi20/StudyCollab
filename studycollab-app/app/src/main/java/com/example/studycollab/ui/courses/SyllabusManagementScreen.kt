package com.example.studycollab.ui.courses

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.studycollab.utils.mouseWheelScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusManagementScreen(
    navController: NavController,
    courseId: String,
    courseName: String,
    viewModel: LecturerCourseViewModel = viewModel()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newTopicTitle by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(courseId) {
        viewModel.loadCourse(courseId)
        visible = true
    }

    val course = viewModel.selectedCourse

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(500)) + slideInVertically(initialOffsetY = { 30 })
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                // Modern Header
                Text(text = courseName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(text = "Syllabus Status", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)

                Spacer(modifier = Modifier.height(24.dp))

                if (course == null) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Modern Progress Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            val progress = if (course.totalLectures > 0) course.completedLectures.toFloat() / course.totalLectures else 0f
                            Text("Course Completion: ${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), strokeCap = androidx.compose.ui.graphics.StrokeCap.Round)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text("Topics", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                    if (course.topics.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No topics yet. Tap + to start planning.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize().mouseWheelScroll(listState),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(
                                start = 0.dp,
                                top = 12.dp,
                                end = 0.dp,
                                bottom = 80.dp
                            )
                        ) {
                            itemsIndexed(course.topics) { index, topic ->
                                TopicItemCard(
                                    title = topic.title,
                                    isCompleted = topic.isCompleted,
                                    onToggle = { viewModel.toggleTopic(courseId, index) },
                                    onDelete = { viewModel.removeTopic(courseId, index) }
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Topic")
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("New Topic", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newTopicTitle,
                    onValueChange = { newTopicTitle = it },
                    label = { Text("Topic Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addTopic(courseId, newTopicTitle)
                    newTopicTitle = ""
                    showAddDialog = false
                }) { Text("Add") }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun TopicItemCard(title: String, isCompleted: Boolean, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isCompleted, onCheckedChange = { onToggle() })
            Text(text = title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}