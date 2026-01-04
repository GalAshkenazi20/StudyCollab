package com.example.studycollab.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.studycollab.utils.UserSession
import kotlinx.coroutines.delay // <-- לייבוא של delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    viewModel: StudyGroupViewModel,
    onBackClick: () -> Unit
) {

    val currentUserId = UserSession.UserSession.userId

    LaunchedEffect(Unit) {
        if (currentUserId != null) {
            viewModel.fetchMyCourses(currentUserId)
        } else {

            println("Error: No user logged in CreateGroupScreen")
        }
    }

    LaunchedEffect(viewModel.successMessage) {
        if (viewModel.successMessage != null) {
            delay(1000)
            onBackClick()
            viewModel.successMessage = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        Text("Create New Study Group", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        // --- FIELD 1: GROUP NAME ---
        OutlinedTextField(
            value = viewModel.groupName,
            onValueChange = { viewModel.groupName = it },
            label = { Text("Group Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- FIELD 2: COURSE SELECTION (Dropdown) ---
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
                modifier = Modifier.menuAnchor().fillMaxWidth()
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

        Spacer(modifier = Modifier.height(20.dp))

        // --- FIELD 3: CLASSMATE SELECTION ---
        Text("Invite Classmates from this Course:", style = MaterialTheme.typography.titleSmall)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.LightGray)
        ) {
            // סינון: הסתרת המשתמש הנוכחי מהרשימה
            val filteredClassmates = viewModel.availableClassmates.filter { student ->
                student._id != currentUserId
            }

            if (filteredClassmates.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (viewModel.selectedCourse == null) "Select a course first" else "No other classmates found",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    items(filteredClassmates) { student ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleStudentSelection(student) }
                                .padding(8.dp)
                        ) {
                            Checkbox(
                                checked = viewModel.selectedStudents.contains(student),
                                onCheckedChange = { viewModel.toggleStudentSelection(student) }
                            )
                            Text(
                                text = student.profile.fullName,
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- FIELD 4: PURPOSE ---
        OutlinedTextField(
            value = viewModel.purpose,
            onValueChange = { viewModel.purpose = it },
            label = { Text("Purpose (e.g. Exam Prep)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        // --- FEEDBACK & LOADING ---
        if (viewModel.errorMessage != null) {
            Text(viewModel.errorMessage!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (viewModel.successMessage != null) {
            Text(viewModel.successMessage!!, color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (viewModel.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            // --- SUBMIT BUTTON ---
            Button(
                onClick = { viewModel.createGroup() },
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.groupName.isNotBlank() && viewModel.selectedCourse != null
            ) {
                Text("Create Group")
            }
        }

        TextButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Cancel and Go Back")
        }
    }
}