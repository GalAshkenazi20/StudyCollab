package com.example.studycollab.ui.courses

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studycollab.data.model.Assignment
import com.example.studycollab.data.remote.ApiClient
import com.example.studycollab.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAssignmentsScreen(
    navController: NavController,
    courseId: String,
    courseName: String
) {
    val context = LocalContext.current

    var assignments by remember { mutableStateOf<List<Assignment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // For "Show Assignment"
    var selectedAssignment by remember { mutableStateOf<Assignment?>(null) }

    LaunchedEffect(courseId) {
        try {
            val response = ApiClient.apiService.getCourseAssignments(courseId)
            if (response.isSuccessful) {
                assignments = response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    // ✅ Show Assignment dialog
    if (selectedAssignment != null) {
        val a = selectedAssignment!!
        AlertDialog(
            onDismissRequest = { selectedAssignment = null },
            title = { Text(a.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Due: ${a.dueAt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    if (!a.description.isNullOrBlank()) {
                        Text(a.description!!)
                    } else {
                        Text("No description provided.", color = Color.Gray)
                    }

                    if (a.fileUrl.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "No file attached to this assignment.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedAssignment = null }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assignments: $courseName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else if (assignments.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("No assignments yet", color = Color.Gray) }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(assignments) { assignment ->
                    AssignmentCard(
                        assignment = assignment,
                        onShowAssignment = { selectedAssignment = assignment },
                        onViewPdf = {
                            val url = Constants.BASE_URL.trimEnd('/') + (assignment.fileUrl ?: "")
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        },
                        onSubmit = {
                            navController.navigate("submit_assignment/${assignment.id}/${courseId}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AssignmentCard(
    assignment: Assignment,
    onShowAssignment: () -> Unit,
    onViewPdf: () -> Unit,
    onSubmit: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {

            Text(assignment.title, style = MaterialTheme.typography.titleMedium)

            Text(
                "Due: ${assignment.dueAt}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {


                OutlinedButton(onClick = onShowAssignment) {
                    Icon(Icons.Default.Description, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Show Assignment")
                }


                if (!assignment.fileUrl.isNullOrBlank()) {
                    OutlinedButton(onClick = onViewPdf) {
                        Icon(Icons.Default.PictureAsPdf, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("View PDF")
                    }
                }

                Button(onClick = onSubmit) {
                    Text("Submit Solution")
                }
            }
        }
    }
}