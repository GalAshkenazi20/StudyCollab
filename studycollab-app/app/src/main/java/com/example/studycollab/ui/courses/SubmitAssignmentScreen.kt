package com.example.studycollab.ui.courses

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitAssignmentScreen(
    navController: NavController,
    assignmentId: String,
    courseId: String
) {
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("No file selected") }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
        fileName = uri?.lastPathSegment ?: "File selected"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Submit Assignment") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (submitted) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF4CAF50)
                )
                Spacer(Modifier.height(16.dp))
                Text("Submitted Successfully!", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("Back to Assignments")
                }
            } else {
                Text(
                    "Select your solution file",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(24.dp))

                // File selection card
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = fileName,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        IconButton(onClick = { filePickerLauncher.launch("application/pdf") }) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Attach File")
                        }
                    }
                }

                if (selectedFileUri == null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "* PDF file is required",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        isSubmitting = true
                        // TODO: Call the upload API via ViewModel
                        // After success, set: submitted = true, isSubmitting = false
                    },
                    enabled = selectedFileUri != null && !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Submit")
                    }
                }
            }
        }
    }
}