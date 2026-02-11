package com.example.studycollab.ui.submissions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studycollab.data.model.SubmissionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionTrackingScreen(
    navController: NavController,
    assignmentId: String, // Added for API calls
    assignmentTitle: String,
    viewModel: SubmissionViewModel // Injected ViewModel
) {
    var selectedSubmissionForGrading by remember { mutableStateOf<String?>(null) }
    var selectedGroupName by remember { mutableStateOf("") }

    // Fetch data when the screen is first displayed
    LaunchedEffect(assignmentId) {
        viewModel.fetchSubmissions(assignmentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Submissions: $assignmentTitle") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.submissions) { submission ->
                        // Reusing the ItemCard with real data from the ViewModel
                        SubmissionItemCard(
                            submission = SubmissionState(
                                groupName = submission.groupId.groupName,
                                status = if (submission.fileUrl.isNotEmpty()) "Submitted" else "Pending",
                                grade = submission.grade
                            ),
                            onGradeClick = {
                                selectedSubmissionForGrading = submission._id
                                selectedGroupName = submission.groupId.groupName
                            }
                        )
                    }
                }
            }
        }

        // Grading logic using the submission ID
        selectedSubmissionForGrading?.let { submissionId ->
            GradeFeedbackDialog(
                groupName = selectedGroupName,
                onDismiss = { selectedSubmissionForGrading = null },
                onConfirm = { grade, feedback ->
                    viewModel.submitGrade(submissionId, grade, feedback)
                    selectedSubmissionForGrading = null
                }
            )
        }
    }
}