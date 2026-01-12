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
import com.example.studycollab.ui.chat.GroupDetailsScreen
import com.example.studycollab.ui.chat.ParticipantsScreen
import com.example.studycollab.ui.chat.StudyGroupScreen
import com.example.studycollab.ui.chat.StudyGroupViewModel
import com.example.studycollab.ui.courses.CourseDetailScreen
import com.example.studycollab.ui.courses.CourseListScreen
import com.example.studycollab.ui.home.HomeScreen
import com.example.studycollab.ui.notifications.NotificationScreen
import com.example.studycollab.ui.notifications.NotificationViewModel
import com.example.studycollab.ui.tasks.TimetableScreen
// וודא שיש לך את הייבוא הזה (או שהשורה למטה תשתמש בנתיב המלא)
import com.example.studycollab.ui.chat.ChatListScreen
import com.example.studycollab.ui.chat.ChatScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Login.route) {

        // --- LOGIN ---
        composable(Screen.Login.route) {
            val authViewModel: AuthViewModel = viewModel()
            LoginScreen(authViewModel) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }

        // --- HOME & NOTIFICATIONS ---
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

        // --- STUDY GROUPS ---
        composable(Screen.StudyGroups.route) {
            val groupViewModel: StudyGroupViewModel = viewModel()
            StudyGroupScreen(navController = navController, viewModel = groupViewModel)
        }

        composable(Screen.CreateStudyGroup.route) {
            val groupViewModel: StudyGroupViewModel = viewModel()
            CreateGroupScreen(viewModel = groupViewModel, onBackClick = { navController.popBackStack() })
        }

        composable(
            route = "group_details/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            val groupViewModel: StudyGroupViewModel = viewModel()

            GroupDetailsScreen(
                groupId = groupId,
                viewModel = groupViewModel,
                navController = navController
            )
        }

        composable("participants/{groupId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("groupId") ?: ""
            ParticipantsScreen(id, viewModel(), navController)
        }

        // --- COURSES ---
        composable(Screen.Courses.route) {
            CourseListScreen(navController)
        }

        composable(
            route = "course_detail/{name}/{code}",
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("code") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val code = backStackEntry.arguments?.getString("code") ?: ""

            CourseDetailScreen(
                navController = navController,
                courseName = name,
                courseCode = code
            )
        }

        // --- CHAT ROOM (Specific Group) ---
        composable(
            route = "chat/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""

            com.example.studycollab.ui.chat.ChatScreen(
                navController = navController,
                groupId = groupId
            )
        }

        // --- TIMETABLE ---
        composable(Screen.Timetable.route) { TimetableScreen(navController) }
        composable(Screen.Chats.route) {
            com.example.studycollab.ui.chat.ChatListScreen(navController = navController)
        }
    }
}