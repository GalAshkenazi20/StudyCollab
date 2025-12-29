package com.example.studycollab.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.studycollab.ui.chat.StudyGroupViewModel
import com.example.studycollab.ui.courses.CourseListScreen
import com.example.studycollab.ui.home.HomeScreen
import com.example.studycollab.ui.tasks.TimetableScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Login.route) {

        // Login
        composable(Screen.Login.route) {
            val authViewModel: AuthViewModel = viewModel()
            LoginScreen(authViewModel) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }

        // Home
        composable(Screen.Home.route) { HomeScreen(navController) }

        // --- SHARED VIEWMODEL SECTION ---
        // We initialize the ViewModel outside the individual routes so it persists
        // while navigating between StudyGroupScreen and CreateGroupScreen.

        composable(Screen.StudyGroups.route) {
            val groupViewModel: StudyGroupViewModel = viewModel()
            StudyGroupScreen(
                navController = navController,
                viewModel = groupViewModel
            )
        }

        composable(Screen.CreateStudyGroup.route) {
            // By calling viewModel() here, Compose finds the existing instance
            // created in the StudyGroups route above.
            val groupViewModel: StudyGroupViewModel = viewModel()

            CreateGroupScreen(
                viewModel = groupViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
        // --------------------------------

        // Courses
        composable(Screen.Courses.route) { CourseListScreen(navController) }

        // Course Detail (Dynamic Route)
        composable(
            route = Screen.CourseDetail.route,
            arguments = listOf(navArgument("courseName") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseName = backStackEntry.arguments?.getString("courseName") ?: "Course"
        }

        // Timetable
        composable(Screen.Timetable.route) { TimetableScreen(navController) }

        // Placeholders
        composable(Screen.Chats.route) { /* ChatListScreen */ }
        composable(Screen.Notifications.route) { /* NotificationScreen */ }
    }
}