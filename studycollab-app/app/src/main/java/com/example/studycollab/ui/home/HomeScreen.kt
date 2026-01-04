package com.example.studycollab.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studycollab.ui.Screen
import com.example.studycollab.utils.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("StudyCollab Dashboard") },
                navigationIcon = {
                    // LOGOUT BUTTON (Top Left)
                    IconButton(onClick = {
                        // Navigate back to Login and clear history
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
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
            verticalArrangement = Arrangement.spacedBy(16.dp) // Space between buttons
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            val displayName = UserSession.UserSession.userName ?: "Student"

            Text(
                text = "Welcome, $displayName",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(20.dp))

            // The 5 Main Buttons
            MenuButton("Study Groups") { navController.navigate(Screen.StudyGroups.route) }
            MenuButton("Chats") { navController.navigate(Screen.Chats.route) }
            MenuButton("Courses") { navController.navigate(Screen.Courses.route) }
            MenuButton("Notifications") { navController.navigate(Screen.Notifications.route) }
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