package com.example.studycollab.ui.courses

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.core.content.ContextCompat
import com.example.studycollab.ui.components.ModernListButton
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAssignmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, fileUri: Uri?, deadline: Long) -> Unit
) {
    val context = LocalContext.current

    // --- STATE ---
    var title by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("No attachment") }

    // Deadline State (Default to tomorrow)
    val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    var selectedDeadline by remember { mutableStateOf(calendar.timeInMillis) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showSourceSelector by remember { mutableStateOf(false) }

    // --- SENSOR LAUNCHERS ---
    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedFileUri = it
            fileName = it.lastPathSegment ?: "File selected"
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            fileName = "Camera_Capture.jpg"
            // For now, we simulate selection. In production, save bitmap to temp Uri.
            Toast.makeText(context, "Photo captured!", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) cameraLauncher.launch()
        else Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
    }

    // --- DATE PICKER DIALOG ---
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDeadline)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDeadline = datePickerState.selectedDateMillis ?: selectedDeadline
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // --- ATTACHMENT SOURCE SELECTOR (Sub-Dialog) ---
    if (showSourceSelector) {
        AlertDialog(
            onDismissRequest = { showSourceSelector = false },
            title = { Text("Attachment Source", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    ModernListButton("Camera", Icons.Default.CameraAlt) {
                        val check = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (check == PackageManager.PERMISSION_GRANTED) cameraLauncher.launch()
                        else permissionLauncher.launch(Manifest.permission.CAMERA)
                        showSourceSelector = false
                    }
                    ModernListButton("Files", Icons.Default.FileUpload) {
                        filePickerLauncher.launch("*/*")
                        showSourceSelector = false
                    }
                }
            },
            confirmButton = {},
            shape = RoundedCornerShape(24.dp)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Assignment", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // 1. Assignment Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // 2. Deadline Box (Modern Interactive UI)
                Text("Submission Deadline", style = MaterialTheme.typography.labelMedium)
                Surface(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        Text(text = formatter.format(Date(selectedDeadline)), fontWeight = FontWeight.SemiBold)
                    }
                }

                // 3. Attachment Section
                Text("Resource Attachment", style = MaterialTheme.typography.labelMedium)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = fileName,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(
                            onClick = { showSourceSelector = true },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Add")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, selectedFileUri, selectedDeadline) },
                enabled = title.isNotBlank(), // Note: Made attachment optional for lecturers
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Publish & Notify")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}