package com.example.studycollab.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.studycollab.ui.Screen
import com.example.studycollab.utils.UserSession

@Composable
fun StudentDashboardScreen(navController: NavController) {
    val studentName = UserSession.userName ?: "Student"
    var visible by remember { mutableStateOf(false) }

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
            // Modern Header
            Text(
                text = "Welcome back,",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = studentName,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Animated Grid
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
                        ModernDashboardCard("Study Groups", Icons.Default.Groups, MaterialTheme.colorScheme.primary) {
                            navController.navigate(Screen.StudyGroups.route)
                        }
                    }
                    item {
                        ModernDashboardCard("Chats", Icons.Default.Chat, MaterialTheme.colorScheme.secondary) {
                            navController.navigate(Screen.Chats.route)
                        }
                    }
                    item {
                        ModernDashboardCard("My Courses", Icons.Default.MenuBook, MaterialTheme.colorScheme.tertiary) {
                            navController.navigate(Screen.Courses.route)
                        }
                    }
                    item {
                        ModernDashboardCard("Timetable", Icons.Default.DateRange, MaterialTheme.colorScheme.error) {
                            navController.navigate(Screen.Timetable.route)
                        }
                    }
                    item {
                        ModernDashboardCard("Office Hours", Icons.Default.Event, MaterialTheme.colorScheme.primary) {
                            navController.navigate(Screen.BookOfficeHours.route)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernDashboardCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .height(150.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = accentColor
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}