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
    val studyViewModel: StudyGroupViewModel = viewModel()
    val assignmentViewModel: AssignmentViewModel = viewModel()
    val notifViewModel: NotificationViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Login.route) {

        composable(Screen.Login.route) {
            LoginScreen(viewModel()) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }

        composable(Screen.Home.route) { HomeScreen(navController, notifViewModel) }

        composable(Screen.Notifications.route) {
            NotificationScreen(viewModel = notifViewModel, onBackClick = { navController.popBackStack() })
        }

        composable(Screen.StudyGroups.route) { StudyGroupScreen(navController, studyViewModel) }

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

        // --- GROUP TASKS LIST ---
        composable(
            route = Screen.GroupTasks.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""

            val group = studyViewModel.myGroups.find { g ->
                g._id.toString().filter { it.isLetterOrDigit() } == groupId.filter { it.isLetterOrDigit() }
            }

            val courseId = group?.courseId?.toString()?.replace("\"", "")
                ?.replace("{", "")?.replace("}", "")?.filter { it.isLetterOrDigit() } ?: ""

            TaskListScreen(
                navController = navController,
                courseId = courseId,
                groupId = groupId,
                viewModel = assignmentViewModel,
                studyViewModel = studyViewModel
            )
        }

        // --- FIXED: GROUP TASK DETAIL (SHOW ONLY ONE TASK) ---
        composable(
            // FIXED: Explicit route string to prevent destination mismatch crashes
            route = "group_task_details/{groupId}/{subTaskId}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("subTaskId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            val subTaskId = backStackEntry.arguments?.getString("subTaskId") ?: ""

            // Correctly passing subTaskId to filter the view
            SubTaskDetailScreen(
                navController = navController,
                groupId = groupId,
                subTaskId = subTaskId,
                viewModel = assignmentViewModel,
                studyViewModel = studyViewModel
            )
        }

        composable(Screen.Courses.route) { CourseListScreen(navController, studyViewModel) }

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