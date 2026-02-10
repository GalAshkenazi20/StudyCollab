package com.example.studycollab.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.studycollab.ui.auth.*
import com.example.studycollab.ui.chat.*
import com.example.studycollab.ui.courses.*
import com.example.studycollab.ui.home.HomeScreen
import com.example.studycollab.ui.notifications.*
import com.example.studycollab.ui.tasks.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // --- SHARED VIEWMODELS ---
    // Declaring these here allows state to persist across screens
    val studyViewModel: StudyGroupViewModel = viewModel()
    val assignmentViewModel: AssignmentViewModel = viewModel()
    val notifViewModel: NotificationViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Login.route) {

        // --- AUTH ---
        composable(Screen.Login.route) {
            LoginScreen(viewModel()) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }

        // --- HOME & NOTIFICATIONS ---
        composable(Screen.Home.route) {
            HomeScreen(navController, notifViewModel)
        }

        composable(Screen.Notifications.route) {
            NotificationScreen(viewModel = notifViewModel, onBackClick = { navController.popBackStack() })
        }

        // --- STUDY GROUPS ---
        composable(Screen.StudyGroups.route) {
            StudyGroupScreen(navController, studyViewModel)
        }

        composable(Screen.CreateStudyGroup.route) {
            CreateGroupScreen(viewModel = studyViewModel, onBackClick = { navController.popBackStack() })
        }

        composable(
            route = Screen.GroupDetails.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            GroupDetailsScreen(groupId, studyViewModel, navController)
        }

        composable(
            route = Screen.Participants.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            ParticipantsScreen(groupId, studyViewModel, navController)
        }

        // --- TASKS & ASSIGNMENTS (FR-6) ---
        composable(
            route = Screen.GroupTasks.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""

            // Safe extraction: finds the courseId even if the _id is a JsonObject
            val group = studyViewModel.myGroups.find { it._id.toString().contains(groupId) }
            val courseId = group?.courseId?.toString()?.replace("\"", "")
                ?.replace("{", "")?.replace("}", "")?.filter { it.isLetterOrDigit() } ?: ""

            TaskListScreen(navController, courseId, assignmentViewModel)
        }

        composable(
            route = Screen.GroupTaskDetails.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("assignmentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            val assignmentId = backStackEntry.arguments?.getString("assignmentId") ?: ""
            SubTaskDetailScreen(navController, groupId, assignmentId, assignmentViewModel, studyViewModel)
        }

        // --- COURSES ---
        composable(Screen.Courses.route) {
            CourseListScreen(navController, studyViewModel)
        }

        composable(
            route = Screen.CourseDetail.route,
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("code") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val code = backStackEntry.arguments?.getString("code") ?: ""
            CourseDetailScreen(navController, name, code, studyViewModel)
        }

        // --- CHAT ---
        composable(
            route = Screen.ChatRoom.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            ChatScreen(navController, groupId)
        }

        composable(Screen.Chats.route) { ChatListScreen(navController) }
        composable(Screen.Timetable.route) { TimetableScreen(navController) }
    }
}