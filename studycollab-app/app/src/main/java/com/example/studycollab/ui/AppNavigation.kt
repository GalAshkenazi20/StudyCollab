package com.example.studycollab.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.studycollab.ui.auth.*
import com.example.studycollab.ui.chat.*
import com.example.studycollab.ui.courses.*
import com.example.studycollab.ui.dashboard.LecturerDashboardScreen
import com.example.studycollab.ui.dashboard.StudentDashboardScreen
import com.example.studycollab.ui.notifications.*
import com.example.studycollab.ui.scheduler.LecturerOfficeHoursSlotsScreen
import com.example.studycollab.ui.submissions.SubmissionTrackingScreen
import com.example.studycollab.ui.submissions.SubmissionViewModel
import com.example.studycollab.ui.tasks.*
import com.example.studycollab.ui.scheduler.OfficeHoursSchedulerScreen
import com.example.studycollab.ui.scheduler.StudentOfficeHoursScreen
import com.example.studycollab.utils.UserSession
import com.example.studycollab.ui.timetable.TimeTableScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // --- SHARED VIEWMODELS ---
    val studyViewModel: StudyGroupViewModel = viewModel()
    val assignmentViewModel: AssignmentViewModel = viewModel()
    val notifViewModel: NotificationViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Login.route) {

        // --- AUTHENTICATION ---
        composable(Screen.Login.route) {
            LoginScreen(viewModel()) {
                // Redirect to the dynamic dashboard route after successful login
                navController.navigate("dashboard") {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }

        // --- Lecturer: Syllabus Management ---
        composable(
            route = "syllabus_management/{courseId}/{courseName}",
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
                navArgument("courseName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            val courseName = backStackEntry.arguments?.getString("courseName") ?: ""
            SyllabusManagementScreen(navController, courseId, courseName)
        }

// --- Lecturer: Materials Management (update existing route to use courseId) ---
// REPLACE the existing materials_management route with:
        composable(
            route = "materials_management/{courseId}/{courseName}",
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
                navArgument("courseName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            val courseName = backStackEntry.arguments?.getString("courseName") ?: ""
            MaterialsManagementScreen(navController, courseId, courseName)
        }

// --- Student: Assignments List ---
        composable(
            route = "student_assignments/{courseId}/{courseName}",
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
                navArgument("courseName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            val courseName = backStackEntry.arguments?.getString("courseName") ?: ""
            StudentAssignmentsScreen(navController, courseId, courseName)
        }

// --- Student: Course Materials View ---
        composable(
            route = "student_materials/{courseId}/{courseName}",
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
                navArgument("courseName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            val courseName = backStackEntry.arguments?.getString("courseName") ?: ""
            StudentMaterialsScreen(navController, courseId, courseName)
        }

// --- Student: Submit Assignment ---
        composable(
            route = "submit_assignment/{assignmentId}/{courseId}",
            arguments = listOf(
                navArgument("assignmentId") { type = NavType.StringType },
                navArgument("courseId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val assignmentId = backStackEntry.arguments?.getString("assignmentId") ?: ""
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            SubmitAssignmentScreen(navController, assignmentId, courseId)
        }

        // --- DYNAMIC DASHBOARD (TRAFFIC CONTROLLER) ---
        composable("dashboard") {
            // Determines which dashboard to show based on the user's role saved in UserSession
            when (UserSession.userRole) {
                "lecturer" -> LecturerDashboardScreen(navController)
                "student" -> StudentDashboardScreen(navController)
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: Unauthorized or unknown role")
                    }
                }
            }
        }

        // --- LECTURER SPECIFIC ROUTES ---
        composable("lecturer_courses") {
            LecturerCoursesScreen(navController)
        }

        composable(
            route = "submission_tracking/{assignmentId}/{assignmentTitle}",
            arguments = listOf(
                navArgument("assignmentId") { type = NavType.StringType },
                navArgument("assignmentTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("assignmentId") ?: ""
            val title = backStackEntry.arguments?.getString("assignmentTitle") ?: ""
            // Providing the specific SubmissionViewModel for grading logic
            SubmissionTrackingScreen(navController, id, title, viewModel())
        }

        // --- Student: View a specific lecturer's available office hours ---
        composable(
            route = "lecturer_office_hours/{lecturerId}/{lecturerName}",
            arguments = listOf(
                navArgument("lecturerId") { type = NavType.StringType },
                navArgument("lecturerName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val lecturerId = backStackEntry.arguments?.getString("lecturerId") ?: ""
            val lecturerName = backStackEntry.arguments?.getString("lecturerName") ?: ""
            LecturerOfficeHoursSlotsScreen(navController, lecturerId, lecturerName)
        }

        composable("office_hours_scheduler") {
            OfficeHoursSchedulerScreen(navController)
        }

        // --- EXISTING STUDENT & SHARED COMPONENTS (REMAINING INTACT) ---
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

        composable(
            route = "group_task_details/{groupId}/{subTaskId}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("subTaskId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            val subTaskId = backStackEntry.arguments?.getString("subTaskId") ?: ""
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
        composable(Screen.Timetable.route) {
            TimeTableScreen(navController)
        }

        composable(Screen.BookOfficeHours.route) {
            StudentOfficeHoursScreen(navController)
        }
    }

}