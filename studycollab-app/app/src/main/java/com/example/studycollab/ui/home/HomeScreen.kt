package com.example.studycollab.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.studycollab.ui.Screen
import com.example.studycollab.ui.notifications.NotificationViewModel
import com.example.studycollab.utils.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, notificationViewModel: NotificationViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        notificationViewModel.refresh()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                },
                // --- NEW ACTION: NOTIFICATION BELL ---
                actions = {
                    // --- UPDATED NOTIFICATION BELL WITH BADGE ---
                    IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                        BadgedBox(
                            badge = {
                                if (notificationViewModel.notificationCount > 0) {
                                    Badge {
                                        Text(notificationViewModel.notificationCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val displayName = UserSession.UserSession.userName ?: "Student"
            Text("Welcome, $displayName", style = MaterialTheme.typography.headlineMedium)

            MenuButton("Study Groups") { navController.navigate(Screen.StudyGroups.route) }
            MenuButton("Chats") { navController.navigate(Screen.Chats.route) }
            MenuButton("Courses") { navController.navigate(Screen.Courses.route) }
            MenuButton("Classes Timetable") { navController.navigate(Screen.Timetable.route) }
        }
    }
}

@Composable
fun MenuButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(60.dp)
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}