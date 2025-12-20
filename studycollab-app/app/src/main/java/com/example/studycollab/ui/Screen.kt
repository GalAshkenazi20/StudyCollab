package com.example.studycollab.ui

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")

    // Main Features
    object StudyGroups : Screen("study_groups")
    object CreateStudyGroup : Screen("create_study_group")

    object Chats : Screen("chats")
    object CreateChat : Screen("create_chat")

    object Courses : Screen("courses")
    object CourseDetail : Screen("course_detail/{courseName}") {
        fun createRoute(courseName: String) = "course_detail/$courseName"
    }

    object Notifications : Screen("notifications")
    object Timetable : Screen("timetable")
}