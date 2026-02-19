package com.example.studycollab.ui

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home") // Acts as the "dashboard" entry
    object StudyGroups : Screen("study_groups")
    object CreateStudyGroup : Screen("create_study_group")
    object Chats : Screen("chats") // Synchronized with StudentDashboard
    object Notifications : Screen("notifications")
    object Timetable : Screen("timetable")

    // --- SHARED / PARAMETERIZED ROUTES ---
    object GroupDetails : Screen("group_details/{groupId}") {
        fun createRoute(groupId: String) = "group_details/$groupId"
    }

    object Participants : Screen("participants/{groupId}") {
        fun createRoute(groupId: String) = "participants/$groupId"
    }

    object GroupTasks : Screen("group_tasks/{groupId}") {
        fun createRoute(groupId: String) = "group_tasks/$groupId"
    }

    object GroupTaskDetails : Screen("group_task_details/{groupId}/{subTaskId}") {
        fun createRoute(groupId: String, subTaskId: String) =
            "group_task_details/$groupId/$subTaskId"
    }

    object ChatRoom : Screen("chat/{groupId}") {
        fun createRoute(groupId: String) = "chat/$groupId"
    }

    object Courses : Screen("courses")
    object CourseDetail : Screen("course_detail/{name}/{code}") {
        fun createRoute(name: String, code: String) = "course_detail/$name/$code"
    }

    // --- NEW LECTURER SPECIFIC ROUTES ---
    object LecturerCourses : Screen("lecturer_courses")

    object SubmissionTracking : Screen("submission_tracking/{assignmentId}/{assignmentTitle}") {
        fun createRoute(assignmentId: String, assignmentTitle: String) =
            "submission_tracking/$assignmentId/$assignmentTitle"
    }

    object MaterialsManagement : Screen("materials_management/{courseName}") {
        fun createRoute(courseName: String) = "materials_management/$courseName"
    }

    object OfficeHoursScheduler : Screen("office_hours_scheduler")

    // --- NEW STUDENT SPECIFIC ROUTES ---
    object BookOfficeHours : Screen("book_office_hours")

    object NotificationManager : Screen("notification_manager")
}