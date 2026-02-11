package com.example.studycollab.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studycollab.ui.Screen
import com.example.studycollab.utils.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturerDashboardScreen(navController: NavController) {
    val lecturerName = UserSession.userName ?: "Lecturer"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lecturer Dashboard") },
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
                text = "Welcome, Prof. $lecturerName",
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
                    DashboardCard("My Courses", Icons.Default.MenuBook) {
                        // FIXED: Points to Lecturer-specific course management
                        navController.navigate(Screen.LecturerCourses.route)
                    }
                }
                item {
                    DashboardCard("Group Oversight", Icons.Default.Groups) {
                        // God-view for lecturers to see all student groups
                        navController.navigate(Screen.StudyGroups.route)
                    }
                }
                item {
                    DashboardCard("Office Hours", Icons.Default.Event) {
                        navController.navigate(Screen.OfficeHoursScheduler.route)
                    }
                }
                item {
                    DashboardCard("Schedule", Icons.Default.DateRange) {
                        navController.navigate(Screen.Timetable.route)
                    }
                }
                item {
                    DashboardCard("Chats", Icons.Default.Chat) {
                        // FIXED: Uses synchronized route to prevent chat_list crash
                        navController.navigate(Screen.Chats.route)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .height(140.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary // Corrected parameter name
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}