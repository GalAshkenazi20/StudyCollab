package com.example.studycollab.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.studycollab.ui.auth.AuthViewModel
import com.example.studycollab.ui.auth.LoginScreen
import com.example.studycollab.ui.chat.CreateGroupScreen
import com.example.studycollab.ui.chat.StudyGroupScreen
import com.example.studycollab.ui.courses.CourseListScreen
import com.example.studycollab.ui.home.HomeScreen
import com.example.studycollab.ui.tasks.TimetableScreen
// Import other screens as you create them...

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Login.route) {

        // Login
        composable(Screen.Login.route) {
            val authViewModel: AuthViewModel = viewModel()
            LoginScreen(authViewModel) {
                navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } }
            }
        }

        // Home
        composable(Screen.Home.route) { HomeScreen(navController) }

        // Study Groups
        composable(Screen.StudyGroups.route) { StudyGroupScreen(navController) }
        composable(Screen.CreateStudyGroup.route) { CreateGroupScreen { navController.popBackStack() } }

        // Courses
        composable(Screen.Courses.route) { CourseListScreen(navController) }

        // Course Detail (Dynamic Route)
        composable(
            route = Screen.CourseDetail.route,
            arguments = listOf(navArgument("courseName") { type = NavType.StringType })
        ) { backStackEntry ->
            // In a real app, you'd use this name to fetch data
            val courseName = backStackEntry.arguments?.getString("courseName") ?: "Course"
            // You can create a simple generic screen for this:
            // CourseDetailScreen(navController, courseName)
        }

        // Timetable
        composable(Screen.Timetable.route) { TimetableScreen(navController) }

        // Add Chat/Notifications placeholders here...
        composable(Screen.Chats.route) { /* Create ChatListScreen similar to others */ }
        composable(Screen.Notifications.route) { /* Create NotificationScreen similar to others */ }
    }
}