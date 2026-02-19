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

@Composable
fun PeerGroupConsultationScreen(
    navController: NavController,
    courseId: String,
    originGroupId: String,
    viewModel: StudyGroupViewModel
) {
    val listState = rememberLazyListState()
    var visible by remember { mutableStateOf(false) }

    // Logic: In a real app, you would fetch all groups for this courseId.
    // For now, we filter the groups currently loaded in the ViewModel.
    val peerGroups = viewModel.myGroups.filter {
        val sameCourse = it.courseId?.toString()?.filter { char -> char.isLetterOrDigit() } == courseId.filter { char -> char.isLetterOrDigit() }
        val isNotMe = it._id.toString().filter { char -> char.isLetterOrDigit() } != originGroupId.filter { char -> char.isLetterOrDigit() }
        sameCourse && isNotMe
    }

    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { 30 }) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Text(text = "PEER NETWORK", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(text = "Course Groups", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)

            Spacer(modifier = Modifier.height(24.dp))

            if (peerGroups.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No other groups found for this course.")
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().mouseWheelScroll(listState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(peerGroups) { group ->
                        ConsultationGroupCard(group.name ?: "Unnamed Group", group.purpose ?: "") {
                            // Logic: Navigate to a chat room shared between groups
                            navController.navigate("chat/group_consultation/${group._id}")
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