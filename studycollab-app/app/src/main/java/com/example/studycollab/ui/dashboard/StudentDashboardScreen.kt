package com.example.studycollab.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studycollab.ui.Screen
import com.example.studycollab.utils.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(navController: NavController) {
    // Welcoming the student by name from the active session
    val studentName = UserSession.userName ?: "Student"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = {
                        UserSession.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Welcome, $studentName",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    DashboardCard("Study Groups", Icons.Default.Groups) {
                        navController.navigate(Screen.StudyGroups.route)
                    }
                }
                item {
                    DashboardCard("Chats", Icons.Default.Chat) {
                        // FIXED: Uses Screen.Chats.route to match NavHost
                        navController.navigate(Screen.Chats.route)
                    }
                }
                item {
                    DashboardCard("My Courses", Icons.Default.MenuBook) {
                        navController.navigate(Screen.Courses.route)
                    }
                }
                item {
                    DashboardCard("Timetable", Icons.Default.DateRange) {
                        navController.navigate(Screen.Timetable.route)
                    }
                }
                item {
                    DashboardCard("Book Office Hours", Icons.Default.Event) {
                        navController.navigate(Screen.BookOfficeHours.route)
                    }
                }
            }
        }
    }
}