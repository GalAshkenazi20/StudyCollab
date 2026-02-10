package com.example.studycollab.ui

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object StudyGroups : Screen("study_groups")
    object CreateStudyGroup : Screen("create_study_group")
    object Chats : Screen("chats") // Restored for your Home Screen

    // Routes with arguments use helper functions to prevent typos
    object GroupDetails : Screen("group_details/{groupId}") {
        fun createRoute(groupId: String) = "group_details/$groupId"
    }

    object Participants : Screen("participants/{groupId}") {
        fun createRoute(groupId: String) = "participants/$groupId"
    }

    object GroupTasks : Screen("group_tasks/{groupId}") {
        fun createRoute(groupId: String) = "group_tasks/$groupId"
    }

    object GroupTaskDetails : Screen("group_task_details/{groupId}/{assignmentId}") {
        fun createRoute(groupId: String, assignmentId: String) =
            "group_task_details/$groupId/$assignmentId"
    }

    object ChatRoom : Screen("chat/{groupId}") {
        fun createRoute(groupId: String) = "chat/$groupId"
    }

    object Courses : Screen("courses")
    object CourseDetail : Screen("course_detail/{name}/{code}") {
        fun createRoute(name: String, code: String) = "course_detail/$name/$code"
    }

    object Notifications : Screen("notifications")
    object Timetable : Screen("timetable")
}