package com.example.studycollab.ui.courses

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.studycollab.data.model.Course
import com.example.studycollab.utils.mouseWheelScroll

@Composable
fun LecturerCoursesScreen(
    navController: NavController,
    viewModel: LecturerCourseViewModel = viewModel()
) {
    val listState = rememberLazyListState()
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchLecturerCourses()
        visible = true
    }

    // Removed local Scaffold to prevent double headers
    Box(modifier = Modifier.fillMaxSize()) {
        if (viewModel.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInVertically(initialOffsetY = { 30 })
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your Curriculum",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    )

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .mouseWheelScroll(listState),
                        contentPadding = PaddingValues(vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
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
                                // FIXED: Lambda now handles the deadline from the dialog
                                onAssignmentPublished = { title, fileUri, deadline ->
                                    viewModel.publishAssignment(course.id, title, deadline, fileUri)
                                }
                            )
                        }
                    }
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
    // FIXED: Signature updated to include deadline
    onAssignmentPublished: (String, Uri?, Long) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Course ID: ${course.id.takeLast(6).uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModernCourseButton(
                    text = "Assignment",
                    icon = Icons.Default.Add,
                    modifier = Modifier.weight(1f),
                    isPrimary = false
                ) { showCreateDialog = true }

                ModernCourseButton(
                    text = "Materials",
                    icon = Icons.Default.Folder,
                    modifier = Modifier.weight(1f),
                    isPrimary = true,
                    onClick = onManageMaterials
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row 2
            ModernCourseButton(
                text = "Manage Syllabus",
                icon = Icons.Default.ChecklistRtl,
                modifier = Modifier.fillMaxWidth(),
                isPrimary = false,
                onClick = onManageSyllabus
            )
        }
    }

    if (showCreateDialog) {
        CreateAssignmentDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, uri, deadline ->
                onAssignmentPublished(title, uri, deadline)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun ModernCourseButton(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    if (isPrimary) {
        Button(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.Bold)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.Bold)
        }
    }
}