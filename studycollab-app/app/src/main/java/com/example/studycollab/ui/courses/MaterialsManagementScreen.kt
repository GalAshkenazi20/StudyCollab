package com.example.studycollab.ui.courses

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.studycollab.ui.components.ModernListButton
import com.example.studycollab.utils.mouseWheelScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialsManagementScreen(
    navController: NavController,
    courseId: String,
    courseName: String,
    viewModel: LecturerCourseViewModel = viewModel()
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var showSourceSheet by remember { mutableStateOf(false) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    val listState = rememberLazyListState()
    val sheetState = rememberModalBottomSheetState()
    var visible by remember { mutableStateOf(false) }

    // --- SENSOR & PERMISSION LAUNCHERS ---

    // 1. The Camera Sensor Launcher
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        showSourceSheet = false
        if (bitmap != null) {
            // Success! In production, save bitmap to Uri and show name dialog
            Toast.makeText(context, "Image captured!", Toast.LENGTH_SHORT).show()
            showAddDialog = true
        }
    }

    // 2. The Permission "Security Guard"
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch()
        } else {
            Toast.makeText(context, "Camera permission is required to take photos.", Toast.LENGTH_LONG).show()
        }
    }

    // 3. File Picker Launcher
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            tempUri = it
            showSourceSheet = false
            showAddDialog = true
        }
    }

    LaunchedEffect(courseId) {
        viewModel.fetchMaterials(courseId)
        visible = true
    }

    // Modern Source Selection Sheet
    if (showSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSourceSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp, start = 20.dp, end = 20.dp)) {
                Text("Upload Material", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                ModernListButton("Take a Photo", Icons.Default.CameraAlt) {
                    // CHECK PERMISSION BEFORE SENSOR USE
                    when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
                        PackageManager.PERMISSION_GRANTED -> cameraLauncher.launch()
                        else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
                ModernListButton("Select a File", Icons.Default.UploadFile) {
                    fileLauncher.launch("*/*")
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(500)) + slideInVertically(initialOffsetY = { 30 })
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Text(text = courseName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(text = "Course Materials", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)

                Spacer(modifier = Modifier.height(24.dp))

                if (viewModel.materials.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No materials uploaded yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().mouseWheelScroll(listState),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(viewModel.materials) { material ->
                            MaterialItemCard(
                                name = material.title,
                                onDelete = { viewModel.deleteMaterial(material.id, courseId) }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showSourceSheet = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Material")
        }
    }

    // Dialog to give the uploaded file/photo a Title
    if (showAddDialog) {
        var titleText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Material Title", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter a name for this material", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        placeholder = { Text("e.g. Lecture 5 Slides") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        tempUri?.let { viewModel.uploadMaterial(courseId, titleText, it) }
                        showAddDialog = false
                    },
                    enabled = titleText.isNotBlank()
                ) {
                    Text("Upload")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun MaterialItemCard(name: String, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FilePresent, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}