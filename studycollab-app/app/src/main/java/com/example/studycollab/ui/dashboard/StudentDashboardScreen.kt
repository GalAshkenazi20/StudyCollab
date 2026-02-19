package com.example.studycollab.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studycollab.ui.Screen
import com.example.studycollab.utils.UserSession

@Composable
fun StudentDashboardScreen(navController: NavController) {
    val studentName = UserSession.userName ?: "Student"

    // Scaffold removed - now handled by AppNavigation
    Column(
        modifier = Modifier
            .fillMaxSize()
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
            item { DashboardCard("Study Groups", Icons.Default.Groups) { navController.navigate(Screen.StudyGroups.route) } }
            item { DashboardCard("Chats", Icons.Default.Chat) { navController.navigate(Screen.Chats.route) } }
            item { DashboardCard("My Courses", Icons.Default.MenuBook) { navController.navigate(Screen.Courses.route) } }
            item { DashboardCard("Timetable", Icons.Default.DateRange) { navController.navigate(Screen.Timetable.route) } }
            item { DashboardCard("Book Office Hours", Icons.Default.Event) { navController.navigate(Screen.BookOfficeHours.route) } }
        }
    }
}