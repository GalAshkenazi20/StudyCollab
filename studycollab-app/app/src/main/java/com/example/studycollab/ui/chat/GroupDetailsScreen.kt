package com.example.studycollab.ui.chat

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.studycollab.ui.Screen
import com.example.studycollab.utils.UserSession
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    groupId: String,
    viewModel: StudyGroupViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var visible by remember { mutableStateOf(false) }

    // ניקוי ID מגרשיים אם יש
    val cleanGroupId = groupId.replace("\"", "")

    // מציאת הקבוצה בצורה בטוחה
    val group = viewModel.myGroups.find { g ->
        g._id.toString().replace("\"", "") == cleanGroupId
    }

    val currentUserId = UserSession.userId ?: ""

    // בדיקת אדמין בטוחה
    val isAdmin = group?.members?.any {
        it.userId.toString().replace("\"", "") == currentUserId && it.role == "admin"
    } == true

    LaunchedEffect(viewModel.consultationTargetRoute) {
        viewModel.consultationTargetRoute?.let { route ->
            // Use a single Log to track all navigation attempts
            android.util.Log.d("GroupDetails_Debug", "Navigating to: $route")

            navController.navigate(route)

            // IMPORTANT: Reset the state so the navigation doesn't re-trigger on recomposition
            viewModel.consultationTargetRoute = null
        }
    }

    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study Group Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(500)) + slideInVertically(initialOffsetY = { 30 }),
            modifier = Modifier.padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                if (group != null) {
                    // --- כותרת ---
                    Text(
                        text = "STUDY GROUP",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- כרטיס מטרה ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Purpose",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = group.purpose ?: "General Purpose",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            // התיקון הגדול: הצגה בטוחה של Description
                            if (!group.description.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = group.description!!,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Collaboration Tools",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- כפתורים ראשיים ---
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { navController.navigate(Screen.GroupTasks.createRoute(cleanGroupId)) },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Assignment, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Tasks")
                        }

                        Button(
                            onClick = {
                                // Resolve the standard room ID before navigating
                                viewModel.openStandardChat(cleanGroupId)
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !viewModel.isLoading // Prevent multiple clicks
                        ) {
                            if (viewModel.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.AutoMirrored.Filled.Chat, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Chat")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { navController.navigate(Screen.Participants.createRoute(cleanGroupId)) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Participants")
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- תמיכה חיצונית (מרצה) ---
                    Text(
                        text = "External Support",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.consultWithLecturer(cleanGroupId)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !viewModel.isLoading
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connecting...")
                        } else {
                            Icon(Icons.Default.School, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Consult with Lecturer")
                        }
                    }

                    if (viewModel.errorMessage != null) {
                        Text(
                            text = viewModel.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // --- ניהול (Admin) ---
                    if (isAdmin) {
                        Spacer(modifier = Modifier.height(40.dp))
                        Text(text = "Administration", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.deleteGroup(groupId = cleanGroupId, onComplete = {
                                    navController.popBackStack()
                                })
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Delete Group")
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))

                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Group info not found.")
                    }
                }
            }
        }
    }
}