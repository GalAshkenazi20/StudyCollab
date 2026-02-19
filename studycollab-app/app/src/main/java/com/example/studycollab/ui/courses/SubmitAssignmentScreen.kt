package com.example.studycollab.ui.courses

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.studycollab.ui.components.ModernListButton
import com.example.studycollab.utils.mouseWheelScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitAssignmentScreen(
    navController: NavController,
    assignmentId: String,
    courseId: String
) {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scrollState = rememberScrollState()

    // --- SENSOR & PERMISSION LAUNCHERS ---

    // 1. Camera Sensor Launcher
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        showSheet = false
        if (bitmap != null) {
            // Success! (Note: Real apps save bitmap to Uri here)
            Toast.makeText(context, "Photo captured!", Toast.LENGTH_SHORT).show()
            // Simulating a successful selection for the UI logic
        }
    }

    // 2. Permission Security Guard
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch()
        } else {
            Toast.makeText(context, "Camera permission is required to take a photo of your work.", Toast.LENGTH_LONG).show()
        }
    }

    // 3. File Selection Launcher
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedUri = it
            showSheet = false
        }
    }

    // Modern Entrance Animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Modern Selection Sheet
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp, start = 20.dp, end = 20.dp)) {
                Text("Select Submission Method", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                ModernListButton("Take a Photo", Icons.Default.CameraAlt) {
                    // SAFE SENSOR CHECK
                    when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
                        PackageManager.PERMISSION_GRANTED -> cameraLauncher.launch()
                        else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
                ModernListButton("Choose from Files", Icons.Default.UploadFile) {
                    fileLauncher.launch("*/*")
                }
            }
        }
    }

    AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { 30 }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .mouseWheelScroll(scrollState)
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            // Modern Header
            Text(
                text = "Course ID: $courseId",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Submit Assignment",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Upload Area Card - Modern Tonal Look
            Card(
                onClick = { showSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Dynamic icon based on selection state
                    val icon = if (selectedUri != null) Icons.Default.CheckCircle else Icons.Default.CloudUpload
                    val tint = if (selectedUri != null) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = tint
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (selectedUri != null) "Ready to Submit" else "Tap to Select File or Photo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    selectedUri?.let {
                        Text(
                            text = it.lastPathSegment ?: "File chosen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Modern Sticky Action Button
            Button(
                onClick = { /* TODO: Backend upload logic */ },
                enabled = selectedUri != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("Upload Submission", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}