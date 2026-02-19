package com.example.studycollab.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.example.studycollab.ui.scheduler.*
import com.example.studycollab.ui.submissions.*
import com.example.studycollab.ui.tasks.*
import com.example.studycollab.ui.timetable.TimeTableScreen
import com.example.studycollab.utils.UserSession
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Shared ViewModels - Preserved
    val studyViewModel: StudyGroupViewModel = viewModel()
    val assignmentViewModel: AssignmentViewModel = viewModel()
    val notifViewModel: NotificationViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Logic for hiding header on Login screen and determining Top-Level navigation
    val showHeader = currentRoute != null && currentRoute != Screen.Login.route

    // Screens where the Burger Menu should appear instead of a Back Button
    val topLevelRoutes = listOf(
        "dashboard",
        Screen.StudyGroups.route,
        Screen.Chats.route,
        Screen.Timetable.route,
        "notification_settings"
    )
    val isTopLevel = currentRoute in topLevelRoutes

    // Dynamic Title Logic
    val screenTitle = when {
        currentRoute == "dashboard" -> "Dashboard"
        currentRoute == Screen.StudyGroups.route -> "My Study Groups"
        currentRoute == Screen.Chats.route -> "My Chats"
        currentRoute == Screen.Timetable.route -> "Timetable"
        currentRoute == "notification_settings" -> "Notification Settings"
        currentRoute == Screen.Notifications.route -> "Notifications"
        else -> "StudyCollab"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        // gesturesEnabled = false on Login screen fixes the "unclickable fields" bug
        gesturesEnabled = showHeader && isTopLevel,
        drawerContent = {
            if (showHeader) {
                ModalDrawerSheet {
                    Text(
                        "StudyCollab",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text("Dashboard") },
                        selected = currentRoute == "dashboard",
                        onClick = { scope.launch { drawerState.close() }; navController.navigate("dashboard") },
                        icon = { Icon(Icons.Default.Home, null) }
                    )
                    NavigationDrawerItem(
                        label = { Text("Study Groups") },
                        selected = currentRoute == Screen.StudyGroups.route,
                        onClick = { scope.launch { drawerState.close() }; navController.navigate(Screen.StudyGroups.route) },
                        icon = { Icon(Icons.Default.Groups, null) }
                    )
                    NavigationDrawerItem(
                        label = { Text("Notification Settings") },
                        selected = currentRoute == "notification_settings",
                        onClick = { scope.launch { drawerState.close() }; navController.navigate("notification_settings") },
                        icon = { Icon(Icons.Default.Settings, null) }
                    )
                    Spacer(Modifier.weight(1f))
                    NavigationDrawerItem(
                        label = { Text("Logout") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            UserSession.logout()
                            navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                        },
                        icon = { Icon(Icons.Default.ExitToApp, null) }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (showHeader) {
                    CenterAlignedTopAppBar(
                        title = { Text(screenTitle) },
                        navigationIcon = {
                            if (isTopLevel) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            } else {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        },
                        actions = {
                            BadgedBox(badge = { if (notifViewModel.notifications.isNotEmpty()) Badge { Text("${notifViewModel.notifications.size}") } }) {
                                IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                                }
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Login.route,
                // Applying padding ONLY when header is visible prevents layout overlays on Login screen
                modifier = Modifier.padding(if (showHeader) innerPadding else PaddingValues(0.dp))
            ) {
                // --- AUTHENTICATION ---
                composable(Screen.Login.route) {
                    LoginScreen(viewModel()) {
                        navController.navigate("dashboard") { popUpTo(Screen.Login.route) { inclusive = true } }
                    }
                }

                // --- DYNAMIC DASHBOARD ---
                composable("dashboard") {
                    when (UserSession.userRole) {
                        "lecturer" -> LecturerDashboardScreen(navController)
                        "student" -> StudentDashboardScreen(navController)
                        else -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Error: Unauthorized") }
                    }
                }

                // --- LECTURER ROUTES ---
                composable("syllabus_management/{courseId}/{courseName}", listOf(navArgument("courseId"){type=NavType.StringType}, navArgument("courseName"){type=NavType.StringType})) { backStackEntry ->
                    SyllabusManagementScreen(navController, backStackEntry.arguments?.getString("courseId") ?: "", backStackEntry.arguments?.getString("courseName") ?: "")
                }
                composable("materials_management/{courseId}/{courseName}", listOf(navArgument("courseId"){type=NavType.StringType}, navArgument("courseName"){type=NavType.StringType})) { backStackEntry ->
                    MaterialsManagementScreen(navController, backStackEntry.arguments?.getString("courseId") ?: "", backStackEntry.arguments?.getString("courseName") ?: "")
                }
                composable("lecturer_courses") { LecturerCoursesScreen(navController) }
                composable("submission_tracking/{assignmentId}/{assignmentTitle}", listOf(navArgument("assignmentId"){type=NavType.StringType}, navArgument("assignmentTitle"){type=NavType.StringType})) { backStackEntry ->
                    SubmissionTrackingScreen(navController, backStackEntry.arguments?.getString("assignmentId") ?: "", backStackEntry.arguments?.getString("assignmentTitle") ?: "", viewModel())
                }
                composable("office_hours_scheduler") { OfficeHoursSchedulerScreen(navController) }

                // --- STUDENT ROUTES ---
                composable("student_assignments/{courseId}/{courseName}", listOf(navArgument("courseId"){type=NavType.StringType}, navArgument("courseName"){type=NavType.StringType})) { backStackEntry ->
                    StudentAssignmentsScreen(navController, backStackEntry.arguments?.getString("courseId") ?: "", backStackEntry.arguments?.getString("courseName") ?: "")
                }
                composable("student_materials/{courseId}/{courseName}", listOf(navArgument("courseId"){type=NavType.StringType}, navArgument("courseName"){type=NavType.StringType})) { backStackEntry ->
                    StudentMaterialsScreen(navController, backStackEntry.arguments?.getString("courseId") ?: "", backStackEntry.arguments?.getString("courseName") ?: "")
                }
                composable("submit_assignment/{assignmentId}/{courseId}", listOf(navArgument("assignmentId"){type=NavType.StringType}, navArgument("courseId"){type=NavType.StringType})) { backStackEntry ->
                    SubmitAssignmentScreen(navController, backStackEntry.arguments?.getString("assignmentId") ?: "", backStackEntry.arguments?.getString("courseId") ?: "")
                }
                composable("lecturer_office_hours/{lecturerId}/{lecturerName}", listOf(navArgument("lecturerId"){type=NavType.StringType}, navArgument("lecturerName"){type=NavType.StringType})) { backStackEntry ->
                    LecturerOfficeHoursSlotsScreen(navController, backStackEntry.arguments?.getString("lecturerId") ?: "", backStackEntry.arguments?.getString("lecturerName") ?: "")
                }
                composable(Screen.BookOfficeHours.route) { StudentOfficeHoursScreen(navController) }

                // --- SHARED COMPONENTS ---
                composable(Screen.Notifications.route) { NotificationScreen(notifViewModel) { navController.popBackStack() } }

                // NEW: Smart Notification Settings Screen
                composable("notification_settings") { NotificationSettingsScreen() }

                composable(Screen.StudyGroups.route) { StudyGroupScreen(navController, studyViewModel) }
                composable(Screen.CreateStudyGroup.route) { CreateGroupScreen(studyViewModel) { navController.popBackStack() } }
                composable(Screen.GroupDetails.route, listOf(navArgument("groupId"){type=NavType.StringType})) { backStackEntry ->
                    GroupDetailsScreen(backStackEntry.arguments?.getString("groupId") ?: "", studyViewModel, navController)
                }
                composable(Screen.Participants.route, listOf(navArgument("groupId"){type=NavType.StringType})) { backStackEntry ->
                    ParticipantsScreen(backStackEntry.arguments?.getString("groupId") ?: "", studyViewModel, navController)
                }
                composable(Screen.GroupTasks.route, listOf(navArgument("groupId"){type=NavType.StringType})) { backStackEntry ->
                    val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                    val group = studyViewModel.myGroups.find { it._id.toString().filter { it.isLetterOrDigit() } == groupId.filter { it.isLetterOrDigit() } }
                    val cId = group?.courseId?.toString()?.replace("\"", "")?.replace("{", "")?.replace("}", "")?.filter { it.isLetterOrDigit() } ?: ""
                    TaskListScreen(navController, cId, groupId, assignmentViewModel, studyViewModel)
                }
                composable("group_task_details/{groupId}/{subTaskId}", listOf(navArgument("groupId"){type=NavType.StringType}, navArgument("subTaskId"){type=NavType.StringType})) { backStackEntry ->
                    SubTaskDetailScreen(navController, backStackEntry.arguments?.getString("groupId") ?: "", backStackEntry.arguments?.getString("subTaskId") ?: "", assignmentViewModel, studyViewModel)
                }
                composable(Screen.Courses.route) { CourseListScreen(navController, studyViewModel) }
                composable(Screen.CourseDetail.route, listOf(navArgument("name"){type=NavType.StringType}, navArgument("code"){type=NavType.StringType})) { backStackEntry ->
                    CourseDetailScreen(navController, backStackEntry.arguments?.getString("name") ?: "", backStackEntry.arguments?.getString("code") ?: "", studyViewModel)
                }
                composable(Screen.ChatRoom.route, listOf(navArgument("groupId"){type=NavType.StringType})) { backStackEntry ->
                    ChatScreen(navController, backStackEntry.arguments?.getString("groupId") ?: "")
                }
                composable(Screen.Chats.route) { ChatListScreen(navController) }
                composable(Screen.Timetable.route) { TimeTableScreen(navController) }
            }
        }
    }
}