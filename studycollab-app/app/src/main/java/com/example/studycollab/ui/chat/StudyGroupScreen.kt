package com.example.studycollab.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.studycollab.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyGroupScreen(
    navController: NavController,
    viewModel: StudyGroupViewModel = viewModel()
) {
    // Refresh the group list every time the user enters this screen
    LaunchedEffect(Unit) {
        // In a real app, this ID comes from your login session
        viewModel.fetchGroups("CURRENT_USER_ID")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Study Groups") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.CreateStudyGroup.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Group")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // 1. Loading Indicator
            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            // 2. Empty State: No real groups found in the repository
            else if (viewModel.myGroups.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No active study groups yet.",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Tap the + button to create your first group.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            // 3. Success State: Display the real list of created groups
            else {
                Text(
                    text = "Your Groups (${viewModel.myGroups.size})",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(viewModel.myGroups) { group ->
                        StudyGroupCard(group = group) {
                            // Navigation to Chat will go here
                            // navController.navigate("group_chat/${group._id}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudyGroupCard(
    group: com.example.studycollab.data.model.StudyGroup,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = group.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        text = group.purpose.uppercase(),
                        modifier = Modifier.padding(4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Course: ${group.courseId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}