package com.example.studycollab.ui

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object StudyGroups : Screen("study_groups")
    object CreateStudyGroup : Screen("create_study_group")
    object Chats : Screen("chats")

    object GroupDetails : Screen("group_details/{groupId}") {
        fun createRoute(groupId: String) = "group_details/$groupId"
    }

    object Participants : Screen("participants/{groupId}") {
        fun createRoute(groupId: String) = "participants/$groupId"
    }

    object GroupTasks : Screen("group_tasks/{groupId}") {
        fun createRoute(groupId: String) = "group_tasks/$groupId"
    }

    // FIXED: Changed assignmentId to subTaskId to match logic and navigation
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

    object Notifications : Screen("notifications")
    object Timetable : Screen("timetable")
}