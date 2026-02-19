package com.example.studycollab.ui.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studycollab.utils.UserSession
import com.example.studycollab.utils.mouseWheelScroll
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    viewModel: StudyGroupViewModel,
    onBackClick: () -> Unit
) {
    val currentUserId = UserSession.userId
    val listState = rememberLazyListState()
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        if (currentUserId != null) viewModel.fetchMyCourses(currentUserId)
    }

    LaunchedEffect(viewModel.successMessage) {
        if (viewModel.successMessage != null) {
            delay(1000)
            onBackClick()
            viewModel.successMessage = null
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInHorizontally()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text("Create Study Group", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
            Text("Collaborate with your classmates.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = viewModel.groupName,
                onValueChange = { viewModel.groupName = it },
                label = { Text("Group Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = viewModel.selectedCourse?.name ?: "Select a Course",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Course Selection") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    viewModel.myCourses.forEach { course ->
                        DropdownMenuItem(
                            text = { Text(course.name) },
                            onClick = {
                                viewModel.onCourseSelected(course)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Invite Classmates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                val filteredClassmates = viewModel.availableClassmates.filter { it._id != currentUserId }

                if (filteredClassmates.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (viewModel.selectedCourse == null) "Select a course first" else "No other classmates found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .mouseWheelScroll(listState) // FIX: Mouse wheel support
                            .padding(8.dp)
                    ) {
                        items(filteredClassmates) { student ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleStudentSelection(student) }
                                    .padding(12.dp)
                            ) {
                                Checkbox(
                                    checked = viewModel.selectedStudents.contains(student),
                                    onCheckedChange = { viewModel.toggleStudentSelection(student) }
                                )
                                Text(
                                    text = student.profile.fullName,
                                    modifier = Modifier.padding(start = 12.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.purpose,
                onValueChange = { viewModel.purpose = it },
                label = { Text("Purpose (e.g. Project Phase 1)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Feedback & Submission
            if (viewModel.errorMessage != null) {
                Text(viewModel.errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }

            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = { viewModel.createGroup() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = viewModel.groupName.isNotBlank() && viewModel.selectedCourse != null
                ) {
                    Text("Create Group", fontWeight = FontWeight.Bold)
                }
            }

            TextButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}