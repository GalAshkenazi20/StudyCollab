package com.example.studycollab.ui.chat

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.studycollab.utils.mouseWheelScroll
import android.util.Log

@Composable
fun PeerGroupConsultationScreen(
    navController: NavController,
    courseId: String,
    originGroupId: String,
    viewModel: StudyGroupViewModel
) {
    val listState = rememberLazyListState()
    var visible by remember { mutableStateOf(false) }

    // 1. Fetch ALL groups in the course when the screen opens
    LaunchedEffect(courseId) {
        viewModel.fetchGroupsByCourse(courseId)
        visible = true
    }

    // 2. Filter the results from the course-wide list in the ViewModel
    // Note: We use coursePeerGroups instead of myGroups
    val peerGroups = viewModel.coursePeerGroups.filter { group ->

        fun getCleanId(id: Any?): String {
            val str = id.toString().replace("\"", "")
            return when {
                str.contains("_id:") -> str.substringAfter("_id:").substringBefore(",").trim()
                str.contains("\$oid:") -> str.substringAfter("\$oid:").substringBefore("}").trim()
                else -> str.trim()
            }
        }

        val currentGroupId = getCleanId(group._id)
        val targetOriginGroupId = originGroupId.replace("\"", "").trim()

        // Only exclude the group I am currently in
        val isNotMe = currentGroupId != targetOriginGroupId

        Log.d("PeerDebug", "Checking Peer: ${group.name} | ID: $currentGroupId vs $targetOriginGroupId | Keep: $isNotMe")

        isNotMe
    }

    // 3. Handle navigation when the ViewModel resolves a room
    LaunchedEffect(viewModel.consultationTargetRoute) {
        viewModel.consultationTargetRoute?.let { route ->
            Log.d("PeerNav", "Successfully navigating to: $route")
            navController.navigate(route)
            viewModel.clearConsultationRoute() // Reset after navigation
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { 30 }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Text(
                text = "PEER NETWORK",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Course Groups",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (peerGroups.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Text("No other groups found for this course.")
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .mouseWheelScroll(listState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(peerGroups) { group ->
                        ConsultationGroupCard(
                            name = group.name ?: "Unnamed Group",
                            purpose = group.purpose ?: ""
                        ) {
                            // Call the resolution logic in ViewModel
                            viewModel.openPeerConsultation(
                                myGroupId = originGroupId,
                                targetGroupId = group._id.toString(),
                                targetGroupName = group.name ?: "Peer Group"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConsultationGroupCard(name: String, purpose: String, onConsult: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = purpose, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(
                onClick = onConsult,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Chat, contentDescription = "Consult")
            }
        }
    }
}