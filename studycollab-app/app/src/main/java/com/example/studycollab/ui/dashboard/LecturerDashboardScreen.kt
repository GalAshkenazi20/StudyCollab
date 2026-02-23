package com.example.studycollab.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.studycollab.ui.Screen
import com.example.studycollab.ui.chat.LecturerChatViewModel
import com.example.studycollab.utils.UserSession

@Composable
fun LecturerDashboardScreen(navController: NavController,
                            chatViewModel: LecturerChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val lecturerName = UserSession.userName ?: "Lecturer"
    var visible by remember { mutableStateOf(false) }
    val lecturerId = UserSession.userId ?: ""

    LaunchedEffect(lecturerId) {
        if (lecturerId.isNotEmpty()) {
            chatViewModel.fetchConsultations(lecturerId)
        }
    }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "Faculty Panel",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "Prof. $lecturerName",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { 50 })
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        ModernDashboardCard("Courses", Icons.Default.MenuBook, MaterialTheme.colorScheme.primary) {
                            navController.navigate(Screen.LecturerCourses.route)
                        }
                    }
                    item {
                        ModernDashboardCard("Oversight", Icons.Default.Groups, MaterialTheme.colorScheme.secondary) {
                            navController.navigate("lecturer_group_oversight")
                        }
                    }
                    item {
                        ModernDashboardCard("Slots", Icons.Default.Event, MaterialTheme.colorScheme.tertiary) {
                            navController.navigate(Screen.OfficeHoursScheduler.route)
                        }
                    }
                    item {
                        ModernDashboardCard("Schedule", Icons.Default.DateRange, MaterialTheme.colorScheme.error) {
                            navController.navigate(Screen.Timetable.route)
                        }
                    }
                    item {
                        ModernDashboardCard("Messages", Icons.Default.Chat, MaterialTheme.colorScheme.primary) {
                            navController.navigate("lecturer_consultations_list")
                        }
                    }
                }
            }
        }
    }
}