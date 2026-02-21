package com.example.studycollab.ui.chat

import android.net.Uri
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.studycollab.data.model.getLecturerName
import com.example.studycollab.data.model.getParticipantId
import com.example.studycollab.data.remote.ApiClient
import com.example.studycollab.ui.Screen
import com.example.studycollab.ui.courses.ModernActionButton
import com.example.studycollab.utils.UserSession
import com.example.studycollab.utils.mouseWheelScroll
import kotlinx.coroutines.launch

@Composable
fun GroupDetailsScreen(
    groupId: String,
    viewModel: StudyGroupViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(false) }

    // Finding the group based on robust string comparison
    val group = viewModel.myGroups.find { g ->
        g._id.toString().filter { it.isLetterOrDigit() }.contains(groupId.filter { it.isLetterOrDigit() })
    }

    val currentUserId = UserSession.userId ?: ""
    val isAdmin = group?.members?.any {
        it.getParticipantId() == currentUserId && it.role == "admin"
    } == true

    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(initialOffsetY = { 30 })
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .mouseWheelScroll(scrollState)
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            group?.let { g ->
                // --- Modern Header ---
                Text(
                    text = "STUDY GROUP",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = g.name ?: "Group Details",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- Purpose Card ---
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
                            text = g.purpose ?: "No purpose specified",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (!g.description.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = g.description, style = MaterialTheme.typography.bodyMedium)
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

                // --- CORE ACTION BUTTONS ---
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { navController.navigate(Screen.GroupTasks.createRoute(groupId)) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Assignment, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Tasks")
                    }

                    Button(
                        onClick = { navController.navigate(Screen.ChatRoom.createRoute(groupId)) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Chat")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                ModernActionButton("Participants", Icons.Default.People) {
                    navController.navigate(Screen.Participants.createRoute(groupId))
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- EXTERNAL SUPPORT SECTION ---
                Text(
                    text = "External Support",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                ModernActionButton("Consult with Lecturer", Icons.Default.CastForEducation) {
                    scope.launch {
                        try {
                            val rawCourseId = g.courseId?.toString()?.filter { it.isLetterOrDigit() } ?: ""

                            val lecturerResponse = ApiClient.apiService.getCourseLecturer(rawCourseId)
                            if (lecturerResponse.isSuccessful) {
                                val lecturerId = lecturerResponse.body()?.get("lecturerId") ?: ""
                                val lecturerName = lecturerResponse.body()?.get("name") ?: "Lecturer"

                                val response = viewModel.setupConsultation(groupId, lecturerId)
                                if (response != null) {
                                    val encodedName = Uri.encode(lecturerName)
                                    navController.navigate("chat/lecturer_consultation/${response._id}/$encodedName")
                                } else {
                                    Toast.makeText(context, "Could not start consultation", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Lecturer not found for this course", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                // --- ADMINISTRATION SECTION ---
                if (isAdmin) {
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(text = "Administration", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { /* Add Members Logic */ },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Members")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.deleteGroup(groupId = groupId, onComplete = {
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

            } ?: Box(modifier = Modifier.fillMaxSize()) {
                Text("Group info not found. Return to list and try again.", modifier = Modifier.padding(20.dp))
            }
        }
    }
}