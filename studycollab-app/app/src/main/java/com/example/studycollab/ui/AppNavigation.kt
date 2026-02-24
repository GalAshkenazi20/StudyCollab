package com.example.studycollab.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
    val authViewModel: AuthViewModel = viewModel()
    val studyViewModel: StudyGroupViewModel = viewModel()
    val assignmentViewModel: AssignmentViewModel = viewModel()
    val notifViewModel: NotificationViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Preserved Original Logic for Header Visibility
    val showHeader = currentRoute != null && currentRoute != Screen.Login.route && currentRoute != Screen.Splash.route

    // Preserved Original Top-Level Routes (Restored notification_settings)
    val topLevelRoutes = listOf(
        "dashboard",
        Screen.StudyGroups.route,
        Screen.Chats.route,
        Screen.Timetable.route,
        "notification_settings"
    )
    val isTopLevel = currentRoute in topLevelRoutes

    // Preserved Dynamic Title Logic
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
        gesturesEnabled = showHeader && isTopLevel,
        drawerContent = {
            if (showHeader) {
                ModalDrawerSheet {
                    Text("StudyCollab", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall)
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
                    // RESTORED: Notification Settings Drawer Item
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
                        title = { Text(screenTitle) }, // RESTORED: Dynamic Title
                        navigationIcon = {
                            if (isTopLevel) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, null) }
                            } else {
                                IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                            }
                        },
                        actions = {
                            BadgedBox(badge = { if (notifViewModel.notifications.isNotEmpty()) Badge { Text("${notifViewModel.notifications.size}") } }) {
                                IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) { Icon(Icons.Default.Notifications, null) }
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier.padding(if (showHeader) innerPadding else PaddingValues(0.dp)),
                enterTransition = { fadeIn(tween(400)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(400)) },
                exitTransition = { fadeOut(tween(400)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(400)) }
            ) {

                composable(Screen.Splash.route) {
                    SplashScreen(navController = navController, viewModel = authViewModel)
                }
                // --- AUTHENTICATION ---
                composable(Screen.Login.route) {
                    // FIXED: Added 'navController' as the second parameter
                    LoginScreen(viewModel(), navController) {
                        navController.navigate("dashboard") {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
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

                composable("lecturer_group_oversight") {
                    LecturerGroupOversightScreen(navController)
                }

                // --- LECTURER ROUTES (Restored All) ---
                composable("syllabus_management/{courseId}/{courseName}", listOf(navArgument("courseId"){type=NavType.StringType}, navArgument("courseName"){type=NavType.StringType})) { b ->
                    SyllabusManagementScreen(navController, b.arguments?.getString("courseId") ?: "", b.arguments?.getString("courseName") ?: "")
                }
                composable("materials_management/{courseId}/{courseName}", listOf(navArgument("courseId"){type=NavType.StringType}, navArgument("courseName"){type=NavType.StringType})) { b ->
                    MaterialsManagementScreen(navController, b.arguments?.getString("courseId") ?: "", b.arguments?.getString("courseName") ?: "")
                }
                composable("lecturer_courses") { LecturerCoursesScreen(navController) }
                composable("submission_tracking/{assignmentId}/{assignmentTitle}", listOf(navArgument("assignmentId"){type=NavType.StringType}, navArgument("assignmentTitle"){type=NavType.StringType})) { b ->
                    SubmissionTrackingScreen(navController, b.arguments?.getString("assignmentId") ?: "", b.arguments?.getString("assignmentTitle") ?: "", viewModel())
                }
                composable("office_hours_scheduler") { OfficeHoursSchedulerScreen(navController) }

                // --- STUDENT ROUTES (Restored All) ---
                composable("student_assignments/{courseId}/{courseName}", listOf(navArgument("courseId"){type=NavType.StringType}, navArgument("courseName"){type=NavType.StringType})) { b ->
                    StudentAssignmentsScreen(navController, b.arguments?.getString("courseId") ?: "", b.arguments?.getString("courseName") ?: "")
                }
                composable("student_materials/{courseId}/{courseName}", listOf(navArgument("courseId"){type=NavType.StringType}, navArgument("courseName"){type=NavType.StringType})) { b ->
                    StudentMaterialsScreen(navController, b.arguments?.getString("courseId") ?: "", b.arguments?.getString("courseName") ?: "")
                }
                composable("submit_assignment/{assignmentId}/{courseId}", listOf(navArgument("assignmentId"){type=NavType.StringType}, navArgument("courseId"){type=NavType.StringType})) { b ->
                    SubmitAssignmentScreen(navController, b.arguments?.getString("assignmentId") ?: "", b.arguments?.getString("courseId") ?: "")
                }
                composable("lecturer_office_hours/{lecturerId}/{lecturerName}", listOf(navArgument("lecturerId"){type=NavType.StringType}, navArgument("lecturerName"){type=NavType.StringType})) { b ->
                    LecturerOfficeHoursSlotsScreen(navController, b.arguments?.getString("lecturerId") ?: "", b.arguments?.getString("lecturerName") ?: "")
                }
                composable(Screen.BookOfficeHours.route) { StudentOfficeHoursScreen(navController) }

                // --- CONSULTATION ROUTES (Integrated with Named Arguments) ---
                composable(
                    route = "chat/lecturer_consultation/{roomId}/{lecturerName}",
                    arguments = listOf(
                        navArgument("roomId") { type = NavType.StringType },
                        navArgument("lecturerName") { type = NavType.StringType }
                    )
                ) { b ->
                    val roomId = b.arguments?.getString("roomId") ?: ""
                    // Fallback here ensures the ChatScreen header always has a string
                    val lecturerName = b.arguments?.getString("lecturerName") ?: "Lecturer"

                    ChatScreen(
                        navController = navController,
                        groupId = roomId, // This is passed to ChatViewModel as currentRoomId
                        consultationName = lecturerName,
                        isConsultation = true
                    )
                }
                composable("course_groups_consultation/{courseId}/{originGroupId}", listOf(navArgument("courseId"){type=NavType.StringType}, navArgument("originGroupId"){type=NavType.StringType})) { b ->
                    PeerGroupConsultationScreen(navController, b.arguments?.getString("courseId") ?: "", b.arguments?.getString("originGroupId") ?: "", studyViewModel)
                }

                // --- SHARED COMPONENTS & STUDY GROUPS (Restored All) ---
                composable(Screen.Notifications.route) { NotificationScreen(notifViewModel) { navController.popBackStack() } }
                composable("notification_settings") { NotificationSettingsScreen() } // RESTORED
                composable(Screen.StudyGroups.route) { StudyGroupScreen(navController, studyViewModel) }
                composable(Screen.CreateStudyGroup.route) { CreateGroupScreen(studyViewModel) { navController.popBackStack() } }
                composable(Screen.GroupDetails.route, listOf(navArgument("groupId"){type=NavType.StringType})) { b ->
                    GroupDetailsScreen(b.arguments?.getString("groupId") ?: "", studyViewModel, navController)
                }
                composable(Screen.Participants.route, listOf(navArgument("groupId"){type=NavType.StringType})) { b ->
                    ParticipantsScreen(b.arguments?.getString("groupId") ?: "", studyViewModel, navController)
                }
                composable(Screen.GroupTasks.route, listOf(navArgument("groupId"){type=NavType.StringType})) { b ->
                    val gid = b.arguments?.getString("groupId") ?: ""
                    val group = studyViewModel.myGroups.find { it._id.toString().filter { it.isLetterOrDigit() } == gid.filter { it.isLetterOrDigit() } }
                    val cId = group?.courseId?.toString()?.replace("\"", "")?.replace("{", "")?.replace("}", "")?.filter { it.isLetterOrDigit() } ?: ""
                    TaskListScreen(navController, cId, gid, assignmentViewModel, studyViewModel)
                }
                composable("group_task_details/{groupId}/{subTaskId}", listOf(navArgument("groupId"){type=NavType.StringType}, navArgument("subTaskId"){type=NavType.StringType})) { b ->
                    SubTaskDetailScreen(navController, b.arguments?.getString("groupId") ?: "", b.arguments?.getString("subTaskId") ?: "", assignmentViewModel, studyViewModel)
                }
                composable(Screen.Courses.route) { CourseListScreen(navController, studyViewModel) }
                composable(Screen.CourseDetail.route, listOf(navArgument("name"){type=NavType.StringType}, navArgument("code"){type=NavType.StringType})) { b ->
                    CourseDetailScreen(navController, b.arguments?.getString("name") ?: "", b.arguments?.getString("code") ?: "", studyViewModel)
                }
                composable(Screen.ChatRoom.route, listOf(navArgument("groupId"){type=NavType.StringType})) { b ->
                    ChatScreen(navController, b.arguments?.getString("groupId") ?: "")
                }
                composable(Screen.Chats.route) { ChatListScreen(navController) }
                composable(Screen.Timetable.route) { TimeTableScreen(navController) }
                composable("lecturer_consultations_list") {
                    // We can share the ViewModel instance here
                    val chatVM: LecturerChatViewModel = viewModel()

                    LecturerConsultationsListScreen(
                        navController = navController,
                        viewModel = chatVM
                    )
                }

                // com.example.studycollab.ui.AppNavigation.kt

                composable(
                    route = "peer_network/{courseId}/{originGroupId}",
                    arguments = listOf(
                        navArgument("courseId") { type = NavType.StringType },
                        navArgument("originGroupId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
                    val originGroupId = backStackEntry.arguments?.getString("originGroupId") ?: ""

                    PeerGroupConsultationScreen(
                        navController = navController,
                        courseId = courseId,
                        originGroupId = originGroupId,
                        viewModel = studyViewModel // Ensure your StudyGroupViewModel is passed here
                    )
                }

                composable(
                    route = "chat/peer_consultation/{roomId}/{groupName}",
                    arguments = listOf(
                        navArgument("roomId") { type = NavType.StringType },
                        navArgument("groupName") { type = NavType.StringType }
                    )
                ) { b ->
                    ChatScreen(
                        navController = navController,
                        groupId = b.arguments?.getString("roomId") ?: "",
                        consultationName = b.arguments?.getString("groupName"),
                        isConsultation = true
                    )
                }
            }
        }
    }
}