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
import com.example.studycollab.ui.chat.GroupDetailsScreen
import com.example.studycollab.ui.chat.ParticipantsScreen
import com.example.studycollab.ui.chat.StudyGroupScreen
import com.example.studycollab.ui.chat.StudyGroupViewModel
import com.example.studycollab.ui.courses.CourseListScreen
import com.example.studycollab.ui.home.HomeScreen
import com.example.studycollab.ui.notifications.NotificationScreen
import com.example.studycollab.ui.notifications.NotificationViewModel
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

        // --- SHARED NOTIFICATION VIEWMODEL ---
        // We define this here so it can be passed to both Home and Notifications
        composable(Screen.Home.route) {
            val notifViewModel: NotificationViewModel = viewModel()
            HomeScreen(navController, notifViewModel)
        }

        composable(Screen.Notifications.route) {
            val notifViewModel: NotificationViewModel = viewModel()
            NotificationScreen(
                viewModel = notifViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- SHARED STUDY GROUP VIEWMODEL ---
        composable(Screen.StudyGroups.route) {
            val groupViewModel: StudyGroupViewModel = viewModel()
            StudyGroupScreen(navController = navController, viewModel = groupViewModel)
        }

        composable(Screen.CreateStudyGroup.route) {
            val groupViewModel: StudyGroupViewModel = viewModel()
            CreateGroupScreen(viewModel = groupViewModel, onBackClick = { navController.popBackStack() })
        }


        // Add this to your NavHost in AppNavigation.kt
        composable(
            route = "group_details/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            val groupViewModel: StudyGroupViewModel = viewModel()

            // We will create this screen next
            GroupDetailsScreen(
                groupId = groupId,
                viewModel = groupViewModel,
                navController = navController
            )
        }

        // In AppNavigation.kt NavHost
        composable("participants/{groupId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("groupId") ?: ""
            ParticipantsScreen(id, viewModel(), navController)
        }

        // --- OTHER ROUTES ---
        composable(Screen.Courses.route) { CourseListScreen(navController) }

        composable(
            route = Screen.CourseDetail.route,
            arguments = listOf(navArgument("courseName") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseName = backStackEntry.arguments?.getString("courseName") ?: "Course"
        }

        composable(Screen.Timetable.route) { TimetableScreen(navController) }
        composable(Screen.Chats.route) { /* ChatListScreen */ }
    }
}