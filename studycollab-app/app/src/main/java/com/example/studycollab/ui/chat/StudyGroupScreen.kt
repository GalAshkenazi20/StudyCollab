package com.example.studycollab.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studycollab.data.model.getCourseName
import com.example.studycollab.ui.Screen
import com.example.studycollab.utils.UserSession
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyGroupScreen(
    navController: NavController,
    viewModel: StudyGroupViewModel // Received as a parameter from AppNavigation
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Trigger data load only when the screen is first composed
    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))

                // Using UserSession.userName (Adjust if you kept the nested .UserSession structure)
                val userName = UserSession.userName ?: "Guest"
                Text(
                    text = "Hello, $userName",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("My Profile") },
                    selected = false,
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    onClick = { /* Future Profile Implementation */ }
                )

                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            // Proper Logout logic here
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0)
                            }
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("קבוצות הלמידה שלי") }, // Matches SRS Hebrew requirements
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                if (viewModel.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (viewModel.myGroups.isEmpty()) {
                    EmptyGroupPlaceholder()
                } else {
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
                                // --- FIXED NAVIGATION ---
                                // Using the centralized createRoute helper to prevent crashes
                                navController.navigate(Screen.GroupDetails.createRoute(group._id))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyGroupPlaceholder() {
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
                    text = "Course: ${group.getCourseName()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}